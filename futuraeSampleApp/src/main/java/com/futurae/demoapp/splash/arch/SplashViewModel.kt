package com.futurae.demoapp.splash.arch

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.utils.LocalStorage
import com.futurae.demoapp.navigation.RootNavigationEvent
import com.futurae.demoapp.utils.SdkWrapper
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.exception.FTCorruptedStateException
import com.futurae.sdk.public_api.exception.FTInvalidArgumentException
import com.futurae.sdk.public_api.exception.FTInvalidStateException
import com.futurae.sdk.public_api.exception.FTKeystoreException
import com.futurae.sdk.public_api.exception.FTLockInvalidConfigurationException
import com.futurae.sdk.public_api.exception.FTLockMechanismUnavailableException
import com.futurae.sdk.public_api.exception.FTMigrationFailedException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: FuturaeDemoApplication) : AndroidViewModel(application) {

    companion object {
        private const val SPLASH_DURATION = 1000L

        fun provideFactory(
            application: FuturaeDemoApplication
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
                try {
                    SdkWrapper.attemptToLaunchSDK(
                        application = application,
                        sdkConfiguration = LocalStorage.persistedSDKConfig
                    )
                    dispatchCheckForMigratableAccounts()
                    dispatchNavigationInfo(navigationInfo = RootNavigationEvent.Home)
                } catch (t: Throwable) {
                    when (t) {
                        is FTInvalidArgumentException,
                        is FTLockInvalidConfigurationException,
                        is FTLockMechanismUnavailableException,
                        is FTKeystoreException -> {
                            dispatchNavigationInfo(
                                navigationInfo = RootNavigationEvent.Error(
                                    title = SdkWrapper.getStringForSDKError(t),
                                    message = t.message ?: "Unknown Error"
                                )
                            )
                        }
                        is FTInvalidStateException -> {
                            // Already init. Proceed with navigation
                            dispatchCheckForMigratableAccounts()
                            dispatchNavigationInfo(navigationInfo = RootNavigationEvent.Home)
                        }
                        is FTMigrationFailedException,
                        is FTCorruptedStateException -> {
                            // show SDK recovery
                            dispatchNavigationInfo(navigationInfo = RootNavigationEvent.Recovery)
                        }
                    }
                }
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
        viewModelScope.launch {
            delay(SPLASH_DURATION)
            _navigationInfo.emit(navigationInfo)
        }
    }
}
