package com.futurae.demoapp.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.R
import com.futurae.demoapp.accountsrecovery.AccountsRecoveryFlow
import com.futurae.demoapp.arch.AuthRequestData
import com.futurae.demoapp.arch.FuturaeViewModel
import com.futurae.demoapp.arch.NotificationType
import com.futurae.demoapp.arch.NotificationUI
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.arch.ResultInformativeViewModel
import com.futurae.demoapp.configuration.ConfigurationScreenRoute
import com.futurae.demoapp.configuration.InitialConfigurationScreenRoute
import com.futurae.demoapp.enrollment.EnrollmentCase
import com.futurae.demoapp.enrollment.EnrollmentRouteComposable
import com.futurae.demoapp.ui.shared.elements.error.ErrorScreen
import com.futurae.demoapp.home.accounts.AccountsScreen
import com.futurae.demoapp.home.accounts.history.AccountHistoryScreen
import com.futurae.demoapp.home.activationcode.ActivationCodeScreen
import com.futurae.demoapp.home.qrscanner.QRScannerScreen
import com.futurae.demoapp.lock.LockScreen
import com.futurae.demoapp.lock.arch.LockScreenConfiguration
import com.futurae.demoapp.lock.arch.LockScreenMode
import com.futurae.demoapp.accountsrecovery.check.arch.AccountsRecoveryCheckViewModel
import com.futurae.demoapp.recovery.SDKRecoveryFlow
import com.futurae.demoapp.settings.adaptive.AdaptiveSettingsScreen
import com.futurae.demoapp.settings.adaptive.collections.AdaptiveCollectDetailsScreen
import com.futurae.demoapp.settings.adaptive.collections.AdaptiveCollectionsScreen
import com.futurae.demoapp.settings.debug.SDKDebugUtilScreen
import com.futurae.demoapp.settings.debug.qrcodeutils.DebugQRCodeUtilsScreen
import com.futurae.demoapp.settings.debug.uriutils.DebugURIUtilsScreen
import com.futurae.demoapp.settings.integrity.IntegritySettingsScreen
import com.futurae.demoapp.settings.more.MoreScreen
import com.futurae.demoapp.settings.sdksettings.SettingsScreen
import com.futurae.demoapp.splash.SplashScreenRoute
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.accountpicker.AccountPicker
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialog
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.demoapp.ui.shared.elements.authenticationconfirmationscreen.AuthenticationRouteComposable
import com.futurae.demoapp.ui.shared.elements.authenticationconfirmationscreen.arch.AuthenticationViewModel
import com.futurae.demoapp.ui.shared.elements.bottomnavigationbar.FuturaeBottomNavigationBar
import com.futurae.demoapp.ui.shared.elements.bottomnavigationbar.bottomNavigationTo
import com.futurae.demoapp.ui.shared.elements.bottomnavigationbar.hasBottomNavigation
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeRouteComposable
import com.futurae.demoapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.demoapp.ui.shared.elements.snackbar.FuturaeSnackbarVisuals
import com.futurae.demoapp.ui.shared.elements.topappbar.arch.FuturaeAppBarViewModel
import com.futurae.demoapp.ui.shared.elements.topappbar.FuturaeTopAppBar
import com.futurae.demoapp.ui.theme.PrimaryColor
import com.futurae.demoapp.utils.LocalStorage
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.common.FuturaeSDKStatus
import com.futurae.sdk.public_api.qr_code.model.QRCode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

enum class FuturaeDemoDestinations(
    val route: String,
    @StringRes val titleResId: Int = R.string.empty,
    val hasBackNavigation: Boolean = false
) {
    SPLASH_ROUTE(route = "splash"),
    CONFIGURATION_ROUTE(
        route = "configuration",
        titleResId = R.string.sdk_configuration
    ),
    ACCOUNTS_ROUTE(
        route = "home/accounts",
        titleResId = R.string.bottom_navigation_accounts_item
    ),
    QR_SCANNER_ROUTE(
        route = "home/qr_scanner",
        titleResId = R.string.bottom_navigation_scan_item
    ),
    ACTIVATION_CODE_ROUTE(
        route = "home/manual_entry",
        titleResId = R.string.bottom_navigation_manual_entry_item
    ),
    AUTHENTICATION_ROUTE(route = "authentication"),
    ACCOUNT_PICKER_ROUTE(route = "account_picker"),
    RESULT_INFORMATIVE_ROUTE(route = "result_informative_screen"),
    ACCOUNT_HISTORY_ROUTE(route = "home/accounts/history"),
    ERROR_ROUTE(route = "error"),
    LOCK_ROUTE(route = "lock_screen"),
    MORE_ROUTE(route = "home/more", titleResId = R.string.bottom_navigation_more_item),
    SETTINGS_ROUTE(
        route = "home/more/settings",
        titleResId = R.string.settings,
        hasBackNavigation = true
    ),
    SETTINGS_ADAPTIVE_ROUTE(
        route = "home/more/settings/adaptive",
        titleResId = R.string.adaptive_overview,
        hasBackNavigation = true
    ),
    SETTINGS_ADAPTIVE_COLLECTIONS_ROUTE(
        route = "home/more/settings/adaptive_collections",
        titleResId = R.string.adaptive_collections,
        hasBackNavigation = true
    ),
    SETTINGS_ADAPTIVE_COLLECTION_DETAILS_ROUTE(
        route = "home/more/settings/adaptive_collection_details",
        titleResId = R.string.adaptive_collection_detail,
        hasBackNavigation = true
    ),
    SETTINGS_INTEGRITY_ROUTE(
        route = "home/more/settings/integrity",
        titleResId = R.string.integrity_check,
        hasBackNavigation = true
    ),
    SETTINGS_SDK_CONFIGURATION_ROUTE(
        route = "home/more/settings/configuration",
        titleResId = R.string.sdk_configuration,
        hasBackNavigation = true
    ),
    SETTINGS_SDK_DEBUG_ROUTE(
        route = "home/more/settings/debug",
        titleResId = R.string.debug_utilities,
        hasBackNavigation = true
    ),
    ACCOUNTS_RESTORATION_ROUTE(
        route = "accounts_restoration",
        titleResId = R.string.account_migration_confirmation_dialog
    ),
    SDK_RESTORATION_ROUTE(
        route = "sdk_restoration",
        titleResId = R.string.account_migration_confirmation_dialog
    ),
    ENROLLMENT_ROUTE(
        route = "enrollment_flow",
        titleResId = R.string.account_enrollment
    ),
    DEBUG_QR_CODE_UTILS_ROUTE(
        route = "home/more/settings/debug/qr_code_utils",
        titleResId = R.string.debug_qr_code_utils,
        hasBackNavigation = true
    ),
    DEBUG_URI_UTILS_ROUTE(
        route = "home/more/settings/debug/uri_utils",
        titleResId = R.string.debug_uri_utils,
        hasBackNavigation = true
    )
}

object NavigationArguments {

    object ErrorRoute {
        const val TITLE_NAV_ARG = "title"
        const val MESSAGE_NAV_ARG = "message"
    }

    object AccountPickerRoute {
        const val QR_CODE_NAV_ARG = "qr_code"
    }

    object AccountHistoryRoute {
        const val USER_ID_NAV_ARG = "user_id"
    }

    object LockRoute {
        const val LOCK_SCREEN_CONFIGURATION = "lock_screen_config"
    }

    object AccountRestorationRoute {
        const val IS_PIN_PROTECTED = "is_pin_protected"
    }

    object AccountEnrollmentRoute {
        const val ENROLLMENT_INPUT = "enrollment_input"
    }

    object AdaptiveCollectionDetailsRoute {
        const val DETAILS = "details"
    }
}

@Composable
fun FuturaeNavigationGraph(
    viewModel: FuturaeViewModel,
    futuraeAppBarViewModel: FuturaeAppBarViewModel,
    resultViewModel: ResultInformativeViewModel,
    authenticationViewModel: AuthenticationViewModel,
    pinProviderViewModel: PinProviderViewModel,
    accountsRecoveryCheckViewModel: AccountsRecoveryCheckViewModel,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var notificationMessage by remember { mutableStateOf<NotificationUI?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val shouldShowBottomBar = remember(currentDestination) {
        currentDestination?.route.hasBottomNavigation()
    }

    val topBarState by futuraeAppBarViewModel.topAppBarUIState.collectAsStateWithLifecycle()
    val derivedTopBarState by remember {
        derivedStateOf { topBarState }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Timber.w("Notifications Permission not granted")
            notificationMessage = NotificationUI(
                type = NotificationType.GENERIC,
                dialogState = FuturaeAlertDialogUIState(
                    title = TextWrapper.Resource(R.string.permission_request_dialog_title),
                    text = TextWrapper.Resource(R.string.permission_request_dialog_notifications_message),
                    confirmButtonCta = TextWrapper.Resource(R.string.ok)
                )
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            FuturaeTopAppBar(derivedTopBarState) {
                navController.navigateUp()
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                FuturaeBottomNavigationBar(navController)
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isError = (data.visuals as? FuturaeSnackbarVisuals)?.isError ?: false

                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) Color.Red else Color.White,
                    contentColor = if (isError) Color.White else Color.Black,
                )
            }
        },
        // Targeting API 35 and over with edgeToEdge(), the status bar color will be decided by the
        // Scaffold drawing behind it. Thus is needs the right container color for the status bar.
        containerColor = PrimaryColor,
    ) { paddingValues ->
        NavHost(
            navController = navController as NavHostController,
            startDestination = FuturaeDemoDestinations.SPLASH_ROUTE.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = LinearEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            composable(route = FuturaeDemoDestinations.SPLASH_ROUTE.route) {
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.hideTopAppBar()
                }

                SplashScreenRoute(
                    checkForMigratableAccounts = accountsRecoveryCheckViewModel::checkForMigratableAccounts,
                ) {
                    navController.navigate(it.route) {
                        popUpTo(FuturaeDemoDestinations.SPLASH_ROUTE.route) {
                            inclusive = true
                        }
                    }

                    if (it is RootNavigationEvent.Home && FuturaeSDK.client.lockApi.isLocked() && !LocalStorage.shouldSetupPin) {
                        navController.navigateToLockScreen(LockScreenMode.UNLOCK)
                    }
                }
            }

            composable(route = FuturaeDemoDestinations.CONFIGURATION_ROUTE.route) {
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.showCommonTopBar(
                        titleResId = FuturaeDemoDestinations.CONFIGURATION_ROUTE.titleResId,
                        hasBackNavigation = FuturaeDemoDestinations.CONFIGURATION_ROUTE.hasBackNavigation
                    )
                }

                InitialConfigurationScreenRoute(
                    onConfigurationComplete = {
                        navController.navigate(FuturaeDemoDestinations.SPLASH_ROUTE.route) {
                            popUpTo(FuturaeDemoDestinations.CONFIGURATION_ROUTE.route) {
                                inclusive = true
                            }
                        }
                    },
                    showSnackbar = {
                        snackbarHostState.showSnackbar(
                            FuturaeSnackbarVisuals(
                                message = it.message.value(context),
                                isError = it is FuturaeSnackbarUIState.Error
                            )
                        )
                    }
                )
            }

            homeNavigation(
                futuraeViewModel = viewModel,
                appBarViewModel = futuraeAppBarViewModel,
                resultViewModel = resultViewModel,
                authenticationViewModel = authenticationViewModel,
                pinProviderViewModel = pinProviderViewModel,
                accountsRecoveryCheckViewModel = accountsRecoveryCheckViewModel,
                navController = navController,
                showSnackbar = {
                    snackbarHostState.showSnackbar(
                        FuturaeSnackbarVisuals(
                            message = it.message.value(context),
                            isError = it is FuturaeSnackbarUIState.Error
                        )
                    )
                }
            )

            composable(
                route = FuturaeDemoDestinations.ERROR_ROUTE.route +
                        "?${NavigationArguments.ErrorRoute.TITLE_NAV_ARG}={title}" +
                        "&${NavigationArguments.ErrorRoute.MESSAGE_NAV_ARG}={message}",
                arguments = listOf(
                    navArgument(name = NavigationArguments.ErrorRoute.TITLE_NAV_ARG) {
                        type = NavType.ReferenceType
                    },
                    navArgument(name = NavigationArguments.ErrorRoute.MESSAGE_NAV_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.hideTopAppBar()
                }

                val title = backStackEntry
                    .arguments
                    ?.getInt(NavigationArguments.ErrorRoute.TITLE_NAV_ARG)
                    ?: R.string.sdk_init_error_initialization_failed
                val message = backStackEntry
                    .arguments
                    ?.getString(NavigationArguments.ErrorRoute.MESSAGE_NAV_ARG)
                ErrorScreen(title, message) {
                    FuturaeSDK.reset(context.applicationContext as FuturaeDemoApplication)
                    LocalStorage.reset()
                    navController.navigate(FuturaeDemoDestinations.SPLASH_ROUTE.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            }

            composable(FuturaeDemoDestinations.AUTHENTICATION_ROUTE.route) {
                // todo consider migrating to shared top app bar
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.hideTopAppBar()
                }

                AuthenticationRouteComposable(
                    viewModel = authenticationViewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }

            composable(FuturaeDemoDestinations.RESULT_INFORMATIVE_ROUTE.route) {
                ResultInformativeRouteComposable(
                    viewModel = resultViewModel,
                    onShowResultTopAppBar = { state, label ->
                        futuraeAppBarViewModel.showResultTopBar(
                            state = state,
                            label = label
                        )
                    },
                    navController = navController
                )
            }

            composable(
                route = FuturaeDemoDestinations.ACCOUNT_PICKER_ROUTE.route +
                        "?${NavigationArguments.AccountPickerRoute.QR_CODE_NAV_ARG}={qr_code}",
                arguments = listOf(
                    navArgument(name = NavigationArguments.AccountPickerRoute.QR_CODE_NAV_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.showAccountPickerTopAppBar()
                }

                val qrCode = backStackEntry
                    .arguments
                    ?.getString(NavigationArguments.AccountPickerRoute.QR_CODE_NAV_ARG) ?: ""
                AccountPicker(
                    onAccountSelectionConfirmed = {
                        val authRequestData = AuthRequestData.UsernamelessQRCode(
                            qrCode = FuturaeSDK.client.qrCodeApi.getQRCode(qrCode) as QRCode.Usernameless,
                            ftAccount = it
                        )
                        authenticationViewModel.handleAuthRequest(authRequestData)
                    }
                )
            }

            composable(
                route = FuturaeDemoDestinations.ACCOUNT_HISTORY_ROUTE.route +
                        "?${NavigationArguments.AccountHistoryRoute.USER_ID_NAV_ARG}={user_id}",
                arguments = listOf(
                    navArgument(name = NavigationArguments.AccountHistoryRoute.USER_ID_NAV_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry
                    .arguments
                    ?.getString(NavigationArguments.AccountHistoryRoute.USER_ID_NAV_ARG) ?: ""
                AccountHistoryScreen(
                    userId = userId,
                    onUpdateTopAppBar = futuraeAppBarViewModel::showAccountHistoryTopAppBar
                )
            }

            composable(
                route = FuturaeDemoDestinations.LOCK_ROUTE.route +
                        "?${NavigationArguments.LockRoute.LOCK_SCREEN_CONFIGURATION}" +
                        "={${NavigationArguments.LockRoute.LOCK_SCREEN_CONFIGURATION}}",
                arguments = listOf(
                    navArgument(name = NavigationArguments.LockRoute.LOCK_SCREEN_CONFIGURATION) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.hideTopAppBar()
                }

                val configJson =
                    backStackEntry.arguments?.getString(NavigationArguments.LockRoute.LOCK_SCREEN_CONFIGURATION)
                val lockScreenConfig =
                    configJson?.let { Json.decodeFromString<LockScreenConfiguration>(it) }
                        ?: throw IllegalArgumentException("LockScreen navigation without locks screen configuration argument")
                LockScreen(
                    navController = navController,
                    pinProviderViewModel = pinProviderViewModel,
                    configuration = lockScreenConfig,
                )
            }

            composable(
                route = FuturaeDemoDestinations.ACCOUNTS_RESTORATION_ROUTE.route +
                        "?${NavigationArguments.AccountRestorationRoute.IS_PIN_PROTECTED}" +
                        "={${NavigationArguments.AccountRestorationRoute.IS_PIN_PROTECTED}}",
                arguments = listOf(
                    navArgument(
                        name = NavigationArguments.AccountRestorationRoute.IS_PIN_PROTECTED
                    ) {
                        type = NavType.BoolType
                    }
                )
            ) { backStackEntry ->
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.hideTopAppBar()
                }

                val isPinProtected = backStackEntry.arguments
                    ?.getBoolean(NavigationArguments.AccountRestorationRoute.IS_PIN_PROTECTED)
                    ?: throw IllegalArgumentException("Account restoration navigation without isPinProtected argument")

                AccountsRecoveryFlow(
                    isPinProtected = isPinProtected,
                    pinProviderViewModel = pinProviderViewModel,
                    navController = navController
                )
            }

            composable(route = FuturaeDemoDestinations.SDK_RESTORATION_ROUTE.route) {
                LaunchedEffect(Unit) {
                    futuraeAppBarViewModel.hideTopAppBar()
                }

                SDKRecoveryFlow(
                    pinProviderViewModel = pinProviderViewModel,
                    navController = navController
                )
            }

            composable(
                route = FuturaeDemoDestinations.ENROLLMENT_ROUTE.route +
                        "?${NavigationArguments.AccountEnrollmentRoute.ENROLLMENT_INPUT}" +
                        "={${NavigationArguments.AccountEnrollmentRoute.ENROLLMENT_INPUT}}",
                arguments = listOf(
                    navArgument(name = NavigationArguments.AccountEnrollmentRoute.ENROLLMENT_INPUT) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val enrollmentCaseJson = backStackEntry.arguments
                    ?.getString(NavigationArguments.AccountEnrollmentRoute.ENROLLMENT_INPUT)
                val enrollmentCase = enrollmentCaseJson
                    ?.let { Json.decodeFromString<EnrollmentCase>(it) }
                    ?: throw IllegalArgumentException("Enrollment navigation without enrollment input argument")

                EnrollmentRouteComposable(
                    enrollmentInput = enrollmentCase.toEnrollmentInput(),
                    pinProviderViewModel = pinProviderViewModel,
                    onHideTopAppBar = {
                        futuraeAppBarViewModel.hideTopAppBar()
                    },
                    onShowCommonTopAppBar = {
                        futuraeAppBarViewModel.showCommonTopBar(titleResId = it, hasBackNavigation = true)
                    },
                    onShowResultTopAppBar = { state, label ->
                        futuraeAppBarViewModel.showResultTopBar(state = state, label = label)
                    },
                    navController = navController
                )
            }
        }
    }

    notificationMessage?.let {
        when (it.type) {
            NotificationType.QR_SCAN -> {
                FuturaeAlertDialog(
                    uiState = it.dialogState,
                    onConfirm = {
                        notificationMessage = null
                        navController.navigate(FuturaeDemoDestinations.QR_SCANNER_ROUTE.route)
                    },
                    onDeny = {
                        notificationMessage = null
                    }
                )
            }

            NotificationType.INFO,
            NotificationType.UNENROLL -> {
                FuturaeAlertDialog(
                    uiState = it.dialogState,
                    onConfirm = {
                        notificationMessage = null
                    },
                    onDeny = {
                        notificationMessage = null
                    }
                )
            }

            NotificationType.AUTH -> {
                // handled by navigation
            }

            NotificationType.GENERIC -> {
                FuturaeAlertDialog(
                    uiState = it.dialogState,
                    onConfirm = {
                        notificationMessage = null
                    },
                    onDeny = {
                        notificationMessage = null
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onAuthRequest
            .onEach { authenticationViewModel.handleAuthRequest(it) }
            .launchIn(this)

        viewModel.onEnrollmentRequest
            .onEach { navController.navigateToEnrollmentFlowRoute(it) }
            .launchIn(this)

        viewModel.snackbarUIState
            .onEach {
                snackbarHostState.showSnackbar(
                    FuturaeSnackbarVisuals(
                        message = it.message.value(context),
                        isError = it is FuturaeSnackbarUIState.Error
                    )
                )
            }
            .launchIn(this)

        authenticationViewModel.notifyUser
            .onEach {
                snackbarHostState.showSnackbar(
                    FuturaeSnackbarVisuals(
                        message = it.message.value(context),
                        isError = it is FuturaeSnackbarUIState.Error
                    )
                )
            }
            .launchIn(this)

        authenticationViewModel.navigateToApprovalScreen
            .onEach {
                navController.navigate(FuturaeDemoDestinations.AUTHENTICATION_ROUTE.route) {
                    popUpTo(FuturaeDemoDestinations.ACCOUNTS_ROUTE.route)
                    launchSingleTop = true
                }
            }
            .launchIn(this)

        resultViewModel.navigateToResultInformativeScreen
            .onEach {
                navController.navigate(FuturaeDemoDestinations.RESULT_INFORMATIVE_ROUTE.route) {
                    launchSingleTop = true
                }
            }
            .launchIn(this)

        authenticationViewModel.navigateToAccounts
            .onEach {
                navController.bottomNavigationTo(FuturaeDemoDestinations.ACCOUNTS_ROUTE)
            }
            .launchIn(this)

        authenticationViewModel.onOfflineVerificationCodeReceived
            .onEach {
                resultViewModel.onOfflineVerificationCodeReceived(it)
            }
            .launchIn(this)

        FuturaeSDK.sdkState().onEach {
            when (it.status) {
                is FuturaeSDKStatus.Corrupted -> {
                    navController.navigate(FuturaeDemoDestinations.SDK_RESTORATION_ROUTE.route) {
                        launchSingleTop = true
                    }
                }

                FuturaeSDKStatus.Locked -> {
                    if (!LocalStorage.isDeviceEnrolled() && LocalStorage.isPinConfig) {
                        // User has not yet set up SDK PIN (set up during first enrollment). Do not show Lock screen,
                        // SDK will only unlock during first enrollment by creating a new PIN during the enrollment.
                        Timber.d("SDK locked under SDK PIN config without accounts.")
                    } else {
                        Timber.d("SDK locked.")
                        navController.navigateToLockScreen(LockScreenMode.UNLOCK)
                    }
                }

                FuturaeSDKStatus.Uninitialized,
                is FuturaeSDKStatus.Unlocked -> {
                    // no-op
                }
            }
        }.launchIn(this)

        viewModel.notifyUserFlow
            .onEach { notificationMessage = it }
            .launchIn(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        FuturaeSDK
            .notificationsFlow
            .onEach { notification ->
                notification.onSuccess {
                    viewModel.handleNotificationReceived(notification = it)
                }
            }
            .launchIn(this)
    }
}

private fun NavGraphBuilder.homeNavigation(
    futuraeViewModel: FuturaeViewModel,
    appBarViewModel: FuturaeAppBarViewModel,
    authenticationViewModel: AuthenticationViewModel,
    resultViewModel: ResultInformativeViewModel,
    pinProviderViewModel: PinProviderViewModel,
    accountsRecoveryCheckViewModel: AccountsRecoveryCheckViewModel,
    navController: NavController,
    showSnackbar: suspend (FuturaeSnackbarUIState) -> Unit
) {
    navigation(startDestination = FuturaeDemoDestinations.ACCOUNTS_ROUTE.route, route = "main") {
        composable(FuturaeDemoDestinations.ACCOUNTS_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.ACCOUNTS_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.ACCOUNTS_ROUTE.hasBackNavigation
                )
            }

            AccountsScreen(
                futuraeViewModel = futuraeViewModel,
                accountsRecoveryCheckViewModel = accountsRecoveryCheckViewModel,
                onAccountClick = {
                    navController.navigate(
                        FuturaeDemoDestinations.ACCOUNT_HISTORY_ROUTE.route +
                                "?${NavigationArguments.AccountHistoryRoute.USER_ID_NAV_ARG}=$it"
                    )
                },
                onAccountRestorationClick = {
                    navController.navigate(
                        FuturaeDemoDestinations.ACCOUNTS_RESTORATION_ROUTE.route +
                                "?${NavigationArguments.AccountRestorationRoute.IS_PIN_PROTECTED}=$it"
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(FuturaeDemoDestinations.QR_SCANNER_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.QR_SCANNER_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.QR_SCANNER_ROUTE.hasBackNavigation
                )
            }

            QRScannerScreen(
                onInvalidQRCode = { resultViewModel.onInvalidQRCode() },
                onEnrollmentRequest = { navController.navigateToEnrollmentFlowRoute(it) },
                onAuthRequest = { authenticationViewModel.handleAuthRequest(it) },
                showAccountPicker = {
                    navController.navigate(
                        "${FuturaeDemoDestinations.ACCOUNT_PICKER_ROUTE.route}?" +
                                "${NavigationArguments.AccountPickerRoute.QR_CODE_NAV_ARG}=$it"
                    )
                }
            )
        }

        composable(FuturaeDemoDestinations.ACTIVATION_CODE_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.ACTIVATION_CODE_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.ACTIVATION_CODE_ROUTE.hasBackNavigation
                )
            }

            ActivationCodeScreen(
                onEnrollmentRequest = { navController.navigateToEnrollmentFlowRoute(it) }
            )
        }

        composable(route = FuturaeDemoDestinations.MORE_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showMoreTopBar()
            }

            MoreScreen(
                accountsRecoveryCheckViewModel = accountsRecoveryCheckViewModel,
                showSnackBar = showSnackbar
            ) { route, shouldLaunchSingleTop ->
                navController.navigate(route) {
                    launchSingleTop = shouldLaunchSingleTop
                }
            }
        }

        composable(route = FuturaeDemoDestinations.SETTINGS_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.SETTINGS_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.SETTINGS_ROUTE.hasBackNavigation
                )
            }

            SettingsScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                pinProviderViewModel = pinProviderViewModel,
                onPinRequested = {
                    navController.navigateToLockScreen(LockScreenMode.CHANGE_PIN)
                },
                showSnackbar = showSnackbar
            )
        }

        composable(route = FuturaeDemoDestinations.SETTINGS_ADAPTIVE_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.SETTINGS_ADAPTIVE_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.SETTINGS_ADAPTIVE_ROUTE.hasBackNavigation
                )
            }

            AdaptiveSettingsScreen { route ->
                navController.navigate(route)
            }
        }

        composable(route = FuturaeDemoDestinations.SETTINGS_ADAPTIVE_COLLECTIONS_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.SETTINGS_ADAPTIVE_COLLECTIONS_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.SETTINGS_ADAPTIVE_COLLECTIONS_ROUTE.hasBackNavigation
                )
            }

            AdaptiveCollectionsScreen {
                val route = "${FuturaeDemoDestinations.SETTINGS_ADAPTIVE_COLLECTION_DETAILS_ROUTE.route}?" +
                        "${NavigationArguments.AdaptiveCollectionDetailsRoute.DETAILS}=$it"
                navController.navigate(route)
            }
        }

        composable(
            route = FuturaeDemoDestinations.SETTINGS_ADAPTIVE_COLLECTION_DETAILS_ROUTE.route +
                    "?${NavigationArguments.AdaptiveCollectionDetailsRoute.DETAILS}" +
                    "={${NavigationArguments.AdaptiveCollectionDetailsRoute.DETAILS}}",
            arguments = listOf(
                navArgument(name = NavigationArguments.AdaptiveCollectionDetailsRoute.DETAILS) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val details = backStackEntry.arguments
                ?.getString(NavigationArguments.AdaptiveCollectionDetailsRoute.DETAILS)
                ?: throw IllegalArgumentException("Collection details navigation without input argument")
            AdaptiveCollectDetailsScreen(details)
        }

        composable(route = FuturaeDemoDestinations.SETTINGS_INTEGRITY_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.SETTINGS_INTEGRITY_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.SETTINGS_INTEGRITY_ROUTE.hasBackNavigation
                )
            }

            IntegritySettingsScreen()
        }


        composable(route = FuturaeDemoDestinations.SETTINGS_SDK_CONFIGURATION_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.SETTINGS_SDK_CONFIGURATION_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.SETTINGS_SDK_CONFIGURATION_ROUTE.hasBackNavigation
                )
            }

            ConfigurationScreenRoute(
                pinProviderViewModel = pinProviderViewModel,
                onConfigurationComplete = {},
                isConfigurationChange = true,
                onPinRequested = {
                    navController.navigateToLockScreen(LockScreenMode.CREATE_PIN)
                },
                showSnackbar = showSnackbar
            )
        }

        composable(route = FuturaeDemoDestinations.SETTINGS_SDK_DEBUG_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.SETTINGS_SDK_DEBUG_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.SETTINGS_SDK_DEBUG_ROUTE.hasBackNavigation
                )
            }

            SDKDebugUtilScreen(
                showSnackbar = showSnackbar,
                activateBiometricsRequested = {
                    navController.navigateToLockScreen(LockScreenMode.ACTIVATE_BIO)
                }
            ) {
                navController.navigate(it)
            }
        }

        composable(route = FuturaeDemoDestinations.DEBUG_QR_CODE_UTILS_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.DEBUG_QR_CODE_UTILS_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.DEBUG_QR_CODE_UTILS_ROUTE.hasBackNavigation
                )
            }

            DebugQRCodeUtilsScreen()
        }

        composable(route = FuturaeDemoDestinations.DEBUG_URI_UTILS_ROUTE.route) {
            LaunchedEffect(Unit) {
                appBarViewModel.showCommonTopBar(
                    titleResId = FuturaeDemoDestinations.DEBUG_URI_UTILS_ROUTE.titleResId,
                    hasBackNavigation = FuturaeDemoDestinations.DEBUG_URI_UTILS_ROUTE.hasBackNavigation
                )
            }

            DebugURIUtilsScreen()
        }
    }
}

fun NavController.navigateToLockScreen(mode: LockScreenMode) {
    val config = LockScreenConfiguration(6, mode)
    val configJson = Json.encodeToString(config)
    val lockScreenRoute = "${FuturaeDemoDestinations.LOCK_ROUTE.route}?" +
            "${NavigationArguments.LockRoute.LOCK_SCREEN_CONFIGURATION}=$configJson"
    this.navigate(lockScreenRoute) {
        launchSingleTop = true
        popUpTo(lockScreenRoute) { inclusive = false }
    }
}

fun NavController.navigateToEnrollmentFlowRoute(enrollmentCase: EnrollmentCase) {
    val enrollmentCaseJson = Json.encodeToString(enrollmentCase)
    val enrollmentRoute = "${FuturaeDemoDestinations.ENROLLMENT_ROUTE.route}?" +
            "${NavigationArguments.AccountEnrollmentRoute.ENROLLMENT_INPUT}=$enrollmentCaseJson"
    navigate(enrollmentRoute)
}