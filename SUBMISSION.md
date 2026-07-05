# LifeLens — Qwen Cloud Hackathon Submission

> **Point your camera at anything, know everything.** An on-device edge agent that perceives with
> the camera, reasons with **Qwen-VL on Qwen Cloud**, and acts locally — even offline.

**Track:** **Track 5 — EdgeAgent**
**Repository:** https://github.com/RhymezxCode/LifeLen · **License:** MIT (see [`LICENSE`](LICENSE))

---

## 1. Why this is an EdgeAgent (Track 5)

Track 5 asks for a Qwen-powered device that *"perceives via edge sensors, reasons via cloud
APIs/Skills, and acts locally,"* with *"robust edge-cloud orchestration under bandwidth/latency
constraints, privacy-aware data handling, and graceful degradation in offline/weak-network
scenarios."* LifeLens is exactly that agent, running on a phone:

| EdgeAgent trait | How LifeLens does it |
|---|---|
| **Perceive (edge sensors)** | CameraX viewfinder + **on-device ML Kit** labelling of every frame in real time — no round-trip needed to see what it's looking at. |
| **Reason (cloud)** | The captured frame is sent to **Qwen-VL** (Alibaba Cloud Model Studio / Qwen Cloud) which returns a structured `Identification`; a second Qwen call synthesises live pricing from grounded web results. |
| **Act (locally)** | The agent routes the result through a `CategoryHandler` policy (food → nutrition, product → spec sheet + price, plant → care card, document → transcription), persists it to a local Room store, and can answer follow-up questions. |
| **Autonomy** | **Auto-scan** mode: when a confident label holds steady, the agent captures and identifies on its own — no shutter tap. |
| **Edge-cloud orchestration + graceful degradation** | If there is **no Qwen key or no network**, the agent degrades gracefully to an **on-device ML Kit identification** and an **offline last-result fallback** instead of failing. Pricing degrades from Serper → a keyless Google fallback → none. |
| **Privacy-aware** | Live labelling and the offline fallback run **fully on-device**; images are stored as local file paths, never uploaded except the single downscaled frame sent to Qwen for a scan. |
| **Ask (multi-turn reasoning)** | "Ask about this" seeds a Qwen chat with the identification context so the user can interrogate the scanned object conversationally. |

The agent's control loop — *perceive → reason → route → act → (optionally) converse* — is the core
of the app, not a bolt-on.

---

## 2. Qwen Cloud / Alibaba Cloud usage (Proof)

LifeLens is built on **Qwen models served by Alibaba Cloud Model Studio (DashScope / Qwen Cloud)**
through the OpenAI-compatible endpoint. Both the vision reasoning and the natural-language price
synthesis are Qwen calls.

- **Models:** `qwen-vl-max` (vision identification) and a Qwen text model (price synthesis, follow-up).
- **Endpoint:** `https://dashscope-intl.aliyuncs.com/compatible-mode/v1/` (Alibaba Cloud Model Studio).

**Proof-of-use code files (Alibaba Cloud services & APIs):**
- Client + endpoint: [`core/network/.../QwenApi.kt`](core/network/src/main/kotlin/com/lifelen/core/network/QwenApi.kt)
- Vision + chat calls: [`core/network/.../QwenClient.kt`](core/network/src/main/kotlin/com/lifelen/core/network/QwenClient.kt)
- Auth (Bearer key): [`core/network/.../QwenAuthInterceptor.kt`](core/network/src/main/kotlin/com/lifelen/core/network/QwenAuthInterceptor.kt)
- Agent pipeline (perceive→reason→route): [`core/data/.../repository/ScanRepository.kt`](core/data/src/main/kotlin/com/lifelen/core/data/repository/ScanRepository.kt)
- Structured prompts: [`core/data/.../qwen/QwenPrompts.kt`](core/data/src/main/kotlin/com/lifelen/core/data/qwen/QwenPrompts.kt)

> Note: LifeLens calls Alibaba Cloud's Qwen APIs directly from the client. Keys are supplied via a
> gitignored `secrets.properties`/`BuildConfig` or entered at runtime — never committed.

---

## 3. Architecture

```mermaid
flowchart TD
    subgraph Edge["📱 Edge (on-device, Android)"]
        cam["Camera sensor (CameraX)"]
        mlkit["ML Kit image labelling<br/>(live label + no-key fallback)"]
        room["Room store + offline last-result"]
        ui["Compose UI · CategoryHandler routing"]
    end
    subgraph Cloud["☁️ Qwen Cloud (Alibaba Cloud Model Studio)"]
        qwenvl["Qwen-VL — identify"]
        qwentext["Qwen — price synthesis / ask"]
    end
    search["Search grounding<br/>(Serper → Google fallback)"]

    cam --> mlkit
    mlkit -->|"stable label → auto-capture"| ui
    ui -->|"downscaled frame"| qwenvl
    qwenvl -->|"structured Identification"| ui
    ui -->|"product query"| search
    search --> qwentext
    qwentext -->|"PriceInfo"| ui
    ui --> room
    ui -.->|"no key / offline"| mlkit
```

Full module/architecture detail: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) · [`TECHNICAL.md`](TECHNICAL.md).

---

## 4. How it maps to the judging criteria

- **Innovation & AI Creativity (30%)** — one Qwen-VL model does both vision *and* grounded price
  synthesis; an extensible `CategoryHandler` policy routes per object type; edge-cloud orchestration
  with **on-device ML Kit** as a real fallback, plus autonomous auto-scan and a follow-up ask loop.
- **Technical Depth & Engineering (30%)** — 15-module Now-in-Android architecture, Hilt DI,
  convention plugins, `StateFlow` UDF, defensive JSON parsing, **139+ unit/Compose tests** runnable
  on the JVM (Robolectric), CI.
- **Problem Value & Impact (25%)** — collapses "see something → understand/buy/track it" into one
  camera gesture; works offline; open-source and productizable.
- **Presentation & Documentation (15%)** — this doc, `README.md`, `docs/ARCHITECTURE.md`,
  `TECHNICAL.md`, `features.md`, and per-screen design specs.

---

## 5. New / significantly updated

LifeLens was **built new during the Submission Period** — the multi-module app, the Qwen-VL
integration, the EdgeAgent edge-cloud loop (ML Kit fallback, offline handling, auto-scan) were all
created within the window. Commit history in the repo documents the build.

---

## 6. Testing instructions

- Add a Qwen key: create `secrets.properties` at the repo root with
  `DASHSCOPE_API_KEY=sk-...` (and optionally `SEARCH_API_KEY=...`), **or** paste keys in the in-app
  **Settings** screen. See [`docs/API-KEYS.md`](docs/API-KEYS.md).
- Build/run: `./gradlew assembleDebug` then install, or open in Android Studio and run `app`.
- **No key needed to see the edge agent degrade gracefully:** with no key set, scanning falls back to
  on-device ML Kit identification, and the live viewfinder label works regardless.
- Run the test suite (JVM, no device): `./gradlew testDebugUnitTest :core:model:test`.

---

## 7. Links (to complete before final submission)

- **Demo video (< 3 min, public):** _TODO — YouTube / Vimeo / Youku link_
- **Blog / social post (optional bonus):** _TODO_
- **Devpost submission:** _TODO_
