# Design

## Context

See proposal.md. Verified against the live API:

- `/api/v1/images?type=video&sort=Most Reactions&period=AllTime` returns items from 2025-04/05, while
  `sort=Newest` returns items created today — the site's browse pages lead with fresh uploads. That is
  the whole "different videos" gap; the endpoint, type filter and paging were already correct.
- `nsfw=None` restricts to SFW only, so a filter level of Off silently shows a much narrower feed than
  the site (whose own request carries `nsfwLevels:[1,2,4,8,16]`). The feed therefore needs to expose
  the level, not silently inherit Settings.
- `ImageGenerationMeta` already parses prompt/negative/sampler/cfg/steps/seed/model/size; the API also
  returns `baseModel` on images, which the DTO dropped.

## Goals / Non-Goals

**Goals:** parity with the site's default ordering and content range, engagement visible in the grid,
one-tap continuation in the viewer.

**Non-Goals:** image tags (the REST image payload carries none — tags live on the post, which needs a
second tRPC call); reactions/liking from the app (auth); video player chrome beyond AVKit defaults.

## Decisions

**1. Default sort Newest at the ViewModel, not the UI.** The feed's own default is what Alpha sees
first; changing it in `MediaFeedUiState` keeps Android/desktop consistent for free.

**2. The level control writes the existing global setting** via `SetNsfwFilterUseCase`, instead of a
feed-local level. A per-feed level would fork the app's single content-filter concept and confuse the
Settings screen. The feed already observes that setting, so it reloads itself.

**3. Paged `TabView` for the viewer.** `.tabViewStyle(.page(indexDisplayMode: .never))` gives
left/right swiping, index reporting and preloading with no gesture bookkeeping. A custom drag plus
offset arithmetic would be more code for the same behaviour and would fight the video player.

**4. Swipe-up is a drag gesture that only triggers on a clearly upward flick** (`height < -60`), with
a visible chevron button as the discoverable affordance, so a vertical scroll inside the video player
cannot trap the gesture.

**5. `baseModel` travels on the domain `Image`** as an optional field with a default, so no caller
breaks and the metadata panel can show it.

## Risks / Trade-offs

- The viewer takes a snapshot array plus an `onLoadMore` callback; if a refresh replaces the whole
  list mid-view the index can point past the end — clamp on render.
- Video playback in a paged container keeps one `AVPlayer` per adjacent page; SwiftUI releases pages
  that leave the window, which stops their audio.
- `ponytail:` tags are omitted rather than fetched; add a post lookup when the panel needs them.
