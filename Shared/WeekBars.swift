import SwiftUI
import WidgetKit

/// A compact 7-day bar chart drawn with plain shapes, so it renders crisply in
/// widgets, including the tinted and clear Home Screen styles.
/// Days that met the goal get the theme gradient; the rest are muted.
struct WeekBars: View {
    var days: [DailySteps]
    var goal: Int
    var theme: StepTheme
    var labelColor: Color
    var spacing: CGFloat = 6

    /// The top of the chart: the goal or the best day, whichever is higher.
    private var scaleMax: Int {
        Swift.max(goal, days.map(\.steps).max() ?? 0, 1)
    }

    var body: some View {
        VStack(spacing: 4) {
            GeometryReader { proxy in
                let height = proxy.size.height
                let goalY = height * (1 - CGFloat(goal) / CGFloat(scaleMax))

                ZStack(alignment: .bottom) {
                    Path { path in
                        path.move(to: CGPoint(x: 0, y: goalY))
                        path.addLine(to: CGPoint(x: proxy.size.width, y: goalY))
                    }
                    .stroke(labelColor.opacity(0.6), style: StrokeStyle(lineWidth: 1, dash: [2, 3]))

                    HStack(alignment: .bottom, spacing: spacing) {
                        ForEach(days) { day in
                            RoundedRectangle(cornerRadius: 3, style: .continuous)
                                .fill(day.steps >= goal ? AnyShapeStyle(theme.barGradient) : AnyShapeStyle(theme.mutedFill))
                                .frame(height: Swift.max(height * CGFloat(day.steps) / CGFloat(scaleMax), 4))
                                .frame(maxWidth: .infinity)
                        }
                    }
                    .widgetAccentable()
                }
            }

            HStack(spacing: spacing) {
                ForEach(days) { day in
                    Text(day.date, format: .dateTime.weekday(.narrow))
                        .font(.system(size: 10, weight: day.id == days.last?.id ? .heavy : .medium, design: .rounded))
                        .frame(maxWidth: .infinity)
                }
            }
            .foregroundStyle(labelColor)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Last 7 days")
        .accessibilityValue("\(days.daysMeeting(goal: goal)) of \(days.count) days at goal")
    }
}

#Preview {
    WeekBars(
        days: StepSnapshot.sample().week(endingOn: .now),
        goal: 10_000,
        theme: .classic,
        labelColor: .secondary
    )
    .frame(width: 200, height: 100)
    .padding()
}
