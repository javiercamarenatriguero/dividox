# DiviDox Finance Design System

**Source:** Stitch project `10568397103146599411` · Asset `f6fd49b3589b42809306d7cdea72c58d`
**Last synced:** 2026-03-26

---

## Creative Philosophy: "The Financial Architect"

Rejects flat/boxy fintech aesthetics. Depth through **Tonal Layering** — no 1px borders, only background shifts.
Glassmorphism for floating elements (`backdrop-blur: 12px`, 80% opacity). Editorial typography rhythm.

---

## Color Palette

### Seed & Generation

| Property | Value |
|---|---|
| Seed color | `#1978e5` |
| Primary override | `#1978E5` |
| Color variant | `FIDELITY` |
| Color mode | `LIGHT` (dark derived) |

### Light Scheme

| Role | Hex | Usage |
|---|---|---|
| `primary` | `#005ab4` | Primary actions, FABs, key CTAs |
| `onPrimary` | `#ffffff` | Text/icons on primary |
| `primaryContainer` | `#0873df` | Gradient end for CTAs (jewel finish) |
| `onPrimaryContainer` | `#fefcff` | Text/icons on primaryContainer |
| `secondary` | `#465f89` | Secondary actions, chips |
| `onSecondary` | `#ffffff` | Text/icons on secondary |
| `secondaryContainer` | `#b7cfff` | Tonal containers for secondary |
| `onSecondaryContainer` | `#405882` | Text/icons on secondaryContainer |
| `tertiary` | `#964400` | Risk/Alert metrics, warning states |
| `onTertiary` | `#ffffff` | Text/icons on tertiary |
| `tertiaryContainer` | `#bd5700` | Alert/warning tonal containers |
| `onTertiaryContainer` | `#fffbff` | Text/icons on tertiaryContainer |
| `error` | `#ba1a1a` | Errors |
| `onError` | `#ffffff` | Text/icons on error |
| `errorContainer` | `#ffdad6` | Error tonal container |
| `onErrorContainer` | `#93000a` | Text/icons on errorContainer |
| `background` | `#f9f9ff` | Page/screen background (Base layer) |
| `onBackground` | `#181c22` | Text/icons on background |
| `surface` | `#f9f9ff` | Base surface layer |
| `onSurface` | `#181c22` | Text/icons on surface |
| `surfaceVariant` | `#e0e2ec` | Alternative surface |
| `onSurfaceVariant` | `#414753` | Micro-data labels, ticker symbols |
| `outline` | `#717785` | Inactive nav icons (never 1px dividers) |
| `outlineVariant` | `#c1c6d5` | Ghost borders at 15% opacity only |
| `scrim` | `#000000` | Modal overlays |
| `inverseSurface` | `#2d3038` | Tooltips, snackbars |
| `inverseOnSurface` | `#eff0fa` | Text on inverseSurface |
| `inversePrimary` | `#aac7ff` | Links on inverseSurface |
| `surfaceTint` | `#005db8` | Surface tinting |
| `surfaceBright` | `#f9f9ff` | Brightest surface |
| `surfaceDim` | `#d8dae3` | Dimmest surface |
| `surfaceContainer` | `#ecedf7` | Secondary content layer |
| `surfaceContainerLow` | `#f2f3fd` | Asset list "well" background |
| `surfaceContainerLowest` | `#ffffff` | Card background (pure white lift) |
| `surfaceContainerHigh` | `#e6e8f1` | Active/elevated elements, ticker icons |
| `surfaceContainerHighest` | `#e0e2ec` | Input field fill (ghost style) |

### Dark Scheme (derived)

| Role | Hex |
|---|---|
| `primary` | `#aac7ff` |
| `onPrimary` | `#001b3e` |
| `primaryContainer` | `#00458d` |
| `onPrimaryContainer` | `#d6e3ff` |
| `secondary` | `#afc7f7` |
| `onSecondary` | `#001b3e` |
| `secondaryContainer` | `#2e4770` |
| `onSecondaryContainer` | `#d6e3ff` |
| `tertiary` | `#ffb68c` |
| `onTertiary` | `#321200` |
| `tertiaryContainer` | `#763400` |
| `onTertiaryContainer` | `#ffdbc9` |
| `error` | `#ffb4ab` |
| `onError` | `#690005` |
| `errorContainer` | `#93000a` |
| `onErrorContainer` | `#ffdad6` |
| `background` | `#0f1117` |
| `onBackground` | `#e0e2ec` |
| `surface` | `#0f1117` |
| `onSurface` | `#e0e2ec` |
| `surfaceVariant` | `#414753` |
| `onSurfaceVariant` | `#c1c6d5` |
| `outline` | `#8b909e` |
| `outlineVariant` | `#414753` |
| `scrim` | `#000000` |
| `inverseSurface` | `#e0e2ec` |
| `inverseOnSurface` | `#2d3038` |
| `inversePrimary` | `#005ab4` |
| `surfaceTint` | `#aac7ff` |
| `surfaceBright` | `#343641` |
| `surfaceDim` | `#0f1117` |
| `surfaceContainer` | `#1c1f27` |
| `surfaceContainerLow` | `#181c22` |
| `surfaceContainerLowest` | `#0a0d14` |
| `surfaceContainerHigh` | `#272a32` |
| `surfaceContainerHighest` | `#31353d` |

---

## Typography

| Property | Value |
|---|---|
| Headline font | Inter |
| Body font | Inter |
| Label font | Inter |

**Editorial rules:**
- Display: portfolio totals — monumental scale
- Headline sm: section titles — pair with `spacing-12` top padding
- Label md/sm: micro-data (tickers, %) — use `onSurfaceVariant`
- Never two text elements of the same size adjacent

> Font files → `common/ui-resources/src/commonMain/composeResources/font/`

---

## Shape

| Tier | Corner radius | Used for |
|---|---|---|
| `extraSmall` | 4 dp | Chips, badges, small tooltips |
| `small` | 8 dp | Text fields, input ghost backgrounds |
| `medium` | 12 dp | Menus, snackbars |
| `large` | 24 dp | **Metric Cards** (signature xl rounding) |
| `extraLarge` | 28 dp | Bottom sheets, full-screen dialogs |

> **Buttons** use `CircleShape` (full pill) — M3 `FilledButton` defaults to `CornerFull`.
> **Roundness setting in Stitch:** `ROUND_EIGHT` — card xl is defined in `designMd`.

---

## Surface Hierarchy

```
Base (background #f9f9ff)
  └─ Secondary content (surfaceContainerLow #f2f3fd)
      └─ Cards (surfaceContainerLowest #ffffff)
          └─ Active elements (surfaceContainerHigh #e6e8f1)
```

**No-Line Rule:** No 1px solid borders. Use background tone shifts only.
Ghost borders: `outlineVariant` at 15% opacity maximum.

---

## Key Tokens Quick Reference

| Intent | Token | Value |
|---|---|---|
| Primary CTA | `primary` | `#005ab4` |
| CTA gradient end | `primaryContainer` | `#0873df` |
| Risk/Alert | `tertiary` | `#964400` |
| Screen background | `surface` | `#f9f9ff` |
| Card background | `surfaceContainerLowest` | `#ffffff` |
| Asset list well | `surfaceContainerLow` | `#f2f3fd` |
| Inactive icons | `outline` | `#717785` |
| Active icons | `primary` | `#005ab4` |
