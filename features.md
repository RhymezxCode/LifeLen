# LifeLens — Feature Catalog

> **Point your camera at anything, know everything.**

This is the full feature catalog for **LifeLens**, grouped into **Core (MVP)** features that ship for the Qwen Code Hackathon and **Stretch** features that build on the same foundation. Each major feature lists a short description, a **User workflow** (the steps the user takes), and a **Technical requirements** note (the modules and APIs involved).

See the delivery scope in **[plan.md](plan.md)** and the implementation detail in **[TECHNICAL.md](TECHNICAL.md)**.

---

## Core (MVP) Features

These are the features that make LifeLens demoable and complete for the hackathon.

### 1. Point-and-scan identification
Point the camera at any object and get an identification with a confidence-appropriate answer. This is the heart of the app.

**User workflow**
1. Open LifeLens; the scanner (camera preview) is the home screen.
2. Frame the object in view.
3. Tap the capture button.
4. See a brief loading state while the image is analyzed.
5. Land on a results screen showing what it is.

**Technical requirements**
- `:feature:scanner` — CameraX preview + `ImageCapture`.
- `:core:network` — Qwen-VL call via DashScope (`qwen-vl-max` / `qwen2.5-vl-72b-instruct`).
- `:core:data` — `ScanRepository.identify(...)` orchestrates capture → analyze → persist.
- `:core:model` — returns a structured `Identification` (`name`, `ScanCategory`, `attributes`).

### 2. Category routing (food vs. product vs. general)
One Qwen call decides what kind of thing it's looking at and shapes the output accordingly, then a `CategoryHandler` enriches it.

**User workflow**
1. Scan any object — no mode-switching required.
2. LifeLens automatically shows the right layout: nutrition panel for food, spec sheet + price for products, knowledge card for everything else.

**Technical requirements**
- Structured-output system prompt in `:core:network` that returns `category`.
- `CategoryHandler` strategy/registry in `:core:data` (`FoodHandler`, `ElectronicsHandler`, `BookHandler`, `ClothingHandler`, `GenericHandler`) selected by `ScanCategory`.
- `:core:model` — `ScanCategory` enum.

### 3. Product spec sheet
For products (electronics, appliances, gadgets), show a clean, structured spec sheet.

**User workflow**
1. Scan a product (e.g. a laptop).
2. View its identified model.
3. Read the spec sheet (key attributes) on the results screen.

**Technical requirements**
- `ElectronicsHandler` in `:core:data` maps Qwen attributes into spec fields.
- `:core:model` — `Identification.attributes`.
- `:feature:results` — spec sheet presentation.

### 4. Live price range + cheapest source (search grounding)
After identifying a product, LifeLens runs a live web/shopping search and has Qwen synthesize a current price range plus the cheapest places to buy, each with a link.

**User workflow**
1. Scan a product.
2. LifeLens fetches live listings in the background.
3. View a price range and a ranked list of buy options with prices and links.
4. Tap a buy link to open the listing.

**Technical requirements**
- `:core:search` — pluggable `SearchClient` (Serper default; Tavily/SerpAPI/Bing alternatives).
- `:core:network` — second Qwen call synthesizes `PriceInfo` from listings.
- `:core:data` — `ElectronicsHandler` builds the query from the `Identification` and orchestrates search → synthesis.
- `:core:model` — `PriceInfo`, `BuyOption`.
- `SEARCH_API_KEY` (see [docs/API-KEYS.md](docs/API-KEYS.md)).

### 5. Food & nutrition
Recognize a dish and its ingredients and return calories with a protein/carbs/fat breakdown and a portion estimate.

**User workflow**
1. Scan a plate of food.
2. View the identified dish and main ingredients.
3. Read calories, macros, and the estimated portion.

**Technical requirements**
- `FoodHandler` in `:core:data`.
- `:core:model` — `NutritionInfo` (calories, protein, carbs, fat, portion).
- `:feature:results` — nutrition panel.

### 6. Document / text extraction (OCR)
Extract readable text from documents, labels, and signs using Qwen-VL's built-in reading ability.

**User workflow**
1. Scan a document or sign.
2. View the extracted text.
3. Copy or share it.

**Technical requirements**
- `:core:network` — Qwen-VL OCR via the same multimodal call.
- `GenericHandler` in `:core:data` for the general/text path.
- `:feature:results` — selectable/copyable text.

### 7. Searchable scan history
Every scan is saved locally and searchable, with its image, identification, and price/nutrition data.

**User workflow**
1. Open the History tab.
2. Scroll saved scans (image thumbnail + name + timestamp).
3. Type in the search box to filter.
4. Tap a scan to re-open its full result.

**Technical requirements**
- `:core:database` — Room entities + DAOs, `Flow`-based queries.
- `:core:data` — `HistoryRepository`.
- `:feature:history` — `HistoryScreen`, `HistoryViewModel`.
- Images stored as file paths (not blobs); see [TECHNICAL.md](TECHNICAL.md).

### 8. Favorite, re-open, share, delete scans
Manage saved scans.

**User workflow**
1. In History (or on a result), tap the star to favorite.
2. Tap a scan to re-open it.
3. Tap share to send a scan card / summary.
4. Swipe or tap delete to remove a scan.

**Technical requirements**
- `:core:database` — update/delete DAO methods, `isFavorite` field.
- `:core:data` — `HistoryRepository` mutations.
- `:feature:history` / `:feature:results` — actions and Android share intent.

### 9. API keys & settings screen
Enter API keys and preferences in-app; keys are stored securely and never hard-coded.

**User workflow**
1. Open Settings.
2. Paste the DashScope key and (optionally) the search key.
3. Toggle preferences (e.g. enable live pricing, theme).
4. Save; keys persist across launches.

**Technical requirements**
- `:core:datastore` — DataStore for settings + secure key storage.
- `:feature:settings` — `SettingsScreen`, `SettingsViewModel`.
- Keys also readable from `BuildConfig` (see [docs/API-KEYS.md](docs/API-KEYS.md)).

### 10. Robust states: loading, empty, error, offline
Every screen handles the unhappy paths gracefully.

**User workflow**
1. On slow networks, see a clear loading indicator.
2. On failure, see a friendly error with a retry action.
3. Offline, still browse cached history and the last result.

**Technical requirements**
- `:core:common` — `Result` wrapper + typed error model.
- ViewModels expose sealed `UiState` (Loading/Success/Empty/Error).
- `:core:designsystem` — shared `LoadingIndicator`, error, and empty components.

---

## MVP Scope for the Hackathon

The minimum that must work end-to-end for a compelling demo:

- **Scan → identify** a general object (Qwen-VL happy path). *(Features 1, 2)*
- **Product path:** spec sheet + **live grounded price** with buy links. *(Features 3, 4)*
- **Food path:** dish + **calories & macros**. *(Feature 5)*
- **Persist** every scan and browse a **searchable history**; favorite/re-open/share/delete. *(Features 7, 8)*
- **Results detail** screen for each scan. *(part of 1–5)*
- **Settings** for API keys via DataStore. *(Feature 9)*
- **Solid states:** loading / empty / error / offline last-result fallback. *(Feature 10)*
- **Document text extraction** as a lightweight bonus within the same call. *(Feature 6)*

Anything below is **Stretch** — nice to have, built on the same architecture, pursued only after the MVP is green.

---

## Stretch Features

Grouped by theme. Each is intentionally additive — it reuses the existing modules and the `CategoryHandler` registry.

### Recognition & scanning

#### Multi-object detection in one frame
Detect and label several objects in a single photo and let the user pick which to drill into.

**User workflow**
1. Scan a scene with multiple items.
2. See tappable labels/boxes over each detected object.
3. Tap one to get its full result.

**Technical requirements** — richer prompt returning a list in `:core:network`; multi-result UI in `:feature:results`; owns no new module.

#### Barcode / QR quick-scan
Fast path for barcodes and QR codes for exact product lookups.

**User workflow**
1. Point at a barcode/QR.
2. LifeLens recognizes it instantly and jumps to the product/link.

**Technical requirements** — CameraX `ImageAnalysis` + ML Kit barcode (or on-device) in `:feature:scanner`; routes into `ScanRepository`.

#### Live translation of signs & menus (OCR + translate)
Extract text and translate it inline.

**User workflow**
1. Scan a foreign sign or menu.
2. Read the original text.
3. Toggle to the translated text.

**Technical requirements** — Qwen-VL OCR + Qwen translation in `:core:network`; `GenericHandler` extension; language preference in `:core:datastore`.

### Shopping & products

#### Price-drop watchlist + alerts
Watch a scanned product and get notified when the price drops.

**User workflow**
1. On a product result, tap "Watch price".
2. Set a target price.
3. Receive a notification when a listing drops below it.

**Technical requirements** — WorkManager periodic job re-running `SearchClient`; new watchlist table in `:core:database`; notifications from `:app`.

#### Compare two products
Scan two items and see a side-by-side comparison.

**User workflow**
1. Scan product A, then product B.
2. Open compare view.
3. Read a spec + price comparison table.

**Technical requirements** — compare screen in `:feature:results`; Qwen synthesis of a diff in `:core:data`.

#### Shopping cart / wishlist export
Collect products and export the list.

**User workflow**
1. Add products to a wishlist.
2. Export as text/CSV or share.

**Technical requirements** — wishlist entity in `:core:database`; export in `:feature:history`.

### Food & health

#### Meal / nutrition daily log & goals
Aggregate food scans into a daily log with calorie/macro goals.

**User workflow**
1. Scan meals through the day.
2. Open the daily log.
3. See totals vs. your goal and progress rings.

**Technical requirements** — aggregate queries in `:core:database`; goals in `:core:datastore`; a nutrition dashboard (new lightweight screen or a `:feature:history` tab).

### Interaction & follow-ups

#### Voice / text follow-up questions about a scan
Ask natural-language questions about the scanned item ("is this good for a vegan?", "does it support Wi‑Fi 6?").

**User workflow**
1. On a result, tap "Ask".
2. Speak or type a question.
3. Read Qwen's grounded answer.

**Technical requirements** — chat-style follow-up call in `:core:network` seeded with the `Identification` context; optional speech-to-text; conversation UI in `:feature:results`.

### Reliability & sharing

#### Offline last-result cache
Always show the most recent result and full history when offline.

**User workflow**
1. Lose connectivity.
2. Still open history and the last scan.

**Technical requirements** — Room as source of truth in `:core:database`; cache policy in `:core:data`. *(A basic form of this is in the MVP as the offline fallback.)*

#### Shareable scan cards
Generate a polished image card for a scan to share to social/chat.

**User workflow**
1. Tap share on a result.
2. Choose "Share as card".
3. Send the rendered image.

**Technical requirements** — Compose-to-bitmap rendering in `:core:designsystem`; share intent in the feature.

### Discovery & organization

#### AR overlay labels
Render labels over the live camera feed instead of a captured still.

**Technical requirements** — continuous `ImageAnalysis` + overlay in `:feature:scanner`; throttled Qwen calls in `:core:data`.

#### Collections / folders
Organize saved scans into user-named collections.

**User workflow**
1. Long-press scans to select.
2. Add them to a collection.
3. Browse by collection.

**Technical requirements** — collection tables in `:core:database`; grouping UI in `:feature:history`.

### Platform & accessibility

#### Dark mode
Full Material 3 dark theme.

**Technical requirements** — dynamic + dark color schemes in `:core:designsystem`; theme preference in `:core:datastore`.

#### Accessibility / TalkBack
Content descriptions, semantics, focus order, and ≥48dp targets throughout.

**Technical requirements** — semantics across all `:feature:*` screens and `:core:designsystem` components.

### Widgets (home screen) — shipped

Five Jetpack Glance widgets bring LifeLens to the home screen. *(Android removed third-party
lock-screen widgets on phones in API 21, so these are home-screen App Widgets — kept compact enough
to suit lock contexts on launchers/OEMs that still allow it.)*

| Widget | Shows | Tap |
|---|---|---|
| **Quick Scan** | Amber scan button | Opens the app to the camera |
| **Last Scan** | Most recent scan — title + price / kcal | Opens the app |
| **Library Stats** | Total scans + today's count | Opens the app |
| **Daily Calories** | Sum of today's food-scan calories + recent meals | Opens the app |
| **Price Watch** | Latest priced item + price-trend delta | Opens the app |

**User workflow**
1. Long-press the home screen → **Widgets** → **LifeLens**.
2. Drop a widget (e.g. Daily Calories) onto the home screen and resize it.
3. Widgets refresh periodically and reflect your latest library.
4. Tap any widget to jump into the app.

**Technical requirements**
- `:feature:widget` — one `GlanceAppWidget` + `GlanceAppWidgetReceiver` per widget, an
  `appwidget-provider` XML descriptor, and manifest `<receiver>`s merged into `:app`.
- Reads the library via a Hilt `@EntryPoint` (`WidgetEntryPoint` → `HistoryRepository.observeHistory()`),
  wrapped in `runCatching` so any failure degrades to a safe empty state.
- Taps open `MainActivity` via `actionStartActivity(ComponentName(...))` — no compile-time dependency on `:app`.
- Previews are standard Compose `@Preview` approximations (a Glance composable can't render on the ordinary Compose preview host).

---

## Feature-to-Module Ownership (quick reference)

| Feature | Primary module(s) |
|---|---|
| Point-and-scan identification | `:feature:scanner`, `:core:network`, `:core:data` |
| Category routing + handlers | `:core:data` (`CategoryHandler` registry) |
| Product spec sheet | `ElectronicsHandler` (`:core:data`), `:feature:results` |
| Live price grounding | `:core:search`, `:core:network`, `:core:data` |
| Food & nutrition | `FoodHandler` (`:core:data`), `:feature:results` |
| Document / OCR | `:core:network`, `GenericHandler` |
| Scan history + manage | `:core:database`, `:core:data`, `:feature:history` |
| Settings & API keys | `:core:datastore`, `:feature:settings` |
| Robust UI states | `:core:common`, `:core:designsystem`, all features |
| Home-screen widgets | `:feature:widget` (Glance) |
