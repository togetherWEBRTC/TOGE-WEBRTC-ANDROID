package example.beechang.together.ui.component.bottomsheet

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import example.beechang.together.R
import example.beechang.together.domain.model.InquiryCategory
import example.beechang.together.ui.component.text.TogeOutLineTextField
import example.beechang.together.ui.theme.LocalTogeAppColor
import example.beechang.together.ui.utils.getUiString

@Composable
fun InquiryBottomSheet(
    modifier: Modifier = Modifier,
    modalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    isShow: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onConfirmInquiry: (InquiryCategory, String/*content*/) -> Unit = { _, _ -> },
) {
    if (isShow) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            sheetState = modalSheetState
        ) {
            InquiryContent(
                onDismissRequest = onDismissRequest, onConfirmInquiry = onConfirmInquiry
            )
        }
    }
}

@Composable
private fun InquiryContent(
    onDismissRequest: () -> Unit,
    onConfirmInquiry: (InquiryCategory, String) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf(InquiryCategory.OTHER) }
    var content by remember { mutableStateOf("") }

    val categoryOptions = InquiryCategory.all()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState, modifier = Modifier
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }, contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.inquiry_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LocalTogeAppColor.current.white,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Column {
                var categoryExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TogeOutLineTextField(
                        value = selectedCategory.getUiString(context),
                        onValueChange = { },
                        readOnly = true,
                        labelText = stringResource(R.string.inquiry_category_label),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        customTrailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        categoryOptions.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.getUiString(context)) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                })
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        item(key = "content_text_field") {
            TogeOutLineTextField(
                value = content,
                labelText = stringResource(R.string.inquiry_content_label),
                onValueChange = { if (it.length <= 500) content = it },
                placeholderText = stringResource(R.string.inquiry_content_placeholder),
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines = 6,
                minLines = 6,
                singleLine = false,
                focusRequester = textFieldFocusRequester,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(300) // 키보드가 올라올 시간 대기
                                listState.animateScrollToItem(3) // 텍스트 필드 인덱스로 이동
                            }
                        }
                    })
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = stringResource(R.string.inquiry_notice),
                style = MaterialTheme.typography.bodySmall,
                color = LocalTogeAppColor.current.grey400,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalTogeAppColor.current.white),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.inquiry_cancel),
                        style = MaterialTheme.typography.titleSmall,
                        color = LocalTogeAppColor.current.black
                    )
                }

                Button(
                    onClick = {
                        if (content.isNotBlank()) {
                            onConfirmInquiry(selectedCategory, content)
                        }
                    },
                    enabled = content.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalTogeAppColor.current.primary500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.inquiry_submit),
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
fun PreviewInquiryBottomSheet() {
    InquiryContent(onDismissRequest = {}, onConfirmInquiry = { _, _ -> })
}