import SwiftUI
import WidgetKit

@main
struct StepCounterWidgetBundle: WidgetBundle {
    var body: some Widget {
        StepsWidget()
        StepsLockScreenWidget()
    }
}
