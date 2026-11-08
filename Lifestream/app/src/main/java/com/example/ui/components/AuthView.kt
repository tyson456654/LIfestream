package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTranslation
import com.example.ui.theme.SlateGrey
import com.example.ui.theme.SuccessGreen

val BlueAccent = Color(0xFFC0563F)
val CrimsonPrimary = BlueAccent
val CardBorderColor = Color(0xFFEBE0D5)
val SlateDark = Color(0xFF1E1715)

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SlateDark,
    unfocusedTextColor = SlateDark,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = BlueAccent,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    focusedLabelColor = BlueAccent,
    unfocusedLabelColor = Color(0xFF64748B),
    focusedPlaceholderColor = Color(0xFF94A3B8),
    unfocusedPlaceholderColor = Color(0xFF94A3B8),
    cursorColor = BlueAccent
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthView(
    screenState: String, // "SIGN_IN", "SIGN_UP", "VERIFY_OTP"
    generatedOtp: String,
    rememberMe: Boolean = false,
    onRememberMeChange: (Boolean) -> Unit = {},
    lang: String = "en",
    isFirebaseInitialized: Boolean = false,
    onSignIn: (credential: String, pin: String, isEmailMode: Boolean, onResult: (Boolean, String) -> Unit) -> Unit,
    onSignUp: (name: String, phone: String, pin: String, bloodType: String, gender: String, role: String, latitude: Double, longitude: Double, email: String?, isEmailMode: Boolean) -> Unit,
    onVerifyOtp: (code: String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onLanguageChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)) // Premium slate gray light background
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .testTag("auth_card"),
            shape = RoundedCornerShape(12.dp), // Strictly rounded 12px corners
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered Logo on top
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFF1E1715), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_launcher_foreground),
                                contentDescription = "LifeStream Logo",
                                modifier = Modifier.size(52.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppTranslation.translate("app_title", lang).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = AppTranslation.translate("logistics_subtitle", lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = BlueAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = screenState,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "auth_form_animation"
                ) { state ->
                    when (state) {
                        "SIGN_IN" -> SignInForm(
                            rememberMe = rememberMe,
                            onRememberMeChange = onRememberMeChange,
                            lang = lang,
                            isFirebaseInitialized = isFirebaseInitialized,
                            onSignIn = onSignIn,
                            onNavigateToSignUp = onNavigateToSignUp,
                            onLanguageChange = onLanguageChange
                        )
                        "SIGN_UP" -> SignUpForm(
                            lang = lang,
                            isFirebaseInitialized = isFirebaseInitialized,
                            onSignUp = onSignUp,
                            onNavigateToSignIn = onNavigateToSignIn,
                            onLanguageChange = onLanguageChange
                        )
                        "VERIFY_OTP" -> VerifyOtpForm(
                            generatedOtp = generatedOtp,
                            lang = lang,
                            onVerifyOtp = onVerifyOtp,
                            onBackToSignIn = onNavigateToSignIn
                        )
                    }
                }
            }
        }
    }
}
}


@Composable
fun SignInForm(
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    lang: String,
    isFirebaseInitialized: Boolean,
    onSignIn: (credential: String, pin: String, isEmailMode: Boolean, onResult: (Boolean, String) -> Unit) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var isEmailMode by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var popUpErrorMessage by remember { mutableStateOf<String?>(null) }
    var showLangDialog by remember { mutableStateOf(false) }

    if (showLangDialog) {
        LanguageSelectionDialog(
            currentLang = lang,
            onDismiss = { showLangDialog = false },
            onSelectLanguage = { selected ->
                onLanguageChange(selected)
                showLangDialog = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AppTranslation.translate("auth_welcome", lang),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = SlateDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = AppTranslation.translate("auth_subtitle", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGrey,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Dropdown Dialog Trigger button for Language Change (Requirement 4)
        Card(
            onClick = { showLangDialog = true },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("signin_change_lang_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Translate Icon",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = when (lang) {
                        "en" -> "🌐 English (US)"
                        "hi" -> "🌐 हिंदी (HI)"
                        "te" -> "🌐 తెలుగు (TE)"
                        "ta" -> "🌐 தமிழ் (TA)"
                        else -> "🌐 Select Language"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Tab Selector for Phone vs Email Login
        TabRow(
            selectedTabIndex = if (isEmailMode) 1 else 0,
            containerColor = Color.Transparent,
            contentColor = CrimsonPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = !isEmailMode,
                onClick = { 
                    isEmailMode = false
                    errorMsg = null
                },
                text = { Text("Phone Login", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = isEmailMode,
                onClick = { 
                    isEmailMode = true
                    errorMsg = null
                },
                text = { Text("Email Login", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (!isEmailMode) {
            // Phone Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(AppTranslation.translate("auth_phone_label", lang)) },
                placeholder = { Text("+91 9xxxx xxxxx") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "phone_icon", tint = BlueAccent) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = authTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signin_phone_input")
            )
        } else {
            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("name@domain.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "email_icon", tint = BlueAccent) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = authTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signin_email_input")
            )
        }

        // Password/PIN Input (PIN for phone, Password for Email)
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text(if (isEmailMode) "Password" else AppTranslation.translate("auth_pin_label", lang)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "pin_icon", tint = BlueAccent) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isEmailMode) KeyboardType.Password else KeyboardType.NumberPassword
            ),
            singleLine = true,
            colors = authTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signin_pin_input")
        )

        // "Remember me" checkbox strictly below password field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = onRememberMeChange,
                colors = CheckboxDefaults.colors(checkedColor = BlueAccent),
                modifier = Modifier.testTag("remember_me_checkbox")
            )
            Text(
                text = AppTranslation.translate("auth_remember_me", lang),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SlateGrey,
                modifier = Modifier.clickable { onRememberMeChange(!rememberMe) }
            )
        }

        errorMsg?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Submit Button: Primary Blue Login
        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                scope.launch {
                    delay(50)
                    if (isEmailMode) {
                        val cleanEmail = email.trim()
                        if (cleanEmail.isBlank()) {
                            errorMsg = "Invalid email or phone number"
                            popUpErrorMessage = "Invalid email or phone number"
                            return@launch
                        }
                        if (pin.isBlank()) {
                            errorMsg = "Password is incorrect"
                            popUpErrorMessage = "Password is incorrect"
                            return@launch
                        }
                        errorMsg = null
                        onSignIn(cleanEmail, pin, true) { success, msg ->
                            if (!success) {
                                errorMsg = msg
                                popUpErrorMessage = msg
                            }
                        }
                    } else {
                        val cleanPhone = phone.trim()
                        if (cleanPhone.isBlank()) {
                            errorMsg = "Invalid email or phone number"
                            popUpErrorMessage = "Invalid email or phone number"
                            return@launch
                        }
                        if (pin.isBlank()) {
                            errorMsg = "Password is incorrect"
                            popUpErrorMessage = "Password is incorrect"
                            return@launch
                        }
                        errorMsg = null
                        onSignIn(cleanPhone, pin, false) { success, msg ->
                            if (!success) {
                                errorMsg = msg
                                popUpErrorMessage = msg
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("signin_submit_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = AppTranslation.translate("auth_login_btn", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        // Error Pop-Up Dialog when credentials are incorrect
        if (popUpErrorMessage != null) {
            AlertDialog(
                onDismissRequest = { popUpErrorMessage = null },
                modifier = Modifier.testTag("auth_error_dialog"),
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFFFEBEB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Authentication Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = popUpErrorMessage ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("auth_error_message")
                        )
                        Text(
                            text = if (popUpErrorMessage == "Password is incorrect") {
                                "The password you entered is incorrect. Please verify your password and try again."
                            } else {
                                "Please check your email address or phone number and try again."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { popUpErrorMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_error_dialog_dismiss_btn")
                    ) {
                        Text(
                            text = "OK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            )
        }

        // Secondary Text link: Sign Up
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onNavigateToSignUp()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppTranslation.translate("auth_no_account", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGrey
            )
            Text(
                text = AppTranslation.translate("auth_register_now", lang),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BlueAccent
            )
        }
    }
}

@Composable
fun SignUpForm(
    lang: String,
    isFirebaseInitialized: Boolean,
    onSignUp: (name: String, phone: String, pin: String, bloodType: String, gender: String, role: String, latitude: Double, longitude: Double, email: String?, isEmailMode: Boolean) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("O+") }
    var gender by remember { mutableStateOf("Male") }
    var role by remember { mutableStateOf("User") }
    var latitudeStr by remember { mutableStateOf("16.4326") }
    var longitudeStr by remember { mutableStateOf("80.9928") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showLangDialog by remember { mutableStateOf(false) }

    if (showLangDialog) {
        LanguageSelectionDialog(
            currentLang = lang,
            onDismiss = { showLangDialog = false },
            onSelectLanguage = { selected ->
                onLanguageChange(selected)
                showLangDialog = false
            }
        )
    }

    val bloodTypesList = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = AppTranslation.translate("auth_create_account_title", lang),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = SlateDark,
            textAlign = TextAlign.Center
        )

        // Dropdown Dialog Trigger button for Language Change (Requirement 4)
        Card(
            onClick = { showLangDialog = true },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("signup_change_lang_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Translate Icon",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = when (lang) {
                        "en" -> "🌐 English (US)"
                        "hi" -> "🌐 हिंदी (HI)"
                        "te" -> "🌐 తెలుగు (TE)"
                        "ta" -> "🌐 தமிழ் (TA)"
                        else -> "🌐 Select Language"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
            }
        }

        // Role Selection
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = AppTranslation.translate("auth_register_as", lang),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = SlateGrey,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("User", "Blood Bank").forEach { currentRole ->
                    val isSelected = role == currentRole
                    Button(
                        onClick = { role = currentRole },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("signup_role_$currentRole"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) BlueAccent else Color(0xFFF1F5F9),
                            contentColor = if (isSelected) Color.White else SlateGrey
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (currentRole == "Blood Bank") AppTranslation.translate("auth_role_bloodbank", lang) else AppTranslation.translate("auth_role_user", lang),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (role == "Blood Bank") AppTranslation.translate("auth_blood_bank_name", lang) else AppTranslation.translate("auth_full_name", lang)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "name_icon", tint = BlueAccent) },
            singleLine = true,
            colors = authTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_name_input")
        )

        // Email (Unified in one form)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address (Optional)") },
            placeholder = { Text("example@domain.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "email_icon", tint = BlueAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            colors = authTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_email_input")
        )

        // Phone (Mandatory)
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(AppTranslation.translate("auth_phone_label", lang) + " *") },
            placeholder = { Text("+91 9xxxx xxxxx") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "phone_icon", tint = BlueAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            colors = authTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_phone_input")
        )

        // PIN
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text(AppTranslation.translate("auth_create_pin", lang) + " *") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "pin_icon", tint = BlueAccent) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            colors = authTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_pin_input")
        )

        // Blood Type
        if (role == "User") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = AppTranslation.translate("auth_blood_type_group", lang),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    bloodTypesList.forEach { type ->
                        val selected = bloodType == type
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selected) BlueAccent else Color(0xFFF1F5F9),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { bloodType = type }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("signup_bt_$type")
                        ) {
                            Text(
                                text = type,
                                color = if (selected) Color.White else SlateGrey,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Gender
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = AppTranslation.translate("auth_gender", lang),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Male", "Female").forEach { gen ->
                        val isSelected = gender == gen
                        Button(
                            onClick = { gender = gen },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("signup_gender_$gen"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) BlueAccent else Color(0xFFF1F5F9),
                                contentColor = if (isSelected) Color.White else SlateGrey
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (gen == "Male") AppTranslation.translate("auth_gender_male", lang) else AppTranslation.translate("auth_gender_female", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        errorMsg?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Sign up Submit button
        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                scope.launch {
                    delay(80)
                    val lat = latitudeStr.toDoubleOrNull() ?: 16.4326
                    val lon = longitudeStr.toDoubleOrNull() ?: 80.9928
                    if (name.isBlank() || phone.isBlank() || pin.isBlank()) {
                        errorMsg = "Please fill in all mandatory details: Name, Phone, and PIN/Password."
                    } else {
                        errorMsg = null
                        onSignUp(name, phone, pin, bloodType, gender, role, lat, lon, if (email.isBlank()) null else email, false)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("signup_submit_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = AppTranslation.translate("auth_signup_btn", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        // Back to sign in link
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onNavigateToSignIn()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppTranslation.translate("auth_already_have_account", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGrey
            )
            Text(
                text = AppTranslation.translate("auth_login_btn", lang),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BlueAccent
            )
        }
    }
}

@Composable
fun VerifyOtpForm(
    generatedOtp: String,
    lang: String,
    onVerifyOtp: (code: String) -> Unit,
    onBackToSignIn: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var otpInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AppTranslation.translate("auth_phone_verification", lang),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = SlateDark,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BlueAccent.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, BlueAccent.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIFESTREAM PHONE GATEWAY SMS",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = BlueAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppTranslation.translate("auth_otp_sent_msg", lang),
                    fontSize = 11.sp,
                    color = SlateGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = generatedOtp,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = SuccessGreen,
                    letterSpacing = 4.sp
                )
            }
        }

        OutlinedTextField(
            value = otpInput,
            onValueChange = { if (it.length <= 6) otpInput = it },
            label = { Text(AppTranslation.translate("auth_enter_otp", lang)) },
            placeholder = { Text("e.g. 123456") },
            leadingIcon = { Icon(Icons.Default.Check, contentDescription = "OTP Icon", tint = BlueAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = authTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_code_input")
        )

        errorMsg?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                scope.launch {
                    delay(80)
                    if (otpInput.length < 6) {
                        errorMsg = AppTranslation.translate("auth_enter_otp_error", lang)
                    } else {
                        errorMsg = null
                        onVerifyOtp(otpInput)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("otp_submit_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = AppTranslation.translate("auth_confirm_verification", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        Text(
            text = AppTranslation.translate("auth_back_to_signin", lang),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = BlueAccent,
            modifier = Modifier
                .clickable {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onBackToSignIn()
                }
                .padding(vertical = 8.dp)
                .testTag("otp_back_to_signin")
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLang: String,
    onDismiss: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Language",
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Language / भाषा चुनें",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val languages = listOf(
                    "en" to "English",
                    "hi" to "हिंदी (Hindi)",
                    "te" to "తెలుగు (Telugu)",
                    "ta" to "தமிழ் (Tamil)"
                )
                languages.forEach { (code, name) ->
                    val isSelected = currentLang == code
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(code) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFFFF1F2) else Color.White
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CrimsonPrimary else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CrimsonPrimary else Color.Black
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectLanguage(code) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CrimsonPrimary,
                                    unselectedColor = Color.LightGray
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = CrimsonPrimary, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

