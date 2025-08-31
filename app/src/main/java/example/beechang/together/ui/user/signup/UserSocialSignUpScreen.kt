package example.beechang.together.ui.user.signup

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.ui.component.button.TogeConfirmButton
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.text.TogeOutLineTextField
import example.beechang.together.ui.component.topbar.TogeSimpleBackTopBar
import example.beechang.together.ui.component.util.ClickShrinkEffect
import example.beechang.together.ui.user.UserNavDestination
import example.beechang.together.ui.utils.RemoteConfigKey
import example.beechang.together.ui.utils.RemoteConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import example.beechang.together.ui.component.dialog.TogeOnlyConfirmBtnDialog


@Composable
fun UserSocialSignUpRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {

    val socialSignUpViewModel: UserSocialSignUpViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val state by socialSignUpViewModel.uiState.collectAsStateWithLifecycle()
    var isShowErrorDialog by remember { mutableStateOf(false) }

    val remoteConfig = RemoteConfigManager

    LaunchedEffect(Unit) {
        socialSignUpViewModel.errorEffect.collect { error ->
            if (error is TogeError.DuplicatedId) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.input_id_duplicate),
                        duration = SnackbarDuration.Short
                    )
                }
            } else if (error is TogeError.NetworkError) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connection_problem),
                    duration = SnackbarDuration.Short
                )
            } else if (error is TogeError.NeedToAgreeTerms) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.need_to_agree_terms),
                    duration = SnackbarDuration.Short
                )
            } else if (error is TogeError.InvalidSocialSignupToken) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.error_signup_failed),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        socialSignUpViewModel.sideEffect.collect { result ->
            when (result) {
                UserSocialSignUpEffect.SuccessSignUp -> {
                    UserNavDestination.naviagetToWelcomeFromSocialSignup(
                        navController = navController,
                        nickname = state.nickname
                    )
                }

                UserSocialSignUpEffect.NeedToToken -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.error_login_failed),
                        duration = SnackbarDuration.Short
                    )
                    navController.navigateUp()
                }
            }
        }
    }

    UserSocialSignUpScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        isShowErrorDialog = isShowErrorDialog,
        /* EVENT */
        onEventNicknameChanged = { nickname ->
            socialSignUpViewModel.onEvent(UserSocialSignUpEvent.NicknameChanged(nickname))
        },
        onEventSubmitSignUp = {
            socialSignUpViewModel.onEvent(UserSocialSignUpEvent.SubmitSignUp)
        },
        onEventTermsAgreementChanged = { isChecked ->
            socialSignUpViewModel.onEvent(UserSocialSignUpEvent.TermsAgreementChanged(isChecked))
        },
        onEventPrivacyAgreementChanged = { isChecked ->
            socialSignUpViewModel.onEvent(UserSocialSignUpEvent.PrivacyAgreementChanged(isChecked))
        },
        onEventAllAgreementsChanged = { isChecked ->
            socialSignUpViewModel.onEvent(UserSocialSignUpEvent.AllAgreementsChanged(isChecked))
        },
        onEventDismissErrorDialog = {
            isShowErrorDialog = false
            navController.navigateUp()
        },
        /* NAVIGATION */
        onClickBack = { navController.navigateUp() },
        onClickPrivacyPolicy = {
            val url = remoteConfig.getValue(RemoteConfigKey.PRIVACY_POLICY)
            if (!url.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            }
        },
        onClickTermsOfService = {
            val url = remoteConfig.getValue(RemoteConfigKey.TERMS_OF_SERVICE)
            if (!url.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            }
        }
    )
}

@Composable
fun UserSocialSignUpScreen(
    modifier: Modifier = Modifier,
    /* STATE */
    state: UserSocialSignUpUiState = UserSocialSignUpUiState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    isShowErrorDialog: Boolean = false,
    /* EVENT */
    onEventNicknameChanged: (String) -> Unit = {},
    onEventSubmitSignUp: () -> Unit = {},
    onEventTermsAgreementChanged: (Boolean) -> Unit = {},
    onEventPrivacyAgreementChanged: (Boolean) -> Unit = {},
    onEventAllAgreementsChanged: (Boolean) -> Unit = {},
    onEventDismissErrorDialog: () -> Unit = {},
    /* NAVIGATION */
    onClickBack: () -> Unit = {},
    onClickPrivacyPolicy: () -> Unit = {},
    onClickTermsOfService: () -> Unit = {},
) {
    val localFocusManager = LocalFocusManager.current

    TogeOnlyConfirmBtnDialog(
        isShowDialog = isShowErrorDialog,
        title = stringResource(R.string.error),
        content = stringResource(R.string.error_signup_failed),
        confirmButtonText = stringResource(R.string.ok),
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        onConfirm = { onEventDismissErrorDialog() },
    )

    TogeScaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            },
        topBar = { TogeSimpleBackTopBar(onClickBack = onClickBack) },
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        isLoading = state.isLoading
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.signup_prompt),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp)) // nickname
            TogeOutLineTextField(
                value = state.nickname,
                onValueChange = { onEventNicknameChanged(it) },
                labelText = stringResource(R.string.nickname),
                placeholderText = stringResource(R.string.input_nickname),
                singleLine = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onImeAction = {},
                focusManager = localFocusManager,
                modifier = Modifier.fillMaxWidth()
            )

            // 약관동의
            Spacer(modifier = Modifier.height(16.dp))
            CustomOutlinedContainer(
                labelText = stringResource(R.string.agreement_section_title),
                modifier = Modifier.fillMaxWidth()
            ) {
                val allAgreed = state.termsAgreed && state.privacyAgreed

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEventAllAgreementsChanged(!allAgreed) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allAgreed,
                            onCheckedChange = onEventAllAgreementsChanged
                        )
                        Text(
                            text = stringResource(R.string.agreement_all),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    AgreementItemRow(
                        prefix = stringResource(R.string.agreement_prefix_required),
                        text = stringResource(R.string.agreement_privacy_policy_title),
                        isChecked = state.privacyAgreed,
                        onCheckedChange = onEventPrivacyAgreementChanged,
                        onClickDetail = { onClickPrivacyPolicy() },
                    )
                    AgreementItemRow(
                        prefix = stringResource(R.string.agreement_prefix_required),
                        text = stringResource(R.string.agreement_terms_of_service_title),
                        isChecked = state.termsAgreed,
                        onCheckedChange = onEventTermsAgreementChanged,
                        onClickDetail = { onClickTermsOfService() },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TogeConfirmButton(
                text = stringResource(R.string.signup),
                onClick = { onEventSubmitSignUp() },
                enabled = state.isSignUpEnabled && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CustomOutlinedContainer(
    labelText: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }

        Text(
            text = " $labelText ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(start = 16.dp)
                .background(color = MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
private fun AgreementItemRow(
    prefix: String,
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClickDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier,
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )

        Text(
            text = prefix,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.offset(y = (-0.5).dp)
        )

        Text(
            text = " $text",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        ClickShrinkEffect(
            onClick = { onClickDetail() }
        ) {
            AsyncImage(
                model = R.drawable.ic_arrow_right,
                contentDescription = "detail",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserSocialSignUpScreen() {
    UserSocialSignUpScreen(
        state = UserSocialSignUpUiState(
            isLoading = false,
            nickname = "TOGE_NICKNAME",
            isSignUpEnabled = true
        )
    )
}
