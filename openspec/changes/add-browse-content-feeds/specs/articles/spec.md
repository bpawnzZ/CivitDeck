# Spec Delta

## Purpose

Reading the site's articles inside the app instead of a browser.

## ADDED Requirements

### Requirement: Article feed
The Articles feed MUST show a paged list of published articles ordered newest-first by default,
each entry showing cover image (when present), title, author and engagement counts.

#### Scenario: Scrolling the feed
- **WHEN** the user scrolls near the end of the loaded articles
- **THEN** the next page is requested using the cursor returned by the previous page

#### Scenario: Alternate sort
- **WHEN** the user selects Most Reactions
- **THEN** the feed reloads from the first page in that ordering

### Requirement: Article reader
Opening an article MUST show its title, author, published date and rendered body content.

#### Scenario: Rendering the body
- **WHEN** an article is opened
- **THEN** its content is fetched by id and rendered as formatted HTML (headings, lists, links,
  code), not as raw markup

#### Scenario: Body unavailable
- **WHEN** the body request fails
- **THEN** the reader shows an error with a retry action and keeps the header information visible
