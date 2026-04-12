# Task: TK-017 · integration:security — Scaffold + Enriched Models + Use Cases

## Description

Scaffold `:integration:security`, define `SecurityHolding`, `PortfolioSummary`, `EnrichedWatchlistEntry`, and `SecurityDetail` enriched models, implement the integration use cases that combine portfolio + market + watchlist data, and register the Koin module.

**User Stories:** DVX-US-005 · DVX-US-007 · DVX-US-020
**ADRs:** ADR-006
**Depends on:** TK-014, TK-015, TK-016
**Blocks:** TK-018
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-017-integration-security` — `skill: manage-git-flow`

### Phase 2: Scaffold
- [ ] **Scaffold `:integration:security`**
  - `integration/security/build.gradle.kts` — `dividox.kmp.library` + `dividox.kmp.test`
  - `include(":integration:security")` in `settings.gradle.kts`
  - **Verify:** `./gradlew :integration:security:compileKotlinJvm`
  - **Commit:** `DVX-TK-017 Scaffold integration:security module`

### Phase 3: Enriched Models + Use Cases (TDD)
- [ ] **Models:**
  - `SecurityHolding` — `Holding` + `StockQuote` + `DividendInfo` + `totalGainPercent: Double`
  - `PortfolioSummary` — `totalValue, totalGain, totalGainPercent, totalYield, dividendsCollected`
  - `EnrichedWatchlistEntry` — `WatchlistEntry` + `StockQuote` + `CompanyInfo` + `isInPortfolio: Boolean`
  - `SecurityDetail` — `ticker, quote, dividendInfo, company, priceHistory, isInPortfolio, isInWatchlist, holdingId?`
  - **Commit:** `DVX-TK-017 Add integration:security enriched models`

- [ ] **Use cases + tests:**
  - `GetPortfolioWithQuotesUseCase` — combines portfolio + multiple quotes + dividend info
  - `GetPortfolioSummaryUseCase` — aggregates `GetPortfolioWithQuotesUseCase`
  - `GetEnrichedWatchlistUseCase` — watchlist entries + quotes + company info + portfolio check
  - `GetSecurityDetailUseCase` — quote + dividend + company + price history + portfolio/watchlist flags
  - `GetSecurityHoldingUseCase` — single ticker: Holding + Quote + DividendInfo (for Edit Holding pre-fill)
  - **Verify:** `./gradlew :integration:security:jvmTest`
  - **Commit:** `DVX-TK-017 Add integration:security use cases with tests`

- [ ] **`SecurityIntegrationModule.kt`** + add to `App.kt` startKoin
  - **Commit:** `DVX-TK-017 Register security integration Koin module`

### Phase 4: Testing & Quality
- [ ] `./gradlew test` + `./gradlew detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 6 **Completed:** 0 **Remaining:** 6

---

## Notes
- `:integration:security` must not make its own Firestore or Ktor calls — orchestrates component use cases only
- `totalGainPercent = (currentValue - costBasis) / costBasis * 100`
- `holdingId` in `SecurityDetail` is non-null only when `isInPortfolio = true`
