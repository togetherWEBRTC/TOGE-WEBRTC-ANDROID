package example.beechang.together.ui.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.core.content.edit


enum class PermissionHandlerStatus {
    GRANTED,
    SHOULD_REQUEST,
    SHOULD_GO_TO_SETTINGS
}

@OptIn(ExperimentalPermissionsApi::class)
data class PermissionData(
    val status: PermissionHandlerStatus,
    val permissionState: PermissionState,
    val rejectionCount: Int
)

@OptIn(ExperimentalPermissionsApi::class)
class MultiPermissionHandler(
    val permissionsData: Map<String, PermissionData>,
    val areAllPermissionsGranted: Boolean
) {
    lateinit var context: Context
    lateinit var sharedPref: SharedPreferences
    lateinit var settingsLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>

    fun requestPermission(
        permission: String,
        isMoveToSettings: Boolean = true,
        onNeedToSettings: () -> Unit = { }
    ) {
        val permissionData = permissionsData[permission] ?: return
        val permissionState = permissionData.permissionState

        if (!permissionState.status.isGranted) {
            val currentCount = permissionData.rejectionCount
            val newCount = currentCount + 1

            sharedPref.edit { putInt("rejection_count_$permission", newCount) }
            permissionState.launchPermissionRequest()
            if (newCount >= 2) {
                if (isMoveToSettings) {
                    navigateToSettings()
                }
                onNeedToSettings()
            }
        }
    }

    fun requestAllPermissions(
        isMoveToSettings: Boolean = false,
        onNeedToSettings: () -> Unit = {}
    ) {
        permissionsData.keys.forEach { permission ->
            if (permissionsData[permission]?.status == PermissionHandlerStatus.SHOULD_REQUEST) {
                requestPermission(permission , isMoveToSettings , onNeedToSettings)
            }
        }
    }

    fun navigateToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        settingsLauncher.launch(intent)
    }

    fun initialize(
        contextParam: Context,
        sharedPrefParam: SharedPreferences,
        settingsLauncherParam: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {
        context = contextParam
        sharedPref = sharedPrefParam
        settingsLauncher = settingsLauncherParam
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberMultiPermissionHandler(permissions: List<String>): MultiPermissionHandler {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    val rejectionCounts = remember {
        mutableStateMapOf<String, Int>().apply {
            permissions.forEach { permission ->
                this[permission] = sharedPref.getInt("rejection_count_$permission", 0)
            }
        }
    }

    val permissionsDataMap = mutableMapOf<String, PermissionData>()

    permissions.forEach { permission ->
        val permissionState = rememberPermissionState(permission = permission)
        val rejectionCount = rejectionCounts[permission] ?: 0

        if (permissionState.status.isGranted && rejectionCount > 0) {
            rejectionCounts[permission] = 0
            sharedPref.edit { putInt("rejection_count_$permission", 0) }
        }

        val status = when {
            permissionState.status.isGranted -> PermissionHandlerStatus.GRANTED
            rejectionCount >= 2 -> PermissionHandlerStatus.SHOULD_GO_TO_SETTINGS
            else -> PermissionHandlerStatus.SHOULD_REQUEST
        }

        permissionsDataMap[permission] = PermissionData(
            status = status,
            permissionState = permissionState,
            rejectionCount = rejectionCount
        )
    }

    val areAllPermissionsGranted = permissionsDataMap.all {
        it.value.status == PermissionHandlerStatus.GRANTED
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    val handler = MultiPermissionHandler(
        permissionsData = permissionsDataMap,
        areAllPermissionsGranted = areAllPermissionsGranted
    )

    handler.initialize(
        context,
        sharedPref,
        settingsLauncher
    )

    return handler
}