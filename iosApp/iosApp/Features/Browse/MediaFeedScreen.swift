import SwiftUI
import Shared

/// Grid browse for the site-wide image and video feeds.
struct MediaFeedScreen: View {
    @State private var viewModel: MediaFeedViewModel
    @Environment(\.civitNsfwBlur) private var blurSettings
    @State private var viewerIndex: Int?

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
            .fullScreenCover(
                isPresented: Binding(
                    get: { viewerIndex != nil },
                    set: { if !$0 { viewerIndex = nil } }
                )
            ) {
                MediaViewerScreen(
                    items: state.items as? [CivitImage] ?? [],
                    initialIndex: viewerIndex ?? 0,
                    onLoadMore: { viewModel.loadMore() }
                )
            }
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

            Menu {
                Button("PG only") { viewModel.setNsfwFilter(level: .off) }
                Button("Soft") { viewModel.setNsfwFilter(level: .soft) }
                Button("All") { viewModel.setNsfwFilter(level: .all) }
            } label: {
                Label(Self.label(for: state.nsfwFilterLevel), systemImage: "eye")
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
                    ForEach(Array(items.enumerated()), id: \.element.id) { index, image in
                        gridCell(image, index: index)
                            .onAppear {
                                if index == items.count - 1 {
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

    private func gridCell(_ image: CivitImage, index: Int) -> some View {
        Button {
            viewerIndex = index
        } label: {
            ZStack(alignment: .bottomLeading) {
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
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }

                HStack(spacing: 8) {
                    Label("\(image.stats?.likeCount ?? 0)", systemImage: "heart.fill")
                    Label("\(image.stats?.commentCount ?? 0)", systemImage: "bubble.right.fill")
                }
                .font(.caption2)
                .foregroundStyle(.white)
                .padding(.horizontal, 6)
                .padding(.vertical, 3)
                .background(.black.opacity(0.45), in: Capsule())
                .padding(6)
            }
            .clipShape(RoundedRectangle(cornerRadius: 10))
        }
        .buttonStyle(.plain)
    }

    private static func isVideo(_ url: String) -> Bool {
        let lower = url.lowercased()
        return lower.contains(".mp4") || lower.contains(".webm") || lower.contains(".mov")
    }

    private static let sorts: [CivitSortOrder] = [.newest, .highestRated, .mostDownloaded]
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

    private static func label(for level: NsfwFilterLevel) -> String {
        switch level {
        case .soft: return "Soft"
        case .all: return "All"
        default: return "PG"
        }
    }
}

/// Full-screen viewer: swipe sideways between items, swipe up for generation details.
struct MediaViewerScreen: View {
    let items: [CivitImage]
    let onLoadMore: () -> Void

    @Environment(\.dismiss) private var dismiss
    @Environment(\.civitNsfwBlur) private var blurSettings
    @State private var index: Int
    @State private var showDetails = false

    init(items: [CivitImage], initialIndex: Int, onLoadMore: @escaping () -> Void) {
        self.items = items
        self.onLoadMore = onLoadMore
        let clamped = items.isEmpty ? 0 : min(max(initialIndex, 0), items.count - 1)
        _index = State(initialValue: clamped)
    }

    private var safeIndex: Int {
        items.isEmpty ? 0 : min(max(index, 0), items.count - 1)
    }

    private var current: CivitImage? {
        items.indices.contains(safeIndex) ? items[safeIndex] : nil
    }

    var body: some View {
        ZStack(alignment: .top) {
            Color.black.ignoresSafeArea()

            TabView(selection: $index) {
                ForEach(Array(items.enumerated()), id: \.element.id) { offset, image in
                    page(image)
                        .tag(offset)
                        .onAppear {
                            if offset >= items.count - 2 {
                                onLoadMore()
                            }
                        }
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .ignoresSafeArea()

            topBar
        }
        .simultaneousGesture(
            DragGesture(minimumDistance: 40).onEnded { value in
                let vertical = value.translation.height
                let horizontal = abs(value.translation.width)
                if vertical < -60 && horizontal < 80 {
                    showDetails = true
                }
            }
        )
        .sheet(isPresented: $showDetails) {
            GenerationDetailsSheet(image: current)
                .presentationDetents([.medium, .large])
        }
    }

    private var topBar: some View {
        HStack {
            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .font(.title2)
                    .foregroundStyle(.white.opacity(0.85))
            }
            .accessibilityLabel("Close")

            Spacer()

            Text(items.isEmpty ? "" : "\(safeIndex + 1) / \(items.count)")
                .font(.caption)
                .foregroundStyle(.white.opacity(0.8))

            Spacer()

            Button {
                showDetails = true
            } label: {
                Image(systemName: "chevron.up.circle.fill")
                    .font(.title2)
                    .foregroundStyle(.white.opacity(0.85))
            }
            .accessibilityLabel("Generation details")
        }
        .padding(.horizontal, Spacing.md)
        .padding(.top, Spacing.sm)
    }

    @ViewBuilder
    private func page(_ image: CivitImage) -> some View {
        ZStack {
            if Self.isVideo(image.url), let url = URL(string: image.url) {
                VideoPlayerContainer(url: url)
            } else {
                CachedAsyncImage(url: URL(string: image.url)) { phase in
                    if let uiImage = phase.image {
                        uiImage
                            .resizable()
                            .scaledToFit()
                            .blur(radius: blurSettings.blurRadius(for: image.nsfwLevel))
                    } else {
                        ProgressView().tint(.white)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private static func isVideo(_ url: String) -> Bool {
        let lower = url.lowercased()
        return lower.contains(".mp4") || lower.contains(".webm") || lower.contains(".mov")
    }
}

/// Prompt, generation settings and engagement for one item.
struct GenerationDetailsSheet: View {
    let image: CivitImage?
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: Spacing.sm) {
                    if let image {
                        header(image)
                        if let meta = image.meta {
                            generation(meta)
                        } else {
                            Text("No generation details available for this item.")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    } else {
                        Text("Nothing selected.")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .padding(Spacing.md)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .navigationTitle("Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }

    @ViewBuilder
    private func header(_ image: CivitImage) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            if let author = image.username {
                Text(author).font(.subheadline.weight(.semibold))
            }
            HStack(spacing: 12) {
                Label("\(image.stats?.likeCount ?? 0)", systemImage: "heart.fill")
                Label("\(image.stats?.heartCount ?? 0)", systemImage: "hand.thumbsup.fill")
                Label("\(image.stats?.commentCount ?? 0)", systemImage: "bubble.right.fill")
            }
            .font(.caption)
            .foregroundStyle(.secondary)

            if let baseModel = image.baseModel, !baseModel.isEmpty {
                Text("Base model: \(baseModel)").font(.caption)
            }
        }
    }

    @ViewBuilder
    private func generation(_ meta: ImageGenerationMeta) -> some View {
        VStack(alignment: .leading, spacing: Spacing.sm) {
            if let prompt = meta.prompt, !prompt.isEmpty {
                section("Prompt", prompt)
            }
            if let negative = meta.negativePrompt, !negative.isEmpty {
                section("Negative prompt", negative)
            }
            VStack(alignment: .leading, spacing: 6) {
                if let sampler = meta.sampler { row("Sampler", sampler) }
                if let steps = meta.steps { row("Steps", "\(steps)") }
                if let cfg = meta.cfgScale { row("CFG", "\(cfg)") }
                if let seed = meta.seed { row("Seed", "\(seed)") }
                if let size = meta.size { row("Size", size) }
                if let model = meta.model { row("Model", model) }
            }
        }
    }

    private func section(_ title: String, _ text: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title).font(.caption.weight(.semibold)).foregroundStyle(.secondary)
            Text(text).font(.caption.monospaced())
        }
    }

    private func row(_ label: String, _ value: String) -> some View {
        HStack {
            Text(label).font(.caption).foregroundStyle(.secondary)
            Spacer()
            Text(value).font(.caption.monospaced())
        }
    }
}
