package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodRequest
import com.example.data.model.Hospital
import com.example.ui.viewmodel.LifeStreamViewModel
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateGrey

@Composable
fun UrgentRequestFeedView(
    requests: List<BloodRequest>,
    hospitals: List<Hospital>,
    isEligible: Boolean,
    onPledge: (BloodRequest) -> Unit,
    onSimulateRequest: (hospitalId: Int, name: String, lat: Double, lon: Double, bloodType: String, units: Int, googleMapsLink: String?, urgencyLevel: String) -> Unit,
    isAdmin: Boolean = false,
    onDeleteRequest: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showSimDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Feed Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "URGENT CLINIC NEEDS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
                Text(
                    text = "Verified Blood Needed",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = { showSimDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("simulate_request_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Request",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "NEW REQUEST", 
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No active urgent requests found.", color = SlateGrey)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(requests) { request ->
                    BloodRequestCard(
                        request = request,
                        isEligible = isEligible,
                        onPledgeClicked = { onPledge(request) },
                        isAdmin = isAdmin,
                        onDeleteClicked = { onDeleteRequest?.invoke(request.id) }
                    )
                }
            }
        }
    }

    if (showSimDialog) {
        SimulateRequestDialog(
            hospitals = hospitals,
            onDismiss = { showSimDialog = false },
            onConfirm = { hospId, hospName, lat, lon, bloodType, units, mapsLink, urgencyLevel ->
                onSimulateRequest(hospId, hospName, lat, lon, bloodType, units, mapsLink, urgencyLevel)
                showSimDialog = false
            }
        )
    }
}

@Composable
fun BloodRequestCard(
    request: BloodRequest,
    isEligible: Boolean,
    onPledgeClicked: () -> Unit,
    isAdmin: Boolean = false,
    onDeleteClicked: (() -> Unit)? = null
) {
    val isOpen = request.status == "Open"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_card_${request.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpen) Color.White else Color(0xFFF8F9FA)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOpen) CrimsonPrimary else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOpen) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blood Badge Section
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOpen) CrimsonPrimary else SlateGrey),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = request.bloodTypeNeeded,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "REQ",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Content Section - Stack Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = request.hospitalName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOpen) Color.Black else SlateGrey,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Verified Badge (Green check icon)
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Hospital",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp).testTag("hospital_verified_check")
                        )
                    }

                    if (isAdmin && onDeleteClicked != null) {
                        IconButton(
                            onClick = onDeleteClicked,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("admin_delete_request_${request.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete request",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                val reqContext = LocalContext.current
                Row(
                    modifier = Modifier
                        .clickable {
                            val mapsUrl = if (!request.googleMapsLink.isNullOrBlank()) {
                                request.googleMapsLink
                            } else {
                                "https://www.google.com/maps/search/?api=1&query=${request.hospitalLatitude},${request.hospitalLongitude}"
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                            reqContext.startActivity(intent)
                        }
                        .padding(vertical = 2.dp),
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
                        text = "Open Location in Google Maps",
                        fontSize = 11.sp,
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Ref: ${request.patientReferenceId}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateGrey,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val (badgeBg, badgeText) = when (request.urgencyLevel.lowercase()) {
                            "critical", "emergency" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                            "high" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                            "medium" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                            else -> Color(0xFFECEFF1) to Color(0xFF37474F)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = request.urgencyLevel.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText
                            )
                        }
                    }
                    Text(
                        text = "${request.unitsRequired} Units Needed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOpen) CrimsonPrimary else SlateGrey
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                if (isOpen) {
                    if (isEligible) {
                        Button(
                            onClick = onPledgeClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("pledge_button_${request.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Pledge to Donate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Cooldown lockout state
                        Button(
                            onClick = onPledgeClicked, // VM handles block gracefully with customized snackbars/alerts too
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("pledge_button_locked_${request.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = SlateGrey
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked icon",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "In Wait Period - Locked",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .background(
                                Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HELP SECURED (Pre-tested Blood Given)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulateRequestDialog(
    hospitals: List<Hospital>,
    onDismiss: () -> Unit,
    onConfirm: (hospitalId: Int, name: String, lat: Double, lon: Double, bloodType: String, units: Int, googleMapsLink: String?, urgencyLevel: String) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    
    // Step 1 States
    var selectedHospital by remember { mutableStateOf(hospitals.firstOrNull()) }
    var isCustomHospital by remember { mutableStateOf(false) }
    var customHospitalName by remember { mutableStateOf("") }
    var customMapsLink by remember { mutableStateOf("") }
    var expandedHospDropdown by remember { mutableStateOf(false) }

    // Step 2 States
    var selectedBloodType by remember { mutableStateOf("O-") }
    val bloodTypesList = listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")

    // Step 3 States
    var selectedUrgency by remember { mutableStateOf("High") }
    val urgencyLevelsList = listOf(
        Triple("Low", "Stable / Scheduled Transfusion", Color(0xFF2E7D32)),
        Triple("Medium", "Required Within 24 Hours", Color(0xFFE65100)),
        Triple("High", "Immediate Match Required", Color(0xFFD84315)),
        Triple("Critical", "Life-Threatening Emergency", Color(0xFFC62828))
    )
    var unitsRequired by remember { mutableStateOf(3) }

    // Validation helper variables
    val isStep1Valid = if (isCustomHospital) {
        customHospitalName.isNotBlank()
    } else {
        selectedHospital != null
    }

    val isStep2Valid = selectedBloodType in bloodTypesList
    val isStep3Valid = selectedUrgency.isNotBlank() && unitsRequired in 1..10

    fun parseLatLngFromUrl(url: String): Pair<Double, Double> {
        try {
            val regex = Regex("""([+-]?\d+\.\d+)\s*,\s*([+-]?\d+\.\d+)""")
            val match = regex.find(url)
            if (match != null) {
                val lat = match.groupValues[1].toDoubleOrNull()
                val lon = match.groupValues[2].toDoubleOrNull()
                if (lat != null && lon != null) {
                    return Pair(lat, lon)
                }
            }
        } catch (e: Exception) {}
        val jitterLat = 16.4326 + (Math.random() - 0.5) * 0.03
        val jitterLon = 80.9928 + (Math.random() - 0.5) * 0.03
        return Pair(jitterLat, jitterLon)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("blood_request_wizard_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Emergency Blood Icon",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "New Blood Request",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Emergency Logistics Broadcast",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Stepper Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (step in 1..3) {
                        val stepTitle = when (step) {
                            1 -> "Location"
                            2 -> "Blood Type"
                            else -> "Urgency"
                        }
                        val isCurrent = step == currentStep
                        val isPassed = step < currentStep

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFFDC2626)
                                            isPassed -> Color(0xFF16A34A)
                                            else -> Color(0xFFE2E8F0)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPassed) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$step",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.White else Color(0xFF64748B)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stepTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) Color(0xFF0F172A) else Color(0xFF64748B),
                                maxLines = 1
                            )
                        }

                        if (step < 3) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(2.dp)
                                    .background(
                                        if (step < currentStep) Color(0xFF16A34A) else Color(0xFFE2E8F0)
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content Area for current step
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentStep) {
                        1 -> {
                            // Step 1: Location & Hospital Details
                            Text(
                                text = "Step 1 of 3: Hospital & Destination",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Select a registered facility or enter custom emergency coordinates.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Segmented Selector for Registered vs Custom
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isCustomHospital) Color.White else Color.Transparent)
                                        .clickable {
                                            isCustomHospital = false
                                            if (selectedHospital == null && hospitals.isNotEmpty()) {
                                                selectedHospital = hospitals.first()
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Network Hospital",
                                        fontSize = 11.sp,
                                        fontWeight = if (!isCustomHospital) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isCustomHospital) Color(0xFF0F172A) else Color(0xFF64748B)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCustomHospital) Color.White else Color.Transparent)
                                        .clickable { isCustomHospital = true }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Custom / Other",
                                        fontSize = 11.sp,
                                        fontWeight = if (isCustomHospital) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCustomHospital) Color(0xFF0F172A) else Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (!isCustomHospital) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedHospDropdown,
                                    onExpandedChange = { expandedHospDropdown = !expandedHospDropdown }
                                ) {
                                    OutlinedTextField(
                                        value = selectedHospital?.name ?: "Select Hospital",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select Hospital / Blood Bank", fontSize = 11.sp) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHospDropdown) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                            .fillMaxWidth()
                                            .testTag("step_1_hospital_dropdown"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFDC2626),
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF0F172A),
                                            focusedContainerColor = Color(0xFFF8FAFC),
                                            unfocusedContainerColor = Color(0xFFF8FAFC)
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedHospDropdown,
                                        onDismissRequest = { expandedHospDropdown = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        hospitals.forEach { hospital ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(hospital.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                        Text("Gudivada, AP • Verified Partner Facility", fontSize = 10.sp, color = Color(0xFF64748B))
                                                    }
                                                },
                                                onClick = {
                                                    selectedHospital = hospital
                                                    isCustomHospital = false
                                                    expandedHospDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                selectedHospital?.let { hosp ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFDCFCE7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Verified Medical Center",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Verified Medical Center Active",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF14532D)
                                                )
                                                Text(
                                                    text = "GPS Coordinates: ${String.format("%.4f", hosp.latitude)}, ${String.format("%.4f", hosp.longitude)}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF15803D)
                                                )
                                                Text(
                                                    text = "Automated high-speed 15 km donor radius alerts enabled",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = customHospitalName,
                                    onValueChange = { customHospitalName = it },
                                    label = { Text("Custom Hospital Name *", fontSize = 11.sp) },
                                    placeholder = { Text("Enter hospital / emergency center name", fontSize = 11.sp) },
                                    singleLine = true,
                                    isError = customHospitalName.isBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("step_1_custom_hospital_name_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFDC2626),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        cursorColor = Color(0xFFDC2626),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC)
                                    )
                                )
                                if (customHospitalName.isBlank()) {
                                    Text(
                                        text = "Hospital name is required for custom entries.",
                                        color = Color(0xFFDC2626),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = customMapsLink,
                                    onValueChange = { customMapsLink = it },
                                    label = { Text("Google Maps Link / GPS Coordinates (Optional)", fontSize = 11.sp) },
                                    placeholder = { Text("Paste link or '16.4326, 80.9928'", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("step_1_custom_hospital_maps_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFDC2626),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        cursorColor = Color(0xFFDC2626),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC)
                                    )
                                )

                                val coordinates = remember(customMapsLink) { parseLatLngFromUrl(customMapsLink) }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Coordinates detected",
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Target Coordinates: ${String.format("%.4f", coordinates.first)}, ${String.format("%.4f", coordinates.second)}",
                                            color = Color(0xFF15803D),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Step 2: Blood Type Required
                            Text(
                                text = "Step 2 of 3: Required Blood Type",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Select the exact blood group required. Matching donors will be alerted.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // 4x2 grid of buttons representing Blood Types
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val rows = bloodTypesList.chunked(4)
                                rows.forEach { rowTypes ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowTypes.forEach { type ->
                                            val isSelected = selectedBloodType == type
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(54.dp)
                                                    .clickable { selectedBloodType = type }
                                                    .testTag("blood_type_option_$type"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFFDC2626) else Color(0xFFF8FAFC)
                                                ),
                                                border = BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFFB91C1C) else Color(0xFFE2E8F0)
                                                ),
                                                elevation = CardDefaults.cardElevation(
                                                    defaultElevation = if (isSelected) 2.dp else 0.dp
                                                )
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = type,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else Color(0xFF0F172A)
                                                        )
                                                        val badgeText = when (type) {
                                                            "O-" -> "Universal"
                                                            "O+" -> "High Need"
                                                            "AB+" -> "Plasma"
                                                            else -> null
                                                        }
                                                        if (badgeText != null) {
                                                            Text(
                                                                text = badgeText,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = if (isSelected) Color(0xFFFEE2E2) else Color(0xFF94A3B8)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Compatibility hint card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Blood Compatibility",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedBloodType == "O-") {
                                            "Selected: O- (Universal donor). Donors with O- can help all patients in critical trauma care."
                                        } else {
                                            "Selected: $selectedBloodType. LifeStream matches $selectedBloodType recipients with matching donors and universal emergency reserves."
                                        },
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E40AF),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        3 -> {
                            // Step 3: Urgency Level & Units Needed
                            Text(
                                text = "Step 3 of 3: Priority & Quantity",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Define dispatch severity and target transfusion units.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            // 4 Urgency Cards
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                urgencyLevelsList.forEach { (level, desc, color) ->
                                    val isSelected = selectedUrgency == level
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedUrgency = level }
                                            .testTag("urgency_level_$level"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) color.copy(alpha = 0.08f) else Color.White
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) color else Color(0xFFE2E8F0)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = level.uppercase(),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = color
                                                )
                                                Text(
                                                    text = desc,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = color,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Units Required Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "UNITS REQUIRED",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B),
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "$unitsRequired Units (~${unitsRequired * 350} mL)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { if (unitsRequired > 1) unitsRequired-- },
                                            enabled = unitsRequired > 1,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (unitsRequired > 1) Color.White else Color(0xFFE2E8F0))
                                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                        ) {
                                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        }

                                        Slider(
                                            value = unitsRequired.toFloat(),
                                            onValueChange = { unitsRequired = it.toInt() },
                                            valueRange = 1f..10f,
                                            steps = 8,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFFDC2626),
                                                activeTrackColor = Color(0xFFDC2626),
                                                inactiveTrackColor = Color(0xFFE2E8F0)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 8.dp)
                                                .testTag("units_required_slider")
                                        )

                                        IconButton(
                                            onClick = { if (unitsRequired < 10) unitsRequired++ },
                                            enabled = unitsRequired < 10,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (unitsRequired < 10) Color.White else Color(0xFFE2E8F0))
                                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase Units",
                                                tint = Color(0xFF0F172A),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row (Back/Cancel & Next/Send)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep--
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("wizard_back_or_cancel_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF64748B)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentStep > 1) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (currentStep > 1) "Back" else "Cancel",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                // Confirm and send
                                if (isCustomHospital) {
                                    if (customHospitalName.isNotBlank()) {
                                        val parsedLatLng = parseLatLngFromUrl(customMapsLink)
                                        onConfirm(
                                            -1,
                                            customHospitalName,
                                            parsedLatLng.first,
                                            parsedLatLng.second,
                                            selectedBloodType,
                                            unitsRequired,
                                            customMapsLink.ifBlank { null },
                                            selectedUrgency
                                        )
                                    }
                                } else {
                                    val hosp = selectedHospital
                                    if (hosp != null) {
                                        onConfirm(
                                            hosp.id,
                                            hosp.name,
                                            hosp.latitude,
                                            hosp.longitude,
                                            selectedBloodType,
                                            unitsRequired,
                                            null,
                                            selectedUrgency
                                        )
                                    }
                                }
                            }
                        },
                        enabled = when (currentStep) {
                            1 -> isStep1Valid
                            2 -> isStep2Valid
                            else -> isStep3Valid
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("wizard_next_or_confirm_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            disabledContainerColor = Color(0xFFE2E8F0),
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFF94A3B8)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentStep < 3) "Next Step" else "Send Alert Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (currentStep < 3) {
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                                } else {
                                    Icons.Default.Send
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
