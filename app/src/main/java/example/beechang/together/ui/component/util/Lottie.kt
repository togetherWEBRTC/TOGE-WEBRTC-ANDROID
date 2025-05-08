package example.beechang.together.ui.component.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import example.beechang.together.R

@Composable
fun LottieLoading(
    lottieResId: Int = R.raw.loading,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(lottieResId)
    )

    LottieAnimation(
        composition = composition,
        iterations = iterations,
        modifier = modifier.size(60.dp)
    )
}

@Composable
fun LottieWelcome(
    lottieResId: Int = R.raw.welcome,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(lottieResId)
    )

    LottieAnimation(
        composition = composition,
        iterations = iterations,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}


@Preview
@Composable
fun PreviewLottieLoading() {
    LottieLoading()
}

@Preview(showBackground = true)
@Composable
fun PreviewLottieWelcome() {
    LottieWelcome()
}