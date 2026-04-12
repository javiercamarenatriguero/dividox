# DiviDox — User Stories

DiviDox is a Kotlin Multiplatform (KMP) app built with Compose Multiplatform, targeting **Android**, **iOS**, and **Desktop (JVM)**. It is a personal finance app focused on stock portfolio tracking with a dividend-first approach. Users can monitor their holdings, track dividend income, analyse individual securities, and discover new dividend-growth opportunities.

> All user stories apply to all three platforms unless explicitly noted otherwise.

---

## Authentication

### DVX-US-001 · Sign In with Email & Password
**As a** returning user,
**I want to** sign in with my email and password,
**so that** I can access my portfolio securely from any device.

**Acceptance Criteria:**
- Email field validates format on blur; invalid format shows an inline error.
- Password field is masked by default; tapping the eye icon toggles visibility.
- Sign In button is disabled while either field is empty.
- On success, app navigates to Dashboard and clears the back stack.
- On failure, an error banner reads "Incorrect email or password. Please try again."
- Error banner clears automatically when the user edits any field.

---

### DVX-US-002 · Sign In with Google
**As a** returning user,
**I want to** sign in with my Google account,
**so that** I can log in without remembering a password.

**Acceptance Criteria:**
- "Continue with Google" button launches the native Google OAuth flow.
- On success, app navigates to Dashboard and clears the back stack.
- On failure, an inline error message is shown below the button.

---

### DVX-US-003 · Create an Account
**As a** new user,
**I want to** create an account with my name, email, and password,
**so that** I can start tracking my portfolio.

**Acceptance Criteria:**
- Full Name, Email, and Password fields are all required.
- Email validates format on blur; shows "An account with this email already exists." if already registered.
- Password field has a visibility toggle (lock icon).
- "I agree to the Terms of Service and Privacy Policy" checkbox must be checked to enable the CTA.
- Create Account button is disabled until all fields are valid and the checkbox is checked.
- On success, app navigates to Dashboard with back stack cleared (no email verification in v1).
- On failure, error banner reads "Something went wrong. Please try again."
- "Already have an account? Sign In" link navigates back to Login.

---

### DVX-US-004 · Recover Password
**As a** user who forgot their password,
**I want to** request a password reset link,
**so that** I can regain access to my account.

**Acceptance Criteria:**
- Accessible via "Forgot Password" link on the Login screen.
- User enters their registered email and taps "Send Reset Link."
- Confirmation message is shown regardless of whether the email exists (security best practice).

---

## Dashboard

### DVX-US-005 · View Portfolio Overview
**As a** user,
**I want to** see a summary of my portfolio's total value, profit, yield, and dividends collected,
**so that** I can quickly assess the health of my investments.

**Acceptance Criteria:**
- Displays Total Value, Profit (%), Yield (%), and Dividends collected.
- A period selector `[ 1D | 1W | 1M | 1Y | YTD | ALL ]` unifies the calculation period for all four metrics.
- Total Value shows absolute variation and % change for the selected period.

---

### DVX-US-006 · Switch Display Currency
**As a** user,
**I want to** toggle between USD and EUR,
**so that** I can view my portfolio in my preferred currency.

**Acceptance Criteria:**
- A USD / EUR toggle is always visible in the Dashboard header.
- Switching currency instantly recalculates all monetary values at the current spot rate.
- The selected currency persists across sessions (linked to Default Currency in Settings).

---

### DVX-US-007 · Monitor Today's Gainers and Losers
**As a** user,
**I want to** see which of my holdings gained and which lost value today,
**so that** I can quickly identify positions driving my portfolio.

**Acceptance Criteria:**
- Shows Top 3 Gainers and Top 3 Losers from the user's own holdings only.
- Each entry shows: Ticker | Price | % Change.
- Gainers displayed in green, losers in red.

---

### DVX-US-008 · View Upcoming Dividend Events
**As a** user,
**I want to** see upcoming dividend payments and ex-dividend dates for my holdings,
**so that** I can plan my cash flow accordingly.

**Acceptance Criteria:**
- Feed lists upcoming dividend credits and ex-dividend date notices.
- Dividend variation notices show % change in green (increase) or red (cut) vs. the previous payment.
- Events are limited to securities in the user's portfolio.

---

### DVX-US-009 · Manage Watchlist from Dashboard
**As a** user,
**I want to** see my favourite securities and remove them directly from the Dashboard,
**so that** I can monitor candidates without leaving the main screen.

**Acceptance Criteria:**
- Watchlist section shows up to 2 entries with Ticker | Market Price | % Change Today.
- "VIEW ALL" link navigates to the full Favorites screen.
- Tapping the heart icon removes the asset from the Watchlist immediately.

---

### DVX-US-010 · Discover Dividend Growth Securities
**As a** user,
**I want to** browse a curated list of high-dividend-growth securities not yet in my portfolio,
**so that** I can discover new investment opportunities.

**Acceptance Criteria:**
- "Market Intelligence" carousel shows securities with dividend CAGR > 10% over 5 years.
- Each card shows: Ticker | Current Yield | 5Y CAGR.
- Tapping "+" navigates to the Add New Holding screen pre-filled with that ticker.
- "VIEW ALL" link navigates to a full discovery list.

---

## My Holdings

### DVX-US-011 · View All Holdings
**As a** user,
**I want to** see a list of all my stock positions,
**so that** I can review my current portfolio composition.

**Acceptance Criteria:**
- Each card shows: Ticker | Company name | Shares | Market Price | Dividend % | Total Value | Gain/Loss badge.
- Gain badge is green for positive, red for negative, relative to cost basis.
- Empty state shows: "No holdings yet. Tap + to add your first one."

---

### DVX-US-012 · Search Holdings
**As a** user,
**I want to** search my holdings by ticker or company name,
**so that** I can find a specific position quickly in a large portfolio.

**Acceptance Criteria:**
- Full-width search input at the top of the screen.
- List filters in real time as the user types.
- Shows empty state if no match is found.

---

### DVX-US-013 · Sort Holdings
**As a** user,
**I want to** sort my holdings by Gain, Max Yield %, or Date Added,
**so that** I can view my portfolio from different analytical angles.

**Acceptance Criteria:**
- Three sort chips: `[ Gain | Max Yield % | Date Added ]`.
- Chips are mutually exclusive.
- Tapping the active chip toggles between ascending and descending order.
- List re-sorts instantly without a page reload.

---

### DVX-US-014 · Add a New Holding
**As a** user,
**I want to** add a new stock position to my portfolio,
**so that** I can start tracking its performance and dividends.

**Acceptance Criteria:**
- Accessible via the "+" FAB on the Holdings screen.
- Smart search auto-suggests securities by Ticker or Company Name.
- User inputs Shares (supports fractions) and Price per Share (cost basis).
- Currency defaults to the user's Default Currency setting; can be changed inline.
- Live "Estimated Total" updates as the user types.
- Dividend Yield of the selected security is shown as context.
- Tapping "+ Add to Portfolio" saves the position with haptic feedback and a success state.

---

### DVX-US-015 · Edit an Existing Holding
**As a** user,
**I want to** edit the details of an existing position,
**so that** I can correct a mistake or record an additional purchase.

**Acceptance Criteria:**
- Accessible via the pencil icon on each holding card.
- Opens the Edit Holding screen pre-filled with existing values.
- User can update Shares, Price per Share, and Currency.
- Changes are saved on confirmation and reflected immediately in the list.

---

## Dividend Activity

### DVX-US-016 · View Lifetime Dividend Summary
**As a** user,
**I want to** see my total lifetime dividends, YTD dividends, and YoY growth,
**so that** I can measure the income my portfolio is generating over time.

**Acceptance Criteria:**
- Displays: Lifetime Dividends | YTD Dividends | YoY Performance | Next Payout | Portfolio YoC.
- Next Payout shows amount, date, and days remaining.
- YoC indicator turns green when at or above target (5.0%), red when below.

---

### DVX-US-017 · Visualise Dividend Projection
**As a** user,
**I want to** see a monthly bar chart of past and projected dividends,
**so that** I can anticipate my future income.

**Acceptance Criteria:**
- Bar chart covers the last 12 months.
- Past months show actual received amounts (filled bars).
- Future months show projected amounts (outlined or muted bars).
- X-axis labels one bar per month.

---

### DVX-US-018 · View Upcoming Dividend Payments
**As a** user,
**I want to** see a list of confirmed upcoming dividend payments,
**so that** I can plan cash withdrawals or reinvestments.

**Acceptance Criteria:**
- Each row shows: Company logo | Ticker | Company name | Amount per share | Total payout | Status badge.
- Status badge is "Confirmed" (green) if ex-dividend date has passed, "Estimated" (gray) if not.

---

### DVX-US-019 · Review Past Dividend Activity
**As a** user,
**I want to** browse my historical dividend receipts grouped by month,
**so that** I can audit my income and verify reinvestment records.

**Acceptance Criteria:**
- Entries grouped by calendar month; each group is collapsible.
- Most recent month is expanded by default; all others collapsed.
- Each entry shows: Ticker | Company name | Date | Method (Cash / Reinvested) | Amount.
- "Method: Reinvested" entries are visually distinct (different icon or muted colour).

---

## Security Analysis

### DVX-US-020 · Analyse a Security
**As a** user,
**I want to** view detailed price, dividend, and fundamental data for a specific stock,
**so that** I can make an informed decision before adding it to my portfolio.

**Acceptance Criteria:**
- Displays: Company name | Exchange | Current Price | Price Change (% and direction icon).
- Price chart with period selector `[ 1D | 1W | 1M | YTD | 1Y | ALL ]`.
- Dividend Metrics grid: Dividend Yield | Annual Payout | Payout Ratio | 5Y Growth Rate.
- Dividend Growth bar chart for the last 10 years.
- Fundamentals: Market Cap | P/E Ratio | Ex-Dividend Date.
- "Refreshed X minutes ago" timestamp; updates on pull-to-refresh.

---

### DVX-US-021 · Add a Security to Portfolio from Analysis
**As a** user,
**I want to** add a stock directly from its analysis screen,
**so that** I can act on research without navigating away.

**Acceptance Criteria:**
- "Add Security" CTA is shown when the asset is not in the portfolio.
- Tapping it opens the Add New Holding sheet pre-filled with the ticker.
- If the asset is already in the portfolio, CTA changes to "Edit Holding."

---

### DVX-US-022 · Toggle Favourite from Security Screen
**As a** user,
**I want to** add or remove a security from my Watchlist directly from the analysis screen,
**so that** I can track interesting stocks without adding them to my portfolio.

**Acceptance Criteria:**
- Heart icon in the header: solid (filled) when favourited, outlined when not.
- Tapping toggles the state immediately with visual feedback.
- Change is reflected in the Favorites screen.

---

## Favorites (Watchlist)

### DVX-US-023 · View Watchlist
**As a** user,
**I want to** see all my favourited securities in one place,
**so that** I can monitor candidates for future investment.

**Acceptance Criteria:**
- Each card shows: Company logo | Ticker | Company name | Spot Price | Daily Change (% + icon + colour).
- A disclaimer "Prices are delayed by 15 minutes" is shown at the bottom.

---

### DVX-US-024 · Search Within Watchlist
**As a** user,
**I want to** search my favourites by ticker or company name,
**so that** I can quickly find a specific entry in a long watchlist.

**Acceptance Criteria:**
- Search bar filters the list in real time.
- Shows empty state if no match is found.

---

### DVX-US-025 · Remove a Favourite
**As a** user,
**I want to** remove a security from my Watchlist,
**so that** I can keep it relevant and uncluttered.

**Acceptance Criteria:**
- Tapping the solid heart icon on a card removes the entry from the list immediately.
- The removal is also reflected on the Dashboard Watchlist widget and the Security Analysis screen.

---

## Search

### DVX-US-026 · Search for a Security
**As a** user,
**I want to** search for any listed stock by ticker or company name,
**so that** I can find securities to analyse or add to my portfolio.

**Acceptance Criteria:**
- Full-text search filters results in real time.
- Each result card shows: Company logo | Ticker | Company name | Spot Price | Daily Change.
- Tapping a result navigates to the Security Analysis screen.
- Disclaimer "Prices are delayed by 15 minutes" shown at the bottom.
- Heart icon on each card toggles favourite status inline.

---

## Profile & Settings

### DVX-US-027 · Enable Biometric Lock
**As a** user,
**I want to** protect the app with Face ID or fingerprint,
**so that** my financial data is secure even if my phone is unlocked.

**Acceptance Criteria:**
- Toggle in Settings; enabled by default.
- Takes effect immediately without requiring a save action.
- On next app launch, biometric prompt is shown before granting access.

---

### DVX-US-028 · Configure Notifications
**As a** user,
**I want to** choose which events trigger push notifications,
**so that** I only receive alerts that are relevant to me.

**Acceptance Criteria:**
- Accessible via "Notifications" row in Settings.
- Options include: dividend payment credits, price alerts, upcoming ex-dividend dates.
- Changes saved immediately.

---

### DVX-US-029 · Set Default Currency
**As a** user,
**I want to** set a default display currency (USD or EUR),
**so that** all monetary values across the app use my preferred currency by default.

**Acceptance Criteria:**
- Inline `[ USD | EUR ]` toggle in Settings.
- Switches instantly and recalculates all values in real time across the app.

---

### DVX-US-030 · Export Portfolio
**As a** user,
**I want to** export my portfolio data as a file,
**so that** I can analyse it in external tools or keep a personal record.

**Acceptance Criteria:**
- "Export Portfolio" button triggers file generation (CSV or PDF).
- A loading indicator is shown while the file is being created.
- Native share sheet appears on completion.

---

### DVX-US-031 · Delete Account
**As a** user,
**I want to** permanently delete my account and all associated data,
**so that** I can exercise my right to be forgotten.

**Acceptance Criteria:**
- Requires a confirmation dialog: "Are you sure? This action is permanent and cannot be undone."
- On confirmation, all user data is deleted and the app returns to the Login screen.
- Action requires the user to be authenticated (not just relying on an active session).

---

### DVX-US-032 · Sign Out
**As a** user,
**I want to** sign out of the app,
**so that** I can secure my account when using a shared device.

**Acceptance Criteria:**
- Requires a confirmation dialog before executing.
- On confirmation, session is cleared and the app returns to the Login screen with back stack cleared.
