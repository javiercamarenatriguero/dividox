# Task: TK-013 · feature:auth — Login + Sign Up + Forgot Password MVI + Auth Navigation

## Description

Implement the three auth screens (Login, Sign Up, Forgot Password) as full MVI flows in `:feature:auth`, then wire the complete `authGraph` in `RootNavGraph` with all navigation callbacks.

**User Stories:** DVX-US-001 · DVX-US-002 · DVX-US-003 · DVX-US-004
**PRD:** PRD-01
**ADRs:** ADR-010, ADR-011
**Stitch Design:** https://stitch.withgoogle.com/projects/10568397103146599411
**Depends on:** TK-012
**Blocks:** TK-014
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-013-feature-auth` — `skill: manage-git-flow`

### Phase 2: Login Screen MVI
- [ ] **`LoginContract`** — State: `email, password, isPasswordVisible, isLoading, error` · Event: `EmailChanged, PasswordChanged, VisibilityToggled, SignInClicked, GoogleSignInClicked, ForgotPasswordClicked, SignUpClicked` · Effect: `NavigateToDashboard, NavigateToForgotPassword, NavigateToSignUp`
- [ ] **`LoginViewModel`** + unit tests — `SignInWithEmailUseCase`, `SignInWithGoogleUseCase`; clear error on field change; disable CTA while loading or fields empty
  - **Verify:** `./gradlew :feature:auth:jvmTest`
- [ ] **`LoginScreen`** — logo + tagline, email/password fields, eye toggle, "Sign In" primary CTA, "Continue with Google" button (hidden on Desktop), "Forgot Password?" + "Sign Up" links, red error banner
  - **Commit:** `DVX-TK-013 Add Login screen MVI`

### Phase 3: Sign Up Screen MVI
- [ ] **`SignUpContract`** — State: `name, email, password, isPasswordVisible, isTermsChecked, isLoading, error` · Event/Effect as needed
- [ ] **`SignUpViewModel`** + unit tests — `SignUpWithEmailUseCase`; validate on submit (email on blur); disable CTA until all valid + terms checked
  - **Verify:** `./gradlew :feature:auth:jvmTest`
- [ ] **`SignUpScreen`** — X close, "Create Account" title, Full Name/Email/Password fields, ToS checkbox, "Create Account" CTA, "Already have an account? Sign In" link
  - **Commit:** `DVX-TK-013 Add Sign Up screen MVI`

### Phase 4: Forgot Password Screen MVI
- [ ] **`ForgotPasswordContract`** + **`ForgotPasswordViewModel`** + unit tests — `ForgotPasswordUseCase`; always show confirmation regardless of email existence
  - **Verify:** `./gradlew :feature:auth:jvmTest`
- [ ] **`ForgotPasswordScreen`** — back arrow, email input, "Send Reset Link" CTA, confirmation message
  - **Commit:** `DVX-TK-013 Add Forgot Password screen MVI`

### Phase 5: Wire authGraph
- [ ] **Implement `authGraph {}` extension** in `RootNavGraph`
  - `LoginRoute` → `SignUpRoute` / `ForgotPasswordRoute`
  - Login/SignUp success → `DashboardRoute` with `popUpTo(0) { inclusive = true }`
  - ForgotPassword back → `LoginRoute`
  - **Verify:** `./gradlew :composeApp:assembleDebug`
  - **Commit:** `DVX-TK-013 Wire complete authGraph`

### Phase 6: Testing & Quality
- [ ] `./gradlew test` + `./gradlew detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 7 **Completed:** 0 **Remaining:** 7

---

## Notes
- Desktop: hide "Continue with Google" button (`expect/actual` or platform check)
- Confirmation message for Forgot Password: "If an account exists, a reset link has been sent." — never distinguish existing vs non-existing email
