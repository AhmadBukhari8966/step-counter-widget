import SwiftUI
import WidgetKit

/// Lock Screen widgets. The system draws these in a single tint, so they rely on
/// standard gauges and text weight rather than color.
struct StepsAccessoryView: View {
    @Environment(\.widgetFamily) private var family
    let entry: StepsEntry

    var body: some View {
        Group {
            switch family {
            case .accessoryCircular: CircularStepsView(entry: entry)
            case .accessoryRectangular: RectangularStepsView(entry: entry)
            default: InlineStepsView(entry: entry)
            }
        }
        .containerBackground(for: .widget) {}
    }
}

struct CircularStepsView: View {
    let entry: StepsEntry

    var body: some View {
        if entry.status == .ready {
            Gauge(value: min(entry.progress, 1)) {
                Image(systemName: "figure.walk")
            } currentValueLabel: {
                Text(entry.steps.compactSteps)
            }
            .gaugeStyle(.accessoryCircular)
            .widgetAccentable()
        } else {
            ZStack {
                AccessoryWidgetBackground()
                Image(systemName: "figure.walk")
                    .font(.title2)
            }
        }
    }
}

struct RectangularStepsView: View {
    let entry: StepsEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            HStack(spacing: 4) {
                Image(systemName: "figure.walk")
                Text("Steps")
                Spacer(minLength: 4)
                if entry.status == .ready {
                    Text(entry.progress.formattedPercent)
                        .monospacedDigit()
                }
            }
            .font(.caption.weight(.semibold))
            .widgetAccentable()

            if entry.status == .ready {
                StepCountText(steps: entry.steps, font: .rounded(.title2))
                Gauge(value: min(entry.progress, 1)) {
                    EmptyView()
                }
                .gaugeStyle(.accessoryLinearCapacity)
            } else {
                Text("Open Step Counter to connect Apple Health.")
                    .font(.caption2)
                    .lineLimit(3)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct InlineStepsView: View {
    let entry: StepsEntry

    var body: some View {
        if entry.status == .ready {
            Label("\(entry.steps.formattedSteps) steps", systemImage: "figure.walk")
        } else {
            Label("Open Step Counter", systemImage: "figure.walk")
        }
    }
}
