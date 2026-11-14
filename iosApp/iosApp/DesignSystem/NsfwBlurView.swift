import SwiftUI
import Shared

/// User's NSFW blur preferences, injected once at the app root.
/// Browse cards read this instead of hardcoding per-level radii, so turning a
/// level's blur down to 100% actually removes the blur everywhere.
private struct CivitNsfwBlurKey: EnvironmentKey {
    static let defaultValue = NsfwBlurSettings(softIntensity: 75, matureIntensity: 25, explicitIntensity: 0)
}

extension EnvironmentValues {
    var civitNsfwBlur: NsfwBlurSettings {
        get { self[CivitNsfwBlurKey.self] }
        set { self[CivitNsfwBlurKey.self] = newValue }
    }
}

extension NsfwBlurSettings {
    /// Blur radius for a thumbnail; an unknown NSFW level never blurs.
    func blurRadius(for level: NsfwLevel?) -> CGFloat {
        CGFloat(blurRadiusFor(level: level ?? NsfwLevel.none))
    }
}

/// Wraps content with a blur overlay based on a blur radius.
/// Supports tap-to-reveal: tapping the blurred area temporarily removes the blur.
/// Resets the revealed state when blurRadius changes so that slider adjustments
/// in Settings immediately take effect.
struct NsfwBlurView<Content: View>: View {
    let blurRadius: CGFloat
    @ViewBuilder let content: () -> Content
    @State private var isRevealed: Bool = false

    var body: some View {
        ZStack {
            content()
                .blur(radius: isRevealed ? 0 : blurRadius)

            if !isRevealed && blurRadius > 0 {
                Color.clear
                    .contentShape(Rectangle())
                    .accessibilityAddTraits(.isButton)
                    .accessibilityLabel(Text("nsfw_show_content_accessibility"))
                    .onTapGesture {
                        withAnimation(MotionAnimation.standard) {
                            isRevealed = true
                        }
                    }
                    .overlay {
                        Text("nsfw_tap_to_reveal")
                            .font(.civitLabelMedium)
                            .foregroundColor(.civitOnSurface.opacity(0.7))
                    }
            }
        }
        .onChange(of: blurRadius) { _ in
            isRevealed = false
        }
    }
}
