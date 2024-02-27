package moe.tlaster.precompose.molecule.sample

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import moe.tlaster.precompose.PreComposeAppController
import platform.Foundation.NSClassFromString
import platform.Foundation.NSLog
import platform.Foundation.NSUserActivity
import platform.UIKit.UIResponder
import platform.UIKit.UIResponderMeta
import platform.UIKit.UIScene
import platform.UIKit.UISceneConnectionOptions
import platform.UIKit.UISceneSession
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindowSceneDelegateProtocol
import platform.UIKit.UIWindowSceneDelegateProtocolMeta
import platform.UIKit.userActivity

@ExportObjCClass
@kotlinx.cinterop.BetaInteropApi
class PrecomposeSceneDelegate : UIResponder, UIWindowSceneDelegateProtocol {
    @OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun scene(
        scene: UIScene,
        willConnectToSession: UISceneSession,
        options: UISceneConnectionOptions
    ) {
        val windowScene = (scene as? UIWindowScene) ?: return

        val activity = (options.userActivities.firstOrNull()
            ?: scene.session.stateRestorationActivity) as? NSUserActivity

        val appController = PreComposeAppController(
            nsUserActivity = activity
        ) { App() }

        if (appController.isRestoredFromState) {
            windowScene.userActivity = activity
            windowScene.title = activity?.title.orEmpty()
        } else {
            NSLog("The provided activity type is null or did not recognised")
        }

        window = UIWindow(frame = UIScreen.mainScreen.bounds)
        window?.windowScene = windowScene
        window?.rootViewController = appController
        window?.makeKeyAndVisible()
    }

    override fun sceneWillResignActive(scene: UIScene) {
        val controller = (window?.rootViewController as? PreComposeAppController)
        scene.userActivity = controller?.willBecomeInactive()
    }

    override fun stateRestorationActivityForScene(scene: UIScene): NSUserActivity? =
        scene.userActivity

    companion object : UIResponderMeta(), UIWindowSceneDelegateProtocolMeta
}

fun PrecomposeSceneDelegateClass() = NSClassFromString("PreComposeSceneDelegate")
