# CompileCook Development Guide

## Build & Run Commands

- **Build all:** `./gradlew assemble`
- **Clean:** `./gradlew clean`
- **Generate SQLDelight:** `./gradlew generateSqlDelightInterface`
- **Android:** `./gradlew :androidApp:installDebug`
- **Desktop:** `./gradlew :composeApp:run`
- **Web:** `./gradlew :composeApp:wasmJsBrowserRun`
- **iOS:** Open `iosApp/iosApp.xcodeproj` in Xcode
- **Export Library Metadata:** `./gradlew :composeApp:exportLibraryDefinitions` (required for About screen)
- **All Tests:** `./gradlew test`
- **Data Tests:** `./gradlew :data:jvmTest`
- **Detekt:** `./gradlew detekt` (auto-correct enabled)
- **Clear JVM Data:** `./gradlew :composeApp:clearJvmData`

## Module Structure

```
androidApp --> composeApp --> repo --> data --> base
                                |              |
                                +--> base -----+
```

- **`base`**: Core utilities, logging (Napier), JSON serialization, coroutine DI, time/enum helpers.
- **`data`**: SQLDelight local DB, Ktor HTTP client, OpenAI client. Raw data access only.
- **`repo`**: Business logic, entity mapping (local <-> domain), auto-sync on startup.
- **`composeApp`**: Decompose navigation components, Compose UI, DI modules, platform entry points.
- **`androidApp`**: Thin shell — only manifest and string resources.

## Architecture

### Layered Data Flow
```
Remote API -> RemoteDataSource -> toLocalEntity() -> SQLDelight DB
                                                          |
                                       Repo (toDomainModel())
                                                          |
                                       Component (UiStateComponentImpl)
                                                          |
                                       StateFlow<S> / Async<T> -> Composable UI
```

- **Read**: UI observes `Flow` from local DB, repo maps to domain models.
- **Write**: Remote fetch -> map -> bulk insert. UI picks up via Flow.
- **AI Chat**: Insert user msg + assistant placeholder -> stream tokens -> update assistant msg in DB -> UI observes via Flow.

### SQLDelight
`.sq` files in `data/src/commonMain/sqldelight/.../db/entity/`:
- **`Dish.sq`**: `DishLocalEntity` + `DishFavoriteLocalEntity`
- **`Tip.sq`**: `TipLocalEntity` + `TipFavoriteLocalEntity`
- **`AiChat.sq`**: `AiChatConversationLocalEntity` + `AiChatMessageLocalEntity`

Domain entities in `repo/entity/` with bidirectional mapping extensions.

### Data Sync
`updateDishes()`/`updateTips()` use full-replace in a single transaction: prune stale favorites -> delete all -> insert all. Batch `DELETE ... WHERE name IN ?` for favorites pruning.

### DI (Koin) — Two-Phase Init
**Sync** (in `AppInitializer.init()`):
- `coroutineModule` (base) — dispatchers, scope
- `initialAppModule` — `AppInitializerSignal`, `DeepLinkHandler`
- `databaseModule` (repo) — driver, DB, queries, local data sources
- `appModule` — `MessageService`
- `uiModule` — Decompose component factories

**Async** (via `loadDataModules()`, UI gated behind `AppInitializerSignal.isReady`):
- `repoModule` — repositories
- `remoteDataSourceModule` — Ktor HTTP client, remote data sources

### Navigation (Decompose)
- `RootComponent` — main `childStack` + AI `childSlot`
- `MainComponent` — tab navigation (DISHES / TIPS) via `bringToFront`
- Detail screens: `DishListComponent`, `DishComponent`, `TipComponent`, `AboutComponent`
- AI layer: `AiComponent` -> `AiChatComponent` + `AiChatListComponent`
- Output markers: `BackOutput`, `ToggleAiDrawerOutput` for universal handling
- Web: `childStackWebNavigation` + `PathMapper` for browser URL sync
- Deep links: `DeepLinkHandler` (Android scheme: `yueban://compilecook/...`)

### UI Patterns
- `BaseComponent` — `ComponentContext by componentContext` + `KoinComponent`, `componentScope` tied to lifecycle.
- `UiStateComponentImpl<S>` — `uiState: StateFlow<S>`, `setState {}`, `execute()` for `Async<T>`, state persistence via `StateKeeper` with custom JSON.
- `Async<T>` — `Uninitialized`, `Loading`, `Success`, `Fail`. `AsyncContent` composable handles all states.
- `AppTheme` — Material 3 + `ExtendedColorScheme` + `ExtendedDimens` + `Noto Sans SC` font.
- `SmartMatcher` — pinyin-aware search with consonant anchoring.
- AI drawer — responsive: overlay / side-by-side fixed / side-by-side draggable.

## Coding Standards
- 2-space indentation.
- Keep logic in `commonMain`.
- Composables accept `Modifier` as first optional parameter.
- Use `AppTheme.colors` for custom colors (`difficultyStar`, `favorite`).
- Constants in `buildsrc.Configs`.
- Logging: `Logger.d()`, `Logger.e()` (Napier wrapper).

## Key Gotchas
1. **Two-phase DI**: Repos and remote sources unavailable until async `loadDataModules()` completes.
2. **`@Transient` on non-serializable state fields**: Only serializable fields survive process death.
3. **StateKeeper JSON bypass**: Uses project's own `Json` via `consumeSafe`/`registerSafe`, not Essenty's default.
4. **Full-replace sync**: `updateDishes()`/`updateTips()` delete all then re-insert. Tests must pass all data in one call, not sequential calls.
5. **Wasm-JS single-threaded**: `DbTransactionLock` is no-op. `resolveBaseUrl()` returns relative paths on localhost for webpack proxy.
6. **`selectLastInsertRowId`**: Must be called in same transaction as insert.
7. **AI `chat()` inserts both user msg and assistant placeholder** before streaming.
8. **`DishRepoImpl` auto-syncs** dishes/tips in global scope on construction.
