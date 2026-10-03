import Foundation

/// The App Group container shared by the app and its widget extension.
///
/// If you change this identifier, change it in `StepCounter.entitlements`
/// and `StepCounterWidget.entitlements` as well.
enum AppGroup {
    static let identifier = "group.com.ahmadbukhari.StepCounter"

    /// Shared defaults. Falls back to the process's own defaults if the App Group
    /// isn't provisioned yet (e.g. in SwiftUI previews), so nothing crashes.
    static var defaults: UserDefaults {
        UserDefaults(suiteName: identifier) ?? .standard
    }
}

/// Values the app and the widget extension share through the App Group.
enum SharedStore {
    private enum Key {
        static let snapshot = "stepSnapshot"
        static let goal = "dailyStepGoal"
    }

    static let defaultGoal = 10_000
    static let goalRange = 1_000...50_000
    static let goalStep = 500

    /// The daily step goal chosen in the app.
    static var goal: Int {
        get {
            let stored = AppGroup.defaults.integer(forKey: Key.goal)
            return stored > 0 ? stored : defaultGoal
        }
        set {
            AppGroup.defaults.set(newValue, forKey: Key.goal)
        }
    }

    /// The most recent step totals read from HealthKit by either the app or the widget.
    static func loadSnapshot() -> StepSnapshot? {
        guard let data = AppGroup.defaults.data(forKey: Key.snapshot) else { return nil }
        return try? JSONDecoder().decode(StepSnapshot.self, from: data)
    }

    /// Saves `snapshot` unless a newer one is already stored (both processes write here).
    static func save(_ snapshot: StepSnapshot) {
        if let existing = loadSnapshot(), existing.fetchedAt > snapshot.fetchedAt { return }
        guard let data = try? JSONEncoder().encode(snapshot) else { return }
        AppGroup.defaults.set(data, forKey: Key.snapshot)
    }
}
