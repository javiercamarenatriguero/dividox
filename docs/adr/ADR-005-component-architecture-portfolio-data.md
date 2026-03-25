# ADR-005: Component Architecture for Portfolio Data

## Status
Proposed

## Context
DiviDox needs to manage several bounded contexts around the user's investment portfolio. Following the multi-module architecture defined in the project (`:component` for domain+data, `:feature` for screens), we need to decide how to split the data concerns.

## Decision
Create four independent `:component` modules, each owning a single bounded context:

### `:component:portfolio`
Static data about the user's holdings, persisted in Firestore.

```
domain/model/
    Holding.kt         # tickerId, shares, purchasePrice, purchaseCurrency, purchaseDate
    HoldingId.kt       # value class wrapping String
domain/repository/
    PortfolioRepository.kt
domain/usecase/
    GetPortfolioUseCase.kt
    AddHoldingUseCase.kt
    RemoveHoldingUseCase.kt
    UpdateHoldingUseCase.kt
data/
    datasource/PortfolioDataSource.kt   # Firestore
    repository/PortfolioRepositoryImpl.kt
```

> No live prices, no market data. Pure static user input.

---

### `:component:watchlist`
User's list of followed tickers (not necessarily owned), persisted in Firestore.

```
domain/model/
    WatchlistEntry.kt   # tickerId, addedAt
domain/repository/
    WatchlistRepository.kt
domain/usecase/
    GetWatchlistUseCase.kt
    AddToWatchlistUseCase.kt
    RemoveFromWatchlistUseCase.kt
    IsInWatchlistUseCase.kt
data/
    datasource/WatchlistDataSource.kt   # Firestore
    repository/WatchlistRepositoryImpl.kt
```

---

### `:component:market`
Live market data from Yahoo Finance (prices, dividends, company info). No persistence — data always fetched from the API.

```
domain/model/
    StockQuote.kt          # tickerId, currentPrice, currency, change, changePercent
    DividendInfo.kt        # yield, frequency, nextExDate, nextPayDate, annualAmount
    CompanyInfo.kt         # name, sector, exchange, logoUrl, description
domain/repository/
    MarketRepository.kt
domain/usecase/
    GetStockQuoteUseCase.kt
    GetMultipleQuotesUseCase.kt   # batch fetch for entire portfolio
    GetDividendInfoUseCase.kt
    GetCompanyInfoUseCase.kt
    GetDividendHistoryUseCase.kt
data/
    datasource/MarketDataSource.kt         # Ktor HTTP client
    repository/MarketRepositoryImpl.kt
    api/YahooFinanceApi.kt
```

---

### `:component:settings`
User preferences, persisted in Firestore.

```
domain/model/
    UserSettings.kt   # baseCurrency, theme, notificationsEnabled
domain/repository/
    SettingsRepository.kt
domain/usecase/
    GetSettingsUseCase.kt
    UpdateSettingsUseCase.kt
data/
    datasource/SettingsDataSource.kt   # Firestore
    repository/SettingsRepositoryImpl.kt
```

## Dependency Rules
```
:component:portfolio  → :common/* only
:component:watchlist  → :common/* only
:component:market     → :common/* only
:component:settings   → :common/* only
```

No component imports another component — cross-component combinations go in `:integration`.

## Consequences
- **Positive**: Each component is independently testable; teams can work in parallel; clear ownership per bounded context
- **Negative**: Firestore reads from portfolio, watchlist, and settings are separate — no single query; handled by `:integration` layer
- **Rule**: `portfolio` never holds current price. `market` never holds user-specific data.

## Related
- [ADR-006](ADR-006-integration-security.md): `:integration:security` — combining portfolio + market
- [ADR-007](ADR-007-yahoo-finance-market-data.md): Yahoo Finance as market data provider
