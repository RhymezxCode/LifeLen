# LifeLens — Technical Documentation

> **Point your camera at anything, know everything.**

Implementation-level documentation for **LifeLens**: the tech stack, Qwen-VL integration, search grounding, image pipeline, data models, the extensible `CategoryHandler` registry, Room schema, DataStore, secrets, the build system, testing, performance, and known limitations.

Companion docs: **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** (structure) · **[docs/API-KEYS.md](docs/API-KEYS.md)** (keys) · **[README.md](README.md)** · **[plan.md](plan.md)**.

> **Naming note:** Product name **LifeLens**; Gradle `rootProject.name` and package/`applicationId` are `lifelen` / `com.lifelen`.

---

## Tech Stack & Versions

| Concern | Choice / Version |
|---|---|
| Language | Kotlin **2.2.10** |
| UI | Jetpack Compose (BOM **2026.02.01**) + Material 3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Camera | CameraX (core, camera2, lifecycle, view) |
| Image loading | Coil **3** |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| AI / Vision | Qwen-VL via DashScope OpenAI-compatible API |
| Search grounding | `AggregatingSearchClient` — free **Google + DuckDuckGo + Bing** scrapes, plus optional keyed **Serper** |
| Localization | `RegionProvider` (coarse location → country/currency) for local-currency pricing |
| Local persistence | Room (scan history) + DataStore (settings & API keys) |
| Connectivity | `NetworkMonitor` (`ConnectivityManager`) for the offline fallback |
| Build | Gradle Kotlin DSL, version catalog, `build-logic` convention plugins, composite build |
| Testing | JUnit4, **Robolectric** (JVM-runnable Android + Compose UI/E2E), Turbine, OkHttp MockWebServer |

**Platform targets:** `minSdk 24`, `targetSdk 37`, `compileSdk 37`, Java **11**, Kotlin **2.2.10**, AGP **9.4.0-alpha03**.

---

## Qwen-VL Integration (DashScope)

Qwen-VL is the brain of LifeLens, accessed through DashScope's **OpenAI-compatible** API — so a standard Retrofit + OkHttp client works with minimal ceremony.

- **Base URL (international):** `https://dashscope-intl.aliyuncs.com/compatible-mode/v1`
- **Endpoint:** `POST /chat/completions`
- **Auth:** `Authorization: Bearer $DASHSCOPE_API_KEY`
- **Models:** `qwen-vl-max` (high quality) and `qwen2.5-vl-72b-instruct` (strong multimodal); selectable per call/preference.
- **Images:** passed as OpenAI-style `image_url` content parts whose URL is a **base64 data URL** (`data:image/jpeg;base64,...`).

### Identification request (structured output)

The system prompt forces strict JSON and routes categories (food vs. product vs. general). Temperature is kept low for determinism.

```jsonc
// POST https://dashscope-intl.aliyuncs.com/compatible-mode/v1/chat/completions
{
  "model": "qwen-vl-max",
  "temperature": 0.2,
  "max_tokens": 800,
  "messages": [
    {
      "role": "system",
      "content": "You are LifeLens, a visual identification engine. Identify the primary object in the image. Respond ONLY with a single JSON object, no prose, matching this schema: {\"name\": string, \"category\": one of [\"FOOD\",\"ELECTRONICS\",\"BOOK\",\"CLOTHING\",\"PLANT\",\"ANIMAL\",\"LANDMARK\",\"DOCUMENT\",\"GENERIC\"], \"confidence\": number 0..1, \"attributes\": object of string->string, \"searchQuery\": string|null (a precise shopping query if this is a purchasable product, else null), \"nutrition\": {\"calories\": number, \"protein\": number, \"carbs\": number, \"fat\": number, \"portion\": string}|null (only if FOOD)}. For DOCUMENT, transcribe the visible text into the attributes map under a \"Text\" key. Never include markdown fences."
    },
    {
      "role": "user",
      "content": [
        { "type": "text", "text": "Identify this and fill the schema." },
        { "type": "image_url", "image_url": { "url": "data:image/jpeg;base64,/9j/4AAQSkZJRg..." } }
      ]
    }
  ]
}
```

### Identification response (example)

```jsonc
{
  "id": "chatcmpl-xxxx",
  "object": "chat.completion",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "{\"name\":\"Dell XPS 13 (9340)\",\"category\":\"ELECTRONICS\",\"confidence\":0.86,\"attributes\":{\"cpu\":\"Intel Core Ultra 7\",\"ram\":\"16GB\",\"display\":\"13.4\\\" FHD+\",\"weight\":\"1.19kg\"},\"searchQuery\":\"Dell XPS 13 9340 Core Ultra 7 16GB price\",\"nutrition\":null}"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": { "prompt_tokens": 1180, "completion_tokens": 96, "total_tokens": 1276 }
}
```

The app deserializes the DashScope envelope with kotlinx.serialization, then parses the `content` string into an `Identification`. Parsing is **defensive**: any stray fences are stripped, and a parse failure falls back to a `GENERIC` identification rather than crashing.

### System-prompt strategy

- **One object, strict JSON, no markdown.** Reduces post-processing and drift.
- **Category routing in the same call** so the `CategoryHandler` registry can dispatch immediately.
- **Conditional fields:** `nutrition` only for `FOOD`; for `DOCUMENT` the visible text is transcribed into a `Text` **attribute** (`DocumentHandler` + `DocumentResultBody` surface it); `searchQuery` only for purchasable products — keeps payloads lean.
- **Low temperature (~0.2)** and a bounded `max_tokens` (~800 for identification) for stable, cheap responses.

### Price-synthesis call

A second Qwen call turns raw listings into a clean `PriceInfo`:

```jsonc
{
  "model": "qwen-vl-max",
  "temperature": 0.2,
  "max_tokens": 600,
  "messages": [
    { "role": "system", "content": "You summarize shopping search results into a JSON price summary: {\"currency\":string,\"low\":number,\"high\":number,\"median\":number,\"buyOptions\":[{\"merchant\":string,\"price\":number,\"url\":string}],\"asOf\":string,\"disclaimer\":string}. Use ONLY the provided listings. Rank buyOptions cheapest first. Respond with JSON only." },
    { "role": "user", "content": "Product: Dell XPS 13 (9340)\nListings:\n1. ExampleStore $999 https://...\n2. OtherShop $1049 https://...\n3. Marketplace $979 https://..." }
  ]
}
```

---

## Search-Grounding Pipeline

Model memory goes stale; live prices don't. After identification, product categories run a grounding step.

1. **Query construction.** `ElectronicsHandler` (and other product handlers) build a query from the `Identification` — preferring the model's `searchQuery`, else composing from `name` + salient attributes.
2. **`SearchClient` interface.** `:core:search` abstracts the provider:

   ```kotlin
   interface SearchClient {
       suspend fun search(query: String, limit: Int = 8): Result<List<SearchResult>>
   }

   data class SearchResult(
       val title: String,
       val snippet: String,
       val url: String,
       val price: String?,      // when the provider returns shopping data
       val merchant: String?,
   )
   ```

   The bound implementation is **`AggregatingSearchClient`**, which fans out to several providers **concurrently** and merges their listings for thorough, resilient grounding:

   | Provider | Key | Notes |
   |---|---|---|
   | **Google** (web scrape) | none | Queried **first** — wins host de-duplication ties. |
   | **DuckDuckGo** (HTML endpoint) | none | Free; independent index. |
   | **Bing** (web scrape) | none | Free; extra retailer coverage. |
   | **Serper.dev** (Google Shopping) | `SEARCH_API_KEY` | Optional booster added only when a key is set. |

   Each provider is best-effort (a failing/rate-limited engine contributes nothing); results are deduped to one listing per retailer host (Google first) and capped before synthesis.
3. **Localize.** A `RegionProvider` resolves the user's country + currency from **coarse location** when granted. The query is biased to that country and Qwen is told to report prices in the local currency; **without location permission the price is generic (USD)**, never a country currency.
4. **Feed back to Qwen.** Listings are passed into the price-synthesis call above, producing a `PriceInfo` with a range and cheapest-first `BuyOption`s.
5. **Disclaimers.** Prices are presented as **ranges** with an "estimate — verify at source" note and live links, since listings vary by region, stock, and time. Grounding + disclaimers together mitigate hallucinated prices.

---

## Image Capture & Preprocessing

Handled in `:feature:scanner` with the encoding utility in `:core:network`.

- **CameraX** `Preview` + `ImageCapture`; capture to an in-memory JPEG.
- **Downscale to ~1024 px** on the long edge — large enough for recognition, small enough to control tokens/cost/latency.
- **JPEG quality ~80%** — a good size/clarity tradeoff.
- **Base64 data URL** (`data:image/jpeg;base64,...`) for the `image_url` content part.
- **Cost awareness:** base64 inflates payload ~33%; downscaling before encoding is the main lever on request size and per-call cost. The captured full-resolution JPEG is written to disk for history; the transmitted copy is the downscaled one.

```kotlin
object ImageEncoder {
    fun toDataUrl(jpeg: ByteArray): String =
        "data:image/jpeg;base64," + Base64.encodeToString(jpeg, Base64.NO_WRAP)
}
```

---

## Data Model Definitions

Defined in `:core:model` (pure Kotlin, `@Serializable` where crossing the wire). Sketches:

```kotlin
enum class ScanCategory {
    FOOD, ELECTRONICS, BOOK, CLOTHING, PLANT, ANIMAL, LANDMARK, DOCUMENT, GENERIC
}

data class Identification(
    val name: String,
    val category: ScanCategory,
    val confidence: Float,
    val attributes: Map<String, String> = emptyMap(), // for DOCUMENT the transcription lands here under "Text"
    val searchQuery: String? = null,
)

data class NutritionInfo(
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val portion: String,
)

data class BuyOption(
    val merchant: String,
    val price: Double,
    val currency: String,
    val url: String,
)

data class PriceInfo(
    val currency: String,
    val low: Double,
    val high: Double,
    val median: Double?,
    val buyOptions: List<BuyOption>,
    val asOf: String,
    val disclaimer: String,
)

data class Scan(
    val id: Long,
    val imagePath: String,
    val identification: Identification,
    val nutrition: NutritionInfo? = null,
    val price: PriceInfo? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long,     // epoch millis
)
```

---

## `CategoryHandler` Registry (implementation)

The registry lives in `:core:data` and is how LifeLens stays extensible (see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the design rationale).

```kotlin
interface CategoryHandler {
    val category: ScanCategory
    suspend fun enrich(identification: Identification): ScanEnrichment
}

data class ScanEnrichment(
    val nutrition: NutritionInfo? = null,
    val price: PriceInfo? = null,
    val extraAttributes: Map<String, String> = emptyMap(),
)

class CategoryHandlerRegistry @Inject constructor(
    handlers: Set<@JvmSuppressWildcards CategoryHandler>,
    private val generic: GenericHandler,
) {
    private val byCategory = handlers.associateBy { it.category }
    fun handlerFor(category: ScanCategory): CategoryHandler =
        byCategory[category] ?: generic
}
```

- `FoodHandler` → returns `NutritionInfo` (already present from the identification call, or a refining call).
- `ElectronicsHandler` / `BookHandler` / `ClothingHandler` → product handlers (a shared `ProductHandler` base): build the search query, call `SearchClient`, call Qwen price synthesis → `PriceInfo`; also normalize spec attributes.
- `PlantHandler` → care guidance (light/water/difficulty/pet-safety) returned inline by Qwen as attributes; no extra call.
- `DocumentHandler` → routes `ScanCategory.DOCUMENT`; the transcription is returned inline in the `Text` attribute (surfaced by `DocumentResultBody`), so no extra call.
- `GenericHandler` → the safe default for animals, landmarks, logos, and anything unrouted.

Handlers are contributed via Hilt multibindings in `DataModule` (add one `@Binds @IntoSet` line per type):

```kotlin
@Module @InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @IntoSet abstract fun generic(h: GenericHandler): CategoryHandler
    @Binds @IntoSet abstract fun food(h: FoodHandler): CategoryHandler
    @Binds @IntoSet abstract fun plant(h: PlantHandler): CategoryHandler
    @Binds @IntoSet abstract fun document(h: DocumentHandler): CategoryHandler
    @Binds @IntoSet abstract fun electronics(h: ElectronicsHandler): CategoryHandler
    @Binds @IntoSet abstract fun book(h: BookHandler): CategoryHandler
    @Binds @IntoSet abstract fun clothing(h: ClothingHandler): CategoryHandler
}
```

### How to add a new object-type handler (checklist)

1. Add/reuse a `ScanCategory` value in `:core:model`.
2. Implement `CategoryHandler` in `:core:data` (e.g. `WineHandler`).
3. Add a `@Binds @IntoSet` for it in `CategoryHandlerModule`.
4. Extend `:feature:results` presentation only if it needs bespoke UI.
5. Unit-test the handler.
6. (Optional) Update the identification system prompt to emit the new category/fields.

`ScanRepository` never changes — it just asks the registry for `handlerFor(category)`.

---

## Room Schema

`:core:database`, set up via the `lifelen.android.room` convention plugin.

- **Entity:** `ScanEntity` stores the **image file path** (not a blob), the identification and enrichment as serialized columns (or flattened fields), `isFavorite`, and `createdAt`.
- **Images on disk, not in the DB:** keeps the database small, queries fast, and avoids large-row overhead. The DB holds a path into app-internal storage.

```kotlin
@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val name: String,
    val category: String,
    val confidence: Float,
    val attributesJson: String,       // serialized Map
    val nutritionJson: String?,       // serialized NutritionInfo
    val priceJson: String?,           // serialized PriceInfo (document text is carried inside attributesJson)
    val isFavorite: Boolean = false,
    val createdAt: Long,
)

@Dao
interface ScanDao {
    @Insert suspend fun insert(scan: ScanEntity): Long
    @Query("SELECT * FROM scans ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ScanEntity>>
    @Query("SELECT * FROM scans WHERE name LIKE '%' || :q || '%' ORDER BY createdAt DESC")
    fun search(q: String): Flow<List<ScanEntity>>
    @Query("SELECT * FROM scans WHERE id = :id")
    suspend fun getById(id: Long): ScanEntity?
    @Query("UPDATE scans SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)
    @Delete suspend fun delete(scan: ScanEntity)
}
```

**Migrations:** for the hackathon, schema export is enabled and destructive migration is acceptable during rapid iteration; production would add versioned `Migration`s. DAO reads return `Flow` so the UI is reactive and offline-capable.

---

## Result Actions & Offline Fallback

**Favorite / delete / share (saved detail).** Re-opening a saved scan shows a detail sheet whose top controls expose a favourite toggle (filled/outline heart) and a delete (trash) action alongside refresh:

- `ResultsViewModel.toggleFavorite()` calls `HistoryRepository.toggleFavorite(id, !isFavorite)` (`ScanDao.setFavorite`) and updates the `Ready` state optimistically.
- `ResultsViewModel.delete()` calls `HistoryRepository.delete(id)` (`ScanDao.deleteById`) and emits `ResultEvent.Deleted`, which the route collects to pop back to the Library.
- Share is a system `ACTION_SEND` chooser carrying the scan's summary (or the document transcription).
- These controls appear only for saved scans — a fresh, unsaved capture is not yet in the database.

**Offline last-result fallback.** A fresh capture that fails is routed by connectivity rather than dead-ending on a generic error:

```kotlin
is DataResult.Error ->
    _uiState.value = if (!networkMonitor.isOnline()) {
        // history is newest-first → most recent saved scan is the fallback
        ResultsUiState.Offline(lastScan = historyRepository.observeHistory().first().firstOrNull())
    } else {
        ResultsUiState.Failed(result.throwable.message ?: "Couldn't identify this")
    }
```

- `NetworkMonitor` (`AndroidNetworkMonitor` over `ConnectivityManager`; needs `ACCESS_NETWORK_STATE`) reports connectivity; it is bound in `DataModule` and mockable in tests via a `FakeNetworkMonitor`.
- `ResultsUiState.Offline(lastScan)` renders "You're offline" + the last scan (`IdentityHeader`) + a **Try again** button.
- `retry()` re-runs identification against the capture draft still held by `ScanSession`, so reconnecting and retrying flows straight into a normal `Ready` result.

---

## DataStore Usage

`:core:datastore` uses Jetpack DataStore (Preferences) for:

- **User settings:** enable/disable live pricing, preferred Qwen model, theme, etc.
- **API keys entered at runtime:** `DASHSCOPE_API_KEY`, `SEARCH_API_KEY`, stored so they persist across launches without rebuilding.

Repositories/clients read the effective key as **DataStore value first, else `BuildConfig`** — so a developer can build with `secrets.properties` while a user can paste keys in Settings.

---

## Secrets & Security

Detailed setup is in **[docs/API-KEYS.md](docs/API-KEYS.md)**. In brief:

- **Never hard-coded.** No API key appears in source, resources, logs, or version control.
- **Build-time:** `secrets.properties` (or `local.properties`) at the repo root supplies `DASHSCOPE_API_KEY` and `SEARCH_API_KEY`, read into `BuildConfig` fields at build time.
- **Runtime:** the Settings screen stores keys in DataStore; runtime value overrides `BuildConfig`.
- **Gitignored:** `local.properties` and `secrets.properties` are gitignored and must never be committed.
- **Transport:** all API calls are HTTPS; the `Authorization: Bearer` header carries the DashScope key.
- **Input validation** at trust boundaries (network/storage) per project standards.

---

## Version Catalog & Build-Logic

- **Version catalog** (`gradle/libs.versions.toml`) centralizes all versions and library/plugin aliases — one place to bump.
- **`build-logic:convention`** is an included (composite) build providing the convention plugins listed below; each module applies the right plugin instead of duplicating config.

| Plugin ID | Purpose |
|---|---|
| `lifelen.android.application` | App module config (SDK 24/37/37, Java 11, defaultConfig, BuildConfig). |
| `lifelen.android.application.compose` | Compose for `:app`. |
| `lifelen.android.library` | Android library defaults. |
| `lifelen.android.library.compose` | Compose for library modules. |
| `lifelen.android.feature` | Feature bundle: library + Hilt + standard feature deps. |
| `lifelen.android.hilt` | Hilt + KSP. |
| `lifelen.android.room` | Room + KSP + schema export. |
| `lifelen.jvm.library` | Pure Kotlin/JVM (e.g. `:core:model`). |

Why: consistent, DRY builds; a new module is productive in a couple of lines; version/behavior changes happen in one place.

---

## Testing Strategy

| Level | Tooling | What it covers |
|---|---|---|
| **Repository / data** | JUnit4 + **OkHttp MockWebServer** + **Turbine** | Qwen client parsing (identification + price synthesis), error mapping (401/rate-limit/parse), search grounding path, `Flow` emissions from repositories. |
| **CategoryHandler** | JUnit4 | Each handler's enrichment logic in isolation (fake `SearchClient`/`QwenVisionClient`). |
| **ViewModel** | JUnit4 + Turbine + test dispatchers | `StateFlow<UiState>` transitions Loading → Ready/Failed/**Offline**; favourite/delete/retry events. |
| **UI / E2E** | **Compose UI test on Robolectric** (JVM, no device) | Every screen renders each `UiState` (incl. **Offline** and document transcription); favourite/delete/share, portion, condition-filter, and search interactions; accessibility semantics. |

- Inject dispatchers (`@Dispatcher`) so coroutines run on a `StandardTestDispatcher` deterministically.
- MockWebServer serves canned DashScope/search JSON so tests are hermetic and offline.
- Turbine asserts exact `Flow` emission sequences.

---

## Performance Notes

- **Image size drives everything** — latency, token cost, and reliability. Downscaling to ~1024px + ~80% JPEG is the primary optimization; base64 adds ~33% overhead on top.
- **Cache & reuse:** Room is the source of truth; re-opening a scan is a local read, no network call. Consider caching identical/near-identical scans.
- **Debounce** capture and history search to avoid redundant calls.
- **Two-call product path** (identify → search → synthesize) is the slowest flow; show incremental UI (identification first, price streaming in) to keep it responsive.
- **Coil 3** handles efficient thumbnail loading in history from disk paths.

---

## Known Limitations & Bleeding-Edge Caveats

- **AGP `9.4.0-alpha03`** is a pre-release; expect occasional plugin/IDE friction. Use a recent Android Studio canary and pin versions.
- **Version alignment on sync:** with Kotlin 2.2.10, Compose BOM 2026.02.01, and Coil 3, some transitive libraries may need alignment when Gradle syncs; the version catalog is the single place to resolve conflicts.
- **Price accuracy** depends on the search provider's coverage and regional stock; prices are estimates with disclaimers and live links, not guarantees.
- **Model variability:** identification confidence varies with lighting, framing, and obscure items; `GenericHandler` ensures graceful degradation.
- **Cost/quota:** vision calls consume tokens per image; heavy testing can hit rate limits — mitigate with caching, downscaling, and cached demo scans (see [plan.md](plan.md) risks).

---

See **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** for how these pieces fit together and **[docs/API-KEYS.md](docs/API-KEYS.md)** to configure keys.
