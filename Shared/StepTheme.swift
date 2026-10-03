import SwiftUI

/// Colors for the progress ring, text and widget background.
struct StepTheme: Hashable, Sendable {
    /// Gradient behind the widget. Empty means the standard light/dark widget background.
    var backgroundColors: [Color]
    /// Gradient along the progress ring and the weekly bars.
    var ringColors: [Color]
    /// White text and track, for themes with a colored background.
    var usesLightContent: Bool

    var primaryText: Color { usesLightContent ? .white : .primary }
    var secondaryText: Color { usesLightContent ? .white.opacity(0.8) : .secondary }
    var track: Color { usesLightContent ? .white.opacity(0.25) : .primary.opacity(0.1) }
    /// Bars for days that missed the goal.
    var mutedFill: Color { usesLightContent ? .white.opacity(0.35) : .primary.opacity(0.15) }
    /// Small icons and highlights.
    var accent: Color { usesLightContent ? .white : (ringColors.last ?? .pink) }

    /// Vertical gradient for bars.
    var barGradient: LinearGradient {
        LinearGradient(colors: ringColors, startPoint: .bottom, endPoint: .top)
    }

    /// Diagonal gradient for badges and icons.
    var diagonalGradient: LinearGradient {
        LinearGradient(colors: ringColors, startPoint: .topLeading, endPoint: .bottomTrailing)
    }
}

extension StepTheme {
    static let classic = StepTheme(
        backgroundColors: [],
        ringColors: [Color(hex: 0xFF9F0A), Color(hex: 0xFF375F)],
        usesLightContent: false
    )

    static let sunset = StepTheme(
        backgroundColors: [Color(hex: 0xFF9500), Color(hex: 0xF5325C)],
        ringColors: [.white, .white],
        usesLightContent: true
    )

    static let ocean = StepTheme(
        backgroundColors: [Color(hex: 0x1D6FE0), Color(hex: 0x14A8C4)],
        ringColors: [.white, .white],
        usesLightContent: true
    )

    static let forest = StepTheme(
        backgroundColors: [Color(hex: 0x11804A), Color(hex: 0x45B046)],
        ringColors: [.white, .white],
        usesLightContent: true
    )

    static let midnight = StepTheme(
        backgroundColors: [Color(hex: 0x262B4F), Color(hex: 0x0C0D1A)],
        ringColors: [Color(hex: 0x64D2FF), Color(hex: 0xBF5AF2)],
        usesLightContent: true
    )
}

extension Font {
    /// SF Rounded at a Dynamic Type style, bold by default.
    ///
    /// Built with `.weight(_:)` on purpose: in widgets on iOS 27, text set with
    /// `.system(_:design:weight:)` shows up on the Home Screen in the regular weight.
    static func rounded(_ style: Font.TextStyle, weight: Font.Weight = .bold) -> Font {
        .system(style, design: .rounded).weight(weight)
    }
}

extension Color {
    /// `Color(hex: 0xFF9F0A)`
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255
        )
    }
}
