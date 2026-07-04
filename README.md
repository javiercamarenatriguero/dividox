# DiviDox

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-1.11.0-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform">
  <img src="https://img.shields.io/badge/KMP-Android%20%7C%20iOS%20%7C%20Desktop-0095D5?logo=kotlin&logoColor=white" alt="Kotlin Multiplatform">
  <img src="https://img.shields.io/badge/Android-API%2031%2B-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/iOS-16%2B-000000?logo=apple&logoColor=white" alt="iOS">
  <img src="https://img.shields.io/badge/Material%20Design-3-757575?logo=materialdesign&logoColor=white" alt="Material Design 3">
  <img src="https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28?logo=firebase&logoColor=black" alt="Firebase">
</p>

<p align="center">
  <img src="docs/images/img_dividox_banner.png" alt="DiviDox Banner" width="100%">
</p>

DiviDox is a mobile application designed for investors focused on **Dividend Growth Investing (DGI)** and passive income. Keep an exhaustive track of your assets' cash flow.

<p align="center">
  <img src="docs/images/img_dividox_features.png" alt="DiviDox Features Showcase" width="100%">
</p>

DiviDox is a **dividend-focused stock portfolio tracker** for investors who want to understand and grow their passive income from equities. Built with Kotlin Multiplatform, it runs natively on Android, iOS, and Desktop (JVM) from a single shared codebase.

## What DiviDox does

### Portfolio management
View your holdings at a glance — current value, daily change, and total gain/loss since purchase. Add or remove positions manually, including purchase price, number of shares, currency, and date.

### Dividend analysis
Go beyond simple yield numbers. DiviDox surfaces dividend history, upcoming ex-dividend and payment dates, annual income projections per holding, and portfolio-wide dividend totals — giving dividend investors the detail they actually need.

### Watchlist
Follow tickers you don't own yet. Track price and dividend data for stocks you're evaluating before adding them to your portfolio.

### Account management
Manage your profile, base currency, and app preferences. Authentication via Google, Apple, or email/password through Firebase.

### Key Features

* **Market API:** International ticker integration (ANA.MC, CABK.MC, GOOG, HSY).
* **Multiplatform:** Available in iOS, Android, MacOS/Windows/Linux.
* **Dual Interface:** Full native support for Dark Mode and Light Mode.
* **Multiple Currencies:** View your total value and invested capital adapted to your local currency (GBP, EUR, USD).
* **Key Metrics:** Instantly check your updated *Dividend Yield* and total accumulated dividends.
* **Payment Calendar:** Stay on top of your *Upcoming Payments* with precise Ex-dates.

<p align="center">
  <img src="docs/images/img_app.gif" alt="DiviDox App Demo" width="280">
</p>

## Tech stack

Kotlin Multiplatform · Compose Multiplatform · Firebase Auth · Firestore · Yahoo Finance API · Material Design 3 · Koin · Clean Architecture

---

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

## Architecture

The project follows a layered modular architecture with strict dependency rules — each layer can only depend on layers below it.


<p align="center">
  <img src="docs/images/img_modular_architecture.png" alt="DiviDox Modular Architecture" width="100%">
</p>

### Modules

| Layer | Description |
|-------|-------------|
| **`app`** | Top-level orchestration layer. Wires navigation and dependency injection for the entire application. |
| **`feature`** | Presentation layer. Each module owns one or more screens and may consume domain logic from multiple `component` modules. |
| **`component`** | Domain and data layers. Each module encapsulates business rules, repository contracts, and their implementations. |
| **`integration`** | Bridge layer that prevents horizontal dependencies between modules in the same layer. |
| **`common`** | Shared utilities and UI resources available to any `component` or `feature` module. |

### Rules

- `app` depends on everything.
- `feature` modules are isolated — they do not depend on each other.
- `component` modules can be used by multiple `feature` modules.
- `common` modules have no internal dependencies.
- All modules target Android, iOS, and Desktop via Kotlin Multiplatform.

---

## AI-Assisted Development

DiviDox supports both **Claude Code** and **GitHub Copilot** as AI development environments. All AI configuration (agents, skills, security instructions) lives in a shared `.ai-context/` directory, with symlinks into `.claude/` and `.github/` so both tools consume the same source of truth.

The primary environment is **Claude Code**, powered by **Claude Opus 4** with a 1M-token context window. The setup spans agents, skills, MCP integrations, hooks, and plugins — orchestrating the full product lifecycle from requirements to code review.

### Spec-Driven Development

The entire development process follows a **Spec-Driven Development** approach, where every feature flows through a structured pipeline defined by agents and skills:

1. **Product Requirements** — The PO agent generates PRDs and product descriptions (`generate-prd`, `product-description`)
2. **User Stories** — Requirements are refined into INVEST-compliant stories with BDD acceptance criteria (`user-story-writer`, `story-map-generator`)
3. **Sprint Tickets** — Stories are decomposed into atomic, estimable tasks (`ticket-writer`, `estimate-effort`, `task-planner`)
4. **Implementation** — The Developer agent implements each ticket following the spec: domain → data → UI → navigation → tests (`implement-domain`, `implement-ui`, `implement-di`, `implement-navigation`)
5. **Review & Verification** — The Code Reviewer agent validates architecture, performance, and security compliance against the original spec (`audit-compose-performance`, `owasp-security-review`)

No code is written without a traceable spec. Each ticket links back to its user story, and each story links back to the product requirement — ensuring alignment from vision to implementation.

### Supported AI Tools

| Tool | Config directory | Mechanism |
|------|-----------------|-----------|
| **Claude Code** | `.claude/` → symlinks to `.ai-context/` | Agents, skills, hooks, MCP servers, `CLAUDE.md` |
| **GitHub Copilot** | `.github/` → symlinks to `.ai-context/` | `copilot-instructions.md`, reusable prompts, agents, skills |

### Model

| Model | Context | Use |
|-------|---------|-----|
| Claude Opus 4 | 1M tokens | Primary model for all agents and orchestration |

### Custom Agents

Three role-based agents collaborate through a defined feature workflow: **PO → Developer → Code Reviewer**.

| Agent | Role | Responsibilities |
|-------|------|------------------|
| **PO** | Product Owner | User stories, story maps, sprint tickets, effort estimation |
| **Developer** | KMP Engineer | Domain logic, Compose UI, DI, navigation, tests — full feature lifecycle |
| **Code Reviewer** | Senior Architect | PR reviews, architecture compliance, Compose performance, security audits |

Agent definitions live in `.ai-context/agents/` and orchestration rules in `AGENTS.md`.

### Skills (35+)

Reusable, locally bundled workflows under `.ai-context/skills/` that encode domain-specific processes.

| Category | Skills |
|----------|--------|
| **Product & Requirements** | `write-meta-prompt`, `generate-prd`, `product-description`, `product-roadmap`, `user-story-writer`, `story-map-generator`, `ticket-writer`, `estimate-effort`, `task-planner` |
| **Architecture & Design** | `generate-adr`, `design-c4`, `design-data-model`, `design-md`, `design-system`, `stitch-design`, `module-organization` |
| **Implementation** | `implement-domain`, `implement-ui`, `implement-di`, `implement-navigation`, `write-unit-test`, `audit-compose-performance` |
| **Security (OWASP MASVS)** | `owasp-security-review`, `masvs-checklist`, `masvs-auth-assessment`, `masvs-secure-storage-audit`, `masvs-crypto-review`, `masvs-network-security-check`, `masvs-platform-interaction-review`, `masvs-code-quality-scan`, `masvs-privacy-audit`, `masvs-resilience-assessment`, `masvs-mobile-threat-model` |
| **Operations** | `manage-git-flow`, `skill-creator`, `full-doc` |

### MCP Servers

| Server | Type | Purpose |
|--------|------|---------|
| **Stitch** (Google) | HTTP | UI design prototyping — screen generation and design system sync |
| **lean-ctx** | Local | Context runtime — cached file reads, shell compression, code search, 10 read modes. Reduces token usage by up to 99% on re-reads |

### Plugins

| Plugin | Purpose |
|--------|---------|
| **lean-ctx** | Context engineering layer — replaces native Read/Shell/Grep with token-optimized equivalents |
| **Caveman** | Communication mode that reduces output tokens ~75% while preserving technical substance |

### Hooks

| Hook | Trigger | Action |
|------|---------|--------|
| Pre-commit check | `PreToolUse` on Bash | Runs Detekt static analysis + JVM tests before commits |

### Security Framework

All security tooling follows **OWASP MASVS v2** mapped to NowSecure Tier 2 (fintech). Security instructions in `.ai-context/security-instructions.md` define mandatory skill triggers per code area (auth, storage, networking, crypto, platform, privacy). The `owasp-security-review` skill serves as a quick PR gate; deeper `masvs-*` skills provide full audit reports.

---

## How to test the app

DiviDox is available on **Android**, **iOS**, and **macOS/Desktop**. Android and Desktop builds are produced automatically by the **On Distribute** CI workflow. iOS requires building locally with Xcode.

### Step 1 — Download the build

1. Go to the **Actions** tab in this repository
2. Click **On Distribute** in the left sidebar
3. Click the latest successful run
4. Scroll to the bottom — **Artifacts** section
5. Download the artifact for your platform

---

### Android

**What you need:** an Android phone (Android 11 / API 31 or newer).

#### Option A — Firebase App Distribution (recommended)

The easiest way. Builds are pushed automatically on every release.

1. You need to be added to the **internal-testers** group — ask a maintainer to add your email in Firebase Console
2. You will receive an email from Firebase with a download link
3. Follow the link, install the Firebase App Tester app if prompted, and download the build directly from there

#### Option B — GitHub Actions artifact

1. Go to the **Actions** tab → **On Distribute** → latest run → **Artifacts** → download `composeApp-release.apk` *(expires after 90 days)*
2. Transfer it to your phone (Google Drive, USB cable, etc.)
3. Open the file on your phone
4. If prompted: **Settings → Install unknown apps** → allow your file manager or browser
5. Tap **Install**

---

### Desktop

**What you need:** nothing — just your computer.

1. Download the artifact for your OS *(expires after 90 days)*:
   - **macOS** → `.dmg`
   - **Windows** → `.msi` or `.exe`
   - **Linux** → `.deb` or `.AppImage`
2. Open the downloaded file and follow the installer
3. Launch **DiviDox** from your Applications folder / Start menu

---

### iOS

> iOS deployment is **not included** in the CI pipeline. The only way to run DiviDox on iOS is by building it locally with Xcode.

**What you need:** a Mac with [Xcode](https://developer.apple.com/xcode/) installed (free from the App Store).

1. Clone the repository
2. Open the `iosApp/iosApp.xcodeproj` project in Xcode
3. Select an iOS Simulator or a connected device as the run destination
4. Press **⌘R** to build and run

---

## How to build from source

See the [Development Setup](#) section below *(coming soon)*.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)