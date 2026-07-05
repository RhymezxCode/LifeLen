# Contributing to LifeLens

Thanks for helping build **LifeLens**. This guide covers the module boundaries you must respect, coding standards, and the PR workflow. Keeping to these rules is what keeps the multi-module build fast and the codebase easy to reason about.

> New here? Start with [README.md](README.md), then [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Module Boundaries & Dependency Rules

LifeLens is a Now-in-Android–style multi-module project. The dependency graph is the single most important thing to respect.

**Golden rules:**

1. **Features never depend on other features.** `:feature:scanner`, `:feature:results`, `:feature:history`, and `:feature:settings` are siblings. If two features need to share something, it belongs in a `:core` module.
2. **Features depend only on** `:core:data`, `:core:model`, `:core:designsystem`, and `:core:common`.
3. **`:app` depends on all features** and wires them together. Nothing depends on `:app`.
4. **`:core:data` hides all data sources.** UI and ViewModels talk to repository interfaces only — never to `:core:network`, `:core:database`, `:core:datastore`, or `:core:search` directly.
5. **`:core:model` has no Android dependencies.** It is a pure Kotlin/JVM module and must stay that way.
6. **Data sources depend down, not sideways.** `:core:network`, `:core:search`, `:core:database`, and `:core:datastore` depend on `:core:model` (and `:core:common`), not on each other.

If a change requires breaking one of these rules, that's a design discussion — open an issue first.

---

## Coding Standards

- **Kotlin official code style.** Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). Keep `kotlin.code.style=official`.
- **ktlint intent.** Format consistently: 4-space indent, trailing commas where they help diffs, one top-level declaration per file where reasonable, imports ordered and unused imports removed.
- **Compose best practices.**
  - Composables are stateless where possible; hoist state to the ViewModel.
  - No business logic, network calls, or DB access inside Composables.
  - Use stable keys and content types for dynamic `LazyColumn`/`LazyRow` items.
  - Add `contentDescription` / semantics for meaningful controls; keep touch targets ≥ 48dp.
- **ViewModels.** Use `@HiltViewModel` with constructor injection. Expose state as a single `StateFlow<UiState>`. Collect in the UI with `collectAsStateWithLifecycle`.
- **No business logic in Activities, Fragments, or Composables.** Logic lives in ViewModels, use cases, and repositories.
- **State that must survive config changes** goes in `rememberSaveable` (transient UI) or the ViewModel/DataStore (durable).
- **Threading.** Never hard-code `Dispatchers.IO`; inject dispatchers via the qualifiers in `:core:common`.
- **Never hard-code secrets.** API keys come from `BuildConfig` (via `secrets.properties`) or DataStore. See [docs/API-KEYS.md](docs/API-KEYS.md).
- **Validate input at trust boundaries** — anything crossing into network or storage.

---

## Adding a New Feature Module

1. Create `feature/<name>/` with its own `build.gradle.kts`.
2. Apply the feature convention plugin — it wires Compose, Hilt, and the common feature dependencies for you:

   ```kotlin
   plugins {
       id("lifelen.android.feature")
       id("lifelen.android.library.compose")
   }
   ```

3. Add the module to `settings.gradle.kts` (`include(":feature:<name>")`).
4. Depend only on `:core:data`, `:core:model`, `:core:designsystem`, `:core:common` (the feature plugin supplies the standard set).
5. Expose a per-feature navigation graph and wire it into the nav host in `:app`.
6. Follow the `Screen` + `ViewModel` + `UiState` pattern used by existing features.

---

## Adding a New Object-Type Handler

LifeLens is designed so new scan categories are additive. To add one (e.g. clothing, wine, art):

1. Add or reuse a value in the `ScanCategory` enum in `:core:model`.
2. Implement `CategoryHandler` in `:core:data` (e.g. `WineHandler`) — it owns that category's enrichment logic (extra search queries, prompt tweaks, mapping to domain fields).
3. Bind it into the handler registry via Hilt (`@IntoSet` / multibinding) so the router picks it up by `ScanCategory`.
4. Add unit tests for the handler.
5. If it needs bespoke UI, extend the results screen presentation — no new feature module required for most cases.

The full checklist is in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [TECHNICAL.md](TECHNICAL.md).

---

## Convention Plugins

Shared Gradle config lives in `build-logic:convention`. Prefer applying an existing plugin over copy-pasting build config:

| Plugin ID | Use for |
|---|---|
| `lifelen.android.application` | The `:app` module. |
| `lifelen.android.application.compose` | Compose config for `:app`. |
| `lifelen.android.library` | Android library `:core` modules. |
| `lifelen.android.library.compose` | Compose-enabled library modules. |
| `lifelen.android.feature` | Feature modules (bundles library + Hilt + common feature deps). |
| `lifelen.android.hilt` | Hilt setup. |
| `lifelen.android.room` | Room setup. |
| `lifelen.jvm.library` | Pure Kotlin/JVM modules (e.g. `:core:model`). |

---

## Running Tests

```bash
# Unit tests (all modules)
./gradlew test

# A single module's unit tests
./gradlew :core:data:test

# Instrumented / Compose UI tests (device or emulator required)
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lint
```

Add focused tests for changed behavior, including edge cases and failure paths. Repository tests use **MockWebServer** + **Turbine**; ViewModel tests assert `StateFlow` emissions; UI tests use Compose UI test.

---

## Branch & PR Conventions

- **Branch naming:** `feature/<short-desc>`, `fix/<short-desc>`, `docs/<short-desc>`, `chore/<short-desc>`.
- **One logical change per PR.** Keep PRs small and reviewable.
- **PR description** should state what changed, why, which modules are touched, and how it was tested. Include screenshots for UI changes.
- **Link the issue** the PR resolves.
- **Green build required:** `./gradlew test lint` must pass before review.
- Respect the dependency rules above — a PR that breaks module boundaries will be sent back.

---

## Commit Message Style

Use **Conventional Commits**:

```
<type>(<scope>): <summary>

<optional body explaining what and why>
```

- **Types:** `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`, `perf`.
- **Scope** is usually the module (e.g. `scanner`, `core-data`, `network`).
- Keep the summary imperative and under ~72 characters.

Examples:

```
feat(scanner): add capture button and image downscaling
fix(core-network): handle 401 from DashScope with typed error
docs(readme): correct product name to LifeLens
test(core-data): cover ScanRepository price-grounding path
```

---

Thanks for contributing to LifeLens — *visual intelligence for everyday life*.
