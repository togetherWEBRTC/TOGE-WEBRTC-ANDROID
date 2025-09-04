package example.beechang.together.ui.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import example.beechang.together.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun rememberGoogleSignInManager(
    onSuccess: (idToken: String) -> Unit,
    onError: (e: Exception) -> Unit,
): GoogleSignInManager {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return remember(context, scope, onSuccess, onError) {
        GoogleSignInManager(context, scope, onSuccess, onError)
    }
}

class GoogleSignInManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onIdTokenReceived: (idToken: String) -> Unit,
    private val onError: (e: Exception) -> Unit,
) {
    private val credentialManager = CredentialManager.create(context)

    fun signIn() {
        scope.launch {
            requestWithGoogleIdOption(false)
        }
    }

    private suspend fun requestWithGoogleIdOption(filterByAuthorizedAccounts: Boolean) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            handleSignIn(result)
        } catch (e: NoCredentialException) {
            Log.e(
                "GoogleSignInManager",
                "No credential found for filter=$filterByAuthorizedAccounts. Trying next step. $e"
            )
        } catch (e: GetCredentialCancellationException) {
            Log.e(
                "GoogleSignInManager",
                "GetCredentialCancellationException : $e"
            )
        } catch (e: GetCredentialException) {
            Log.e("GoogleSignInManager", "GetCredentialException caught: ${e.message}", e)
            onError(e)
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                onIdTokenReceived(googleIdTokenCredential.idToken)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleSignInManager", "Google ID Token parsing failed.", e)
                onError(e)
            }
        } else {
            val errorMsg = "Unexpected credential type received: ${credential::class.java.name}"
            Log.e("GoogleSignInManager", errorMsg)
            onError(IllegalStateException(errorMsg))
        }
    }

    private fun generateNonce(): String = UUID.randomUUID().toString()
}