# Proposal

## Why

The app browses only models. Civitai's own front page leads with **Images, Videos, Articles** (and
Models) as first-class content types, so a user who wants to look at what people are making — rather
than to download a checkpoint — has nowhere to go. The data is already public through the REST API
the app uses; only the browse surface is missing.

## What Changes

- Add a **content-type switcher** (Models · Images · Videos · Articles) to the top of the Discover
  tab, mirroring the site's nav bar. Models keeps the existing screen untouched.
- Add an **image feed**: infinite-scrolling NSFW-filtered grid of the site's images with
  sort (Most Reactions / Newest / Most Comments) and period (Day / Week / Month / Year / All Time).
- Add a **video feed**: same feed, restricted to `type=video`, drawn from the same endpoint.
- Add a full-screen **media viewer** for a tapped image or video (video plays inline).
- Add an **article feed** (Newest / Most Reactions) and an **article reader** that renders the
  article body.
- Reuse the existing image stack (`CivitAiApi.getImages` → `ImageRepository` → `GetImagesUseCase`)
  rather than a second fetch path; add the one missing parameter (`type`) to it.
- Android/desktop UI for the new feeds is **not** part of this change (shared layer is, so those
  screens are a UI-only follow-up).

## Capabilities

### New Capabilities

- `media-feed`: paged browsing of Civitai images and videos, with sort/period/NSFW controls and a
  full-screen viewer.
- `articles`: paged browsing of Civitai articles and a reader that renders article content.

### Modified Capabilities

None — no existing spec covers browsing non-model content.
