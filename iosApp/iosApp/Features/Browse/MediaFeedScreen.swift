import SwiftUI
import Shared

/// Grid browse for the site-wide image and video feeds.
struct MediaFeedScreen: View {
    @State private var viewModel: MediaFeedViewModel
    @Environment(\.civitNsfwBlur) private var blurSettings
    @State private var viewerItem: MediaViewerItem?

    private let columns = [GridItem(.adaptive(minimum: 110, maximum: 200), spacing: Spacing.sm)]

    init(mediaType: MediaContentType) {
        _viewModel = State(initialValue: KoinHelper.shared.createMediaFeedViewModel(mediaType: mediaType))
    }

    var body: some View {
        Observing(viewModel.uiState) {
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        } content: { state in
            VStack(spacing: 0) {
                filterBar(state: state)
                content(state: state)
            }
        }
        .fullScreenCover(item: $viewerItem) { item in
            MediaViewerScreen(item: item)
        }
    }

    @ViewBuilder
    private func filterBar(state: MediaFeedUiState) -> some View {
        HStack(spacing: Spacing.sm) {
            Menu {
                ForEach(Self.sorts, id: \.self) { sort in
                    Button(Self.label(for: sort)) { viewModel.setSort(sort: sort) }
                }
            } label: {
                Label(Self.label(for: state.selectedSort), systemImage: "arrow.up.arrow.down")
                    .font(.caption)
            }

            Menu {
                ForEach(Self.periods, id: \.self) { period in
                    Button(Self.label(for: period)) { viewModel.setPeriod(period: period) }
                }
            } label: {
                Label(Self.label(for: state.selectedPeriod), systemImage: "calendar")
                    .font(.caption)
            }

            Spacer()

            if let error = state.error {
                Button {
                    viewModel.retry()
                } label: {
                    Label(error, systemImage: "arrow.clockwise")
                        .font(.caption2)
                        .lineLimit(1)
                }
                .foregroundStyle(.red)
            }
        }
        .padding(.horizontal, Spacing.md)
        .padding(.bottom, Spacing.sm)
    }

    @ViewBuilder
    private func content(state: MediaFeedUiState) -> some View {
        let items = state.items as? [CivitImage] ?? []
        if items.isEmpty {
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                VStack(spacing: Spacing.sm) {
                    Image(systemName: "photo.on.rectangle")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text("No media")
                        .font(.headline)
                    Text(state.error ?? "Nothing to show for this filter.")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        } else {
            ScrollView {
                LazyVGrid(columns: columns, spacing: Spacing.sm) {
                    ForEach(items, id: \.id) { image in
                        gridCell(image)
                            .onAppear {
                                if image.id == items.last?.id {
                                    viewModel.loadMore()
                                }
                            }
                    }
                }
                .padding(.horizontal, Spacing.md)

                if state.isLoadingMore {
                    ProgressView().padding(.vertical, Spacing.md)
                }
            }
        }
    }

    private func gridCell(_ image: CivitImage) -> some View {
        Button {
            viewerItem = MediaViewerItem(
                id: image.id,
                url: image.url,
                thumbnailUrl: image.thumbnailUrl ?? image.url,
                isVideo: Self.isVideo(image.url),
                aspectRatio: image.height > 0 ? CGFloat(image.width) / CGFloat(image.height) : 1
            )
        } label: {
            ZStack {
                CachedAsyncImage(url: URL(string: image.thumbnailUrl ?? image.url)) { phase in
                    if let uiImage = phase.image {
                        uiImage.resizable().aspectRatio(contentMode: .fill)
                    } else {
                        Rectangle().fill(Color.secondary.opacity(0.15))
                    }
                }
                .aspectRatio(image.height > 0 ? CGFloat(image.width) / CGFloat(image.height) : 1, contentMode: .fit)
                .clipped()
                .blur(radius: blurSettings.blurRadius(for: image.nsfwLevel))

                if Self.isVideo(image.url) {
                    Image(systemName: "play.circle.fill")
                        .font(.title2)
                        .foregroundStyle(.white)
                        .shadow(radius: 3)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 10))
        }
        .buttonStyle(.plain)
    }

    private static func isVideo(_ url: String) -> Bool {
        let lower = url.lowercased()
        return lower.contains(".mp4") || lower.contains(".webm") || lower.contains(".mov")
    }

    private static let sorts: [CivitSortOrder] = [.highestRated, .newest, .mostDownloaded]
    private static let periods: [TimePeriod] = [.allTime, .day, .week, .month, .year]

    private static func label(for sort: CivitSortOrder) -> String {
        switch sort {
        case .newest: return "Newest"
        case .mostDownloaded: return "Most Comments"
        case .quality: return "Quality"
        default: return "Most Reactions"
        }
    }

    private static func label(for period: TimePeriod) -> String {
        switch period {
        case .day: return "Today"
        case .week: return "Week"
        case .month: return "Month"
        case .year: return "Year"
        default: return "All Time"
        }
    }
}

struct MediaViewerItem: Identifiable {
    let id: Int64
    let url: String
    let thumbnailUrl: String
    let isVideo: Bool
    let aspectRatio: CGFloat
}

/// Full-screen image or video viewer.
struct MediaViewerScreen: View {
    let item: MediaViewerItem
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Color.black.ignoresSafeArea()

            if item.isVideo, let url = URL(string: item.url) {
                VideoPlayerContainer(url: url)
                    .ignoresSafeArea()
            } else {
                CachedAsyncImage(url: URL(string: item.url)) { phase in
                    if let uiImage = phase.image {
                        uiImage.resizable().scaledToFit()
                    } else {
                        ProgressView().tint(.white)
                    }
                }
            }

            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .font(.title)
                    .foregroundStyle(.white.opacity(0.85))
                    .padding()
            }
            .accessibilityLabel("Close")
        }
    }
}
