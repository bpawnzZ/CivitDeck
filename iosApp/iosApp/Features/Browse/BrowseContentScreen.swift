import SwiftUI
import Shared

/// The site's top-level content types, as a segmented switcher inside Discover.
enum BrowseContentKind: String, CaseIterable, Identifiable {
    case models = "Models"
    case images = "Images"
    case videos = "Videos"
    case articles = "Articles"

    var id: String { rawValue }
}

struct BrowseContentScreen: View {
    let searchViewModel: ModelSearchViewModel
    @State private var kind: BrowseContentKind = .models

    var body: some View {
        VStack(spacing: 0) {
            Picker("Content", selection: $kind) {
                ForEach(BrowseContentKind.allCases) { kind in
                    Text(kind.rawValue).tag(kind)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, Spacing.md)
            .padding(.vertical, Spacing.sm)

            // Each branch keeps its own state: switching type does not reset a feed.
            switch kind {
            case .models:
                ModelSearchScreen(viewModel: searchViewModel)
            case .images:
                MediaFeedScreen(mediaType: .image)
            case .videos:
                MediaFeedScreen(mediaType: .video)
            case .articles:
                ArticleFeedScreen()
            }
        }
    }
}
