package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Hospital
import com.example.data.model.User
import com.example.data.model.BloodRequest
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningRed
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateGrey
import com.example.ui.theme.SlateLight
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.AppTranslation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import org.json.JSONObject
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun DashboardView(
    user: User?,
    hospitals: List<Hospital>,
    daysLeft: Int,
    isEligible: Boolean,
    onToggleCooldown: (Boolean) -> Unit,
    onStartEligibilityCheck: () -> Unit,
    lang: String = "en",
    onToggleGender: (String) -> Unit = {},
    themeMode: String = "SYSTEM",
    onThemeModeChanged: (String) -> Unit = {},
    onTriggerTestNotification: () -> Unit = {},
    allUsers: List<User> = emptyList(),
    allRequests: List<BloodRequest> = emptyList(),
    onDeleteUser: ((Int) -> Unit)? = null,
    onUpdateHospitalStockAndVerify: ((Hospital, Boolean, String) -> Unit)? = null,
    onDeleteRequest: ((Int) -> Unit)? = null,
    onToggleRequestStatus: ((BloodRequest) -> Unit)? = null,
    onRequestTabSwitchToFeed: (() -> Unit)? = null,
    onSimulateRequest: ((hospitalId: Int, name: String, lat: Double, lon: Double, bloodType: String, units: Int, googleMapsLink: String?, urgencyLevel: String) -> Unit)? = null,
    onUpdateUserLocation: ((Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedHospitalForMap by remember { mutableStateOf<Hospital?>(null) }
    
    // Choose the initially selected hospital if none is selected
    LaunchedEffect(hospitals) {
        if (selectedHospitalForMap == null && hospitals.isNotEmpty()) {
            selectedHospitalForMap = hospitals.first()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        UserHeaderCard(
            user = user,
            isEligible = isEligible,
            onToggleCooldown = onToggleCooldown,
            onToggleGender = onToggleGender,
            lang = lang
        )

        // User Role Quick Actions Flow (Request & Donate blood)
        if (user?.role == "User") {
            var interestedInDonating by remember { mutableStateOf(true) }
            var feedbackText by remember { mutableStateOf("") }
            var selectedStars by remember { mutableStateOf(5) }
            var showFeedbackThanks by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donor_interest_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Part 1: Donor Interest Tracker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔴 LIVE DONOR STATUS CHECK",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Are you currently interested and available to donate blood?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Switch(
                            checked = interestedInDonating,
                            onCheckedChange = { interestedInDonating = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen,
                                uncheckedThumbColor = SlateGrey,
                                uncheckedTrackColor = Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier.testTag("donor_interest_switch")
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = if (interestedInDonating) 
                            "🟢 You are active! Nearby hospitals and users can request your help in real time." 
                        else 
                            "🟡 Paused. You will not be listed for active local donation drives.",
                        fontSize = 11.sp,
                        color = if (interestedInDonating) SuccessGreen else SlateGrey,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_quick_actions_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "YOUR DONOR & RECIPIENT ACTIONS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "As a verified user on the LifeStream network, you can request emergency blood reserves for nearby hospitals or view active clinic drives to make a donation pledge.",
                        fontSize = 12.sp,
                        color = SlateGrey,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var showUserRequestDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showUserRequestDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("user_request_blood_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Request Blood", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Blood", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onRequestTabSwitchToFeed?.invoke() },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("user_donate_blood_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = SlateDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Donate Blood", modifier = Modifier.size(16.dp), tint = CrimsonPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Donate Blood", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (showUserRequestDialog && onSimulateRequest != null) {
                            SimulateRequestDialog(
                                hospitals = hospitals,
                                onDismiss = { showUserRequestDialog = false },
                                onConfirm = { hospId, hospName, lat, lon, bloodType, units, mapsLink, urgency ->
                                    onSimulateRequest(hospId, hospName, lat, lon, bloodType, units, mapsLink, urgency)
                                    showUserRequestDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Blood Bank Storage Control Panel (Donate & take blood)
        if (user?.role == "Blood Bank") {
            var selectedHospByBank by remember { mutableStateOf<Hospital?>(hospitals.firstOrNull()) }
            var showSuccessMessage by remember { mutableStateOf<String?>(null) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("blood_bank_storage_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BLOOD BANK STORAGE CONTROLLER",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Update the live inventory level of tested, certified blood units. Click + to add (donate) blood units or - to take (allocate) units.",
                        fontSize = 11.sp,
                        color = SlateGrey,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dropdown for Hospital Selection
                    var expandedHospSelector by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedHospSelector = true },
                            modifier = Modifier.fillMaxWidth().testTag("bank_hospital_select_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Managing: ${selectedHospByBank?.name ?: "None"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                        DropdownMenu(
                            expanded = expandedHospSelector,
                            onDismissRequest = { expandedHospSelector = false }
                        ) {
                            hospitals.forEach { hosp ->
                                DropdownMenuItem(
                                    text = { Text(hosp.name, fontSize = 12.sp) },
                                    onClick = {
                                        selectedHospByBank = hosp
                                        expandedHospSelector = false
                                    }
                                )
                            }
                        }
                    }

                    selectedHospByBank?.let { currentHosp ->
                        Spacer(modifier = Modifier.height(14.dp))

                        val stockLevels = remember(currentHosp.currentStockLevels, currentHosp.id) {
                            parseStockLevels(currentHosp.currentStockLevels).toMutableMap()
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stockLevels.forEach { (bloodType, units) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(CrimsonPrimary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = bloodType,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = CrimsonPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "$units Units Available",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Take Blood Button
                                        IconButton(
                                            onClick = {
                                                if (units > 0) {
                                                    val newMap = stockLevels.toMap().toMutableMap()
                                                    newMap[bloodType] = units - 1
                                                    val newJson = serializeStockLevels(newMap)
                                                    onUpdateHospitalStockAndVerify?.invoke(currentHosp, currentHosp.verifiedStatus, newJson)
                                                    showSuccessMessage = "Took 1 unit of $bloodType blood from ${currentHosp.name}"
                                                }
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp))
                                                .testTag("take_blood_${bloodType}_btn")
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SlateDark)
                                        }

                                        // Donate/Add Blood Button
                                        IconButton(
                                            onClick = {
                                                val newMap = stockLevels.toMap().toMutableMap()
                                                newMap[bloodType] = units + 1
                                                val newJson = serializeStockLevels(newMap)
                                                onUpdateHospitalStockAndVerify?.invoke(currentHosp, currentHosp.verifiedStatus, newJson)
                                                showSuccessMessage = "Donated 1 unit of $bloodType blood to ${currentHosp.name}"
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                .testTag("donate_blood_${bloodType}_btn")
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SuccessGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    showSuccessMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = msg,
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Admin Control Suite (Control everything)
        if (user?.role == "Admin") {
            var selectedAdminTab by remember { mutableStateOf(0) } // 0: Users, 1: Hospital Stocks

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_logistics_suite_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ADMIN LOGISTICS CONTROL SUITE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(CrimsonPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ROOT SYSTEM ADMIN", color = CrimsonPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { selectedAdminTab = 0 },
                            modifier = Modifier.weight(1f).height(36.dp).testTag("admin_users_tab_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAdminTab == 0) CrimsonPrimary else MaterialTheme.colorScheme.tertiary,
                                contentColor = if (selectedAdminTab == 0) Color.White else SlateGrey
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Users/Banks (${allUsers.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedAdminTab = 1 },
                            modifier = Modifier.weight(1f).height(36.dp).testTag("admin_clinics_tab_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAdminTab == 1) CrimsonPrimary else MaterialTheme.colorScheme.tertiary,
                                contentColor = if (selectedAdminTab == 1) Color.White else SlateGrey
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clinics (${hospitals.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedAdminTab = 2 },
                            modifier = Modifier.weight(1f).height(36.dp).testTag("admin_requests_tab_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAdminTab == 2) CrimsonPrimary else MaterialTheme.colorScheme.tertiary,
                                contentColor = if (selectedAdminTab == 2) Color.White else SlateGrey
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Requests (${allRequests.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedAdminTab == 0) {
                        // User and Blood Bank management list with filtering
                        var userFilterState by remember { mutableStateOf("ALL") } // "ALL", "DONORS", "BANKS"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AssistChip(
                                onClick = { userFilterState = "ALL" },
                                label = { Text("All (${allUsers.size})", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (userFilterState == "ALL") CrimsonPrimary.copy(alpha = 0.12f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (userFilterState == "ALL") CrimsonPrimary else CardBorderColor),
                                modifier = Modifier.testTag("filter_users_all")
                            )
                            AssistChip(
                                onClick = { userFilterState = "DONORS" },
                                label = { Text("Donors (${allUsers.count { it.role == "User" }})", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (userFilterState == "DONORS") CrimsonPrimary.copy(alpha = 0.12f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (userFilterState == "DONORS") CrimsonPrimary else CardBorderColor),
                                modifier = Modifier.testTag("filter_users_donors")
                            )
                            AssistChip(
                                onClick = { userFilterState = "BANKS" },
                                label = { Text("Blood Banks (${allUsers.count { it.role == "Blood Bank" }})", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (userFilterState == "BANKS") CrimsonPrimary.copy(alpha = 0.12f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (userFilterState == "BANKS") CrimsonPrimary else CardBorderColor),
                                modifier = Modifier.testTag("filter_users_banks")
                            )
                        }

                        val filteredUsers = when (userFilterState) {
                            "DONORS" -> allUsers.filter { it.role == "User" }
                            "BANKS" -> allUsers.filter { it.role == "Blood Bank" }
                            else -> allUsers
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredUsers.forEach { managedUser ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = managedUser.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = when (managedUser.role) {
                                                            "Admin" -> CrimsonPrimary.copy(alpha = 0.1f)
                                                            "Blood Bank" -> Color(0xFFFFB300).copy(alpha = 0.1f)
                                                            else -> SuccessGreen.copy(alpha = 0.1f)
                                                        },
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = managedUser.role.uppercase(),
                                                    color = when (managedUser.role) {
                                                        "Admin" -> CrimsonPrimary
                                                        "Blood Bank" -> Color(0xFFFFB300)
                                                        else -> SuccessGreen
                                                    },
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Phone: ${managedUser.phone}",
                                            fontSize = 10.sp,
                                            color = SlateGrey
                                        )
                                        val context = LocalContext.current
                                        Row(
                                            modifier = Modifier.clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${managedUser.latitude},${managedUser.longitude}"))
                                                context.startActivity(intent)
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = "Map Location",
                                                tint = CrimsonPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${getDistrictName(managedUser.latitude, managedUser.longitude)} (📍 Open in Google Maps)",
                                                fontSize = 9.sp,
                                                color = CrimsonPrimary,
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = TextDecoration.Underline
                                            )
                                        }
                                    }

                                    // Delete action (Admins cannot delete themselves)
                                    if (managedUser.id != user?.id) {
                                        IconButton(
                                            onClick = { onDeleteUser?.invoke(managedUser.id) },
                                            modifier = Modifier.size(32.dp).testTag("delete_user_${managedUser.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete User",
                                                tint = CrimsonPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedAdminTab == 1) {
                        // Hospital clinics list with verification status control
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            hospitals.forEach { currentHosp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentHosp.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(if (currentHosp.verifiedStatus) SuccessGreen else WarningRed, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentHosp.verifiedStatus) "VERIFIED CLINIC" else "UNVERIFIED CLINIC",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (currentHosp.verifiedStatus) SuccessGreen else WarningRed
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onUpdateHospitalStockAndVerify?.invoke(
                                                currentHosp,
                                                !currentHosp.verifiedStatus,
                                                currentHosp.currentStockLevels
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentHosp.verifiedStatus) WarningRed.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f),
                                            contentColor = if (currentHosp.verifiedStatus) WarningRed else SuccessGreen
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp).testTag("toggle_hosp_verification_${currentHosp.id}")
                                    ) {
                                        Text(
                                            text = if (currentHosp.verifiedStatus) "REVOKE" else "APPROVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    } else if (selectedAdminTab == 2) {
                        // Request management list with matching metrics
                        if (allRequests.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No active or historic donation requests on the platform.", fontSize = 11.sp, color = SlateGrey)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (req in allRequests) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, CardBorderColor)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(CrimsonPrimary, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(req.bloodTypeNeeded, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "${req.unitsRequired} Units Required",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "At: ${req.hospitalName}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "Patient Ref: ${req.patientReferenceId}",
                                                            fontSize = 8.sp,
                                                            color = SlateGrey,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    if (req.status == "Open") CrimsonPrimary.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f),
                                                                    RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = req.status.uppercase(),
                                                                color = if (req.status == "Open") CrimsonPrimary else SuccessGreen,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }
                                                    }
                                                    val reqContext = LocalContext.current
                                                    Row(
                                                        modifier = Modifier.clickable {
                                                            val mapsUrl = if (!req.googleMapsLink.isNullOrBlank()) {
                                                                req.googleMapsLink
                                                            } else {
                                                                "https://www.google.com/maps/search/?api=1&query=${req.hospitalLatitude},${req.hospitalLongitude}"
                                                            }
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                                                            reqContext.startActivity(intent)
                                                        },
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Place,
                                                            contentDescription = "Map Location",
                                                            tint = CrimsonPrimary,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "${getDistrictName(req.hospitalLatitude, req.hospitalLongitude)} (📍 View on Google Maps)",
                                                            fontSize = 8.sp,
                                                            color = CrimsonPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            textDecoration = TextDecoration.Underline
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    // Fulfill/Toggle Button
                                                    TextButton(
                                                        onClick = { onToggleRequestStatus?.invoke(req) },
                                                        modifier = Modifier.height(30.dp).testTag("admin_toggle_req_${req.id}"),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                                    ) {
                                                        Text(
                                                            text = if (req.status == "Open") "Fulfill" else "Re-open",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = if (req.status == "Open") SuccessGreen else CrimsonPrimary
                                                        )
                                                    }

                                                    // Cancel / Delete Button
                                                    IconButton(
                                                        onClick = { onDeleteRequest?.invoke(req.id) },
                                                        modifier = Modifier.size(32.dp).testTag("admin_delete_req_${req.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete Request",
                                                            tint = CrimsonPrimary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Display nearest matches using LocationMatchingService math
                                            val nearbyMatched = allUsers.filter { otherUser -> 
                                                otherUser.role == "User" || otherUser.role == "Blood Bank"
                                            }.map { u ->
                                                val earthRadiusKm = 6371.0
                                                val dLat = Math.toRadians(u.latitude - req.hospitalLatitude)
                                                val dLon = Math.toRadians(u.longitude - req.hospitalLongitude)
                                                val lat1Rad = Math.toRadians(req.hospitalLatitude)
                                                val lat2Rad = Math.toRadians(u.latitude)
                                                val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                                        Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad)
                                                val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                                                val dist = earthRadiusKm * c
                                                
                                                val isCompatible = when (req.bloodTypeNeeded) {
                                                    "O-" -> u.getDecryptedBloodType() == "O-"
                                                    "O+" -> u.getDecryptedBloodType() == "O-" || u.getDecryptedBloodType() == "O+"
                                                    "A+" -> u.getDecryptedBloodType() == "A-" || u.getDecryptedBloodType() == "O+" || u.getDecryptedBloodType() == "O-" || u.getDecryptedBloodType() == "A+"
                                                    "A-" -> u.getDecryptedBloodType() == "A-" || u.getDecryptedBloodType() == "O-"
                                                    "B+" -> u.getDecryptedBloodType() == "B-" || u.getDecryptedBloodType() == "O+" || u.getDecryptedBloodType() == "O-" || u.getDecryptedBloodType() == "B+"
                                                    "B-" -> u.getDecryptedBloodType() == "B-" || u.getDecryptedBloodType() == "O-"
                                                    "AB-" -> u.getDecryptedBloodType() != "O+" && u.getDecryptedBloodType() != "A+" && u.getDecryptedBloodType() != "B+" && u.getDecryptedBloodType() != "AB+"
                                                    else -> true // Universal compatibility recipient
                                                }
                                                Triple(u, dist, isCompatible)
                                            }.filter { it.second <= 25.0 }
                                             .sortedBy { it.second }

                                            if (nearbyMatched.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Geospatial Matches within 25km Range limit:",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SlateGrey
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val rowScrollState = rememberScrollState()
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rowScrollState),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    for (matchTuple in nearbyMatched) {
                                                        val matchUser = matchTuple.first
                                                        val distVal = matchTuple.second
                                                        val isComp = matchTuple.third
                                                        val roleColor = if (matchUser.role == "Blood Bank") Color(0xFFFFB300) else Color(0xFF43A047)
                                                        Row(
                                                            modifier = Modifier
                                                                .background(roleColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                .border(1.dp, roleColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 3.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(modifier = Modifier.size(5.dp).background(roleColor, CircleShape))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "${matchUser.name} (${matchUser.getDecryptedBloodType()}) • ${matchUser.gender} • ${matchUser.phone} • ${String.format("%.1f", distVal)}km",
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (isComp) {
                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                Text("✓ Match", fontSize = 8.sp, color = SuccessGreen, fontWeight = FontWeight.Black)
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("No compatible matching users or blood banks detected within 25km.", fontSize = 8.sp, color = WarningRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Row containing Circle Progress and Quick Eligibility Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Cooldown Progress Ring Component (Weight 1.1)
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .height(200.dp)
                    .testTag("cooldown_progress_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val totalDays = if ((user?.gender ?: "Male").equals("Female", ignoreCase = true)) 120 else 90
                    val waitTimeText = when(lang) {
                        "hi" -> "$totalDays दिनों का प्रतीक्षा समय"
                        "te" -> "$totalDays రోజుల నిరీక్షణ సమయం"
                        "ta" -> "$totalDays நாட்கள் காத்திருப்பு காலம்"
                        else -> "$totalDays-DAY WAIT TIME"
                    }
                    Text(
                        text = waitTimeText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey,
                        textAlign = TextAlign.Center
                    )

                    // Draw circular custom canvas progress ring
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .testTag("cooldown_ring"),
                        contentAlignment = Alignment.Center
                    ) {
                        val progressFraction = if (daysLeft > 0) (totalDays - daysLeft) / totalDays.toFloat() else 1f
                        val progressColor = if (isEligible) SuccessGreen else WarningRed
                        
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // Draw background circle
                            drawCircle(
                                color = Color(0xFFE2E8F0),
                                radius = size.minDimension / 2,
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Draw foreground arc
                            drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progressFraction,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Center Status text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isEligible) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SuccessGreen
                                )
                            } else {
                                Text(
                                    text = "$daysLeft",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WarningRed
                                )
                                Text(
                                    text = "DAYS LEFT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isEligible) {
                            AppTranslation.translate("switch_active", lang)
                        } else {
                            AppTranslation.translate("switch_cooldown", lang)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEligible) SuccessGreen else WarningRed,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. Eligibility & State Simulation Quick Check (Weight 0.9)
            Card(
                modifier = Modifier
                    .weight(0.9f)
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Clinics",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Check if you can donate?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Take our quick 3-step safety quiz to start.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 14.sp
                        )
                    }

                    Button(
                        onClick = onStartEligibilityCheck,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("eligibility_check_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Start Quiz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Interactive Map Widget
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("map_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Map Icon",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Partner Blood Banks & Active Donors (Within 25km)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Current User Location Editor inside Map Card (Streamlined without raw coords, with Google Maps link)
                user?.let { currentUser ->
                    var showLocationEditor by remember { mutableStateOf(false) }
                    val currentContext = LocalContext.current

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                            .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "My Location",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${currentUser.latitude},${currentUser.longitude}"))
                                        currentContext.startActivity(intent)
                                    }
                                ) {
                                    Text(
                                        text = "Your Area: ${getDistrictName(currentUser.latitude, currentUser.longitude)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(📍 View on Google Maps)",
                                        fontSize = 10.sp,
                                        color = CrimsonPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                }
                            }

                            TextButton(
                                onClick = { showLocationEditor = !showLocationEditor },
                                modifier = Modifier.height(28.dp).testTag("toggle_location_editor_btn"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (showLocationEditor) "Cancel" else "Change Area",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CrimsonPrimary
                                )
                            }
                        }

                        if (showLocationEditor) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select your current district to update matching range:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateGrey
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val quickDistricts = listOf(
                                    Triple("Railway Station Area", "16.4335", "80.9940"),
                                    Triple("Eluru Road", "16.4385", "80.9972"),
                                    Triple("Anjaneyapeta", "16.4350", "80.9915"),
                                    Triple("Bus Stand Road", "16.4290", "80.9880"),
                                    Triple("Gudivada Main Road", "16.4310", "80.9930"),
                                    Triple("Nehru Chowk", "16.4340", "80.9960")
                                )
                                quickDistricts.forEach { (distName, la, lo) ->
                                    val isCurrent = getDistrictName(currentUser.latitude, currentUser.longitude) == distName
                                    AssistChip(
                                        onClick = {
                                            if (onUpdateUserLocation != null) {
                                                onUpdateUserLocation(la.toDouble(), lo.toDouble())
                                                showLocationEditor = false
                                            }
                                        },
                                        label = { Text(distName, fontSize = 9.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (isCurrent) CrimsonPrimary.copy(alpha = 0.1f) else Color.Transparent
                                        ),
                                        border = BorderStroke(1.dp, if (isCurrent) CrimsonPrimary else CardBorderColor),
                                        modifier = Modifier.testTag("edit_preset_$distName")
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Map Canvas Widget Showing spatial rendering
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSystemInDarkTheme()) Color(0xFF1E1715) else Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_map_background_1784372038634),
                        contentDescription = "Map Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (isSystemInDarkTheme()) 0.35f else 0.8f
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // Draw Grid lines representing GPS coordinate lines
                        val step = 40.dp.toPx()
                        var x = 0f
                        while (x < size.width) {
                            drawLine(Color(0xFFCBD5E1), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                            x += step
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(Color(0xFFCBD5E1), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                            y += step
                        }

                        // Draw radar wave rings (25km range limits)
                        drawCircle(
                            color = CrimsonPrimary.copy(alpha = 0.08f),
                            radius = 65.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = CrimsonPrimary.copy(alpha = 0.15f),
                            radius = 40.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )

                        val centerLat = user?.latitude ?: 16.4326
                        val centerLon = user?.longitude ?: 80.9928

                        // 1. Draw Primary user Location Dot (Center point)
                        drawCircle(
                            color = Color(0xFF1E88E5), // Blue location dot
                            radius = 8.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )

                        // 2. Plot Registered Hospitals / Clinics
                        hospitals.forEach { hospital ->
                            val dist = calculateDist(centerLat, centerLon, hospital.latitude, hospital.longitude)
                            if (dist <= 25.0) {
                                val dy = (hospital.latitude - centerLat) * 111.0
                                val dx = (hospital.longitude - centerLon) * 111.0 * Math.cos(Math.toRadians(centerLat))
                                
                                val pxPerKm = centerX / 25f // 25 km fits on screen
                                val drawX = centerX + (dx * pxPerKm).toFloat()
                                val drawY = centerY - (dy * pxPerKm).toFloat()

                                if (drawX in 0f..size.width && drawY in 0f..size.height) {
                                    drawCircle(
                                        color = CrimsonPrimary,
                                        radius = 6.dp.toPx(),
                                        center = Offset(drawX, drawY)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.dp.toPx(),
                                        center = Offset(drawX, drawY)
                                    )
                                }
                            }
                        }

                        // 3. Plot other network users / blood banks
                        allUsers.forEach { otherUser ->
                            if (user == null || otherUser.id != user.id) {
                                val dist = calculateDist(centerLat, centerLon, otherUser.latitude, otherUser.longitude)
                                if (dist <= 25.0) {
                                    val dy = (otherUser.latitude - centerLat) * 111.0
                                    val dx = (otherUser.longitude - centerLon) * 111.0 * Math.cos(Math.toRadians(centerLat))

                                    val pxPerKm = centerX / 25f
                                    val drawX = centerX + (dx * pxPerKm).toFloat()
                                    val drawY = centerY - (dy * pxPerKm).toFloat()

                                    if (drawX in 0f..size.width && drawY in 0f..size.height) {
                                        val color = if (otherUser.role == "Blood Bank") {
                                            Color(0xFFFFB300) // Gold for Blood Bank
                                        } else if (otherUser.role == "Admin") {
                                            Color(0xFF8E24AA) // Purple for Admin
                                        } else {
                                            Color(0xFF43A047) // Green for standard User/Donor
                                        }
                                        drawCircle(
                                            color = color,
                                            radius = 5.dp.toPx(),
                                            center = Offset(drawX, drawY)
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 1.5.dp.toPx(),
                                            center = Offset(drawX, drawY)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Dynamic Multi-Color Map Legends
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF1E88E5), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${user?.name ?: "Guest"} (You)", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(CrimsonPrimary, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Registered Clinics (${hospitals.size})", color = Color.White, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF43A047), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                val standardUsersCount = allUsers.count { it.role == "User" && (user == null || it.id != user.id) }
                                Text("Network Donors ($standardUsersCount)", color = Color.White, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFB300), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                val banksCount = allUsers.count { it.role == "Blood Bank" && (user == null || it.id != user.id) }
                                Text("Blood Banks ($banksCount)", color = Color.White, fontSize = 8.sp)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(CrimsonPrimary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("25km Range Active", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Map hospital picker listing
                Text(
                    text = "SELECT REGISTERED BANK TO VIEW VERIFIED LIVE STOCK LEVELS:",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    hospitals.forEach { hospital ->
                        val isSelected = selectedHospitalForMap?.id == hospital.id
                        val distance = if (user != null) {
                            calculateDist(user.latitude, user.longitude, hospital.latitude, hospital.longitude)
                        } else 2.5

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedHospitalForMap = hospital }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Clinic",
                                    tint = if (isSelected) CrimsonPrimary else SlateGrey,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = hospital.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Verified Partner • ${getDistrictName(hospital.latitude, hospital.longitude)} ",
                                            fontSize = 10.sp,
                                            color = SlateGrey
                                        )
                                        val hospContext = LocalContext.current
                                        Text(
                                            text = "📍 Maps Link",
                                            fontSize = 10.sp,
                                            color = CrimsonPrimary,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.Underline,
                                            modifier = Modifier.clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${hospital.latitude},${hospital.longitude}"))
                                                hospContext.startActivity(intent)
                                            }
                                        )
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format("%.1f", distance)} km away",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary
                                )
                                Text(
                                    text = "TAP FOR STOCK",
                                    fontSize = 8.sp,
                                    color = SlateGrey,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Inventory stocks widget drawer for Selected Hospital
                selectedHospitalForMap?.let { selected ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selected.name} Stock",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED STOCK",
                                        color = SuccessGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid drawing blood volumes
                            val stockMap = parseStockLevels(selected.currentStockLevels)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                stockMap.entries.take(4).forEach { (bType, count) ->
                                    StockMiniCard(
                                        bloodType = bType,
                                        units = count,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                stockMap.entries.drop(4).take(4).forEach { (bType, count) ->
                                    StockMiniCard(
                                        bloodType = bType,
                                        units = count,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserHeaderCard(
    user: User?,
    isEligible: Boolean,
    onToggleCooldown: (Boolean) -> Unit,
    onToggleGender: (String) -> Unit,
    lang: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_header_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CrimsonPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppTranslation.translate("welcome_back", lang),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = user?.name ?: "Rajesh Kumar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // User blood type display badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = user?.getDecryptedBloodType() ?: "O-",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = AppTranslation.translate("blood_type", lang),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // User verification details
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = AppTranslation.translate("secured_data", lang),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Role: ${user?.role ?: "Donor"} • Phone: ${user?.phone ?: "Not registered"}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun StockMiniCard(
    bloodType: String,
    units: Int,
    modifier: Modifier = Modifier
) {
    val levelColor = when {
        units > 12 -> SuccessGreen
        units > 5 -> Color(0xFFF57C00) // Orange Warning
        else -> WarningRed
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = bloodType,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$units Units",
                fontSize = 10.sp,
                color = levelColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(levelColor.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (units > 20) 1f else units / 20f)
                        .fillMaxHeight()
                        .background(levelColor)
                )
            }
        }
    }
}

// Distance solver helper
fun calculateDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadiusKm * c
}

// Maps coordinate coordinates to named districts
fun getDistrictName(lat: Double, lon: Double): String {
    val quickDistricts = listOf(
        Triple("Railway Station Area", 16.4335, 80.9940),
        Triple("Eluru Road", 16.4385, 80.9972),
        Triple("Anjaneyapeta", 16.4350, 80.9915),
        Triple("Bus Stand Road", 16.4290, 80.9880),
        Triple("Gudivada Main Road", 16.4310, 80.9930),
        Triple("Nehru Chowk", 16.4340, 80.9960)
    )
    var closestName = "Gudivada Main Road"
    var minDist = Double.MAX_VALUE
    for (d in quickDistricts) {
        val dist = calculateDist(lat, lon, d.second, d.third)
        if (dist < minDist) {
            minDist = dist
            closestName = d.first
        }
    }
    return closestName
}

// Simple JSON extraction to map
fun parseStockLevels(jsonString: String): Map<String, Int> {
    return try {
        val json = JSONObject(jsonString)
        val m = mutableMapOf<String, Int>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            m[key] = json.getInt(key)
        }
        m
    } catch (e: Exception) {
        mapOf("O-" to 2, "O+" to 5, "A+" to 8)
    }
}

fun serializeStockLevels(map: Map<String, Int>): String {
    val json = JSONObject()
    map.forEach { (key, value) ->
        json.put(key, value)
    }
    return json.toString()
}
