# Task: DVX-TK-045 — Redesign News Section for Better Readability and Visual Appeal

## Problem

The current news section (`NewsCard` / `NewsSection`) is visually flat and unattractive:
- No images or thumbnails
- Plain text list with minimal visual hierarchy
- No card elevation or background contrast
- Compact mode is barely distinguishable from full mode
- No visual distinction between read/unread or fresh/old articles
- Missing sentiment or category indicators

## Current Components

- `common/ui-resources/.../components/NewsCard.kt` — single news item (title + summary + publisher + time)
- `common/ui-resources/.../components/NewsSection.kt` — list wrapper with title + dividers
- `common/ui-resources/.../components/NewsItemUi.kt` — data model
- Used in: `feature/details/.../SecurityDetailScreen.kt`

## Proposed Improvements

### Visual Enhancements
1. **Card-based layout** — wrap each item in `ElevatedCard` or `OutlinedCard` with rounded corners
2. **Thumbnail images** — if API provides image URLs, show article thumbnail (leading or top)
3. **Better typography** — headline in `titleSmall`, publisher in colored chip or badge
4. **Time indicator** — relative time with icon (clock icon + "2h ago")
5. **Source favicon/logo** — small publisher icon next to source name
6. **Sentiment indicator** — colored dot or icon for bullish/bearish/neutral (if data available)

### Layout Options
- **Option A — Horizontal cards**: thumbnail left, text right (like Google News)
- **Option B — Vertical cards**: thumbnail top, text below (like Apple News)
- **Option C — Mixed**: first item large (vertical), rest compact (horizontal)

### Data Model Changes
Check if `NewsItemUi` needs new fields:
```kotlin
data class NewsItemUi(
    val title: String,
    val summary: String?,
    val publisher: String,
    val publishedAtEpochSeconds: Long,
    val link: String,
    val imageUrl: String?,        // NEW — article thumbnail
    val sentiment: Sentiment?,    // NEW — if available from API
)
```

### Skeleton/Loading State
Current placeholder is basic gray boxes. Improve with shimmer animation using
`Modifier.placeholder()` or custom shimmer brush.

## Design Reference

Check Stitch project for news section design: https://stitch.withgoogle.com/projects/10568397103146599411

## Constraints

- Use `MaterialTheme.spacing.*` — no hardcoded dp values
- All strings from `strings.xml`
- Components reusable across features (stays in `common/ui-resources`)
- Must work on Android, iOS, and Desktop

## Priority

**Medium** — improves perceived quality of the app.

## Labels

`enhancement`, `ui/ux`, `design`
