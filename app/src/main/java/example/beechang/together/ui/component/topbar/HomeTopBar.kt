package example.beechang.together.ui.component.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import example.beechang.together.R
import example.beechang.together.ui.TogetherApp
import example.beechang.together.ui.component.text.TogeClickableText


@Composable
fun HomeTopBar(
    modifier: Modifier,
    title: String,
    rightContent: @Composable () -> Unit
) {
    TogeBaseTopBar(
        modifier = modifier,
        leftContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        rightContent = {
            rightContent()
        },
        weightTriple = Triple(1f, 0.1f, 1f)
    )
}

@Preview(
    name = "HomeTopBar - Logged In",
    showBackground = true,
    widthDp = 360
)
@Composable
fun PreviewHomeTopBarLoggedIn() {
    TogetherApp {
        HomeTopBar(
            title = "Together",
            rightContent = {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
            },
            modifier = Modifier
        )
    }
}

@Preview(
    name = "HomeTopAppBar - Logged Out",
    showBackground = true,
    widthDp = 360
)
@Composable
fun PreviewHomeTopBarLoggedOut() {
    TogetherApp {
        HomeTopBar(
            title = "Together",
            rightContent = {
                TogeClickableText(
                    text = stringResource(R.string.login),
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { }
                )
            },
            modifier = Modifier
        )
    }
}
