# Tasks

## 1. Shared layer — media feed

- [ ] 1.1 Add `type` (media type) parameter to `CivitAiApi.getImages`, `ImageRepository`, `ImageRepositoryImpl`, `GetImagesUseCase`; map `thumbnail` in `ImageDto`/`DtoMapper`
- [ ] 1.2 Add `MediaFeedViewModel` (feature-gallery) with paging, sort, period, NSFW level, media type; wire into `GalleryModule`
- [ ] 1.3 Unit test `MediaFeedViewModel` paging + reload-on-filter-change against a fake repository

## 2. Shared layer — articles

- [ ] 2.1 `feature:feature-articles` module (build.gradle.kts, settings.gradle.kts include)
- [ ] 2.2 `ArticleDto`/`ArticleListResponse` + `getArticles`/`getArticle(id)` in `CivitAiApi`, mapped to domain `Article`/`ArticleDetail`
- [ ] 2.3 `ArticleRepository`/`GetArticlesUseCase`/`GetArticleUseCase` + Koin module
- [ ] 2.4 `ArticleFeedViewModel` (cursor paging, sort) + `ArticleDetailViewModel` (load body, retry)
- [ ] 2.5 Unit tests for both ViewModels

## 3. iOS bridge

- [ ] 3.1 KoinHelper accessors for the three new ViewModels (iosMain)
- [ ] 3.2 Swift view-model owners following the `Observing` pattern

## 4. iOS UI

- [ ] 4.1 `BrowseContentScreen` — content-type bar (Models/Images/Videos/Articles) hosting the
      existing `ModelSearchScreen` plus the new feeds
- [ ] 4.2 `MediaFeedScreen` — grid, sort/period menu, NSFW blur, infinite scroll, tap to viewer
- [ ] 4.3 `MediaViewerScreen` — full-size image / `AVPlayer` video
- [ ] 4.4 `ArticleFeedScreen` — list with cover, title, author, stats; infinite scroll
- [ ] 4.5 `ArticleDetailScreen` — `WKWebView` body, header, retry, external links to browser
- [ ] 4.6 Register all new Swift files in `project.pbxproj`; point the Discover tab at
      `BrowseContentScreen`

## 5. Verify + release

- [ ] 5.1 `./gradlew :shared:jvmTest` and `detekt` green
- [ ] 5.2 `release.yml` `ios_only` run green; IPA downloaded and inspected
- [ ] 5.3 Tag a release with the feature and report the install link
- [ ] 5.4 Write `AGENT.md` for the repo (build/CI/traps) and archive this change
