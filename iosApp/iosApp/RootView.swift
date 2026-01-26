import SwiftUI
import ComposeApp

struct RootView: UIViewControllerRepresentable {
    let rootFactory: () -> RootComponent
    let backDispatcher: BackDispatcher

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = RootViewControllerKt.RootViewController(rootFactory: rootFactory, backDispatcher: backDispatcher)
        controller.overrideUserInterfaceStyle = .light
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
