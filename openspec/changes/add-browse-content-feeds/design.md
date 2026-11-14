# Design

## Context

See proposal.md — Why. Relevant current state, verified against the live API and this tree:

- `civitai.red` and `civitai.com` share one API; the app pins `https://civitai.com/api/v1`
  (`CivitAiEndpoints.kt`), so no new host handling is needed.
- `GET /api/v1/images` accepts `type=image|video`, `sort=Most Reactions|Newest|Most Comments`,
  `period=Day|Week|Month|Year|AllTime`, `nsfw`, `limit`, `cursor`, `withMeta`. Video items carry the
  `.mp4` in `url` and a poster in `thumbnail`. Cursor paging via `metadata.nextCursor`.
- `GET /api/v1/articles` returns `items` + `metadata.nextCursor`; `sort` accepts `Newest` and
  `Most Reactions` (anything else is a 400). `GET /api/v1/articles/{id}` returns the HTML body in
  `content`; list items have no body.
- An image stack already exists end to end — `CivitAiApi.getImages`, `ImageRepository`,
  `GetImagesUseCase`, domain `Image` (already carries `contentType`) — used by the per-model gallery.
- The iOS app registers Swift files explicitly in `iosApp.xcodeproj/project.pbxproj` (no
  file-system-synchronized groups), so new Swift files need project entries.
- Discover tab is `ModelSearchScreen`, which is self-contained and must stay as-is.

## Goals / Non-Goals

**Goals:** one shared (KMP) fetch/paging layer reused by every platform; iOS UI shipped in this
change; zero behavioural change to the Models screen.

**Non-Goals:** Android and desktop Compose screens (follow-up, shared layer already serves them);
Bounties/Challenges (no public REST endpoint — 404 on `/api/v1/bounties`); article comments,
reactions, or posting; a new bottom tab (the switcher lives inside Discover).

## Decisions

**1. Reuse `getImages` with a `type` parameter rather than a new endpoint.**
The video feed is the same endpoint with `type=video`, so only `CivitAiApi.getImages` →
`ImageRepository` → `GetImagesUseCase` gain one optional parameter. Alternative (separate video API
call) duplicates query building and caching for no gain.

**2. New `MediaFeedViewModel`, not `ImageGalleryViewModel`.**
The gallery ViewModel is bound to a `modelVersionId`, auto-saves prompts, and owns gallery-only
aspect-ratio state. Reusing it would mean nullable-ising its core identity and threading prompt
persistence through a browse feed. The new ViewModel is ~100 lines on the existing `PaginatedLoader`
and reuses `SortOrder` / `TimePeriod` / `nsfwFilterLevel` semantics.

**3. New `feature:feature-articles` module.** Follows the repo's module-per-feature layout
(`feature-collections` is the template). Keeps article DTO→repo→VM wiring out of the gallery module,
which is about images.

**4. Content switcher inside Discover, not a new tab.**
The site puts these in the top nav, and a 4th bottom tab would compete with Discover for the same
intent. An `@State` enum + segmented bar in a thin SwiftUI container keeps `ModelSearchScreen`
untouched; child views stay alive so their state survives switching.

**5. Article body rendered in `WKWebView`.** Bodies are HTML (`<p>`, lists, links, images). Rendering
HTML→`AttributedString` loses images and table layout; a `WKWebView` is ~40 lines and correct. Links
open in the system browser via a navigation delegate so the reader cannot get trapped.

## Risks / Trade-offs

- **Unsigned-build only.** New Swift files must be added to `project.pbxproj` by hand; a missed entry
  is a compile error on CI, which is caught by the release workflow before any IPA ships.
- **NSFW content on browse surfaces.** The feed passes the user's existing NSFW level as `nsfw=`, and
  the grid applies the same blur settings as model cards; the app never widens the filter here.
- **Article HTML is untrusted third-party markup.** `WKWebView` runs it sandboxed with JavaScript
  disabled, no local file access, and external links handed to the OS.
- **Video playback uses `AVPlayer` on remote `.mp4`s.** No HLS/DRM; a broken URL shows the poster
  with a play error rather than a black screen.
- `ponytail:` article feed and media feed share the `PaginatedLoader` but not a generic "feed" base
  class — two call sites do not justify an abstraction; add one if a third feed appears.
