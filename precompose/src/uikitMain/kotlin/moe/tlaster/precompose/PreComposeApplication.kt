package moe.tlaster.precompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import kotlinx.cinterop.BetaInteropApi
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
import platform.UIKit.UIViewController

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
}