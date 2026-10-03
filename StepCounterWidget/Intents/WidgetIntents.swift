import AppIntents
import WidgetKit

/// The options shown when the user picks Edit Widget on the Home Screen widget.
struct StepsWidgetConfiguration: WidgetConfigurationIntent {
    static let title: LocalizedStringResource = "Daily Steps"
    static let description = IntentDescription("Shows today's steps from Apple Health.")

    @Parameter(title: "Theme", default: .classic)
    var theme: WidgetThemeOption
}

enum WidgetThemeOption: String, AppEnum {
    case classic
    case sunset
    case ocean
    case forest
    case midnight

    static let typeDisplayRepresentation: TypeDisplayRepresentation = "Theme"

    static let caseDisplayRepresentations: [WidgetThemeOption: DisplayRepresentation] = [
        .classic: DisplayRepresentation(title: "Classic", image: .init(systemName: "circle.lefthalf.filled")),
        .sunset: DisplayRepresentation(title: "Sunset", image: .init(systemName: "sun.horizon.fill")),
        .ocean: DisplayRepresentation(title: "Ocean", image: .init(systemName: "water.waves")),
        .forest: DisplayRepresentation(title: "Forest", image: .init(systemName: "leaf.fill")),
        .midnight: DisplayRepresentation(title: "Midnight", image: .init(systemName: "moon.stars.fill")),
    ]

    var stepTheme: StepTheme {
        switch self {
        case .classic: .classic
        case .sunset: .sunset
        case .ocean: .ocean
        case .forest: .forest
        case .midnight: .midnight
        }
    }
}

/// Runs when the refresh button on the medium or large widget is tapped.
struct RefreshStepsIntent: AppIntent {
    static let title: LocalizedStringResource = "Refresh Steps"
    static let isDiscoverable = false

    func perform() async throws -> some IntentResult {
        // WidgetKit reloads the tapped widget once this returns. Reload the
        // Lock Screen widgets as well so every widget shows the same count.
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}
