package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import moe.tlaster.precompose.lifecycle.Lifecycle
import platform.Foundation.NSLog
import platform.Foundation.NSUserActivity
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIRectEdgeLeft
import platform.UIKit.UIScreenEdgePanGestureRecognizer
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.userActivity
import platform.objc.sel_registerName

private const val PRECOMPOSE_ACTIVITY_TYPE = "moe.tlaster.precompose.state.activity"

/**
* Developers should call didBecomeActive, willBecomeInactive, didBecomeDestroyed events manually
* while using the controller directly in order to support correct Lifecycle events handle and state restoration
* example: {@see sample PrecomposeSceneDelegate.kt}
 */
@OptIn(ExperimentalForeignApi::class)
@kotlinx.cinterop.BetaInteropApi
@ExportObjCClass
class PreComposeAppController(
    private val configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    nsUserActivity: NSUserActivity? = null,
    private val content: @Composable () -> Unit
) : UIViewController(null, null) {
    private val windowHolder: PreComposeWindowHolder
    private var _isRestoredFromState = false

    val isRestoredFromState get() = _isRestoredFromState
    init {
        var restoredData = emptyMap<String, List<Any?>>()

        @Suppress("UNCHECKED_CAST")
        if (nsUserActivity?.activityType == PRECOMPOSE_ACTIVITY_TYPE) {
            (nsUserActivity.userInfo as? Map<String, List<Any?>>)?.let { data ->
                restoredData = data
            }
                ?: NSLog("The provided nsUserActivity.userInfo must conform to Map<String, List<Any?>>")

            _isRestoredFromState = true
        }

        windowHolder = PreComposeWindowHolder(restored = restoredData.toMap())
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        val leftSwipeRecognizer = UIScreenEdgePanGestureRecognizer(
            target = this,
            action = sel_registerName("${::handleEdgePanGesture.name}:")
        ).apply {
            edges = UIRectEdgeLeft
        }

        view.addGestureRecognizer(leftSwipeRecognizer)

        windowHolder.lifecycle.currentState = Lifecycle.State.Active
        val composeVC = ComposeUIViewController(configure) {
            ProvidePreComposeCompositionLocals(
                windowHolder,
            ) {
                content.invoke()
            }
        }

        composeVC.view.setFrame(view.bounds)
        composeVC.view.autoresizingMask =
            UIViewAutoresizingFlexibleHeight or UIViewAutoresizingFlexibleWidth

        view.addSubview(composeVC.view)

        addChildViewController(composeVC)
        composeVC.didMoveToParentViewController(this)
    }

    fun didBecomeActive() {
        // Active state has already been set upon viewDidLoad, so avoid events duplication
        if (windowHolder.lifecycle.currentState != Lifecycle.State.Active) {
            windowHolder.lifecycle.currentState = Lifecycle.State.Active
        }
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        val windowScene = view.window?.windowScene
        if (windowScene?.userActivity?.activityType == PRECOMPOSE_ACTIVITY_TYPE) {
            windowScene.userActivity = null
        }
    }

    @ObjCAction
    fun handleEdgePanGesture(gesture: UIScreenEdgePanGestureRecognizer) {
        if (gesture.state == UIGestureRecognizerStateEnded) {
            windowHolder.backDispatcher.onBackPress()
        }
    }

    fun willBecomeInactive(): NSUserActivity {
        windowHolder.lifecycle.currentState = Lifecycle.State.InActive

        val activity = NSUserActivity(PRECOMPOSE_ACTIVITY_TYPE).apply {
            title = "PreCompose scene restoration activity"
        }

        val saved = windowHolder.saveableStateRegistry.performSave()
        activity.addUserInfoEntriesFromDictionary(saved.toMap())
        NSLog("saved $saved")

        return activity
    }

    fun didBecomeDestroyed() {
        windowHolder.lifecycle.currentState = Lifecycle.State.Destroyed
    }
}