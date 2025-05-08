package example.beechang.together.ui.component.snackbar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun TogeSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    textColor: Color = LocalTogeAppColor.current.white,
    actionColor: Color = LocalTogeAppColor.current.secondary500,
    dismissActionColor: Color = LocalTogeAppColor.current.white,
    backgroundColor: Color = LocalTogeAppColor.current.grey800
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = backgroundColor,
                contentColor = textColor,
                actionColor = actionColor,
                dismissActionContentColor = dismissActionColor,
                shape = RoundedCornerShape(12.dp)
            )
        }
    )
}