import Foundation

extension Int {
    /// "8,432"
    var formattedSteps: String {
        formatted(.number)
    }

    /// "8.4K" — for tight spaces such as the Lock Screen.
    var compactSteps: String {
        formatted(.number.notation(.compactName).precision(.fractionLength(0...1)))
    }
}

extension Double {
    /// "84%"
    var formattedPercent: String {
        formatted(.percent.precision(.fractionLength(0)))
    }
}
