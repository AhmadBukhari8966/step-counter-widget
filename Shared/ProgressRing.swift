import SwiftUI
import WidgetKit

/// A circular progress ring with a gradient and rounded ends.
struct ProgressRing: View {
    /// 0...1 fills the ring; anything above 1 keeps it full.
    var progress: Double
    var colors: [Color]
    var trackColor: Color
    var lineWidth: CGFloat

    private var clampedProgress: Double { min(max(progress, 0), 1) }

    var body: some View {
        ZStack {
            Circle()
                .stroke(trackColor, lineWidth: lineWidth)

            Circle()
                .trim(from: 0, to: clampedProgress)
                .stroke(
                    AngularGradient(
                        colors: colors,
                        center: .center,
                        startAngle: .zero,
                        endAngle: .degrees(360 * clampedProgress)
                    ),
                    style: StrokeStyle(lineWidth: lineWidth, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
                .overlay(alignment: .top) {
                    if clampedProgress > 0 {
                        topCap
                    }
                }
                // Tinted and clear Home Screens draw this part in the accent tint.
                .widgetAccentable()
        }
        .padding(lineWidth / 2)
        .aspectRatio(1, contentMode: .fit)
    }

    /// Covers the gradient seam at 12 o'clock. Before the goal it's the start cap; once the
    /// ring is full it's the end of the lap, drawn on top with a soft shadow like Apple's rings.
    private var topCap: some View {
        let isLapped = clampedProgress >= 1
        return Circle()
            .fill((isLapped ? colors.last : colors.first) ?? .accentColor)
            .frame(width: lineWidth, height: lineWidth)
            .shadow(color: .black.opacity(isLapped ? 0.3 : 0), radius: lineWidth / 10, x: lineWidth / 8)
            .offset(y: -lineWidth / 2)
    }
}

#Preview {
    ProgressRing(progress: 0.72, colors: StepTheme.classic.ringColors, trackColor: StepTheme.classic.track, lineWidth: 20)
        .frame(width: 200)
        .padding()
}
