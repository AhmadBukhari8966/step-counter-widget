import HealthKit
import WidgetKit

/// Copies the latest step totals into the App Group and refreshes the widgets.
enum StepSync {
    @discardableResult
    static func refresh() async throws -> StepSnapshot {
        let previous = SharedStore.loadSnapshot()
        let snapshot = try await HealthSteps.fetchSnapshot()
        SharedStore.save(snapshot)
        // Reloads started from the background count against the widgets'
        // daily refresh budget, so skip them when nothing changed.
        if previous?.days != snapshot.days {
            WidgetCenter.shared.reloadAllTimelines()
        }
        return snapshot
    }
}

/// Keeps the widgets current while the app isn't open. HealthKit launches the app in the
/// background when new step samples are saved (at most hourly for steps), and the new
/// totals are passed on to the widgets.
@MainActor
final class StepUpdateObserver {
    static let shared = StepUpdateObserver()

    private static let enabledKey = "backgroundStepUpdatesEnabled"
    private var isRunning = false

    /// Call on every launch.
    func resumeIfEnabled() {
        if UserDefaults.standard.bool(forKey: Self.enabledKey) {
            start()
        }
    }

    /// Call once the user has been asked for Health access.
    func start() {
        UserDefaults.standard.set(true, forKey: Self.enabledKey)
        guard !isRunning, HealthSteps.isAvailable else { return }
        isRunning = true

        HealthSteps.store.execute(Self.makeObserverQuery())
        HealthSteps.store.enableBackgroundDelivery(for: HealthSteps.stepType, frequency: .hourly) { _, _ in }
    }

    /// Built off the main actor because HealthKit calls the update handler on a background queue.
    private nonisolated static func makeObserverQuery() -> HKObserverQuery {
        HKObserverQuery(sampleType: HealthSteps.stepType, predicate: nil) { _, completionHandler, error in
            let completion = ObserverCompletion(handler: completionHandler)
            guard error == nil else {
                completion.handler()
                return
            }
            Task {
                _ = try? await StepSync.refresh()
                completion.handler()
            }
        }
    }
}

/// HealthKit's completion handler type isn't marked `Sendable`. It's called exactly once,
/// after the refresh finishes, which tells HealthKit the update was handled.
private struct ObserverCompletion: @unchecked Sendable {
    let handler: HKObserverQueryCompletionHandler
}
