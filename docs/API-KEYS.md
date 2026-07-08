# LifeLens — API Keys Setup

> **Point your camera at anything, know everything.**

LifeLens is **Qwen-only** for identification and needs just **one** key:

1. **`DASHSCOPE_API_KEY`** — for **Qwen-VL** (vision + language) via DashScope. **Required** for everything: identification, specs, nutrition, price synthesis and follow-up Q&A. A working default key ships with the app so it runs out of the box; paste your own in Settings to override it.
2. **`SEARCH_API_KEY`** — **optional.** Live price grounding already works **without any key** by aggregating several free search engines (Google, DuckDuckGo, Bing). Adding a **Serper** key simply adds Google Shopping's structured listings to the mix for richer pricing.

Keys are **never hard-coded**. You provide them either at **build time** (via `secrets.properties`) or at **runtime** (in the in-app Settings screen). This guide covers both.

Related: **[TECHNICAL.md](../TECHNICAL.md)** · **[README.md](../README.md)** · **[docs/ARCHITECTURE.md](ARCHITECTURE.md)**.

---

## 1. Get a DashScope (Qwen) API Key

DashScope is Alibaba Cloud's model service; Qwen-VL is served through its **OpenAI-compatible** endpoint.

1. Go to **Alibaba Cloud Model Studio** (DashScope) and sign in / create an account.
2. Open the **API Keys** section and **create an API key**. It looks like `sk-...`.
3. **Use the international endpoint.** LifeLens is configured for:
   `https://dashscope-intl.aliyuncs.com/compatible-mode/v1`
   Make sure your key belongs to the same region/account as this endpoint (see [Troubleshooting](#troubleshooting)).
4. Confirm access to the vision models: **`qwen-vl-max`** and **`qwen2.5-vl-72b-instruct`**.
5. Copy the key — you'll paste it below.

> Keep the key private. Treat it like a password; it bills your account.

---

## 2. Search for live pricing (no key required)

Price grounding is behind a single `SearchClient` (`AggregatingSearchClient`) that queries **several providers concurrently** and merges the listings for thorough, resilient results. If one engine is rate-limited or changes its markup, the others still return.

### Supported search platforms

| Provider | Key needed? | Role |
|---|---|---|
| **Google** (web scrape) | No | **Queried first** — leads de-duplication, per the "Google over any other API" rule. |
| **DuckDuckGo** (HTML endpoint) | No | Free breadth; different index than Google. |
| **Bing** (web scrape) | No | Free breadth; catches retailers the others miss. |
| **Serper.dev** (Google Shopping) | Yes (`SEARCH_API_KEY`) | Optional booster — adds structured Shopping prices when a key is present. |

Results are deduped to one listing per retailer host (Google wins ties), capped, and handed to Qwen to synthesize a price range with buy links.

### Optional: add a Serper key for richer prices
1. Sign up at **serper.dev**.
2. Copy your API key from the dashboard.
3. Use it as `SEARCH_API_KEY` (build time or in Settings).

### Location-aware currency
When you grant **coarse location**, LifeLens biases the price search to your country and asks Qwen to report prices in your **local currency** (e.g. ₦ for Nigeria). **Deny/skip it and pricing stays generic (USD)** — never a country currency you didn't opt into.

---

## 3. Where to Put the Keys

### Option A — `secrets.properties` (build time)

Create a file named **`secrets.properties`** at the **repo root** (next to `settings.gradle.kts`). It is **gitignored** and read into `BuildConfig` at build time.

```properties
# secrets.properties  (repo root — DO NOT COMMIT)
DASHSCOPE_API_KEY=sk-your-dashscope-key-here
SEARCH_API_KEY=            # optional — leave blank to use the free Google/DuckDuckGo/Bing search
```

- The build reads these into `BuildConfig` fields the network/search modules consume.
- You may alternatively place them in `local.properties` (also gitignored) if you prefer a single file.

### Option B — In-app Settings (runtime)

1. Launch LifeLens.
2. Open **Settings**.
3. Paste your **DashScope** key and (optionally) your **search** key.
4. Save. Keys are stored via **DataStore** and persist across launches.

**Precedence:** a key entered in Settings (DataStore) **overrides** the `BuildConfig` value. This lets developers build with `secrets.properties` while end users (e.g. judges running a prebuilt APK) simply paste keys in the app.

---

## 4. These Files Are Gitignored — Never Commit Keys

`local.properties` and `secrets.properties` are listed in `.gitignore` and **must never be committed**. Before pushing:

- Do not paste real keys into source, XML, README, or commit messages.
- If a key is ever committed, **revoke and regenerate it immediately** on the provider dashboard.
- Verify with:

  ```bash
  git check-ignore -v secrets.properties local.properties
  git status --porcelain   # should NOT list either file
  ```

---

## 5. Verify Your Setup

1. Add keys via Option A or B.
2. Build and run:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```
3. Scan a common object → you should get an identification (confirms the **DashScope** key).
4. Scan a product → you should get a price range + buy links (works with the free search; a Serper key just adds Shopping listings).

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| **401 / "invalid api key" / "Incorrect API key"** | Missing, wrong, or malformed `DASHSCOPE_API_KEY`; extra spaces/quotes. | Re-copy the key (no surrounding quotes/whitespace). Confirm it's saved in Settings or present in `secrets.properties`. Regenerate if unsure. |
| **403 / access denied to model** | Key lacks access to `qwen-vl-max` / `qwen2.5-vl-72b-instruct`. | Enable the vision models in Model Studio for your account. |
| **Region / endpoint mismatch** | Key created for a different region than the international endpoint. | Ensure the key matches `https://dashscope-intl.aliyuncs.com/compatible-mode/v1`, or switch to the endpoint that matches your key's region. |
| **429 / rate limited / quota exceeded** | Too many calls, or free-tier quota hit. | Slow down (debounce), reduce image size, wait for the window to reset, or upgrade the plan. LifeLens caches results in Room to cut repeat calls. |
| **No prices / empty buy options** | All search engines were rate-limited or returned nothing for the query. | Retry; add a **Serper** key for structured Shopping results; identification still works without any pricing. |
| **Prices in the wrong currency** | Location permission state changed the currency basis. | Grant coarse location for local currency, or deny it for generic USD pricing. |
| **Prices look off** | Grounding varies by region/stock/time; model estimates. | Expected — LifeLens shows **ranges with disclaimers** and live links; verify at the source. |
| **Keys not picked up after adding to `secrets.properties`** | Stale build / Gradle didn't re-read. | Re-sync Gradle and rebuild (`./gradlew clean assembleDebug`). Or just enter keys in Settings (takes effect immediately). |
| **Large images fail / slow** | Payload too big. | LifeLens downscales to ~1024px automatically; ensure you're on the current build (see [TECHNICAL.md](../TECHNICAL.md)). |

---

## Summary

- **One required key:** `DASHSCOPE_API_KEY` (Qwen powers everything; a default ships so the app runs out of the box). `SEARCH_API_KEY` (Serper) is optional — free Google/DuckDuckGo/Bing search runs without it.
- **Two ways to provide them:** `secrets.properties` → `BuildConfig` (build time) **or** Settings screen → DataStore (runtime; overrides build-time).
- **Never commit them.** Both `secrets.properties` and `local.properties` are gitignored.

Back to **[README.md](../README.md)** · Deep-dive in **[TECHNICAL.md](../TECHNICAL.md)**.
