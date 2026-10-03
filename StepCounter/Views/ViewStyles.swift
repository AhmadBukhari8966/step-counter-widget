import SwiftUI

extension View {
    /// A rounded card on the grouped background.
    func cardStyle() -> some View {
        padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemGroupedBackground), in: .rect(cornerRadius: 24, style: .continuous))
    }

    /// Liquid Glass on iOS 26 and later; the standard prominent style before that.
    @ViewBuilder
    func prominentButton() -> some View {
        if #available(iOS 26.0, *) {
            buttonStyle(.glassProminent)
        } else {
            buttonStyle(.borderedProminent)
        }
    }
}
