package com.luna.cycle

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.luna.cycle.ui.CycleViewModel
import com.luna.cycle.ui.LunaApp
import java.util.Locale

class MainActivity : FragmentActivity() {
    private val viewModel: CycleViewModel by viewModels {
        CycleViewModel.factory((application as LunaApplication).repository)
    }
    private var lockPromptShown = false

    override fun attachBaseContext(newBase: Context) {
        val language = newBase.getSharedPreferences("luna_settings", MODE_PRIVATE)
            .getString("app_language", "en") ?: "en"
        val locale = Locale.forLanguageTag(language)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        Locale.setDefault(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LunaApp(viewModel) }
        showBiometricLockIfEnabled()
    }

    override fun onStart() {
        super.onStart()
        showBiometricLockIfEnabled()
    }

    override fun onStop() {
        lockPromptShown = false
        super.onStop()
    }

    private fun showBiometricLockIfEnabled() {
        if (lockPromptShown || !getSharedPreferences("luna_settings", MODE_PRIVATE)
                .getBoolean("biometric_lock", false)
        ) return

        val availability = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            finish()
            return
        }

        lockPromptShown = true
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    finish()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_title))
                .setSubtitle(getString(R.string.biometric_subtitle))
                .setNegativeButtonText(getString(R.string.biometric_cancel))
                .build()
        )
    }
}
