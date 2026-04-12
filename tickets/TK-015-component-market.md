# Task: TK-015 · component:market — Scaffold + Ktor + Domain + Data

## Description

Scaffold `:component:market`, add Ktor dependencies and configure `YahooFinanceClient`, define market domain models and `MarketRepository`, implement all use cases with TDD, then build the Yahoo Finance data layer with in-memory cache and register the Koin module.

**ADRs:** ADR-005, ADR-007
**Depends on:** TK-013
**Blocks:** TK-017
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-015-component-market` — `skill: manage-git-flow`

### Phase 2: Scaffold + Ktor Setup
- [ ] **Scaffold `:component:market`**
  - `component/market/build.gradle.kts` — `dividox.kmp.library` + `dividox.kmp.test`
  - `include(":component:market")` in `settings.gradle.kts`
  - **Commit:** `DVX-TK-015 Scaffold component:market module`

- [ ] **Add Ktor dependencies**
  - `ktor-client-core`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`
  - Engines: `ktor-client-okhttp` (Android/JVM), `ktor-client-darwin` (iOS)
  - **Commit:** `DVX-TK-015 Add Ktor dependencies to component:market`

- [ ] **Configure `YahooFinanceClient`**
  - `ContentNegotiation` (kotlinx.serialization), `HttpTimeout` 10s, `defaultRequest` base URL `https://query1.finance.yahoo.com`
  - Location: `component/market/src/commonMain/kotlin/.../data/api/YahooFinanceClient.kt`
  - **Verify:** `./gradlew :component:market:compileKotlinJvm`
  - **Commit:** `DVX-TK-015 Configure YahooFinanceClient`

### Phase 3: Domain Layer (TDD)
- [ ] **Domain models:** `StockQuote(ticker, price, change, changePercent, currency, lastUpdated)`, `DividendInfo(ticker, yield, annualPayout, payoutRatio, fiveYearGrowth, exDividendDate)`, `CompanyInfo(ticker, name, exchange, logoUrl)`, `ChartPeriod` enum (`ONE_DAY, ONE_WEEK, ONE_MONTH, YTD, ONE_YEAR, ALL`), `PricePoint(timestamp: Instant, close: Double)`
- [ ] **`MarketRepository` interface:** `getStockQuote`, `getMultipleQuotes`, `getDividendInfo`, `getCompanyInfo`, `getDividendHistory`, `getPriceHistory(ticker, period): Flow<List<PricePoint>>`
- [ ] **Use cases + tests:** `GetStockQuoteUseCase`, `GetMultipleQuotesUseCase`, `GetDividendInfoUseCase`, `GetCompanyInfoUseCase`, `GetDividendHistoryUseCase`, `GetPriceHistoryUseCase`, `SearchSecuritiesUseCase`
  - **Verify:** `./gradlew :component:market:jvmTest`
  - **Commit:** `DVX-TK-015 Add market domain layer with tests`

### Phase 4: Data Layer
- [ ] **Internal DTOs** (all `@Serializable`, all `internal`) for Yahoo Finance JSON responses
- [ ] **`YahooFinanceApi`** — Ktor calls for quote, batch quote, dividend summary, dividend history, price chart, search
- [ ] **`MarketRepositoryImpl`** — in-memory cache: quotes 60s TTL, dividend info 1h TTL (TDD, mock API)
  - **Verify:** `./gradlew :component:market:jvmTest`
- [ ] **`MarketModule.kt`** + add to `App.kt` startKoin
  - **Commit:** `DVX-TK-015 Implement market data layer with Yahoo Finance and Koin module`

### Phase 5: Testing & Quality
- [ ] `./gradlew test` + `./gradlew detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 7 **Completed:** 0 **Remaining:** 7

---

## Notes
- All DTOs are `internal` — domain models never expose Yahoo Finance field names
- "Prices delayed 15 minutes" disclaimer is a UI requirement on every market-data screen (ADR-007)
- `SearchSecuritiesUseCase` is included here for reuse by Portfolio and Search features
