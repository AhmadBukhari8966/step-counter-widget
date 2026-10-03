import AppIntents
import SwiftUI
import WidgetKit

// MARK: - Small

struct SmallStepsView: View {
    let entry: StepsEntry

    var body: some View {
        VStack(spacing: 6) {
            HStack(spacing: 4) {
                Image(systemName: entry.goalReached ? "checkmark.circle.fill" : "figure.walk")
                    .foregroundStyle(entry.theme.accent)
                    .widgetAccentable()
                Text("Steps")
                Spacer(minLength: 4)
                Text(entry.progress.formattedPercent)
                    .monospacedDigit()
            }
            .font(.caption.weight(.semibold))
            .foregroundStyle(entry.theme.secondaryText)

            StepRing(entry: entry, lineWidth: 11) {
                VStack(spacing: 0) {
                    StepCountText(steps: entry.steps, font: .rounded(.title2))
                    Text("of \(entry.goal.compactSteps)")
                        .font(.caption2.weight(.semibold))
                        .foregroundStyle(entry.theme.secondaryText)
                }
            }
        }
    }
}

// MARK: - Medium

struct MediumStepsView: View {
    let entry: StepsEntry

    var body: some View {
        HStack(spacing: 18) {
            StepRing(entry: entry, lineWidth: 13) {
                VStack(spacing: 0) {
                    StepCountText(steps: entry.steps, font: .rounded(.title2))
                    Text("steps")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(entry.theme.secondaryText)
                }
            }

            VStack(alignment: .leading, spacing: 8) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 1) {
                        GoalStatusText(entry: entry)
                            .font(.subheadline.weight(.bold))
                        Text("\(entry.progress.formattedPercent) of \(entry.goal.formattedSteps)")
                            .font(.caption2.weight(.medium))
                            .foregroundStyle(entry.theme.secondaryText)
                    }
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                    Spacer(minLength: 4)
                    RefreshButton(theme: entry.theme)
                }

                WeekBars(
                    days: entry.week,
                    goal: entry.goal,
                    theme: entry.theme,
                    labelColor: entry.theme.secondaryText
                )
            }
        }
    }
}

// MARK: - Large

struct LargeStepsView: View {
    let entry: StepsEntry

    var body: some View {
        let week = entry.week

        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 6) {
                Label("Today", systemImage: "figure.walk")
                    .font(.subheadline.weight(.semibold))
                Spacer(minLength: 4)
                if let updated = entry.snapshot?.fetchedAt {
                    Text("Updated \(updated, format: .dateTime.hour().minute())")
                        .font(.caption2)
                }
                RefreshButton(theme: entry.theme)
            }
            .foregroundStyle(entry.theme.secondaryText)

            HStack(spacing: 16) {
                StepRing(entry: entry, lineWidth: 14) {
                    Text(entry.progress.formattedPercent)
                        .font(.rounded(.title3))
                        .minimumScaleFactor(0.6)
                        .lineLimit(1)
                }
                .frame(width: 104, height: 104)

                VStack(alignment: .leading, spacing: 2) {
                    StepCountText(steps: entry.steps, font: .system(size: 42, weight: .bold, design: .rounded))
                    Text("of \(entry.goal.formattedSteps) steps")
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(entry.theme.secondaryText)
                    GoalStatusText(entry: entry)
                        .font(.footnote.weight(.bold))
                        .foregroundStyle(entry.theme.accent)
                        .padding(.top, 4)
                }
            }

            WeekBars(
                days: week,
                goal: entry.goal,
                theme: entry.theme,
                labelColor: entry.theme.secondaryText,
                spacing: 10
            )
            .frame(maxHeight: .infinity)

            HStack {
                StatColumn(title: "7-Day Total", value: week.totalSteps.compactSteps, theme: entry.theme)
                Spacer()
                StatColumn(title: "Daily Avg", value: week.averageSteps.formattedSteps, theme: entry.theme)
                Spacer()
                StatColumn(title: "Goals Met", value: "\(week.daysMeeting(goal: entry.goal))/\(week.count)", theme: entry.theme)
            }
        }
    }
}

// MARK: - Extra Large Portrait (iOS 27)

/// The large layout with a day-by-day list underneath.
struct ExtraLargePortraitStepsView: View {
    let entry: StepsEntry

    var body: some View {
        VStack(spacing: 16) {
            LargeStepsView(entry: entry)

            Rectangle()
                .fill(entry.theme.track)
                .frame(height: 1)

            VStack(spacing: 8) {
                ForEach(entry.week.reversed()) { day in
                    DayRow(day: day, isToday: day.id == entry.week.last?.id, entry: entry)
                }
            }
        }
    }
}

private struct DayRow: View {
    let day: DailySteps
    let isToday: Bool
    let entry: StepsEntry

    private var metGoal: Bool { day.steps >= entry.goal }

    var body: some View {
        HStack(spacing: 8) {
            if isToday {
                Text("Today")
            } else {
                Text(day.date, format: .dateTime.weekday(.wide))
            }
            Spacer(minLength: 8)
            Text(day.steps.formattedSteps)
                .font(.rounded(.subheadline))
                .monospacedDigit()
            Image(systemName: metGoal ? "checkmark.circle.fill" : "circle")
                .foregroundStyle(metGoal ? entry.theme.accent : entry.theme.secondaryText.opacity(0.5))
                .widgetAccentable()
                .accessibilityLabel(metGoal ? "Goal met" : "Goal not met")
        }
        .font(.subheadline.weight(isToday ? .semibold : .regular))
    }
}

// MARK: - Pieces

/// The progress ring with content in its center.
struct StepRing<Center: View>: View {
    let entry: StepsEntry
    var lineWidth: CGFloat
    @ViewBuilder var center: Center

    var body: some View {
        ZStack {
            ProgressRing(
                progress: entry.progress,
                colors: entry.theme.ringColors,
                trackColor: entry.theme.track,
                lineWidth: lineWidth
            )
            center
                .padding(lineWidth + 4)
        }
    }
}

/// The step count, shrinking to fit and animating between values.
struct StepCountText: View {
    let steps: Int
    let font: Font

    var body: some View {
        Text(steps.formattedSteps)
            .font(font)
            .minimumScaleFactor(0.5)
            .lineLimit(1)
            .contentTransition(.numericText(value: Double(steps)))
    }
}

/// "1,568 to go" or "Goal reached!"
struct GoalStatusText: View {
    let entry: StepsEntry

    var body: some View {
        if entry.goalReached {
            Text("Goal reached!")
        } else {
            Text("\(entry.remaining.formattedSteps) to go")
        }
    }
}

struct RefreshButton: View {
    let theme: StepTheme

    var body: some View {
        Button(intent: RefreshStepsIntent()) {
            Image(systemName: "arrow.clockwise")
                .font(.system(size: 11, weight: .bold))
                .foregroundStyle(theme.secondaryText)
                .frame(width: 24, height: 24)
                .background(theme.track, in: Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Refresh")
    }
}

struct StatColumn: View {
    let title: LocalizedStringKey
    let value: String
    let theme: StepTheme

    var body: some View {
        VStack(alignment: .leading, spacing: 1) {
            Text(title)
                .font(.caption2.weight(.medium))
                .foregroundStyle(theme.secondaryText)
            Text(value)
                .font(.rounded(.subheadline))
                .monospacedDigit()
        }
    }
}

/// Shown instead of steps when Health access is missing or unavailable.
struct WidgetMessage: View {
    let symbol: String
    let text: LocalizedStringKey
    let theme: StepTheme

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: symbol)
                .font(.title2)
                .foregroundStyle(theme.accent)
                .widgetAccentable()
            Text(text)
                .font(.caption.weight(.medium))
                .multilineTextAlignment(.center)
                .foregroundStyle(theme.secondaryText)
        }
    }
}
