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
and to `api.pokemontcg.io` at runtime.

### Pokémon TCG API key (recommended)

The app works without any configuration, but pokemontcg.io's anonymous tier has a
low rate limit and returns 5xx errors under load rather than a clean "rate limited"
response — you'll see this if you browse several cards in quick succession. A free
key raises that limit substantially:

1. Get a key at [pokemontcg.io/signup](https://pokemontcg.io/signup) (no cost).
2. Add it to your local, untracked `local.properties` (create the file if Android
   Studio hasn't already):
   ```
   POKEMONTCG_API_KEY=your-key-here
   ```
3. Re-sync/rebuild — the key is picked up automatically via a generated `BuildConfig`
   field and sent as the `X-Api-Key` header on every request.

The app also retries a request up to twice on a 5xx before surfacing an error,
which smooths over occasional transient failures even without a key.
