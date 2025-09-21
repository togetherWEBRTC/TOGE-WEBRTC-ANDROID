package example.beechang.together.ui.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import example.beechang.together.ui.component.text.TogeOutLineTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import example.beechang.together.BuildConfig
import example.beechang.together.R
import example.beechang.together.domain.model.ReportReason
import example.beechang.together.ui.component.util.CircularImage
import example.beechang.together.ui.theme.LocalTogeAppColor

data class ReportUserInfoUi(
    val userId: String,
    val name: String,
    val profileUrl: String,
) {
    fun getProfileFullUrl(): String {
        return BuildConfig.RES_URL + profileUrl
    }
}

@Composable
fun ReportUserBottomSheet(
    modifier: Modifier = Modifier,
    modalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    isShow: Boolean = false,
    participants: List<ReportUserInfoUi> = listOf(),
    preSelectedUserId: String? = null,
    onDismissRequest: () -> Unit = {},
    onConfirmReport: (String/*reported user id*/, ReportReason/* reason */, String/* discription */) -> Unit = { _, _, _ -> },
) {
    if (isShow) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            sheetState = modalSheetState
        ) {
            ReportUserContent(
                participants = participants,
                preSelectedUserId = preSelectedUserId,
                onDismissRequest = onDismissRequest,
                onConfirmReport = onConfirmReport
            )
        }
    }
}

@Composable
private fun ReportUserContent(
    participants: List<ReportUserInfoUi>,
    preSelectedUserId: String?,
    onDismissRequest: () -> Unit,
    onConfirmReport: (String, ReportReason, String) -> Unit,
) {
    var selectedUserId by remember(preSelectedUserId) {
        mutableStateOf(preSelectedUserId ?: participants.firstOrNull()?.userId ?: "")
    }
    var selectedReason by remember { mutableStateOf(ReportReason.OTHER) }
    var description by remember { mutableStateOf("") }

    val reasonOptions = ReportReason.all()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.report_user_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LocalTogeAppColor.current.white,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {
            Column {
                var userExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = userExpanded,
                    onExpandedChange = { userExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TogeOutLineTextField(
                        value = participants.find { it.userId == selectedUserId }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        labelText = stringResource(R.string.report_target_label),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        customTrailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = userExpanded,
                        onDismissRequest = { userExpanded = false }
                    ) {
                        participants.forEach { participant ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularImage(
                                            imageUrl = participant.getProfileFullUrl(),
                                            size = 32.dp,
                                            borderWidth = 0.4.dp,
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = participant.name)
                                    }
                                },
                                onClick = {
                                    selectedUserId = participant.userId
                                    userExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Column {
                var reasonExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = reasonExpanded,
                    onExpandedChange = { reasonExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TogeOutLineTextField(
                        value = selectedReason.displayName,
                        onValueChange = { },
                        readOnly = true,
                        labelText = stringResource(R.string.report_reason_label),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        customTrailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = reasonExpanded,
                        onDismissRequest = { reasonExpanded = false },
                    ) {
                        reasonOptions.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason.displayName) },
                                onClick = {
                                    selectedReason = reason
                                    reasonExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            TogeOutLineTextField(
                value = description,
                labelText = stringResource(R.string.report_description_label),
                onValueChange = { if (it.length <= 300) description = it },
                placeholderText = stringResource(R.string.report_description_placeholder),
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                minLines = 4,
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalTogeAppColor.current.white
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.report_cancel),
                        style = MaterialTheme.typography.titleSmall,
                        color = LocalTogeAppColor.current.black
                    )
                }

                Button(
                    onClick = {
                        if (selectedUserId.isNotEmpty()) {
                            onConfirmReport(selectedUserId, selectedReason, description)
                        }
                    },
                    enabled = selectedUserId.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalTogeAppColor.current.primary500
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.report_submit),
                        style = MaterialTheme.typography.titleSmall,
                        color = LocalTogeAppColor.current.white
                    )
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFFE8E8E8, showBackground = true)
@Composable
fun PrevReportUserBottomSheet() {
    ReportUserContent(
        participants = listOf(
            ReportUserInfoUi(
                userId = "user1",
                name = "USER NAME",
                profileUrl = ""
            )
        ),
        preSelectedUserId = null,
        onDismissRequest = {},
        onConfirmReport = { _, _, _ -> }
    )
}