# CompileCook Development Guide

## Build & Run Commands

### Multiplatform Build
- **Build all:** `./gradlew assemble`
- **Clean project:** `./gradlew clean`
- **Generate SQLDelight Code:** `./gradlew generateSqlDelightInterface`
- **Update Dependencies:** `./gradlew refreshVersions` (followed by manual edits to `versions.properties`)

### Platform-Specific Run
- **Android:** `./gradlew :androidApp:installDebug`
- **Desktop (JVM):** `./gradlew :composeApp:run`
- **Web (Wasm-JS):** `./gradlew :composeApp:wasmJsBrowserRun`
- **iOS:** Open `iosApp/iosApp.xcodeproj` in Xcode and run.

### Critical Setup
- **Export Library Metadata:** `./gradlew :composeApp:exportLibraryDefinitions` (Must be run for the "About" screen to work).

### Tests & Linting
- **Run All Tests:** `./gradlew test`
- **Run Detekt:** `./gradlew detekt` (Auto-correct is enabled).
- **Run Common Tests:** `./gradlew :composeApp:testCommonMain`

### Utility & Data Scripts
- **Clear JVM Data:** `./gradlew :composeApp:clearJvmData` (Clears local DB/cache for Desktop).
- **Parse Dishes:** `python3 scripts/parse_dishes.py`
- **Parse Tips:** `python3 scripts/parse_tips.py`

## Code Style & Architecture

### Architecture
- **Navigation:** **Decompose** (Component-based navigation, state preservation).
- **Dependency Injection:** **Koin** (Modules found in `di` packages).
- **Persistence:** **SQLDelight** (Definitions in `:data` module).
- **Networking:** **Ktor** (Client in `:data` module).
- **Logging:** **Napier** (Cross-platform logger).

### Project Structure
- `:base`: Core utilities, shared models, and basic components.
- `:data`: Raw data access (Local DB, Remote API).
- `:repo`: Repository layer (Business logic and data mapping).
- `:composeApp`: Shared UI implementation and entry points for JVM/Wasm/iOS.
- `:androidApp`: Android entry point and Android-specific resources.

### UI & Theming
- **Theming:** Use `AppTheme` which provides Material 3 `ColorScheme`, `Typography`, `Shapes`, and `ExtendedDimens`.
- **Custom Colors:** Use `AppTheme.colors` for project-specific colors (e.g., `difficultyStar`, `favorite`).
- **Typography:** Uses a custom font (`Noto Sans SC`) to support CJK characters, especially for Web.
- **Components:** Composables should accept a `Modifier` as the first optional parameter.
- **Images:** Coil 3 is used for image loading with a custom `coilPreviewHandler` for previews.

### Coding Standards
- **Indentation:** 2-space indentation.
- **Multiplatform Logic:** Keep as much logic as possible in `commonMain`.
- **State Management:** Use Decompose `Value` or `StateFlow` for observing state in UI.
- **Config:** Store constants like SDK versions and package names in `buildsrc.Configs`.
