package example.beechang.together.ui.component.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.windowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun TogeBaseTopBar(
    modifier: Modifier = Modifier,
    leftContent: @Composable () -> Unit = {},
    centerContent: @Composable () -> Unit = {},
    rightContent: @Composable () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    arrangement: Arrangement.Horizontal = Arrangement.SpaceAround,
    weightTriple: Triple<Float, Float, Float> = Triple(1f/*left*/, 1f/*center title*/, 1f/*right*/)
) {
    Column(
        modifier = modifier
           .fillMaxWidth()
           .background(backgroundColor)
    ) {
        Spacer( // Status bar space for edge-to-edge design
            modifier = Modifier
               .windowInsetsPadding(
                  windowInsets.only(WindowInsetsSides.Top)
               )
               .fillMaxWidth()
        )

        Row(
            modifier = Modifier
               .fillMaxWidth()
               .height(TopAppBarDefaults.MediumAppBarCollapsedHeight)
               .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = arrangement
        ) {
            // left content
            Box(
                modifier = Modifier.weight(weightTriple.first),
                contentAlignment = Alignment.CenterStart
            ) {
                leftContent()
            }

            // center content
            Box(
                modifier = Modifier.weight(weightTriple.second),
                contentAlignment = Alignment.Center
            ) {
                centerContent()
            }

            // right content
            Box(
                modifier = Modifier.weight(weightTriple.third),
                contentAlignment = Alignment.CenterEnd
            ) {
                rightContent()
            }
        }
    }
}
