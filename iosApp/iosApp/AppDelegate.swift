import UIKit
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        IosAppInitializer.shared.onCreate()

        return true
    }

    func applicationWillTerminate(_ application: UIApplication) {
        IosAppInitializer.shared.onTerminate()
    }
}
