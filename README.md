# PoGo TCG Helper

An Android app for Pokémon Trading Card Game players: search the full card catalog,
check current market prices, and track your personal collection.

## Features

- **Card search** — look up any Pokémon TCG card by name via the free, open-source
  [TCGdex](https://tcgdex.dev) API, browsed as a grid of card art. Sort alphabetically
  ascending/descending (server-side), filter to cards you already own or don't
  own yet across any collection (local, no extra network calls), and filter by
  rarity (server-side, using TCGdex's own list of recognized rarities).
- **Card detail** — full artwork, HP/types, attacks, weaknesses/resistances, and
  current market prices from both TCGplayer (USD) and Cardmarket (EUR).
- **Multiple collections** — organize owned cards into as many named collections as
  you like (e.g. "Charizards", "Trade binder"), each tracked independently with its
  own card list and total value. A card can be added to more than one collection at
  once, each with its own quantity. Each collection displays as a 2-column grid;
  ownership is shown three ways at once (desaturated art, a check-vs-add icon, and a
  text label) so it never depends on color alone.
- **Set trackers** — pick any real TCG set to bulk-create a completion checklist for
  it, separate from your real collections: nothing in a tracker counts as owned
  anywhere else in the app (search's owned filter, collection totals, etc.). Choose
  the master set (every card, including secret rares) or just the base set (cards
  numbered within the set's official printed count). Tick a card off as you get it
  to watch your progress ("142 / 198 collected"), and optionally tap it to actually
  add that card into one of your real collections whenever you're ready to.
- **Scan to look up** — point the camera at a card and tap to scan. There's no free
  image-recognition API that identifies a specific Pokémon card from a photo, so this
  reads the card's printed name with on-device OCR (ML Kit, offline, no account) and
  runs it through the normal search, with the recognized text shown and editable in
  case OCR misreads it (glare, italic fonts, etc. can throw it off).

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Coroutines + Flow for async data and reactive state
- Retrofit + OkHttp + kotlinx.serialization for networking
- Room for local persistence
- Navigation Compose
- Coil for image loading
- CameraX + ML Kit on-device text recognition for card scanning
- Manual dependency injection (`AppContainer`) — no DI framework, kept intentionally simple

## Project structure

```
app/src/main/java/com/pogotcghelper/app/
├── data/
│   ├── network/      # Retrofit API + DTOs for the TCGdex API
│   ├── local/         # Room database/entities for the owned-card collection
│   └── repository/    # CardRepository, CollectionRepository, DTO→domain mapping
├── domain/model/       # Plain Kotlin domain models (Card, OwnedCard, Collection, ...)
├── di/                 # AppContainer: manual dependency wiring
├── ocr/                 # Camera-frame -> ML Kit text recognition bridge
└── ui/
    ├── search/         # Card search/browse screen
    ├── scan/            # Camera capture + recognized-text review/search screen
    ├── detail/         # Card detail screen (attacks, prices, add-to-collection picker)
    ├── collection/      # Collections list + per-collection card list screens
    ├── navigation/      # NavHost + bottom navigation
    └── theme/           # Material 3 theme
```

## Building

Open the project in Android Studio (Ladybug or newer) and run it, or from the CLI:

```
./gradlew assembleDebug
```

Requires network access to Google's Maven repository (for AndroidX/Compose/Room)
and to `api.tcgdex.net` at runtime. No account, API key, or configuration is
required — [TCGdex](https://tcgdex.dev) is free and open-source. The app also
retries a request up to twice on a 5xx before surfacing an error, as insurance
against any transient failure.

> **Note:** the app used to run against pokemontcg.io, but that service's new
> account signups are now closed (it's being folded into a paid successor,
> Scrydex) and its anonymous tier was returning frequent 5xx errors under load.
> TCGdex was substituted as a free, actively-maintained alternative. Its exact
> JSON schema couldn't be verified end-to-end against this sandbox's network
> restrictions, so if you notice a missing field (e.g. a blank price or image)
> that's likely a mismapped field name — flag it and it can be corrected.
