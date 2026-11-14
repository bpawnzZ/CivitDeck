# Spec Delta

## Purpose

Browsing the site's images and videos as media, independent of any model page.

## ADDED Requirements

### Requirement: Content-type navigation
The Discover surface MUST offer a content-type switcher with Models, Images, Videos and Articles,
and MUST open on Models.

#### Scenario: Switching content type
- **WHEN** the user selects Images, Videos or Articles in the switcher
- **THEN** the corresponding feed is shown without leaving the Discover surface
- **AND** the previous feed's scroll position and filters are retained when switching back

### Requirement: Image feed
The Images feed MUST show a paged, infinite-scrolling grid of the site's images, newest page
appended on scroll.

#### Scenario: First page
- **WHEN** the Images feed is opened
- **THEN** a first page of thumbnails is requested and rendered in a grid
- **AND** a further page is requested when the user nears the end

#### Scenario: Empty or failed page
- **WHEN** a page request returns no items or fails
- **THEN** the feed shows an empty or error state that can be retried
- **AND** already-loaded items remain visible

### Requirement: Media type filter
The feed MUST be able to request images only or videos only from the same endpoint.

#### Scenario: Videos feed
- **WHEN** the user selects Videos
- **THEN** only items whose media type is video are requested and rendered
- **AND** each item shows its preview thumbnail rather than playing inline in the grid

### Requirement: Sort and period
The feed MUST support sort by Most Reactions, Newest and Most Comments, and period Day, Week, Month,
Year and All Time, defaulting to Most Reactions / All Time.

#### Scenario: Changing sort
- **WHEN** the user picks a different sort or period
- **THEN** the feed is reloaded from the first page with that ordering
- **AND** previously loaded items for the old ordering are discarded

### Requirement: NSFW filtering and blur
The feed MUST honour the existing NSFW filter level and blur settings.

#### Scenario: Blur applied
- **WHEN** the user's NSFW filter excludes a level, or a blur intensity is configured for it
- **THEN** the thumbnail is hidden or blurred to that intensity, consistent with the rest of the app

### Requirement: Media viewer
Tapping a grid item MUST open a full-screen viewer showing the full-size image, or a playable video
for video items.

#### Scenario: Opening a video
- **WHEN** the user taps a video thumbnail
- **THEN** the viewer plays the video with playback controls
- **AND** closing the viewer stops playback
