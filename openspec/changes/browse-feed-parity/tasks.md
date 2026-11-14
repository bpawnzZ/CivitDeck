# Tasks

## 1. Shared

- [ ] 1.1 Default `MediaFeedUiState.selectedSort` to Newest
- [ ] 1.2 Add `setNsfwFilter(level:)` to `MediaFeedViewModel` via `SetNsfwFilterUseCase`; register in GalleryModule
- [ ] 1.3 Add `baseModel` to `ImageDto`, domain `Image`, and the mapper
- [ ] 1.4 Extend `MediaFeedViewModelTest` for the new default and the level change

## 2. iOS

- [ ] 2.1 Grid tile: like + comment counts
- [ ] 2.2 Feed bar: NSFW level menu
- [ ] 2.3 Viewer: paged TabView, swipe to next/prev, load more near the end
- [ ] 2.4 Viewer: swipe-up generation panel (prompt, negative, sampler, steps, CFG, seed, size, base model, author)
- [ ] 2.5 Register any new Swift file in project.pbxproj; add SKIE typealiases if new Kotlin types are named

## 3. Verify + ship

- [ ] 3.1 `:shared:jvmTest`, `:feature:feature-gallery:jvmTest`, `detekt` green
- [ ] 3.2 `release.yml` ios_only run green; IPA inspected
- [ ] 3.3 Tag the release, tick tasks, archive the change
