import SwiftUI
import WebKit
import Shared

typealias CivitArticleDetail = Core_domainArticleDetail

/// Article reader: header plus the rendered HTML body.
struct ArticleDetailScreen: View {
    @State private var viewModel: ArticleDetailViewModel
    @Environment(\.dismiss) private var dismiss

    init(articleId: Int64) {
        _viewModel = State(
            initialValue: KoinHelper.shared.createArticleDetailViewModel(articleId: articleId)
        )
    }

    var body: some View {
        NavigationStack {
            Observing(viewModel.uiState) {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } content: { state in
                content(state: state)
            }
            .navigationTitle("Article")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .onAppear { viewModel.load() }
    }

    @ViewBuilder
    private func content(state: ArticleDetailUiState) -> some View {
        if let article = state.article {
            ScrollView {
                VStack(alignment: .leading, spacing: Spacing.sm) {
                    Text(article.title)
                        .font(.title3.weight(.semibold))

                    HStack(spacing: 6) {
                        if let author = article.username {
                            Text(author)
                        }
                        if let published = article.publishedAt, !published.isEmpty {
                            Text("· \(published.prefix(10))")
                        }
                    }
                    .font(.caption)
                    .foregroundStyle(.secondary)

                    if !article.tags.isEmpty {
                        Text((article.tags as? [String] ?? []).joined(separator: " · "))
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }

                    ArticleHTMLView(html: article.contentHtml)
                        .frame(minHeight: 320)
                }
                .padding(.horizontal, Spacing.md)
                .padding(.vertical, Spacing.sm)
            }
        } else if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            VStack(spacing: Spacing.sm) {
                Text(state.error ?? "Could not load this article.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                Button("Retry") { viewModel.retry() }
                    .buttonStyle(.borderedProminent)
            }
            .padding()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

/// Renders the article body HTML. JS off, no local file access, links open outside.
struct ArticleHTMLView: UIViewRepresentable {
    let html: String

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.defaultWebpagePreferences.allowsContentJavaScript = false
        let view = WKWebView(frame: .zero, configuration: config)
        view.navigationDelegate = context.coordinator
        view.isOpaque = false
        view.backgroundColor = .clear
        view.scrollView.backgroundColor = .clear
        return view
    }

    func updateUIView(_ view: WKWebView, context: Context) {
        view.loadHTMLString(Self.document(html), baseURL: nil)
    }

    private static func document(_ body: String) -> String {
        """
        <!doctype html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
        :root { color-scheme: light dark; }
        body { font: -apple-system-body; line-height: 1.5; margin: 0; padding: 0 0 24px 0;
               color: #111; background: transparent; word-wrap: break-word; }
        @media (prefers-color-scheme: dark) { body { color: #eee; } }
        img, video { max-width: 100%; height: auto; border-radius: 8px; }
        pre { overflow-x: auto; background: rgba(127,127,127,0.15); padding: 8px; border-radius: 8px; }
        a { color: #4a9eff; }
        </style></head><body>\(body)</body></html>
        """
    }

    final class Coordinator: NSObject, WKNavigationDelegate {
        func webView(
            _ webView: WKWebView,
            decidePolicyFor navigationAction: WKNavigationAction,
            decisionHandler: @escaping (WKNavigationActionPolicy) -> Void
        ) {
            guard let url = navigationAction.request.url else {
                decisionHandler(.allow)
                return
            }
            // Only the initial loadHTMLString (about:blank) stays in the reader.
            if navigationAction.navigationType == .linkActivated {
                UIApplication.shared.open(url)
                decisionHandler(.cancel)
                return
            }
            decisionHandler(.allow)
        }
    }
}
