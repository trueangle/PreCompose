package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun dialogProperties(
    usePlatformDefaultWidth: Boolean,
    usePlatformInsets: Boolean,
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside
)

@Composable
internal actual fun PreComposeDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest, properties, content)
}