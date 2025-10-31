package com.futurae.sampleapp.splash.arch

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.utils.LocalStorage
import com.futurae.sampleapp.navigation.RootNavigationEvent
import com.futurae.sampleapp.utils.SdkWrapper
import com.futurae.sdk.FuturaeSDK
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: FuturaeSampleApplication) : AndroidViewModel(application) {

    companion object {
        private const val SPLASH_DURATION = 1000L

        fun provideFactory(
            application: FuturaeSampleApplication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = SplashViewModel(application) as T
        }
    }

    // use replay, to handle following case:
    // App starts and shows /splash
    // App adds /lock_screen on top
    // splash navigation event is emitted but not collected due to lifecycle stopping observation
    // lock screen is unlocked, /splash starts collecting but doesn't receive nav event
    private val _navigationInfo = MutableSharedFlow<RootNavigationEvent>(replay = 1)
    val navigationInfo: SharedFlow<RootNavigationEvent> = _navigationInfo.asSharedFlow()

    private val _checkForMigratableAccounts = MutableSharedFlow<Unit>(replay = 1)
    val checkForMigratableAccounts: SharedFlow<Unit> = _checkForMigratableAccounts.asSharedFlow()

    init {
        when {
            FuturaeSDK.isSDKInitialized -> {
                dispatchCheckForMigratableAccounts()
                dispatchNavigationInfo(navigationInfo = RootNavigationEvent.Home)
            }

            LocalStorage.hasExistingConfiguration -> {
                val navigationInfo = SdkWrapper.attemptToLaunchSdkWithErrorHandling(
                    application = application,
                    sdkConfiguration = LocalStorage.persistedSDKConfig
                )

                dispatchNavigationInfo(navigationInfo)
            }

            else -> {
                dispatchNavigationInfo(navigationInfo = RootNavigationEvent.Configuration)
            }
        }
    }

    private fun dispatchCheckForMigratableAccounts() {
        viewModelScope.launch {
            _checkForMigratableAccounts.emit(Unit)
        }
    }

    private fun dispatchNavigationInfo(navigationInfo: RootNavigationEvent) {
        if (navigationInfo == RootNavigationEvent.Home) {
            dispatchCheckForMigratableAccounts()
        }

        viewModelScope.launch {
            delay(SPLASH_DURATION)
            _navigationInfo.emit(navigationInfo)
        }
    }
}
