package example.beechang.together.ui.component.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import example.beechang.together.R
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun CallingMoreBottomSheet(
    modifier: Modifier = Modifier,
    modalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    isShow: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onReportBadUser: () -> Unit = {},
    onReportErrorAndInquiry: () -> Unit = {},
) {
    if (isShow) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp

        val sheetWidth = remember(screenWidth) {
            when {
                screenWidth > 600.dp -> 400.dp
                else -> screenWidth * 0.9f
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetState = modalSheetState,
            sheetMaxWidth = sheetWidth,
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(vertical = 12.dp)
            ) {
                MoreMenuItem(
                    icon = R.drawable.ic_report,
                    text = stringResource(R.string.report_bad_user),
                    onClick = {
                        onReportBadUser()
                        onDismissRequest()
                    }
                )

                MoreMenuItem(
                    icon = R.drawable.ic_help,
                    text = stringResource(R.string.report_error_and_inquiry),
                    onClick = {
                        onReportErrorAndInquiry()
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = LocalTogeAppColor.current.white
        )

        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalTogeAppColor.current.white
        )
    }
}

@Preview
@Composable
fun PreviewCallingMoreBottomSheet() {
    CallingMoreBottomSheet(
        isShow = true
    )
}