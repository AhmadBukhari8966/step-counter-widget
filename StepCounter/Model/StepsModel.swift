import Observation
import WidgetKit

@MainActor
@Observable
final class StepsModel {
    enum HealthAccess {
        case checking
        case notRequested
        case requested
        case unavailable
    }

    private(set) var access: HealthAccess = .checking
    private(set) var snapshot: StepSnapshot? = SharedStore.loadSnapshot()
    private(set) var isRefreshing = false
    private(set) var errorMessage: String?

    /// The daily goal, shared with the widgets.
    var goal: Int = SharedStore.goal {
        didSet {
            guard goal != oldValue else { return }
            SharedStore.goal = goal
            scheduleWidgetReload()
        }
    }

    @ObservationIgnored private var widgetReloadTask: Task<Void, Never>?

    var todaySteps: Int { snapshot?.steps(on: .now) ?? 0 }
    var week: [DailySteps] { (snapshot ?? .empty).week(endingOn: .now) }

    /// Called whenever the app becomes active.
    func refreshIfAuthorized() async {
        guard HealthSteps.isAvailable else {
            access = .unavailable
            return
        }
        if await HealthSteps.shouldRequestAuthorization() {
            access = .notRequested
            return
        }
        access = .requested
        StepUpdateObserver.shared.start()
        await refresh()
    }

    /// Shows the Health permission sheet, then loads steps.
    func connectHealth() async {
        do {
            try await HealthSteps.requestAuthorization()
        } catch {
            errorMessage = error.localizedDescription
            return
        }
        errorMessage = nil
        access = .requested
        StepUpdateObserver.shared.start()
        await refresh()
    }

    func refresh() async {
        guard !isRefreshing else { return }
        isRefreshing = true
        defer { isRefreshing = false }

        do {
            snapshot = try await StepSync.refresh()
            errorMessage = nil
        } catch is CancellationError {
            // The app left the foreground mid-refresh; it refreshes again on return.
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    /// Waits for the stepper to settle before asking the widgets to redraw.
    private func scheduleWidgetReload() {
        widgetReloadTask?.cancel()
        widgetReloadTask = Task {
            try? await Task.sleep(for: .milliseconds(500))
            guard !Task.isCancelled else { return }
            WidgetCenter.shared.reloadAllTimelines()
        }
    }
}
