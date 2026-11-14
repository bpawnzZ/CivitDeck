import SwiftUI
import Shared

/// Paged list of the site's articles.
struct ArticleFeedScreen: View {
    @State private var viewModel = KoinHelper.shared.createArticleFeedViewModel()
    @State private var selectedArticleId: Int64?

    var body: some View {
        Observing(viewModel.uiState) {
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        } content: { state in
            content(state: state)
        }
        .onAppear { viewModel.loadFirstPage() }
        .sheet(item: Binding(
            get: { selectedArticleId.map(IdentifiableId.init) },
            set: { selectedArticleId = $0?.value }
        )) { wrapper in
            ArticleDetailScreen(articleId: wrapper.value)
        }
    }

    @ViewBuilder
    private func content(state: ArticleFeedUiState) -> some View {
        let items = state.items as? [Article] ?? []

        VStack(spacing: 0) {
            HStack {
                Menu {
                    Button("Newest") { viewModel.setSort(sort: .newest) }
                    Button("Most Reactions") { viewModel.setSort(sort: .mostReactions) }
                } label: {
                    Label(
                        state.sort == .mostReactions ? "Most Reactions" : "Newest",
                        systemImage: "arrow.up.arrow.down"
                    )
                    .font(.caption)
                }
                Spacer()
                if let error = state.error {
                    Button { viewModel.retry() } label: {
                        Label(error, systemImage: "arrow.clockwise")
                            .font(.caption2)
                            .lineLimit(1)
                    }
                    .foregroundStyle(.red)
                }
            }
            .padding(.horizontal, Spacing.md)
            .padding(.bottom, Spacing.sm)

            if items.isEmpty {
                if state.isLoading {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    VStack(spacing: Spacing.sm) {
                        Image(systemName: "doc.text")
                            .font(.largeTitle)
                            .foregroundStyle(.secondary)
                        Text(state.error ?? "No articles")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: Spacing.sm) {
                        ForEach(items, id: \.id) { article in
                            Button {
                                selectedArticleId = article.id
                            } label: {
                                ArticleRow(article: article)
                            }
                            .buttonStyle(.plain)
                            .onAppear {
                                if article.id == items.last?.id {
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
    }
}

private struct ArticleRow: View {
    let article: Article

    var body: some View {
        HStack(alignment: .top, spacing: Spacing.sm) {
            if let cover = article.coverUrl, let url = URL(string: cover) {
                CachedAsyncImage(url: url) { phase in
                    if let image = phase.image {
                        image.resizable().scaledToFill()
                    } else {
                        Rectangle().fill(Color.secondary.opacity(0.15))
                    }
                }
                .frame(width: 88, height: 66)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(article.title)
                    .font(.subheadline.weight(.semibold))
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                if let author = article.username {
                    Text(author)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Text(
                    "\u{2764} \(article.likeCount)  \u{1F4AC} \(article.commentCount)  \u{1F441} \(article.viewCount)"
                )
                .font(.caption2)
                .foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 6)
        .contentShape(Rectangle())
    }
}

/// Wraps a bare id so it can drive a `.sheet(item:)`.
struct IdentifiableId: Identifiable {
    let value: Int64
    var id: Int64 { value }
}
