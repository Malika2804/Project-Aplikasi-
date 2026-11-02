package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MedicalSurfaceVariant
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.ToskaContainer
import com.example.ui.theme.ToskaSecondary
import kotlinx.coroutines.delay

@Composable
fun BreathingExerciseDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("Siap?") }
    var currentInstruction by remember { mutableStateOf("Tekan 'Mulai Latihan' untuk memandu napas dalam.") }
    var secondsRemaining by remember { mutableIntStateOf(4) }
    var cycleCount by remember { mutableIntStateOf(0) }
    val targetCycles = 4

    // Breathing loop timer: 4s Inhale -> 2s Hold -> 6s Exhale
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning && cycleCount < targetCycles) {
                // Phase 1: Inhale (4 seconds)
                currentPhase = "Tarik Napas"
                currentInstruction = "Tarik napas perlahan melalui hidung, rasakan rongga dada & perut mengembang..."
                for (s in 4 downTo 1) {
                    secondsRemaining = s
                    delay(1000)
                    if (!isRunning) break
                }
                if (!isRunning) break

                // Phase 2: Hold (2 seconds)
                currentPhase = "Tahan Napas"
                currentInstruction = "Tahan sejenak, rasakan ketenangan otot tubuh..."
                for (s in 2 downTo 1) {
                    secondsRemaining = s
                    delay(1000)
                    if (!isRunning) break
                }
                if (!isRunning) break

                // Phase 3: Exhale (6 seconds)
                currentPhase = "Hembuskan Perlahan"
                currentInstruction = "Hembuskan lembut lewat mulut dengan bibir sedikit mengerucut (pursed lips)..."
                for (s in 6 downTo 1) {
                    secondsRemaining = s
                    delay(1000)
                    if (!isRunning) break
                }
                if (!isRunning) break

                cycleCount++
            }

            if (cycleCount >= targetCycles) {
                currentPhase = "Selesai!"
                currentInstruction = "Luar biasa! Latihan napas dalam 4 siklus telah selesai. Otot tubuh Anda kini lebih rileks."
                isRunning = false
            }
        }
    }

    val targetScale = when {
        !isRunning -> 1.0f
        currentPhase == "Tarik Napas" -> 1.22f
        currentPhase == "Tahan Napas" -> 1.22f
        currentPhase == "Hembuskan Perlahan" -> 0.86f
        else -> 1.0f
    }
    val phaseDurationMs = when {
        !isRunning -> 400
        currentPhase == "Tarik Napas" -> 4000
        currentPhase == "Tahan Napas" -> 300
        currentPhase == "Hembuskan Perlahan" -> 6000
        else -> 400
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = phaseDurationMs, easing = FastOutSlowInEasing),
        label = "breathScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("breathing_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = ToskaSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Latihan Relaksasi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.5.sp
                            )
                            Text(
                                text = "Teknik Napas Dalam 4-2-6",
                                style = MaterialTheme.typography.bodySmall,
                                color = ToskaSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_breathing_dialog")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MedicalSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breathing visual circle
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .scale(animatedScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        ToskaSecondary.copy(alpha = 0.25f),
                                        OceanBluePrimary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                    )

                    // Inner main circle
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(OceanBluePrimary, ToskaSecondary)
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = if (isRunning) "$secondsRemaining" else "4-2-6",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = currentPhase,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cycle progress badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Siklus: $cycleCount / $targetCycles",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OceanBluePrimary,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Instruction text
                Text(
                    text = currentInstruction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    minLines = 2,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isRunning && cycleCount < targetCycles) {
                        Button(
                            onClick = {
                                isRunning = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("start_breathing_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cycleCount == 0) "Mulai Latihan" else "Lanjutkan",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isRunning) {
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                currentPhase = "Dijeda"
                                currentInstruction = "Latihan dijeda. Tekan 'Lanjutkan' kapan saja."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("pause_breathing_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Jeda", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                onComplete()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("complete_breathing_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = ToskaSecondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Terapkan ke Checklist ✓", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (cycleCount > 0) {
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                cycleCount = 0
                                currentPhase = "Siap?"
                                currentInstruction = "Tekan 'Mulai Latihan' untuk memandu napas dalam."
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("reset_breathing_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Ulangi", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
