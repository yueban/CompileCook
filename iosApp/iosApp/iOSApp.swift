import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            RootView(
                rootFactory: { appDelegate.createRootComponent() },
                backDispatcher: appDelegate.backDispatcher
            )
                .ignoresSafeArea()
                .onOpenURL { url in
                    guard url.scheme == "yueban", url.host == "compilecook" else {
                        print("Ignoring invalid url: \(url.absoluteString)")
                        return
                    }

                    IosDeepLinkHandler.shared.handle(url: url.absoluteString)
                }
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    private var stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)
    var backDispatcher: BackDispatcher = BackDispatcherKt.BackDispatcher()

    func createRootComponent() -> RootComponent {
        return DefaultRootComponent(
            componentContext: DefaultComponentContext(
                lifecycle: ApplicationLifecycle(),
                stateKeeper: stateKeeper,
                instanceKeeper: nil,
                backHandler: backDispatcher
            ),
            deepLinkUrl: nil
        )
    }

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        IosAppInitializer.shared.onCreate()

        return true
    }

    func application(_ application: UIApplication, shouldSaveSecureApplicationState coder: NSCoder) -> Bool {
        StateKeeperUtilsKt.save(coder: coder, state: stateKeeper.save())
        return true
    }

    func application(_ application: UIApplication, shouldRestoreSecureApplicationState coder: NSCoder) -> Bool {
        stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: StateKeeperUtilsKt.restore(coder: coder))
        return true
    }

    func applicationWillTerminate(_ application: UIApplication) {
        IosAppInitializer.shared.onTerminate()
    }
}
