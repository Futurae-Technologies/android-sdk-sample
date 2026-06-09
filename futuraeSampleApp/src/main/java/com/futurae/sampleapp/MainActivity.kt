package com.futurae.sampleapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.futurae.sampleapp.accountsrecovery.check.arch.AccountsRecoveryCheckViewModel
import com.futurae.sampleapp.arch.FuturaeViewModel
import com.futurae.sampleapp.arch.PinProviderViewModel
import com.futurae.sampleapp.arch.ResultInformativeViewModel
import com.futurae.sampleapp.navigation.FuturaeNavigationGraph
import com.futurae.sampleapp.ui.shared.elements.authenticationconfirmationscreen.arch.AuthenticationViewModel
import com.futurae.sampleapp.ui.shared.elements.topappbar.arch.FuturaeAppBarViewModel
import com.futurae.sampleapp.ui.theme.FuturaeTheme
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.utils.NotificationHelper
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.common.LockConfigurationType

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
    private val accountsRecoveryCheckViewModel: AccountsRecoveryCheckViewModel by viewModels {
        AccountsRecoveryCheckViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.ENABLE_FLAG_SECURE) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        enableEdgeToEdge()
        super<FragmentActivity>.onCreate(savedInstanceState)
        setContent {
            FuturaeSampleApp()
        }

        handleIntent(intent)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
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
                // perform account refresh, deferring until SDK is ready if needed
                futuraeViewModel.scheduleAccountStatusFetch()
            } else if (keys.any { it == NotificationHelper.EXTRA_QR }) {
                // navigate to QR
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun FuturaeSampleApp() {
        FuturaeTheme {
            FuturaeNavigationGraph(
                viewModel = futuraeViewModel,
                futuraeAppBarViewModel = futuraeAppBarViewModel,
                resultViewModel = resultViewModel,
                authenticationViewModel = authenticationViewModel,
                pinProviderViewModel = pinProviderViewModel,
                accountsRecoveryCheckViewModel = accountsRecoveryCheckViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = BuildConfig.ENABLE_TEST_TAGS },
            )
        }
    }
}