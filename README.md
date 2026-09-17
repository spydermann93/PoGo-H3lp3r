# PoGo TCG Helper

An Android app for Pokémon Trading Card Game players: search the full card catalog,
check current market prices, and track your personal collection.

## Features

- **Card search** — look up any Pokémon TCG card by name via the public
  [pokemontcg.io](https://pokemontcg.io) API, browsed as a grid of card art.
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
│   ├── network/      # Retrofit API + DTOs for pokemontcg.io
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
and to `api.pokemontcg.io` at runtime. No API key is required for the public
Pokémon TCG API at low request volumes.

> **Note:** this project was scaffolded in a sandboxed environment without access
> to the Android SDK or Google's Maven repository, so the Gradle build has not
> been verified end-to-end here. Run `./gradlew assembleDebug` locally or in CI
> to build and catch any issues.
