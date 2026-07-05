# LifeLens — Design Build Plan

Build the UI to the v1.0 Design Spec (`LifeLen-Design-Spec.md`) — a **dark-only, camera-first
"instrument"** aesthetic — on top of the existing multi-module data layer (Qwen-VL client,
repositories, Room, DataStore, `CategoryHandler`). Source of truth: the spec + the 5 HTML mockups
(S02/S04/S05/S06/S08) + `LifeLen-screens.pdf`.

## Screens (S01–S10)

| ID | Screen | Type | Notes |
|----|--------|------|-------|
| S01 | Permission prime | Full screen | Bracket illustration, "Enable camera" / "Not now" |
| S02 | Camera home | Viewfinder | Flash/settings, DetectionBrackets + LabelChip, ModeStrip (Auto/Electronics/Food/Text), bottom chrome: gallery · shutter · library-thumb(badge) |
| S03 | Processing | Frozen frame + sheet | Dim capture 55%, ResultSheet→half with Skeleton, identity→stats→modules resolve in order |
| S04 | Result · electronics | Bottom sheet (half 60%) | IdentityHeader → StatRow(4) → PriceBlock → Save+Share → SourceFootnote |
| S05 | Prices | Pushed screen | SummaryStrip(Lowest/Avg/Sellers) → condition chips → PriceRow list (best-price badge) → renewed group → footnote |
| S06 | Result · food | Bottom sheet (half) | IdentityHeader(Photo estimate) → PortionRow(stepper) → calorie hero → MacroBar → NutritionRows → Save+Adjust items |
| S07 | Low confidence | Sheet variant | Neutral badge "~52%", "Looks like…", AlternativesRow, mode shortcut chips |
| S08 | Library | Pushed screen | NavBar+count, SearchBar, filter chips, day-grouped LibraryRow (price/kcal/care + TrendPill, swipe pin/delete), FloatingScanButton |
| S09 | Saved item detail | Sheet full-depth | Reopen over saved photo, refresh + "Scanned {date}", TrendPill vs scan-day |
| S10 | Errors & empty | States | No-match peek sheet, offline banner, empty library |

## Architecture decisions

- **Keep** the data layer: `:core:network` (Qwen), `:core:search`, `:core:database`, `:core:datastore`,
  `:core:data` (`ScanRepository`, `HistoryRepository`, `SettingsRepository`, `CategoryHandler` registry).
- **Rebuild** `:core:designsystem` as the LifeLens dark design system: exact color tokens (§2.1),
  9 typography styles (§2.2, mono for all measured values), shapes, and the full component library (§3).
- **Navigation**: single-Activity Compose. Camera is `startDestination`. Result is a **bottom sheet**
  hosted over the camera (not a route) using `ModalBottomSheet` with custom snap detents; Prices, Library,
  Saved-detail, Permission are routes. Back returns to camera in ≤2 gestures (spec §5 flow map).
- **State**: MVI-ish — each screen a `@HiltViewModel` exposing `StateFlow<UiState>`; `collectAsStateWithLifecycle`.
- **Adaptive result system** (§4): `CategoryHandler` already routes by `ScanCategory`; UI renders a
  category-driven **module stack** (StatRow + half/full modules) from one `ResultSheet`. Adding a category =
  add a handler + a module-config entry, no new screen.
- **Fonts**: system sans (Inter stand-in) + `FontFamily.Monospace` for all `data-*` styles (the mono
  readout is core to the aesthetic). Space Grotesk / JetBrains Mono can be dropped into `res/font` later;
  typography is centralized so it's a one-file swap. *(Decision: offline-safe, no downloadable-font runtime dep.)*

## Data-model extensions (`:core:model` + parser/prompts)

- `Identification.attributes` stays an ordered map (drives StatRow + SpecTable).
- `NutritionInfo`: add `fiber`, `sugars`, `sodium`, `percentDailyValue`.
- `BuyOption`: add `condition` (New/Renewed/Used) + `meta` (shipping/stock string); `PriceInfo`: add `average`, `sellerCount`, `source`, `fetchedAt`.
- `Scan`: add `previousLowPrice` (trend on refresh), `source`, `fetchedAt`.
- Update Qwen prompts/DTOs/`AnalysisParser` and `PricingSynthesizer` to populate them.

## Build sequence

1. **Design system** — tokens, typography, shapes, `LifeLensTheme` (dark), icon set (bundle spec SVGs → vectors), primitives (Button, Chip, IconButton, Stepper, SearchBar), brand (DetectionBrackets, ShutterButton, ThumbBracket).
2. **Model + data** extensions + prompts/parser/handlers.
3. **Camera (S02)** + permission (S01) + capture/processing (S03).
4. **Result sheet (S04/S06/S07)** with category module stacks + Save/Share.
5. **Prices (S05)**, **Library (S08)**, **Saved detail (S09)**.
6. **States (S10)** — skeletons, empty, offline, no-match; Toast.
7. **Navigation + DI wiring**; build green.
8. **Tests** — unit (parser, pricing, handlers, VMs w/ Turbine + fakes), edge cases (offline, empty, low-confidence, invalid JSON), Compose UI tests for key flows.

## Verification

- `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` green.
- Manual/instrumented smoke of the core loop: camera → capture → result sheet → save → library → prices.
- Edge cases exercised in unit tests: empty library, offline last-fetched, <70% confidence alternatives, malformed model JSON, portion recompute.
