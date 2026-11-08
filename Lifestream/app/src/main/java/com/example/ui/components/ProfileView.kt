package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.DonationRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    user: User?,
    donationRecords: List<DonationRecord>,
    daysLeft: Int,
    onSaveProfile: (name: String, phone: String, pin: String, gender: String, bloodType: String) -> Unit,
    lang: String = "en",
    modifier: Modifier = Modifier
) {
    if (user == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CrimsonPrimary)
        }
        return
    }

    val decryptedBloodType = remember(user) { user.getDecryptedBloodType() }

    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var pin by remember { mutableStateOf(user.password) }
    var gender by remember { mutableStateOf(user.gender) }
    var bloodType by remember { mutableStateOf(decryptedBloodType) }

    val bloodTypes = listOf("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero visual header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_hero_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CrimsonPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar Placeholder",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Role: ${user.role} | Active Cooldown status: ${user.status}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Encryption advisory info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CLIENT-SIDE SECURITY PROTOCOL",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "In compliance with emergency logistics standards, sensitive details (such as your blood type) are encrypted locally using AES-256 equivalent keys before entering Room db records.",
                    fontSize = 11.sp,
                    color = SlateGrey,
                    lineHeight = 15.sp
                )
            }
        }

        // Recovery Countdown Status Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profile_recovery_status_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RECOVERY COUNTDOWN & ELIGIBILITY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val maxInterval = if (user.gender.equals("Female", ignoreCase = true)) 120f else 90f
                val progress = if (daysLeft > 0) ((maxInterval - daysLeft) / maxInterval).coerceIn(0f, 1f) else 1.0f
                val percentRecovered = (progress * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (daysLeft == 0) {
                            Text(
                                text = "ELIGIBLE TO DONATE 🟢",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your safety interval has completed. You are fully eligible to respond to nearby urgent requests.",
                                fontSize = 11.sp,
                                color = SlateGrey,
                                lineHeight = 15.sp
                            )
                        } else {
                            Text(
                                text = "COOLDOWN ACTIVE ⏳",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CrimsonPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Clinical safety protocol is active. Please allow your body to fully replenish iron levels.",
                                fontSize = 11.sp,
                                color = SlateGrey,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Circular recovery progress
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            color = if (daysLeft == 0) SuccessGreen else CrimsonPrimary,
                            trackColor = Color(0xFFF1F5F9),
                            strokeWidth = 5.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "$percentRecovered%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (daysLeft == 0) SuccessGreen else CrimsonPrimary
                        )
                    }
                }

                if (daysLeft > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$daysLeft days remaining",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary
                        )
                        Text(
                            text = "Safety Interval: ${maxInterval.toInt()} days",
                            fontSize = 11.sp,
                            color = SlateGrey
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = CrimsonPrimary,
                        trackColor = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // Donation History Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profile_donation_history_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OFFICIAL DONATION RECORD HISTORY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (donationRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No past donations recorded in Room Database.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateGrey,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your successful urgent match pledges will appear here as certified records.",
                                fontSize = 11.sp,
                                color = SlateGrey.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        donationRecords.forEachIndexed { index, record ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Blood Drop representation
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFFFFF1F2), shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, CrimsonPrimary.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = record.bloodType,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CrimsonPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.hospitalName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Units: ${record.unitsDonated}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SlateGrey
                                        )
                                        Text(
                                            text = "•",
                                            fontSize = 10.sp,
                                            color = SlateGrey
                                        )
                                        Text(
                                            text = "Ref: ${record.patientReferenceId}",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = SlateGrey
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    // Token issued badge
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "🎟️ TOKEN: ${record.replacementTokenId}",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = dateFormat.format(Date(record.donationDate)),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey,
                                    textAlign = TextAlign.End
                                )
                            }
                            if (index < donationRecords.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Edit Profile fields card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, CardBorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PERSONAL DATA CONTROL",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )

                // Full name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon", tint = CrimsonPrimary, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                // Phone number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Icon", tint = CrimsonPrimary, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_phone_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                // Secret PIN
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Secret PIN / Password", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "PIN Icon", tint = CrimsonPrimary, modifier = Modifier.size(20.dp)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_pin_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Gender Segmented selector
                Text(
                    text = "GENDER IDENTITY (COOLDOWN BASIS)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isMale = gender.equals("Male", ignoreCase = true)
                    Button(
                        onClick = { gender = "Male" },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("profile_gender_male_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMale) CrimsonPrimary else MaterialTheme.colorScheme.tertiary,
                            contentColor = if (isMale) Color.White else SlateGrey
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Male (90d Cooldown)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { gender = "Female" },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("profile_gender_female_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isMale) CrimsonPrimary else MaterialTheme.colorScheme.tertiary,
                            contentColor = if (!isMale) Color.White else SlateGrey
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Female (120d Cooldown)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Blood Type selector
                Text(
                    text = "BLOOD TYPE SELECTOR",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey
                )

                // Grid layout for blood types
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chunked = bloodTypes.chunked(4)
                    chunked.forEach { rowTypes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowTypes.forEach { type ->
                                val isSelected = bloodType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .background(
                                            color = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.background,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) CrimsonPrimary else CardBorderColor,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { bloodType = type }
                                        .testTag("profile_blood_type_$type"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submit/Save button
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank() && pin.isNotBlank()) {
                            onSaveProfile(name, phone, pin, gender, bloodType)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_save_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE CHANGES", fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
