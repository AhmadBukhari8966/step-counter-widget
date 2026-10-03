import SwiftUI

/// First-run screen that asks for permission to read steps.
struct ConnectHealthView: View {
    @Environment(StepsModel.self) private var model
    @State private var isConnecting = false

    var body: some View {
        VStack(spacing: 28) {
            Spacer()

            ZStack {
                Circle()
                    .fill(StepTheme.classic.diagonalGradient)
                    .frame(width: 128, height: 128)
                Image(systemName: "figure.walk")
                    .font(.system(size: 60, weight: .semibold))
                    .foregroundStyle(.white)
            }
            .accessibilityHidden(true)

            VStack(spacing: 12) {
                Text("Your Steps at a Glance")
                    .font(.largeTitle.bold())
                Text("Step Counter reads your daily step count from Apple Health and shows it here and in widgets on your Home Screen and Lock Screen. It doesn't send your data anywhere.")
                    .font(.body)
                    .foregroundStyle(.secondary)
            }
            .multilineTextAlignment(.center)

            Spacer()

            VStack(spacing: 12) {
                Button {
                    Task {
                        isConnecting = true
                        await model.connectHealth()
                        isConnecting = false
                    }
                } label: {
                    Label("Connect Apple Health", systemImage: "heart.fill")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                }
                .controlSize(.large)
                .prominentButton()
                .disabled(isConnecting)

                if let message = model.errorMessage {
                    Text(message)
                        .font(.footnote)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.center)
                }
            }
        }
        .padding(24)
        .frame(maxWidth: 520)
    }
}

#Preview {
    ConnectHealthView()
        .environment(StepsModel())
}
