package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/**
 * @param usePlatformInsets in commonMain results in a compilation error for androidMain target.
 * Because there is no consistency in params of DialogProperties between commonMain/compose.ui.window.DialogProperties
 * and androidMain/compose.ui.window.DialogProperties.
 * To workaround this limitation, we use expect/actual api with custom android impl
 */
internal expect fun dialogProperties(
    usePlatformDefaultWidth: Boolean = false,
    usePlatformInsets: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
): DialogProperties

@Composable
internal expect fun PreComposeDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = dialogProperties(),
    content: @Composable () -> Unit
)