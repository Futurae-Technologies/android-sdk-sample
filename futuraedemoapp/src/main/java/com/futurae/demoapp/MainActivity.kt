package com.futurae.demoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.futurae.demoapp.arch.FuturaeViewModel
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.arch.ResultInformativeViewModel
import com.futurae.demoapp.migration.arch.MigrationViewModel
import com.futurae.demoapp.ui.shared.elements.authenticationconfirmationscreen.arch.AuthenticationViewModel
import com.futurae.demoapp.ui.shared.elements.topappbar.FuturaeAppBarViewModel
import com.futurae.demoapp.ui.theme.FuturaeTheme
import com.futurae.demoapp.utils.NotificationHelper
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.common.LockConfigurationType
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : FragmentActivity(), DefaultLifecycleObserver {

    private val futuraeViewModel: FuturaeViewModel by viewModels {
        FuturaeViewModel.provideFactory()
    }
    private val futuraeAppBarViewModel: FuturaeAppBarViewModel by viewModels()
    private val resultViewModel: ResultInformativeViewModel by viewModels()
    private val authenticationViewModel: AuthenticationViewModel by viewModels {
        AuthenticationViewModel.provideFactory()
    }
    private val pinProviderViewModel: PinProviderViewModel by viewModels()
    private val migrationViewModel: MigrationViewModel by viewModels {
        MigrationViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super<FragmentActivity>.onCreate(savedInstanceState)
        setContent {
            FuturaeDemoApp()
        }

        handleIntent(intent)

        // Observe here so we can collect even when app is in the background
        lifecycleScope.launch {
            futuraeViewModel.notifyUserFlow.collect {
                if (isAppInForeground()) {
                    // Let compose navigation graph handle it
                } else {
                    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
                        NotificationHelper.showNotification(this@MainActivity, it)
                    } else {
                        Timber.w("POST_NOTIFICATIONS not granted. Cannot display push notification")
                    }
                }
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun isAppInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    override fun onStop(owner: LifecycleOwner) {
        if (LocalStorage.persistedSDKConfig.lockConfigurationType != LockConfigurationType.NONE) {
            FuturaeSDK.client.lockApi.lock()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        //handle URI
        intent.dataString
            ?.takeIf { it.isNotBlank() }
            ?.let { uriCall ->
                futuraeViewModel.onNewURI(uriCall)
            }

        // handle notification pending intent extras
        intent.extras?.let { extras ->
            val keys = extras.keySet()
            if (keys.any { it == NotificationHelper.EXTRA_AUTH || it == NotificationHelper.EXTRA_UNENROLL }){
                // perform account refresh
                futuraeViewModel.fetchAccountsStatus()
            } else if (keys.any { it == NotificationHelper.EXTRA_QR }) {
                // navigate to QR
            }
        }
    }

    @Composable
    fun FuturaeDemoApp() {
        FuturaeTheme {
            FuturaeNavigationGraph(
                viewModel = futuraeViewModel,
                futuraeAppBarViewModel = futuraeAppBarViewModel,
                resultViewModel = resultViewModel,
                authenticationViewModel = authenticationViewModel,
                pinProviderViewModel = pinProviderViewModel,
                migrationViewModel = migrationViewModel,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}