package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateGrey

/**
 * Deliverable 4: In-App Medical & Legal Disclaimer Component (Markdown styled)
 */
@Composable
fun DisclaimerView(
    onAccepted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("disclaimer_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Professional clinical banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Shield",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "LIFESTREAM",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateGrey,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Medical & Legal Agreement",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.tertiary, thickness = 1.dp)

                // Scrollable Markdown-like clinical disclaimer text
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .verticalScroll(scrollState)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(14.dp)
                ) {
                    // Document Header
                    Text(
                        text = "LEGAL STATEMENT & PROTOCOLS",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Clause 1: No Peer-to-Peer Transfers
                    DisclaimerClauseHeader(num = "1", title = "NO DIRECT BLOOD TRANSFERS")
                    DisclaimerClauseBody(
                        text = "LifeStream connects blood donors with registered blood banks and official hospitals. We do NOT allow private or direct blood transfers between people. All blood donations must happen at official partner clinics."
                    )

                    // Clause 2: Replacement Token Model
                    DisclaimerClauseHeader(num = "2", title = "SAVING LIVES QUICKLY")
                    DisclaimerClauseBody(
                        text = "This app uses a replacement system to save lives fast. When you donate blood, it goes to the hospital's stock. In return, the hospital immediately gives safe, pre-tested blood from their cold storage to the patient in need. This avoids long delay times and saves lives instantly."
                    )

                    // Clause 3: 56-Day Cooldown Lockdown
                    DisclaimerClauseHeader(num = "3", title = "56-DAY WAITING TIME")
                    DisclaimerClauseBody(
                        text = "For your safety, you must wait at least 56 days between whole blood donations. This helps your body make new blood cells. If you donated in the last 56 days, your profile will show as locked. You can only donate again once your wait time is over."
                    )

                    // Clause 4: Zero Liability and Warranties
                    DisclaimerClauseHeader(num = "4", title = "OUR RESPONSIBILITY")
                    DisclaimerClauseBody(
                        text = "LifeStream is a tool to coordinate blood needs and is not a medical company or hospital. We do not do medical testing or draw blood. All safety checks and collections are done by licensed hospital nurses and doctors."
                    )

                    // Contact marker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Disclaimer info",
                            tint = SlateGrey,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reference Protocol: FDA-21-CFR-640 / WHO Blood Guidelines",
                            fontSize = 10.sp,
                            color = SlateGrey,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Acknowledge and proceed actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "By clicking below, you agree that you understand and accept all terms and rules listed above.",
                        fontSize = 11.sp,
                        color = SlateGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        onClick = onAccepted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("accept_disclaimer_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "I AGREE & CONTINUE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DisclaimerClauseHeader(num: String, title: String) {
    Text(
        text = "CLAUSE $num: $title",
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
    )
}

@Composable
fun DisclaimerClauseBody(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = SlateGrey,
        lineHeight = 16.sp,
        textAlign = TextAlign.Justify,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
