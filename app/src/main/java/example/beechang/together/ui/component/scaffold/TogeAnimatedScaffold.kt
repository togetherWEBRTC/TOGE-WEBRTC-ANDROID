package example.beechang.together.ui.component.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import example.beechang.together.ui.component.util.LottieLoading
import kotlinx.coroutines.delay


@Composable
fun AnimatedTogeScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    windowInsets: WindowInsets = WindowInsets.safeDrawing,
    autoHideDelay: Long = 3000L,
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    var barsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(barsVisible) {
        if (barsVisible) {
            delay(autoHideDelay)
            barsVisible = false
        }
    }

    Scaffold(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                barsVisible = !barsVisible
            },
        contentWindowInsets = windowInsets,
        topBar = {
            AnimatedVisibility(
                visible = barsVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    topBar()
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = barsVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    bottomBar()
                }
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor
    ) { innerPadding ->

        val targetTop = innerPadding.calculateTopPadding()
        val targetBottom = innerPadding.calculateBottomPadding()

        val animatedTop by animateDpAsState(
            targetValue = targetTop,
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
        )
        val animatedBottom by animateDpAsState(
            targetValue = targetBottom,
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = animatedBottom)
//                .padding(top = animatedTop, bottom = animatedBottom)
        ) {
            content()
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieLoading()
                }
            }
        }
    }
}