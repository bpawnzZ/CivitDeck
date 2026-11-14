# Spec Delta

## ADDED Requirements

### Requirement: Feed ordering matches the site
The image and video feeds MUST default to the newest items, and MUST offer Newest, Most Reactions and
Most Comments as selectable orderings.

#### Scenario: Opening the feed
- **WHEN** the user opens Images or Videos
- **THEN** the first page is the most recently uploaded media, not the all-time most-liked

### Requirement: Level control in the feed
The feed MUST let the user change the NSFW level it fetches without leaving the feed.

#### Scenario: Raising the level
- **WHEN** the user selects All in the feed's level control
- **THEN** the feed reloads and subsequent pages may include mature and explicit items
- **AND** the change applies to the rest of the app, because there is one content-filter setting

### Requirement: Tile engagement
Each grid tile MUST show its like and comment counts.

#### Scenario: Tile with no engagement
- **WHEN** an item has zero likes and zero comments
- **THEN** the counters read zero rather than disappearing or showing placeholder text

### Requirement: Viewer navigation
The full-screen viewer MUST move to the next or previous item on horizontal swipe, and MUST request
more items when the user nears the last loaded one.

#### Scenario: Swiping past the last loaded item
- **WHEN** the user swipes to the final loaded item
- **THEN** the next page is requested and the viewer continues into it

### Requirement: Generation details
Swiping up in the viewer MUST reveal the item's generation details: prompt, negative prompt,
sampler, steps, CFG scale, seed, size, base model and author, omitting fields the item does not carry.

#### Scenario: Item without metadata
- **WHEN** an item has no generation metadata
- **THEN** the panel still opens and states that no generation details are available
