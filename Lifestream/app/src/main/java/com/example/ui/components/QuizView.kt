package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTranslation
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateGrey
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningRed

@Composable
fun QuizView(
    step: Int,
    eligible: Boolean,
    lang: String = "en",
    onAnswer: (questionIndex: Int, answer: Boolean) -> Unit,
    onReset: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quiz_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Quiz Icon",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppTranslation.translateQuiz("quick_check", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary
                    )
                }
                if (step in 1..8) {
                    Text(
                        text = "${AppTranslation.translateQuiz("step", lang)} $step ${AppTranslation.translateQuiz("of", lang)} 8",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateGrey,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (step in 1..8) {
                LinearProgressIndicator(
                    progress = { (step - 1) / 8f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .testTag("quiz_progress"),
                    color = CrimsonPrimary,
                    trackColor = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (step) {
                1 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q1", lang),
                    description = AppTranslation.translateQuiz("q1_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(1, true) },
                    onNoSelected = { onAnswer(1, false) }
                )
                2 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q2", lang),
                    description = AppTranslation.translateQuiz("q2_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(2, true) },
                    onNoSelected = { onAnswer(2, false) }
                )
                3 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q3", lang),
                    description = AppTranslation.translateQuiz("q3_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(3, false) }, // YES means they had a tattoo and are INELIGIBLE (so we pass false)
                    onNoSelected = { onAnswer(3, true) }  // NO means they are clean and ELIGIBLE (so we pass true)
                )
                4 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q4", lang),
                    description = AppTranslation.translateQuiz("q4_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(4, false) }, // YES is ineligible
                    onNoSelected = { onAnswer(4, true) } // NO is eligible
                )
                5 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q5", lang),
                    description = AppTranslation.translateQuiz("q5_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(5, true) }, // YES is eligible
                    onNoSelected = { onAnswer(5, false) } // NO is ineligible
                )
                6 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q6", lang),
                    description = AppTranslation.translateQuiz("q6_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(6, false) }, // YES is ineligible
                    onNoSelected = { onAnswer(6, true) } // NO is eligible
                )
                7 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q7", lang),
                    description = AppTranslation.translateQuiz("q7_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(7, false) }, // YES is ineligible
                    onNoSelected = { onAnswer(7, true) } // NO is eligible
                )
                8 -> QuizQuestion(
                    question = AppTranslation.translateQuiz("q8", lang),
                    description = AppTranslation.translateQuiz("q8_desc", lang),
                    yesText = AppTranslation.translateQuiz("yes", lang),
                    noText = AppTranslation.translateQuiz("no", lang),
                    onYesSelected = { onAnswer(8, false) }, // YES is ineligible
                    onNoSelected = { onAnswer(8, true) } // NO is eligible
                )
                else -> {
                    // Results Step (9)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (eligible) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Eligible Check",
                                tint = SuccessGreen,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = AppTranslation.translate("quiz_success_title", lang),
                                style = MaterialTheme.typography.titleLarge,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppTranslation.translateQuiz("success_body", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGrey,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Ineligible block",
                                tint = WarningRed,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = AppTranslation.translate("quiz_fail_title", lang),
                                style = MaterialTheme.typography.titleLarge,
                                color = WarningRed,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppTranslation.translateQuiz("fail_body", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGrey,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = onReset,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(AppTranslation.translate("quiz_re_test", lang))
                            }

                            if (eligible) {
                                Button(
                                    onClick = onNext,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CrimsonPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(AppTranslation.translate("go_to_home", lang))
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
fun QuizQuestion(
    question: String,
    description: String,
    yesText: String = "YES",
    noText: String = "NO",
    onYesSelected: () -> Unit,
    onNoSelected: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = SlateGrey,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onNoSelected,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(48.dp)
                    .testTag("quiz_no_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = noText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onYesSelected,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(48.dp)
                    .testTag("quiz_yes_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = yesText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
