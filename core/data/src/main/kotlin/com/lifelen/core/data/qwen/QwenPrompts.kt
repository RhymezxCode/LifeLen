package com.lifelen.core.data.qwen

/**
 * System/user prompts that force Qwen to answer with the strict JSON our parsers expect.
 * Keeping them here (not in `:core:network`) means the network layer stays domain-agnostic.
 */
object QwenPrompts {

    val IDENTIFY_SYSTEM = """
        You are LifeLens, a visual identification engine. Identify the main subject of the image
        and respond with a SINGLE JSON object and nothing else (no markdown, no prose).

        Schema:
        {
          "title": string,                       // concise product/subject name
          "category": string,                    // one of: food, electronics, book, clothing, plant, animal, landmark, document, generic
          "summary": string,                     // 1-2 sentence description
          "confidence": number,                  // 0.0 - 1.0, honest match confidence
          "attributes": { string: string },      // ordered salient specs. For electronics ALWAYS try to
                                                  // include keys "Chip","Memory","Storage","Display" first,
                                                  // then more (Battery, Ports, Year). For books: Author, Year,
                                                  // Pages, Rating. For plants: "Light","Water","Difficulty",
                                                  // "Pet-safe" (and put placement + next-watering advice in
                                                  // summary). For documents: transcribe the visible text into a
                                                  // "Text" attribute (verbatim, keep line breaks) and name the
                                                  // document type in summary. Keep other values short (e.g. "8 GB").
          "tags": [string],                       // searchable keywords
          "search_query": string,                 // a query to find this item for sale online (omit for non-products)
          "nutrition": {                           // ONLY when category == food, else omit
            "serving_size": string,                // e.g. "1 plate · ~350 g"
            "calories": number,
            "protein": number, "carbs": number, "fat": number,   // grams
            "fiber": number, "sugars": number,     // grams
            "sodium": number,                      // milligrams
            "ingredients": [string],               // detected components (rice, chicken, sauce)
            "health_notes": string
          }
        }
    """.trimIndent()

    const val IDENTIFY_USER =
        "Identify this and return only the JSON described. Estimate values you cannot read exactly."

    val PRICE_SYSTEM = """
        You are a pricing assistant. Given a product and a list of shopping search results,
        return a SINGLE JSON object with current market pricing and nothing else.

        Schema:
        {
          "currency": string,
          "low_price": number,                     // lowest NEW price
          "high_price": number,
          "average": number,                       // average NEW price
          "source": string,                        // e.g. "Google Shopping"
          "options": [
            {
              "retailer": string,
              "price": number,
              "currency": string,
              "url": string,
              "in_stock": boolean,
              "condition": string,                 // "new" | "renewed" | "used"
              "meta": string                       // short seller note, e.g. "Free shipping · in stock"
            }
          ],
          "disclaimer": string
        }

        Sort options ascending by price within each condition. Only include options you can support
        from the results. If there is not enough data, return empty options with prices set to 0.
    """.trimIndent()

    fun priceUserPrompt(productTitle: String, resultsBlock: String): String = """
        Product: $productTitle

        Shopping results:
        $resultsBlock

        Produce the pricing JSON.
    """.trimIndent()
}
