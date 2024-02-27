package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import moe.tlaster.precompose.lifecycle.Lifecycle
import moe.tlaster.precompose.lifecycle.LifecycleOwner
import moe.tlaster.precompose.lifecycle.LifecycleRegistry
import moe.tlaster.precompose.lifecycle.LocalLifecycleOwner
import moe.tlaster.precompose.stateholder.LocalSavedStateHolder
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.SavedStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import moe.tlaster.precompose.ui.BackDispatcher
import moe.tlaster.precompose.ui.BackDispatcherOwner
import moe.tlaster.precompose.ui.LocalBackDispatcherOwner
import platform.Foundation.NSUserActivity
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillTerminateNotification
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(BetaInteropApi::class)
@Suppress("FunctionName")
fun PreComposeApplication(
    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
    nsUserActivity: NSUserActivity? = null,
    content: @Composable () -> Unit,
): UIViewController = PreComposeAppController(configure, nsUserActivity, content)

@Composable
fun ProvidePreComposeCompositionLocals(
    holder: PreComposeWindowHolder = remember {
        PreComposeWindowHolder()
    },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLifecycleOwner provides holder,
        LocalStateHolder provides holder.stateHolder,
        LocalBackDispatcherOwner provides holder,
        LocalSavedStateHolder provides holder.savedStateHolder,
    ) {
        content.invoke()
    }
}

@Composable
actual fun PreComposeApp(
    content: @Composable () -> Unit,
) {
    ProvidePreComposeCompositionLocals {
        content.invoke()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class AppStateHolder(
    private val lifecycle: LifecycleRegistry,
) : NSObject() {
    init {
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("appMovedToForeground:"),
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
        )
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("appMovedToBackground:"),
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
        )
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            selector = NSSelectorFromString("appWillTerminate:"),
            name = UIApplicationWillTerminateNotification,
            `object` = null,
        )
        lifecycle.updateState(Lifecycle.State.Active)
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun appMovedToForeground(notification: NSNotification) {
        lifecycle.updateState(Lifecycle.State.Active)
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun appMovedToBackground(notification: NSNotification) {
        lifecycle.updateState(Lifecycle.State.InActive)
    }

    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun appWillTerminate(notification: NSNotification) {
        lifecycle.updateState(Lifecycle.State.Destroyed)
    }
}

class PreComposeWindowHolder(
    restored: Map<String, List<Any?>>? = null
) : LifecycleOwner, BackDispatcherOwner {
    override val lifecycle by lazy {
        LifecycleRegistry()
    }

    val stateHolder by lazy {
        StateHolder()
    }

    val saveableStateRegistry = SaveableStateRegistry(restored) { true }

    val savedStateHolder = SavedStateHolder(
        "root",
        saveableStateRegistry,
    )

    override val backDispatcher by lazy {
        BackDispatcher()
    }
    private val holder = AppStateHolder(lifecycle)
}