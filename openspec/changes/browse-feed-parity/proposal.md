# Proposal

## Why

The new browse feeds do not show what civitai.red/videos and /images show: they open on
"Most Reactions / All Time", which surfaces 2025's top postings instead of today's uploads, and the
grid hides the engagement numbers the site puts on every tile. The detail view is a dead end — no
prompt, no generation settings, and no way onward to the next item.

## What Changes

- Media feeds open on **Newest** (matching the site's default ordering) instead of Most Reactions.
- The feed bar gains an **NSFW level selector** (None / Soft / All) writing the existing global
  content-filter setting, so the feed can show what the site shows without a trip to Settings.
- Grid tiles show **likes and comment counts**.
- The full-screen viewer becomes **swipeable between items**, loads more as the end is reached,
  and **swipe up** opens a generation panel: prompt, negative prompt, sampler, steps, CFG, seed,
  size, base model, author.
- `Image` carries `baseModel` (already in the API response, previously dropped).

## Capabilities

### Modified Capabilities

- `media-feed`: ordering default, level control, tile stats, and viewer navigation are
  requirement changes to the existing capability.
