import SwiftUI
import WidgetKit

/// Home Screen widget in small, medium and large, plus Extra Large Portrait on iOS 27.
/// The small size also shows up in StandBy and CarPlay.
struct StepsWidget: Widget {
    static let kind = "StepsWidget"

    private static var supportedFamilies: [WidgetFamily] {
        var families: [WidgetFamily] = [.systemSmall, .systemMedium, .systemLarge]
        if #available(iOS 27.0, *) {
            families.append(.systemExtraLargePortrait)
        }
        return families
    }

    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: Self.kind, intent: StepsWidgetConfiguration.self, provider: StepsHomeProvider()) { entry in
            StepsWidgetView(entry: entry)
        }
        .configurationDisplayName("Daily Steps")
        .description("Today's steps and your progress toward your daily goal.")
        .supportedFamilies(Self.supportedFamilies)
    }
}

/// Lock Screen widgets (iPhone and iPad).
struct StepsLockScreenWidget: Widget {
    static let kind = "StepsLockScreenWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: Self.kind, provider: StepsAccessoryProvider()) { entry in
            StepsAccessoryView(entry: entry)
        }
        .configurationDisplayName("Steps")
        .description("Your step count on the Lock Screen.")
        .supportedFamilies([.accessoryCircular, .accessoryRectangular, .accessoryInline])
    }
}

struct StepsWidgetView: View {
    @Environment(\.widgetFamily) private var family
    let entry: StepsEntry

    var body: some View {
        content
            .foregroundStyle(entry.theme.primaryText)
            .containerBackground(for: .widget) {
                ThemeBackground(theme: entry.theme)
            }
    }

    @ViewBuilder
    private var content: some View {
        switch entry.status {
        case .ready:
            switch family {
            case .systemSmall: SmallStepsView(entry: entry)
            case .systemMedium: MediumStepsView(entry: entry)
            case .systemLarge: LargeStepsView(entry: entry)
            // The only other size offered is iOS 27's Extra Large Portrait.
            default: ExtraLargePortraitStepsView(entry: entry)
            }
        case .needsHealthAccess:
            WidgetMessage(symbol: "heart.text.square", text: "Open Step Counter to connect Apple Health.", theme: entry.theme)
        case .healthUnavailable:
            WidgetMessage(symbol: "heart.slash", text: "Apple Health isn't available on this device.", theme: entry.theme)
        }
    }
}

/// The widget background. The system removes it in StandBy and in the tinted and clear Home Screen styles.
private struct ThemeBackground: View {
    let theme: StepTheme

    var body: some View {
        if theme.backgroundColors.isEmpty {
            Color(.widgetBackground)
        } else {
            LinearGradient(colors: theme.backgroundColors, startPoint: .topLeading, endPoint: .bottomTrailing)
        }
    }
}

#Preview("Small", as: .systemSmall) {
    StepsWidget()
} timeline: {
    StepsEntry.sample()
    StepsEntry.sample(theme: .sunset)
    StepsEntry.sample(theme: .midnight)
}

#Preview("Medium", as: .systemMedium) {
    StepsWidget()
} timeline: {
    StepsEntry.sample()
    StepsEntry.sample(theme: .ocean)
}

#Preview("Large", as: .systemLarge) {
    StepsWidget()
} timeline: {
    StepsEntry.sample()
    StepsEntry.sample(theme: .forest)
}

#Preview("Lock Screen", as: .accessoryRectangular) {
    StepsLockScreenWidget()
} timeline: {
    StepsEntry.sample()
}
