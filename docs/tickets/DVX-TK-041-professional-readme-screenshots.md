# Task: DVX-TK-041 — Professional README with Multi-Platform Screenshots

## Description

Enhance `README.md` to showcase Dividox as a professional KMP product with high-quality
screenshots across all three supported platforms: **Android**, **iOS**, and **Desktop (JVM)**.

**Key Requirements:**
- Hero section with a composite image of all 3 platforms
- Feature highlights with per-feature screenshots
- Platform matrix with build/run instructions
- Badges: build status, Kotlin version, Compose Multiplatform version, license
- Short architecture note (KMP + Compose Multiplatform)
- All images hosted in `docs/screenshots/` (no external hosts)
- README renders correctly on GitHub

**Status:** In Progress
**Branch:** `feature/DVX-TK-041-professional-readme-screenshots`
**GitHub Issue:** https://github.com/javiercamarenatriguero/dividox/issues/78

---

### Phase 1: Screenshot Capture

- [ ] **Run app on Android emulator and capture screenshots**
  - Screens: Dashboard, Market Indices, News Feed, Onboarding Carousel, Security watchlist
  - Output: `docs/screenshots/android/`
  - **Commit:** `DVX-TK-041 Add Android screenshots`

- [ ] **Run app on iOS Simulator and capture screenshots**
  - Same screens as Android
  - Output: `docs/screenshots/ios/`
  - **Commit:** `DVX-TK-041 Add iOS screenshots`

- [ ] **Run app on Desktop (JVM) and capture screenshots**
  - Command: `./gradlew :composeApp:run`
  - Same screens as Android
  - Output: `docs/screenshots/desktop/`
  - **Commit:** `DVX-TK-041 Add Desktop screenshots`

---

### Phase 2: Hero Image Composition

- [ ] **Compose multi-platform hero image**
  - Combine Android phone frame + iOS phone frame + Desktop window frame side-by-side
  - Tools: Figma / Sketch / GIMP / Canva — whichever is available
  - Output: `docs/screenshots/hero.png`
  - Recommended size: 1600×900px, transparent or dark background
  - **Commit:** `DVX-TK-041 Add hero composite image`

---

### Phase 3: README Rewrite

- [ ] **Rewrite README structure**

  Target structure:
  ```
  1. Hero image
  2. Badges (build · Kotlin · Compose MP · license)
  3. About (1-paragraph app description)
  4. Platforms (Android · iOS · Desktop matrix)
  5. Features (table or sections with screenshot per feature)
  6. Getting Started (build & run per platform)
  7. Architecture (KMP + Compose MP note, link to CLAUDE.md)
  8. Contributing
  9. License
  ```

  - All strings must match actual app content
  - Link to `CLAUDE.md` for contributor architecture details
  - **Commit:** `DVX-TK-041 Rewrite README with structure and content`

- [ ] **Embed screenshots into README**
  - Use relative paths: `docs/screenshots/android/dashboard.png` etc.
  - Hero image as first element after title
  - Feature screenshots as HTML `<img>` tags with `width="300"` for consistent sizing
  - **Commit:** `DVX-TK-041 Embed screenshots in README`

---

### Phase 4: Badges & CI

- [ ] **Add badges**
  - GitHub Actions build status badge (from `.github/workflows/` workflow file)
  - Kotlin version badge (shields.io static)
  - Compose Multiplatform version badge (shields.io static)
  - License badge
  - **Commit:** `DVX-TK-041 Add badges to README`

---

### Phase 5: Verification

- [ ] **Verify rendering on GitHub**
  - Push branch and open PR
  - Check all images load correctly in the GitHub preview
  - Check all links resolve (no 404s)
  - Check badges render

- [ ] **Create Pull Request**
  - Title: `DVX-TK-041 · Docs — Professional README with multi-platform screenshots`
  - Skill: `skill: manage-git-flow`

---

## Progress Tracking

**Total Tasks:** 10
**Completed:** 0
**Remaining:** 10

## Notes

- Images go in `docs/screenshots/{android,ios,desktop}/` — keep filenames lowercase with hyphens
- Hero image is the most impactful element — invest time here
- No external image hosting (Imgur, CDN, etc.) — all assets must live in the repo
- Platform matrix should clearly show the same Compose code runs on all 3 targets
