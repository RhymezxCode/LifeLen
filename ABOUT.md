# About LifeLens

> **Point your camera at anything, know everything.**
> Visual intelligence for everyday life — your camera, now with a brain.

LifeLens is a real-time visual intelligence Android app. You point your phone at any object, and LifeLens tells you what it is and everything worth knowing about it — a full spec sheet and live price for a product, calories and macros for a meal, the species of a plant, the name of a landmark, the title of a book. One tap replaces a dozen searches.

---

## The Problem

We live in a visual world but search for it in text. Every day we run into things we want to understand *right now*:

- You see a **laptop** on a friend's desk and wonder what it costs and whether there's a better deal — so you squint at the lid for a model number, type a half-remembered name into a search box, then bounce between five shopping tabs comparing prices.
- You're served a **meal** and want to log its calories — so you guess the dish name, open a nutrition app, and hunt through a database of near-matches, none of which is quite your plate.
- You spot a **plant** on a hike, a **landmark** in a new city, a **book** on a shelf, a strange **gadget** in a drawer — and you have no words to search with, because you don't know what it is yet.

The common thread: **you have to translate what you see into words before a machine can help you.** That translation is slow, lossy, and frustrating. The information exists — it's just gated behind knowing the right query.

---

## The Solution

LifeLens removes the translation step. You show it the thing; it does the knowing.

**One camera tap → structured knowledge.** A single multimodal call to **Qwen-VL** turns a photo into a clean, structured answer:

- **Products** → identification + full spec sheet + current market price range + where to buy it cheapest, with live shopping links.
- **Food & meals** → the dish and its ingredients + calories + protein / carbs / fat macros + a portion estimate.
- **Everything else** → plants, animals, landmarks, books, gadgets, appliances, logos, and documents (with text extraction) get clear, contextual, general-knowledge answers.

Every scan is saved to a **searchable local history** with the captured image, the identification, any price or nutrition data, and a timestamp. You can favorite, re-open, share, and delete scans — so LifeLens becomes a personal, visual memory of the things you've encountered.

---

## Who It's For

| Persona | What they get from LifeLens |
|---|---|
| **Shoppers hunting the best price** | Instant identification and a live, grounded price range with the cheapest place to buy — no manual comparison shopping. |
| **Health-conscious eaters** | Point-and-log calorie and macro estimates for real plates, not database look-alikes. |
| **Curious learners** | Immediate answers about plants, animals, gadgets, and objects they can't name. |
| **Travelers** | On-the-spot recognition of landmarks, signs, menus, and documents, with text extraction. |

---

## Real-World Use Cases

### Shopping a laptop
You're in an electronics store and a laptop catches your eye. You point LifeLens at it. Qwen-VL identifies the model, LifeLens shows a spec sheet (CPU, RAM, display, weight), and a live search-grounding step returns a current price range plus a ranked list of the cheapest places to buy — each with a working link. You walk out knowing whether the in-store price is fair.

### Logging a meal
Lunch arrives. Before the first bite you scan the plate. LifeLens recognizes the dish and its main ingredients, estimates the portion, and returns calories with a protein / carbs / fat breakdown. It's saved to history, so your day's meals become a visual food log.

### Identifying a plant on a hike
A striking flower on the trail. One scan returns the species, whether it's toxic, and care or habitat notes. No keyword guessing — you didn't need to know its name to learn it.

### Naming a landmark while traveling
You round a corner in an unfamiliar city and face an old building. LifeLens names the landmark, gives its history and significance, and you keep exploring instead of stopping to search.

### Capturing a book
A recommendation on a shelf. Scan the cover: title, author, a short synopsis, and enough detail to add it to your reading list — captured in history for later.

---

## What Makes LifeLens Different

- **See-first, not search-first.** You never have to name the thing. The camera *is* the query.
- **Structured, not chatty.** Answers come back as clean spec sheets, nutrition panels, and price cards — not a wall of prose.
- **Grounded prices, not guesses.** Product prices are backed by a **live search** at scan time and synthesized by Qwen, so they reflect the market today rather than the model's training cutoff. Estimates carry honest disclaimers.
- **One brain, many domains.** The same Qwen-VL model routes across food, products, plants, landmarks, books, and documents — no separate app per category.
- **Extensible by design.** A `CategoryHandler` registry (see [ARCHITECTURE.md](docs/ARCHITECTURE.md)) means new object types — clothing, wine, art, medication — are additive, low-risk changes rather than rewrites.
- **Yours and searchable.** History lives on-device, so your scans are a private, growing library you can search, favorite, and revisit.

---

## Why Qwen-VL Is the Right Brain

LifeLens is only possible because one model can *both* see and reason:

- **True multimodal understanding.** Qwen-VL (`qwen-vl-max`, `qwen2.5-vl-72b-instruct`) reads a raw photo and produces structured understanding — object identity, category, fine-grained attributes, and even text within the image (OCR) — in a single call.
- **Category routing in one shot.** The same call decides whether it's looking at food, a product, or general knowledge and shapes its output accordingly (nutrition vs. specs vs. facts), which is exactly what our `CategoryHandler` strategy consumes.
- **Natural-language synthesis of search results.** After the live shopping search, Qwen fuses noisy listings into a coherent price range and a ranked set of buy options — the same model doing vision *and* language means no brittle glue between two systems.
- **Structured-output friendly.** With the right system prompt Qwen reliably emits JSON we can parse directly into domain models, keeping the app deterministic and testable.
- **Accessible and pluggable.** Through the DashScope **OpenAI-compatible** API, Qwen-VL drops into a standard Retrofit + OkHttp networking stack, so the integration is clean and swappable across Qwen model tiers.

In short: Qwen-VL is not a feature of LifeLens — it *is* the product. It turns "a picture" into "an answer."

---

## The Hackathon Framing

LifeLens is built for the **Qwen Code Hackathon** (deadline **July 9, 2026**). The challenge is to show what Qwen makes newly possible, and LifeLens answers directly: it puts Qwen-VL's multimodal reasoning at the center of an everyday, high-utility experience where a single model powers *both* the vision understanding and the language synthesis of live, grounded results. It's a focused, demoable, genuinely useful showcase of Qwen as an application's brain rather than a bolt-on.

See the delivery plan in **[plan.md](plan.md)**, the full feature catalog in **[features.md](features.md)**, and the technical deep-dive in **[TECHNICAL.md](TECHNICAL.md)**.
