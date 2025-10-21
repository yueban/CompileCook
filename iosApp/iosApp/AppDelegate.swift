import UIKit
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        #if DEBUG

        IosAppInitializer.shared.onCreate(debug: true)

        #else

        IosAppInitializer.shared.onCreate(debug: false)

        #endif

        return true
    }

    func applicationWillTerminate(_ application: UIApplication) {
        IosAppInitializer.shared.onTerminate()
    }
}
