import UIKit

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // HealthKit can launch the app in the background to deliver new steps.
        // The observer query has to be registered again on every launch for that to work.
        StepUpdateObserver.shared.resumeIfEnabled()
        return true
    }
}
