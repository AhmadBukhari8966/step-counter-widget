import Charts
import SwiftUI

struct DashboardView: View {
    @Environment(StepsModel.self) private var model

    var body: some View {
        @Bindable var model = model

        ScrollView {
            VStack(spacing: 16) {
                TodayCard(steps: model.todaySteps, goal: model.goal, updatedAt: model.snapshot?.fetchedAt)

                if let message = model.errorMessage {
                    Label(message, systemImage: "exclamationmark.triangle.fill")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .cardStyle()
                }

                WeekCard(days: model.week, goal: model.goal)
                GoalCard(goal: $model.goal)

                if model.week.allSatisfy({ $0.steps == 0 }) {
                    NoStepsCard()
                }

                WidgetTipsCard()
            }
            .padding()
            .frame(maxWidth: 640)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await model.refresh() }
        .navigationTitle("Steps")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                if model.isRefreshing {
                    ProgressView()
                } else {
                    Button("Refresh", systemImage: "arrow.clockwise") {
                        Task { await model.refresh() }
                    }
                }
            }
        }
    }
}

// MARK: - Today

struct TodayCard: View {
    let steps: Int
    let goal: Int
    let updatedAt: Date?

    private let theme = StepTheme.classic
    private var progress: Double { Double(steps) / Double(max(goal, 1)) }

    var body: some View {
        VStack(spacing: 20) {
            ZStack {
                ProgressRing(progress: progress, colors: theme.ringColors, trackColor: theme.track, lineWidth: 24)
                VStack(spacing: 2) {
                    Image(systemName: steps >= goal ? "checkmark.circle.fill" : "figure.walk")
                        .font(.title2.weight(.semibold))
                        .foregroundStyle(theme.diagonalGradient)
                    Text(steps.formattedSteps)
                        .font(.system(size: 50, weight: .bold, design: .rounded))
                        .minimumScaleFactor(0.5)
                        .lineLimit(1)
                        .contentTransition(.numericText(value: Double(steps)))
                    Text("of \(goal.formattedSteps) steps")
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(.secondary)
                }
                .padding(40)
            }
            .frame(width: 250, height: 250)
            .animation(.smooth, value: steps)
            .animation(.smooth, value: goal)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("\(steps.formattedSteps) steps today")
            .accessibilityValue("\(progress.formattedPercent) of your \(goal.formattedSteps) step goal")

            HStack(spacing: 12) {
                StatTile(title: "Progress", value: progress.formattedPercent)
                StatTile(title: steps >= goal ? "Over Goal" : "To Go", value: abs(goal - steps).formattedSteps)
                StatTile(title: "Updated", value: updatedAt?.formatted(date: .omitted, time: .shortened) ?? "–")
            }
        }
        .frame(maxWidth: .infinity)
        .cardStyle()
    }
}

private struct StatTile: View {
    let title: LocalizedStringKey
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.rounded(.headline))
                .monospacedDigit()
                .lineLimit(1)
                .minimumScaleFactor(0.7)
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(Color(.tertiarySystemGroupedBackground), in: .rect(cornerRadius: 14, style: .continuous))
    }
}

// MARK: - Week

struct WeekCard: View {
    let days: [DailySteps]
    let goal: Int

    private let theme = StepTheme.classic

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .firstTextBaseline) {
                Text("Last 7 Days")
                    .font(.headline)
                Spacer()
                Text("Avg \(days.averageSteps.formattedSteps)")
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(.secondary)
            }

            Chart {
                ForEach(days) { day in
                    BarMark(
                        x: .value("Day", day.date, unit: .day),
                        y: .value("Steps", day.steps)
                    )
                    .foregroundStyle(day.steps >= goal ? AnyShapeStyle(theme.barGradient) : AnyShapeStyle(theme.mutedFill))
                    .cornerRadius(6)
                }

                RuleMark(y: .value("Goal", goal))
                    .lineStyle(StrokeStyle(lineWidth: 1, dash: [4, 4]))
                    .foregroundStyle(Color.secondary)
                    .annotation(position: .top, alignment: .leading) {
                        Text("Goal")
                            .font(.caption2.weight(.semibold))
                            .foregroundStyle(.secondary)
                    }
            }
            .chartXAxis {
                AxisMarks(values: .stride(by: .day)) { _ in
                    AxisValueLabel(format: .dateTime.weekday(.abbreviated), centered: true)
                }
            }
            .chartYAxis {
                AxisMarks { value in
                    AxisGridLine()
                    AxisValueLabel {
                        if let steps = value.as(Int.self) {
                            Text(steps.compactSteps)
                        }
                    }
                }
            }
            .frame(height: 190)

            HStack {
                Label("\(days.daysMeeting(goal: goal)) of \(days.count) days at goal", systemImage: "flame.fill")
                Spacer()
                Text("\(days.totalSteps.formattedSteps) total")
            }
            .font(.footnote)
            .foregroundStyle(.secondary)
        }
        .cardStyle()
    }
}

// MARK: - Goal

struct GoalCard: View {
    @Binding var goal: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Daily Goal")
                .font(.headline)
            Stepper(value: $goal, in: SharedStore.goalRange, step: SharedStore.goalStep) {
                Text("\(goal.formattedSteps) steps")
                    .font(.rounded(.title2))
                    .monospacedDigit()
                    .contentTransition(.numericText(value: Double(goal)))
                    .animation(.snappy, value: goal)
            }
            Text("Your widgets show progress toward this goal.")
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .cardStyle()
    }
}

// MARK: - Help

struct NoStepsCard: View {
    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            Image(systemName: "questionmark.circle.fill")
                .font(.title3)
                .foregroundStyle(.orange)
            VStack(alignment: .leading, spacing: 4) {
                Text("No steps showing?")
                    .font(.subheadline.weight(.semibold))
                Text("Make sure Step Counter can read your steps: open Settings, go to Privacy & Security › Health › Step Counter, and turn on Steps.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .cardStyle()
    }
}

struct WidgetTipsCard: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Add a Widget")
                .font(.headline)
            TipRow(
                symbol: "apps.iphone",
                title: "Home Screen",
                text: "Touch and hold an empty area, tap Edit, then Add Widget, and search for Step Counter."
            )
            TipRow(
                symbol: "lock.iphone",
                title: "Lock Screen",
                text: "Touch and hold the Lock Screen, tap Customize, choose Lock Screen, then tap the widget area."
            )
            TipRow(
                symbol: "paintpalette.fill",
                title: "Themes",
                text: "Touch and hold a Home Screen widget and choose Edit Widget to pick a color theme."
            )
        }
        .cardStyle()
    }
}

private struct TipRow: View {
    let symbol: String
    let title: LocalizedStringKey
    let text: LocalizedStringKey

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            Image(systemName: symbol)
                .font(.title3)
                .foregroundStyle(.tint)
                .frame(width: 28)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline.weight(.semibold))
                Text(text)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
    }
}

#Preview {
    NavigationStack {
        ScrollView {
            VStack(spacing: 16) {
                TodayCard(steps: 7_532, goal: 10_000, updatedAt: .now)
                WeekCard(days: StepSnapshot.sample().week(endingOn: .now), goal: 10_000)
                GoalCard(goal: .constant(10_000))
            }
            .padding()
        }
        .background(Color(.systemGroupedBackground))
    }
}
