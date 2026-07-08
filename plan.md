# LifeLens — Hackathon Delivery Plan

> **Qwen Code Hackathon** · Today: **July 5, 2026** · Deadline: **July 9, 2026** · 5 days.

This is the delivery plan for **LifeLens**. It defines the goal, scope, a realistic day-by-day timeline, milestones, per-module tasks, risks, the definition of done, the demo, and the submission checklist. The plan is aggressive but achievable because the architecture (see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)) front-loads reuse: features share one repository layer and an extensible `CategoryHandler` registry.

---

## Goal & Success Criteria

**Goal:** Ship a demoable Android app where pointing the camera at an object returns structured knowledge — spec sheet + live price for products, calories + macros for food, general answers otherwise — with every scan saved to a searchable history, all powered by **Qwen-VL**.

**Success criteria (the demo must show):**

1. Scan a **general object** and get a correct identification. *(Qwen-VL happy path)*
2. Scan a **product** and get a spec sheet plus a **live, grounded price range** and buy links.
3. Scan **food** and get calories + protein/carbs/fat.
4. Every scan is **persisted** and appears in a **searchable history**; favorite / re-open / share / delete work.
5. **API keys** are configured via Settings (DataStore), never hard-coded.
6. The app handles **loading / empty / error / offline** gracefully.
7. Qwen-VL is visibly the **centerpiece** — one model does vision *and* price synthesis.

---

## Scope

| Scope | Items |
|---|---|
| **MVP (must ship)** | Scan → identify; category routing (food/product/general) via `CategoryHandler`; product spec sheet; live price grounding + buy links; food nutrition; scan history (search, favorite, re-open, share, delete); results detail screen; settings/API keys via DataStore; loading/empty/error/offline states; document text extraction (bonus within the same call). |
| **Stretch (if time allows)** | Multi-object detection; barcode/QR quick-scan; OCR translation; price-drop watchlist + alerts; compare two products; wishlist export; nutrition daily log & goals; voice/text follow-up questions; shareable scan cards; AR overlay labels; collections/folders; dark mode; deeper accessibility; quick-scan widget. |
| **Out of scope (this hackathon)** | User accounts / cloud sync; server backend; iOS/web clients; in-app purchases; social feed; on-device model inference; multi-language localization beyond OCR translation stretch. |

Full feature detail (with user workflows + technical requirements) is in **[features.md](features.md)**.

---

## Day-by-Day Timeline

| Day | Date | Theme | Headline outcome |
|---|---|---|---|
| **Day 1** | Jul 5 | Foundation | Multi-module project builds; design system + nav shell; camera permission + live preview. |
| **Day 2** | Jul 6 | First light | Capture → image pipeline → Qwen-VL → first end-to-end "identify an object". |
| **Day 3** | Jul 7 | Structure & memory | Structured results (specs/food/nutrition); Room persistence; history + results detail UI. |
| **Day 4** | Jul 8 | Grounding & polish | Live price grounding + buy links; settings/API keys; empty/error/loading/offline states. |
| **Day 5** | Jul 9 | Ship | Testing, bug-fixing, demo video, screenshots, README polish, submission. |

### Day 1 (Jul 5) — Project scaffolding & foundation
- Set up the composite build: `build-logic:convention` with all convention plugins (`lifelen.android.application`, `.library`, `.feature`, `.hilt`, `.room`, `.jvm.library`, and the two `.compose` variants).
- Create the module skeleton: `:app`, `:core:*` (model, common, designsystem, datastore, database, network, search, data), `:feature:*` (scanner, results, history, settings).
- Version catalog (`libs.versions.toml`) with the canonical versions.
- `:core:designsystem`: theme (colors, typography, shapes), base components (`LifelenButton`, `LoadingIndicator`, `ScanCard`).
- `:app`: `@HiltAndroidApp`, `MainActivity`, Navigation Compose host with placeholder destinations.
- `:feature:scanner`: camera permission flow + CameraX live preview.

### Day 2 (Jul 6) — Capture, image pipeline, Qwen-VL
- CameraX `ImageCapture` → JPEG → downscale (~1024px) → compress → base64 data URL.
- `:core:network`: Retrofit + OkHttp + kotlinx.serialization; DashScope client (`QwenVisionClient`) against `https://dashscope-intl.aliyuncs.com/compatible-mode/v1`.
- Structured-output system prompt for identification with category routing.
- `:core:data`: `ScanRepository.identify(...)` wiring capture → Qwen → `Identification`.
- **Milestone: first end-to-end "identify an object" happy path** on device.

### Day 3 (Jul 7) — Structured results, persistence, history
- `:core:model`: finalize `Scan`, `Identification`, `ScanCategory`, `NutritionInfo`, `PriceInfo`, `BuyOption`.
- `CategoryHandler` registry in `:core:data` with `FoodHandler`, `ElectronicsHandler`, `BookHandler`, `ClothingHandler`, `GenericHandler`.
- `:core:database`: Room entities, DAOs, DB; store image as file path; `Flow` queries.
- `ScanRepository` persists each `Scan`; `HistoryRepository` reads/searches.
- `:feature:results`: detail UI (spec sheet / nutrition panel / knowledge card).
- `:feature:history`: list + search + favorite/re-open/share/delete.

### Day 4 (Jul 8) — Search grounding, settings, polish
- `:core:search`: `SearchClient` interface + default impl (Serper); query built from `Identification`.
- Second Qwen call synthesizes `PriceInfo` (range + cheapest `BuyOption`s with URLs).
- `:core:datastore` + `:feature:settings`: API key entry + preferences.
- Wire keys from `secrets.properties` → `BuildConfig` and from DataStore at runtime.
- Polish: loading/empty/error states everywhere; offline last-result fallback; disclaimers on prices.

### Day 5 (Jul 9) — Testing, demo, submission
- Unit tests (repositories + MockWebServer + Turbine), ViewModel tests, a couple of Compose UI tests.
- Bug-fixing and stabilization pass; verify all success criteria on a clean install.
- Record the 2–3 min demo video; capture screenshots; polish README and docs.
- Final submission.

---

## Milestones / Checklist

- [ ] **M1 (Day 1):** Project builds; nav shell runs; camera preview shows.
- [ ] **M2 (Day 2):** First object identified end-to-end via Qwen-VL.
- [ ] **M3 (Day 3):** Scans persist; history + results detail work; food/product routing live.
- [ ] **M4 (Day 4):** Live grounded price with buy links; settings/API keys; robust states.
- [ ] **M5 (Day 5):** Tested, recorded, screenshotted, submitted.

---

## Task Breakdown by Module

| Module | Key tasks |
|---|---|
| `build-logic:convention` | Author all convention plugins; verify each module applies the right one. |
| `:app` | `@HiltAndroidApp`, `MainActivity`, Navigation Compose host, wire feature graphs, permissions. |
| `:core:model` | Domain models + `ScanCategory` enum; keep Android-free. |
| `:core:common` | Dispatcher qualifiers, `Result` wrapper, error types, extensions. |
| `:core:designsystem` | Theme + reusable components + icons. |
| `:core:datastore` | Settings + secure API key storage. |
| `:core:database` | Room entities, DAOs, DB; image-path storage; migrations note. |
| `:core:network` | Retrofit/OkHttp; `QwenVisionClient` (DashScope); DTOs; image encoding; prompts. |
| `:core:search` | `SearchClient` + `AggregatingSearchClient` (Google + DuckDuckGo + Bing + optional Serper). |
| `:core:data` | `ScanRepository`, `HistoryRepository`, `CategoryHandler` registry + handlers. |
| `:feature:scanner` | Preview, permission, capture, image pipeline, analyze trigger. |
| `:feature:results` | Result detail UI variants; share; ask-followup (stretch). |
| `:feature:history` | List, search, favorite, re-open, delete. |
| `:feature:settings` | Key entry, preferences, validation. |

---

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| **Bleeding-edge build tooling** (AGP `9.4.0-alpha03`, Kotlin 2.2.10, Compose BOM 2026.02.01) — sync/plugin breakage. | Medium | High | Pin versions in the catalog; keep `build-logic` isolated; note known-good IDE build; be ready to nudge library versions on sync; avoid last-day tooling upgrades. |
| **API rate limits / cost** on DashScope and the search API during dev + demo. | Medium | Medium | Downscale images (~1024px) to cut tokens/cost; cache results in Room; debounce scans; keep a couple of pre-captured demo scans that hit cache. |
| **Network reliability during the live demo.** | Medium | High | Record the demo video ahead of time; offline **last-scan/history fallback** from Room; keep cached golden scans that render without a live call. |
| **Model hallucination on prices.** | Medium | High | **Search grounding** feeds real listings to Qwen; show price *ranges* with a **disclaimer**; link out to live sources so the user can verify. |
| **Structured-output drift** (Qwen returns non-JSON / wrong shape). | Medium | Medium | Strict JSON system prompt + low temperature; defensive parsing with fallbacks; `GenericHandler` catch-all so nothing crashes. |
| **Scope creep** across 5 days. | High | Medium | Freeze MVP scope; stretch items only after M4; timebox each day. |
| **CameraX / device quirks.** | Low | Medium | Test on a real device early (Day 1); handle permission denial and capture errors. |
| **Secret leakage.** | Low | High | Keys only via `secrets.properties`/`BuildConfig` or DataStore; both files gitignored; no keys in code, logs, or commits. |

---

## Definition of Done

A feature is done when:

- It works end-to-end on a real device.
- It exposes a sealed `UiState` and handles loading / empty / error / offline.
- It respects module boundaries (features depend only on `:core:data`/`model`/`designsystem`/`common`).
- No secrets are hard-coded.
- It has at least one focused test for the changed behavior (repository, ViewModel, or UI).
- Accessibility basics are present (content descriptions, ≥48dp targets).
- `./gradlew test lint` passes.

The **project** is done when all MVP success criteria pass on a clean install, the demo video is recorded, screenshots and README are polished, and the submission checklist is complete.

---

## Demo Plan (2–3 minute video)

1. **Hook (0:00–0:15):** Tagline — "Point your camera at anything, know everything." Open to the live scanner.
2. **Product (0:15–0:55):** Scan a laptop → spec sheet → **live price range + cheapest buy link**. Emphasize the price is grounded by a live search and synthesized by Qwen.
3. **Food (0:55–1:25):** Scan a meal → dish + **calories & macros**.
4. **General + OCR (1:25–1:50):** Scan a plant/landmark/book, then a document to show text extraction.
5. **History (1:50–2:15):** Show the searchable history; favorite, re-open, share.
6. **Qwen framing (2:15–2:40):** One-line architecture callout — a single Qwen-VL model powers vision *and* price synthesis; the `CategoryHandler` registry makes new object types easy.
7. **Close (2:40–3:00):** Recap + tagline.

---

## Submission Checklist

- [ ] All MVP success criteria pass on a clean install.
- [ ] Demo video (2–3 min) recorded and uploaded.
- [ ] Screenshots captured for scanner, results, history, settings.
- [ ] `README.md` polished with screenshots and correct product name (**LifeLens**).
- [ ] `docs/API-KEYS.md` accurate; no real keys anywhere in the repo.
- [ ] `secrets.properties` / `local.properties` gitignored and absent from history.
- [ ] `./gradlew assembleDebug test lint` all green.
- [ ] Repo public / accessible to judges; LICENSE present (MIT).
- [ ] Hackathon submission form completed with links.
- [ ] Qwen usage clearly explained in the write-up.

---

See also: **[README.md](README.md)** · **[ABOUT.md](ABOUT.md)** · **[features.md](features.md)** · **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** · **[TECHNICAL.md](TECHNICAL.md)** · **[docs/API-KEYS.md](docs/API-KEYS.md)**.
