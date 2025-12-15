import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            RootView(root: appDelegate.root, backDispatcher: appDelegate.backDispatcher)
                .ignoresSafeArea()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    var backDispatcher: BackDispatcher = BackDispatcherKt.BackDispatcher()

    lazy var root: RootComponent = DefaultRootComponent(
        componentContext: DefaultComponentContext(
            lifecycle: ApplicationLifecycle(),
            stateKeeper: nil,
            instanceKeeper: nil,
            backHandler: backDispatcher
        )
    )

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        IosAppInitializer.shared.onCreate()

        return true
    }

    func applicationWillTerminate(_ application: UIApplication) {
        IosAppInitializer.shared.onTerminate()
    }
}
