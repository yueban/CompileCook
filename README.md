## 🛠 Development & Build

### 1. Generate Library Metadata
This project uses the `AboutLibraries` plugin to display open-source licenses. You **must** generate the library definitions before the first build or whenever you update `libs.versions.toml`.

```bash
./gradlew :composeApp:exportLibraryDefinitions
```

### 2. Running the Application
Choose the command for your target platform:

| Platform | Command |
| :--- | :--- |
| **Android** | `./gradlew :composeApp:installDebug` |
| **Desktop (JVM)** | `./gradlew :composeApp:run` |
| **Web (WasmJS)** | `./gradlew :composeApp:wasmJsBrowserRun` |
| **iOS** | Open `iosApp/iosApp.xcworkspace` in Xcode and run. |

> [!TIP]
> If you encounter a `MissingResourceException` related to `aboutlibraries.json`, ensure you have executed step 1 above.
