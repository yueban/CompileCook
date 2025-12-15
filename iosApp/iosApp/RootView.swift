import SwiftUI
import ComposeApp

struct RootView: UIViewControllerRepresentable {
    let root: RootComponent
    let backDispatcher: BackDispatcher

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = RootViewControllerKt.RootViewController(root: root, backDispatcher: backDispatcher)
        controller.overrideUserInterfaceStyle = .light
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
