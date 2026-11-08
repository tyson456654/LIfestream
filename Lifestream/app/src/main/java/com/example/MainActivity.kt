package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BloodRequest
import com.example.ui.components.DashboardView
import com.example.ui.components.DisclaimerView
import com.example.ui.components.MatchingLogDialog
import com.example.ui.components.QuizView
import com.example.ui.components.UrgentRequestFeedView
import com.example.ui.components.AuthView
import com.example.ui.components.SplashView
import com.example.ui.components.ProfileView
import com.example.ui.viewmodel.LifeStreamViewModel
import com.example.ui.viewmodel.NotificationState
import com.example.ui.theme.AppTranslation
import com.example.ui.theme.LifeStreamTheme
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SlateGrey
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppTranslation.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MainContainer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer() {
    val viewModel: LifeStreamViewModel = viewModel()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isDarkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val primaryUser by viewModel.primaryUser.collectAsStateWithLifecycle()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Automatically schedules real system alarms and notifications whenever active cooldown status modifies
    LaunchedEffect(primaryUser) {
        val user = primaryUser
        if (user != null) {
            val remainingDays = viewModel.getCooldownDaysLeft()
            if (remainingDays > 0) {
                val interval = if (user.gender.equals("Female", ignoreCase = true)) 120 else 90
                val expirationTimeMs = user.lastDonationDate + (interval * 24L * 60L * 60L * 1000L)
                com.example.util.NotificationScheduler.scheduleCooldownAlert(context, user.name, user.gender, expirationTimeMs)
            } else {
                com.example.util.NotificationScheduler.cancelCooldownAlert(context)
            }
        }
    }

    LifeStreamTheme(darkTheme = isDarkTheme) {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        // Observe StateFlows safely with lifecycle
        val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
        val userDonationRecords by viewModel.userDonationRecords.collectAsStateWithLifecycle()

        val disclaimerAccepted by viewModel.disclaimerAccepted.collectAsStateWithLifecycle()
        val allRequests by viewModel.allRequests.collectAsStateWithLifecycle()
        val prioritizedRequests by viewModel.prioritizedRequests.collectAsStateWithLifecycle()

        val allHospitals by viewModel.allHospitals.collectAsStateWithLifecycle()
        val recentFcmDispatches by viewModel.recentFcmDispatches.collectAsStateWithLifecycle()
        val matchingLogState by viewModel.matchingLogState.collectAsStateWithLifecycle()
        val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

        val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
        val authScreenState by viewModel.authScreenState.collectAsStateWithLifecycle()
        val generatedOtpCode by viewModel.generatedOtpCode.collectAsStateWithLifecycle()
        val rememberMeChecked by viewModel.rememberMeChecked.collectAsStateWithLifecycle()

        val quizStep by viewModel.quizStep.collectAsStateWithLifecycle()
        val quizEligible by viewModel.quizEligible.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Urgent Feed, 2: Eligibility Quiz

    // Replacement token pop-up details
    var activeReplacementTokenNotice by remember { mutableStateOf<String?>(null) }
    var matchingLogDialogOpened by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackStars by remember { mutableStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    val daysLeft = viewModel.getCooldownDaysLeft()
    val isEligible = viewModel.isEligibleToDonate()

    // Splash screen or slide-down / fade overlay for onboarding agreement and custom authentication
    if (!isLoggedIn) {
        if (authScreenState == "SPLASH") {
            SplashView()
        } else {
            AuthView(
                screenState = authScreenState,
                generatedOtp = generatedOtpCode,
                rememberMe = rememberMeChecked,
                onRememberMeChange = { viewModel.setRememberMe(it) },
                lang = appLanguage,
                isFirebaseInitialized = viewModel.isFirebaseInitialized,
                onSignIn = { credential, pin, isEmail, onResult ->
                    if (isEmail) {
                        viewModel.signInWithEmail(credential, pin) { success, msg ->
                            onResult(success, msg)
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    } else {
                        viewModel.signIn(credential, pin) { success, msg ->
                            onResult(success, msg)
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Secure OTP transmitted successfully. Please check your system notifications.")
                                } else {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        }
                    }
                },
                onSignUp = { name, phone, pin, bloodType, gender, role, lat, lon, email, isEmail ->
                    if (isEmail && email != null) {
                        viewModel.signUpWithEmail(name, email, phone, pin, bloodType, gender, role, lat, lon) { success, msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    } else {
                        viewModel.signUp(name, phone, pin, bloodType, gender, role, lat, lon, email) { success, msg ->
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Donor credentials registered successfully. Please verify your phone with the secure OTP sent to your notifications.")
                                } else {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        }
                    }
                },
                onVerifyOtp = { code ->
                    viewModel.verifyOtp(code) { success, msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                },
                onNavigateToSignUp = { viewModel.setAuthScreenState("SIGN_UP") },
                onNavigateToSignIn = { viewModel.setAuthScreenState("SIGN_IN") },
                onLanguageChange = { viewModel.setLanguage(it) }
            )
        }
    } else if (!disclaimerAccepted) {
        DisclaimerView(
            onAccepted = {
                viewModel.acceptDisclaimer()
                scope.launch {
                    snackbarHostState.showSnackbar("Disclaimer accepted. Onboarding completed successfully!")
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    modifier = Modifier.testTag("app_top_bar"),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = CrimsonPrimary
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(CrimsonPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock, // secure representation
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LifeStream",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "LOGISTICS DEPLOYMENT MODEL",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SlateGrey,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Trigger matching logs debugger
                        IconButton(
                            onClick = { matchingLogDialogOpened = true },
                            modifier = Modifier.testTag("view_matching_logs_icon_button")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "View matching maps log",
                                    tint = if (recentFcmDispatches.isNotEmpty()) SuccessGreen else SlateGrey
                                )
                                if (recentFcmDispatches.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SuccessGreen, CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }

                        // Secure logout button to test authentication flow easily
                        IconButton(
                            onClick = { 
                                viewModel.logout()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Logged out securely from active session!")
                                }
                            },
                            modifier = Modifier.testTag("app_logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Log out from secure session",
                                tint = CrimsonPrimary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .testTag("app_bottom_nav_bar")
                        .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text(AppTranslation.translate("tab_dashboard", appLanguage), fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Dashboard"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            indicatorColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text(AppTranslation.translate("tab_feed", appLanguage), fontWeight = FontWeight.Bold) },
                        icon = {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Urgent Feed"
                                )
                                val openRequestsCount = allRequests.count { it.status == "Open" }
                                if (openRequestsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(CrimsonPrimary, CircleShape)
                                            .size(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$openRequestsCount",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            indicatorColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.testTag("nav_tab_feed")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            viewModel.resetQuiz()
                            selectedTab = 2
                        },
                        label = { Text(AppTranslation.translate("tab_eligibility", appLanguage), fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Eligibility Questionnaire"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            indicatorColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.testTag("nav_tab_quiz")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text(AppTranslation.translate("tab_profile", appLanguage), fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Security Profile"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            indicatorColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tab_animation"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardView(
                            user = primaryUser,
                            hospitals = allHospitals,
                            daysLeft = daysLeft,
                            isEligible = isEligible,
                            onToggleCooldown = { forceInCooldown ->
                                viewModel.toggleUserCooldown(forceInCooldown)
                                scope.launch {
                                    val msg = if (forceInCooldown) {
                                        "Forced User Cooldown state!"
                                    } else {
                                        "Forced User Cooldown to Active (Eligible)!"
                                    }
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            onStartEligibilityCheck = {
                                viewModel.resetQuiz()
                                selectedTab = 2
                            },
                            lang = appLanguage,
                            onToggleGender = { gender ->
                                viewModel.updateUserGender(gender)
                            },
                            themeMode = themeMode,
                            onThemeModeChanged = { mode ->
                                viewModel.setThemeMode(mode)
                            },
                            onTriggerTestNotification = {
                                val intervalDays = if ((primaryUser?.gender ?: "Male").equals("Female", ignoreCase = true)) "120" else "90"
                                com.example.util.NotificationScheduler.showCooldownExpiredNotification(
                                    context,
                                    primaryUser?.name ?: "Donor",
                                    intervalDays
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar("Cooldown alert notification sent!")
                                }
                            },
                            allUsers = allUsers,
                            allRequests = allRequests,
                            onDeleteUser = { userId ->
                                viewModel.deleteUser(userId)
                                scope.launch {
                                    snackbarHostState.showSnackbar("User deleted successfully by Admin!")
                                }
                            },
                            onUpdateHospitalStockAndVerify = { hospital, isVerified, stockJson ->
                                viewModel.updateHospitalStockAndVerify(hospital, isVerified, stockJson)
                            },
                            onDeleteRequest = { requestId ->
                                viewModel.deleteRequest(requestId)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Blood request cancelled/deleted by Admin!")
                                }
                            },
                            onToggleRequestStatus = { request ->
                                viewModel.toggleRequestStatus(request)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Blood request status updated!")
                                }
                            },
                            onRequestTabSwitchToFeed = {
                                selectedTab = 1
                            },
                            onSimulateRequest = { hospId, hospName, lat, lon, bloodType, units, mapsLink, urgency ->
                                viewModel.simulateNewRequestCreated(hospId, hospName, lat, lon, bloodType, units, mapsLink, urgency)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Emergency $bloodType request created!")
                                    matchingLogDialogOpened = true
                                }
                            },
                            onUpdateUserLocation = { lat, lon ->
                                viewModel.updateUserLocation(lat, lon)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Live GPS Location coordinates successfully updated!")
                                }
                            }
                        )

                        1 -> UrgentRequestFeedView(
                            requests = prioritizedRequests,
                            hospitals = allHospitals,
                            isEligible = isEligible,
                            onPledge = { request ->
                                viewModel.pledgeToDonate(request) { success, message ->
                                    if (success) {
                                        activeReplacementTokenNotice = message
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Pledge accepted! Inventory released.")
                                        }
                                        viewModel.runGeospatialMatching(request)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                                }
                            },
                            onSimulateRequest = { hospId, hospName, lat, lon, bloodType, units, mapsLink, urgency ->
                                viewModel.simulateNewRequestCreated(hospId, hospName, lat, lon, bloodType, units, mapsLink, urgency)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Urgent $bloodType request created! Nearby donors notified.")
                                    matchingLogDialogOpened = true
                                }
                            },
                            isAdmin = primaryUser?.role == "Admin",
                            onDeleteRequest = { requestId ->
                                viewModel.deleteRequest(requestId)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Request successfully cancelled/deleted by Admin!")
                                }
                            }
                        )

                        2 -> QuizView(
                            step = quizStep,
                            eligible = quizEligible,
                            lang = appLanguage,
                            onAnswer = { index, answer ->
                                viewModel.answerQuizQuestion(index, answer)
                            },
                            onReset = {
                                viewModel.resetQuiz()
                            },
                            onNext = {
                                selectedTab = 0
                            }
                        )

                        3 -> ProfileView(
                            user = primaryUser,
                            donationRecords = userDonationRecords,
                            daysLeft = daysLeft,
                            lang = appLanguage,
                            onSaveProfile = { name, phone, pin, gender, bloodType ->
                                viewModel.updateUserProfile(name, phone, pin, gender, bloodType)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Profile updated securely!")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Educational Replacement Token Dialogue
    activeReplacementTokenNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { activeReplacementTokenNotice = null },
            modifier = Modifier.testTag("replacement_token_dialog"),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            tonalElevation = 6.dp,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Pledge success icon",
                        tint = SuccessGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Replacement Token Issued",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Fast-Track Clinical Protocol",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "THE REPLACEMENT PROTOCOL",
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notice,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "LifeStream secures patient coverage immediately by exchanging this voucher for pre-tested blood from our clinical vaults, bypassing the 48h laboratory assay delay. You saved a life!",
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        activeReplacementTokenNotice = null 
                        showFeedbackDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Post-Donation Experience Feedback Popup Dialog
    if (showFeedbackDialog) {
        var userSubmittedFeedback by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { 
                showFeedbackDialog = false 
                feedbackStars = 5
                feedbackText = ""
            },
            modifier = Modifier.testTag("feedback_dialog"),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            tonalElevation = 6.dp,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Feedback Icon",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (userSubmittedFeedback) "Feedback Submitted" else "How is your LifeStream experience?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Donor Community Experience",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (!userSubmittedFeedback) {
                        Text(
                            text = "THANK YOU FOR PLEDGING TO DONATE!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please rate your experience with LifeStream's live matching system. Your feedback helps us build a stronger life-saving community.",
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Star Rating Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            (1..5).forEach { star ->
                                val isSelected = star <= feedbackStars
                                IconButton(
                                    onClick = { feedbackStars = star },
                                    modifier = Modifier.size(36.dp).testTag("feedback_dialog_star_$star")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$star Stars",
                                        tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            placeholder = { Text("Write your suggestions or comments here...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("feedback_dialog_text_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "❤️ Your feedback has been securely transmitted and recorded. We will use your valuable inputs to constantly improve the LifeStream matching experience.",
                            fontSize = 13.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (!userSubmittedFeedback) {
                    Button(
                        onClick = {
                            userSubmittedFeedback = true
                            scope.launch {
                                snackbarHostState.showSnackbar("Thank you! Experience feedback securely recorded.")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary, contentColor = Color.White),
                        modifier = Modifier.testTag("feedback_dialog_submit_btn")
                    ) {
                        Text("Submit", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            showFeedbackDialog = false
                            feedbackStars = 5
                            feedbackText = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary, contentColor = Color.White)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!userSubmittedFeedback) {
                    TextButton(
                        onClick = { 
                            showFeedbackDialog = false 
                            feedbackStars = 5
                            feedbackText = ""
                        }
                    ) {
                        Text("Not now", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                }
            }
        )
    }

    // Matching Log Debugger Overlay (Displays matches and full raw high-priority JSON payloads)
    if (matchingLogDialogOpened) {
        MatchingLogDialog(
            dispatches = recentFcmDispatches,
            onDismiss = { matchingLogDialogOpened = false }
        )
    }

    // Observe active runs to pop up match logs view immediately when a query runs on request creation
    val matchingLogsTriggered by viewModel.matchingLogState.collectAsStateWithLifecycle()
    if (matchingLogsTriggered is NotificationState.Logged) {
        val matches = (matchingLogsTriggered as NotificationState.Logged).notifications
        MatchingLogDialog(
            dispatches = matches,
            onDismiss = { viewModel.dismissMatchingLog() }
        )
    }
}
}
