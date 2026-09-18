# PoGo TCG Helper

An Android app for Pokémon Trading Card Game players: search the full card catalog,
check current market prices, and track your personal collection.

## Features

- **Card search** — look up any Pokémon TCG card by name via the free, open-source
  [TCGdex](https://tcgdex.dev) API, browsed as a grid of card art.
- **Card detail** — full artwork, HP/types, attacks, weaknesses/resistances, and
  current market prices from both TCGplayer (USD) and Cardmarket (EUR).
- **Collection tracker** — add cards you own with a tap, track quantities, and see
  your collection's total estimated value, persisted locally with Room.

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Coroutines + Flow for async data and reactive state
- Retrofit + OkHttp + kotlinx.serialization for networking
- Room for local persistence
- Navigation Compose
- Coil for image loading
- Manual dependency injection (`AppContainer`) — no DI framework, kept intentionally simple

## Project structure

```
app/src/main/java/com/pogotcghelper/app/
├── data/
│   ├── network/      # Retrofit API + DTOs for the TCGdex API
│   ├── local/         # Room database/entities for the owned-card collection
│   └── repository/    # CardRepository, CollectionRepository, DTO→domain mapping
├── domain/model/       # Plain Kotlin domain models (Card, OwnedCard, Attack, ...)
├── di/                 # AppContainer: manual dependency wiring
└── ui/
    ├── search/         # Card search/browse screen
    ├── detail/         # Card detail screen (attacks, prices, add to collection)
    ├── collection/      # Collection tracker screen
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
