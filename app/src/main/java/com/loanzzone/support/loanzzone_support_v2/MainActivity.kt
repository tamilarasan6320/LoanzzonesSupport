package com.loanzzone.support.loanzzone_support_v2

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.loanzzone.support.loanzzone_support_v2.ui.SupportHomeScreen
import com.loanzzone.support.loanzzone_support_v2.ui.theme.LoanzzonesSupportTheme

/**
 * [AppUpdateType.IMMEDIATE] — full-screen Play update when a newer store version exists.
 * Only works for installs from Google Play (not sideloaded APK).
 */
class MainActivity : AppCompatActivity() {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(applicationContext) }

    private val immediateUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            requestImmediatePlayUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestImmediatePlayUpdate()

        setContent {
            LoanzzonesSupportTheme(darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SupportHomeScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    immediateUpdateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                )
            }
        }
    }

    private fun requestImmediatePlayUpdate() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        immediateUpdateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    )
                }
            }
    }
}
