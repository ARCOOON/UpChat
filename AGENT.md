# Agent Specification for UpChat

## Overview
This agent acts as an automated development assistant for the UpChat project. It understands the project architecture, coding standards, backend abstraction model, Gradle multi-module structure, and repository layout. The agent supports OpenAI Codex-style instructions and other LLM-based coding workflows.

## Responsibilities
- Generate Kotlin, Jetpack Compose, Hilt, Firebase, Appwrite, or Supabase code.
- Follow the UpChat modular architecture:
  - `app`
  - `ui`
  - `core`
  - `common`
  - `data-*` backend modules
- Always provide complete implementations, no placeholders.
- Maintain backend-agnostic domain logic.
- Keep UI layer pure and free of data logic.
- Bind concrete implementations via Hilt only in `BackendModule`.

## Coding Rules
- Do not leak backend SDKs outside `data-*` modules.
- Always use `XxxService` (domain) and `XxxServiceImpl` (backend-specific).
- Mapping between backend models and domain models must occur in the data module.
- Compose UI must consume state via ViewModels only.
- All functions must be fully implemented.
- Use inline dependency versions (no catalogs).
- Avoid DTO naming; use `remote` instead.

## File Generation Rules
When generating new screens or features:
1. Create a UI screen and ViewModel under `ui/`.
2. Add domain service methods under `core/`.
3. Implement the service in the selected backend module (`data-firebase`, etc.).
4. Add Hilt bindings in `app/di/BackendModule.kt`.
5. Add navigation route in `app/navigation/AppNavHost.kt`.

## Backend Switching
The agent must respect the backend-pluggable design:
- Only one backend module should be active.
- Bindings determine which implementation is used.
- Domain and UI code must never reference a backend directly.

## Output Format
- Always output full files or modules when asked.
- No partial snippets unless explicitly requested.
- No assumptions; follow the UpChat structure exactly.

## Compliance
The agent must not change project architecture unless instructed. All generated code must compile and fit into the multi-module project structure as defined.
