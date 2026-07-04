# Task: TK-037 · feature:dashboard + feature:analysis — News Feed

## Description

Add a news feed powered by Yahoo Finance `/v1/finance/search` (no auth, no crumb required — same HTTP client already in use). News appears in two places:

1. **Dashboard** — General market news section below Índices Bursátiles, using major index tickers as query (`^GSPC`, `^IBEX`, etc.)
2. **Quote Analysis** — Compact news section (max 3 items) below Fundamentales, scoped to the viewed ticker

Additionally, **reorder the Dashboard sections**: move *Tus Favoritos* above *Índices Bursátiles*.

### API

```
GET https://query1.finance.yahoo.com/v1/finance/search
  ?q={ticker_or_index}
  &quotesCount=0
  &newsCount=10
  &lang=es
  &region=ES
```

- No authentication required
- Returns news in Spanish when `lang=es&region=ES` is passed
- Confirmed working with curl for tickers (`AAPL`) and indices (`^GSPC`, `^IBEX`)

### News item fields

| Field | Description |
|-------|-------------|
| `uuid` | Unique identifier |
| `title` | Headline |
| `publisher` | Source name (Bloomberg, Investing.com…) |
| `link` | Article URL (open in browser) |
| `providerPublishTime` | Unix timestamp |
| `thumbnail.resolutions` | Array of image URLs (140x140, original) |
| `relatedTickers` | Associated tickers |

**Depends on:** TK-015 (component:market), TK-018 (feature:dashboard), TK-024 (feature:analysis)
**Blocks:** —
**Status:** Backlog

---

## Subtasks

### Phase 1: Domain & Data — `component/market`
- [x] Add `NewsItem` domain model: `uuid`, `title`, `publisher`, `link`, `publishedAt: Long`, `thumbnailUrl: String?`, `relatedTickers: List<String>`
- [x] Add `NewsItemDto` + mapper from search response `news` array
- [x] Extend `SearchResponseDto` to parse `news` field
- [x] Add `GetStockNewsUseCase(ticker: String, count: Int = 10): Flow<List<NewsItem>>`
- [x] Add `GetMarketNewsUseCase(indexTicker: String = "^GSPC", count: Int = 10): Flow<List<NewsItem>>`
- [x] **Commit:** `DVX-TK-037 Add NewsItem domain model and GetNews use cases`

### Phase 2: Shared UI — `common/ui-resources`
- [x] Add `NewsCard` composable: thumbnail (optional, rounded), title (2 lines max), publisher + relative date on one line, tap → `LocalUriHandler.openUri(link)`
- [x] Add `NewsSection` composable: title row + vertical list of `NewsCard`, with a "Ver más" link
- [x] Add strings: `news_section_title`, `news_view_more`, `news_no_results`
- [x] **Commit:** `DVX-TK-037 Add NewsCard and NewsSection composables`

### Phase 3: Dashboard — `feature/dashboard`
- [x] **Reorder sections**: move *Tus Favoritos* above *Índices Bursátiles*
- [x] Add `GetMarketNewsUseCase` to `DashboardViewModel`, load on init with `^GSPC` or user's `defaultMarket` index
- [x] Add `newsList: List<NewsItem>` to `DashboardState`
- [x] Render `NewsSection` below Índices Bursátiles in `DashboardScreen`
- [x] News loads independently (does not block portfolio/watchlist rendering)
- [x] **Commit:** `DVX-TK-037 Add market news section to Dashboard and reorder sections`

### Phase 4: Quote Analysis — `feature/analysis`
- [x] Add `GetStockNewsUseCase` to `SecurityDetailViewModel`, load on ticker init, max 3 items
- [x] Add `newsList: List<NewsItem>` to `SecurityDetailState`
- [x] Render compact `NewsSection` (max 3 cards, no "Ver más" needed) below Fundamentales in `SecurityDetailScreen`
- [x] Keep cards compact: no thumbnail, title (1 line ellipsis), publisher + date inline
- [x] **Commit:** `DVX-TK-037 Add compact news section to quote analysis screen`

### Phase 5: Tests & Quality
- [x] Unit test `GetStockNewsUseCase`: happy path, empty list, network error
- [x] Unit test `GetMarketNewsUseCase`
- [x] `./gradlew :component:market:jvmTest`
- [x] `./gradlew detekt`
- [x] **Commit:** `DVX-TK-037 Add news use case unit tests`

---

## Progress Tracking
**Total Tasks:** 14 · **Completed:** 0 · **Remaining:** 14

## Notes
- `lang=es&region=ES` returns Spanish-language sources (Investing.com, MT Newswires ES). English returns Bloomberg, Reuters, AP — higher quality but not localized.
- Thumbnail is optional — not all articles include one; `NewsCard` must handle null gracefully.
- News does not require a separate HTTP client or module — reuse existing Ktor client in `:component:market`.
- Dashboard news query: use the index matching `AppSettings.defaultMarket` (e.g. `ES` → `^IBEX`, `US` → `^GSPC`).
