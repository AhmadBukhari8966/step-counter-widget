import SwiftUI

struct ContentView: View {
    @Environment(StepsModel.self) private var model
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        NavigationStack {
            Group {
                switch model.access {
                case .checking:
                    ProgressView()
                case .notRequested:
                    ConnectHealthView()
                case .requested:
                    DashboardView()
                case .unavailable:
                    ContentUnavailableView(
                        "Apple Health Unavailable",
                        systemImage: "heart.slash",
                        description: Text("Step Counter reads your steps from Apple Health, which isn't available on this device.")
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color(.systemGroupedBackground))
        }
        .task(id: scenePhase) {
            guard scenePhase == .active else { return }
            await model.refreshIfAuthorized()
        }
    }
}

#Preview {
    ContentView()
        .environment(StepsModel())
}
