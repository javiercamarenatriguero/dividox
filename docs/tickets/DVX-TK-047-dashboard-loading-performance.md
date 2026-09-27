# DVX-TK-047 — Dashboard Loading Performance

**Type:** Technical Improvement
**Layer:** Feature / Dashboard + Integration
**Points:** 8
**Priority:** High
**Branch:** `feature/DVX-TK-047-dashboard-loading-performance`

## Context

Current Dashboard cold-start blocks the entire screen behind a single global
`CircularProgressIndicator` until portfolio quotes + summary + period gain all arrive.
Market indices, news and watchlist are loaded in parallel but hidden behind the same
loader, so the user perceives a slow start even when secondary data is already ready.

There is also no cache-first hydration: every field is network-bound. Empty portfolios
never emit through `portfolioShared.filter { it.isNotEmpty() }`, leaving the loader on
forever for new users. Heavy background work (`syncDividendHistory`) runs on the critical
path.

Goal: make the Dashboard paint progressively (each section as it arrives), hydrate from
cache first, and parallelize/warm-up as much as possible so the perceived load time drops
dramatically.

## Scope

### 1. Progressive rendering (highest impact / lowest effort)

- [ ] Remove the full-screen loader in `DashboardScreen.kt`.
- [ ] Add per-section skeletons (portfolio hero card, period detail, portfolio-today,
      favourites, market indices, news).
- [ ] Split `isLoading` into per-block flags in `DashboardViewState`:
      `isSummaryLoading`, `isPeriodGainLoading`, `isPortfolioTodayLoading`.
      Keep `marketIndicesLoading`, `marketNewsLoading` (already present).

### 2. Empty-portfolio bug

- [ ] Drop `.filter { it.isNotEmpty() }` on `portfolioShared` in
      `DashboardViewModel.observeData()`. Emit `emptyList()` and render the empty state.

### 3. Move sync off critical path

- [ ] `syncDividendHistory` currently fires on every portfolio emission from `init`.
      Move it to a low-priority job (e.g. `viewModelScope.launch { delay(...) ... }`
      or trigger from a background coroutine) so it does not compete with first paint.

### 4. Share flows

- [ ] `observeAppSettings()` is collected 4+ times. Convert to
      `.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings.default())`
      and reuse the shared state.

### 5. Cache-first (stale-while-revalidate)

- [ ] Persist last known `PortfolioSummary`, quotes, market indices and news to
      Room/DataStore.
- [ ] Repository flows emit the cached snapshot first, then trigger network refresh
      that overwrites. `LastUpdatedBar` already surfaces staleness.
- [ ] Applies to: `GetPortfolioWithQuotesUseCase`, `GetMajorMarketIndicesUseCase`,
      `GetMarketNewsUseCase`, `GetEnrichedWatchlistUseCase`.

### 6. Preload on splash / post-login

- [ ] After successful auth, kick off warm-up in an Application-scoped coroutine:
      `getMultipleQuotes(portfolio ∪ watchlist tickers)`, `getMajorMarketIndices`,
      `getMarketNews`. Results land in cache so the Dashboard hydrates instantly.

### 7. Dedup ticker calls

- [ ] `GetPortfolioWithQuotesUseCase` and `GetEnrichedWatchlistUseCase` each call
      `GetMultipleQuotesUseCase` separately. Merge into a single batched request
      (union of tickers) and split the response, or share a per-session quote cache
      inside the market repository.

### 8. Selective refresh

- [ ] Pull-to-refresh currently cancels and rebuilds every flow via `observeData()` +
      `observeMarketIndices()`. Change to per-source invalidation (repositories
      re-fetch, flows stay alive). Optional: independent refresh entry points per
      section.

### 9. Micro / polish

- [ ] Move currency conversion out of the big `combine` in the VM to the UI layer
      (`remember(state.summary, currency)`), so a currency change does not rebuild
      the whole state.
- [ ] Add `.flowOn(Dispatchers.Default)` to mapping/sorting hot paths inside the VM.
- [ ] Consider `SupervisorJob` for the children launched inside `observeData()` so
      one failure does not tear the whole chain down.
- [ ] Split dividend enrichment (`Phase 2` in `GetPortfolioWithQuotesUseCase`) into
      its own flow so it does not rebuild the whole holding list just to add yield
      fields.

## Acceptance Criteria

- Cold start on a warm cache paints skeletons in < 200 ms and real values in
  < 1 s for the sections whose data is cached.
- Cold start on empty cache shows skeletons per section immediately and each section
  fills in as its data lands, with no full-screen blocker.
- Empty portfolio no longer keeps the loader spinning — empty state is rendered.
- `syncDividendHistory` never blocks the first paint (verified by log/trace).
- Currency change does not trigger a full state rebuild; UI updates in < 1 frame.
- `GetMultipleQuotesUseCase` is called at most once per (portfolio ∪ watchlist)
  ticker set per refresh cycle.

## Order of Attack (suggested PR slicing)

1. **PR 1** — sections 1, 2, 3, 4 (visual + bug + flow sharing). Small, high impact.
2. **PR 2** — sections 5, 6 (cache-first + splash preload). Room/DataStore additions.
3. **PR 3** — sections 7, 8, 9 (dedup, selective refresh, polish).

## Files Involved

- `feature/dashboard/src/commonMain/kotlin/com/akole/dividox/feature/dashboard/DashboardViewModel.kt`
- `feature/dashboard/src/commonMain/kotlin/com/akole/dividox/feature/dashboard/DashboardContract.kt`
- `feature/dashboard/src/commonMain/kotlin/com/akole/dividox/feature/dashboard/DashboardScreen.kt`
- `integration/security/src/commonMain/kotlin/com/akole/dividox/integration/security/domain/usecase/GetPortfolioWithQuotesUseCase.kt`
- `integration/security/src/commonMain/kotlin/com/akole/dividox/integration/security/domain/usecase/GetEnrichedWatchlistUseCase.kt`
- `component/market/src/commonMain/kotlin/com/akole/dividox/component/market/domain/usecase/GetMajorMarketIndicesUseCase.kt`
- `component/market/src/commonMain/kotlin/com/akole/dividox/component/market/domain/usecase/GetMarketNewsUseCase.kt`
- Repository / local data source layers under `component/market` and `integration/security`
  for cache-first work.

## Notes / Non-goals

- No visual redesign of Dashboard cards. Skeletons should match existing card shapes.
- No API contract change. All work is client-side.
- Security review: no new persistence of PII. If summary/quotes are cached, follow the
  existing local-persistence patterns (Room, no plaintext secrets).
