# CODESTYLE

This document defines the coding standards for the UpChat project. The goal is to keep the codebase consistent, predictable, and easy to extend.

---

## General principles

- Prefer clarity over cleverness.
- Keep features isolated per module and per package.
- Do not leak backend SDKs outside their dedicated data-* modules.
- Avoid magic; prefer explicit wiring via DI.

---

## Languages and tooling

- Primary language: Kotlin.
- Build: Gradle with Kotlin DSL (`build.gradle.kts`).
- UI: Jetpack Compose.
- DI: Hilt.

---

## Project architecture rules

1. `app` module
   - Only composition, DI wiring, navigation.
   - No direct backend calls.
   - No business logic.

2. `ui` module
   - Only Compose UI and ViewModels.
   - Consumes domain via `AuthService`, `ChatService`, `ProfileService`, and other domain interfaces.
   - No backend SDK imports.

3. `core` module
   - Domain models and service interfaces only.
   - No Android SDK, no backend SDKs.

4. `common` module
   - Shared utilities (crypto, Result, logging, dispatchers, generic constants).
   - No backend SDKs.

5. `data-*` modules
   - Backend-specific implementations (Firebase, Appwrite, Supabase, etc.).
   - Implement only `*ServiceImpl` classes and data access.
   - All backend SDK usage and remote models live here.

---

## Naming conventions

### Modules

- `app` – main Android app.
- `ui` – UI and ViewModels.
- `core` – domain.
- `common` – shared utilities.
- `data-firebase`, `data-appwrite`, `data-supabase` – backend-specific modules.

### Packages

- Packages follow `com.upchat.<module>.<feature>` where sensible.
- Domain:
  - `com.upchat.core.domain.model`
  - `com.upchat.core.domain.auth`
  - `com.upchat.core.domain.chat`
  - `com.upchat.core.domain.profile`
- UI:
  - `com.upchat.ui.auth`
  - `com.upchat.ui.chat`
  - `com.upchat.ui.profile`
  - `com.upchat.ui.components`
- Backend modules (examples):
  - `com.upchat.data.firebase.remote`
  - `com.upchat.data.firebase.datasource`
  - `com.upchat.data.firebase.mapper`
  - `com.upchat.data.firebase.auth`

### Classes and interfaces

- Domain service interfaces: `XxxService` (e.g. `AuthService`, `ChatService`).
- Backend implementations: `XxxServiceImpl` (e.g. `AuthServiceImpl`, `ChatServiceImpl`).
- Remote backend models: `BackendEntityTypeModel` (e.g. `FirebaseUserModel`, `AppwriteChatModel`).
- Domain models: plain nouns (e.g. `User`, `Chat`, `Message`).
- ViewModels: `XxxViewModel` (e.g. `AuthViewModel`, `ChatViewModel`).
- Composables: `PascalCase`, usually suffixed with `Screen` or `Dialog` where appropriate.

### Files

- One top-level class or interface per file, named after the class.
- Group files by feature, not by layer (inside modules).

---

## Kotlin style

- Use `val` by default; use `var` only when necessary.
- Use expression bodies for simple functions.
- Use explicit types for public APIs (functions, properties, interfaces).
- Prefer data classes for models.
- Prefer sealed classes or enums for constrained sets of states.
- Use extension functions to keep mappers and minor utilities close to their types.

### Nullability

- Use non-null types by default.
- Use nullable types only when they represent legitimate absence.
- Avoid double-bang `!!`; wrap with safe calls and explicit error handling instead.

### Error handling

- Use `Result` type (from `common`) for service-level errors where appropriate.
- Log errors via `Logger` from `common` instead of ad-hoc `println` or manual logging.
- Do not swallow exceptions silently; either propagate via `Result` or log explicitly.

---

## Compose UI style

- Composable function names: `PascalCase` and descriptive.
- `@Composable` functions should be side-effect-free; side effects are handled via
  `LaunchedEffect`, `SideEffect`, etc.
- State hoisting: prefer passing state down and events up.
- Screen composables should accept state and event callbacks rather than owning logic directly.

Example pattern:

- `AuthViewModel` exposes `uiState: StateFlow<AuthUiState>` and functions like `onSignInRequested`.
- `AuthScreen` collects state from the ViewModel and renders UI.

---

## ViewModel style

- ViewModels live in `ui` module under their feature package.
- ViewModels depend on domain services (`AuthService`, `ChatService`, etc.).
- Use `viewModelScope` for coroutines.
- Expose state via `StateFlow` or `Immutable` state holders.
- Keep ViewModels free of direct backend SDK usage.

---

## Data and backend style

- All backend SDK calls must stay inside data-* modules.
- Use data sources (`XxxDataSource`) for low-level SDK interactions.
- Use mappers (`XxxMapper`) to convert between remote models and domain models.
- `*ServiceImpl` classes:
  - Implement the corresponding `XxxService` from `core`.
  - Coordinate data sources and mappers.
  - Expose only domain models and `Result` types to callers.

---

## Gradle and dependencies

- Each module has its own `build.gradle.kts`.
- Plugin versions are defined in root `build.gradle.kts` using `apply false`.
- Dependencies use inline versions to keep Android Studio suggestions functional.
- No external version catalogs for now.

---

## Tests

- Unit tests should target `core` and data-* modules primarily.
- Avoid flaky tests around real backend calls; use fakes/mocks.
- Test mapping logic (remote <-> domain) explicitly.

---

## Documentation

- Whenever a new feature is introduced:
  - Update `MODULES.md` if module responsibilities change.
  - Document backend-specific quirks in a backend-specific README under the corresponding data-* module if needed.

The objective is that any contributor can open the project, read this file and `MODULES.md`, and immediately understand where to put new code and how to keep the layering intact.