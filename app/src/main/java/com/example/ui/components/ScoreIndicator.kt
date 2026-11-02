package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ToskaSecondary
import com.example.ui.theme.WarningAmber

enum class ScoreType {
    ANXIETY,    // 1 = Sangat Tenang, 10 = Sangat Cemas
    READINESS   // 1 = Belum Siap, 10 = Sangat Siap
}

@Composable
fun ScoreSelector1To10(
    title: String,
    subtitle: String,
    currentScore: Int,
    scoreType: ScoreType,
    onScoreSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "score"
) {
    val scoreColor = when (scoreType) {
        ScoreType.ANXIETY -> when (currentScore) {
            in 1..3 -> SuccessGreen
            in 4..6 -> WarningAmber
            else -> DangerCoral
        }
        ScoreType.READINESS -> when (currentScore) {
            in 1..3 -> DangerCoral
            in 4..6 -> WarningAmber
            else -> SuccessGreen
        }
    }

    val animatedColor by animateColorAsState(targetValue = scoreColor, label = "scoreColor")

    val scoreLabel = when (scoreType) {
        ScoreType.ANXIETY -> when (currentScore) {
            in 1..2 -> "Sangat Tenang & Rileks (1-2)"
            in 3..4 -> "Kecemasan Ringan Normal (3-4)"
            in 5..6 -> "Kecemasan Sedang (5-6)"
            in 7..8 -> "Kecemasan Berat (7-8)"
            else -> "Kecemasan Sangat Berat / Panik (9-10)"
        }
        ScoreType.READINESS -> when (currentScore) {
            in 1..2 -> "Sangat Belum Siap (1-2)"
            in 3..4 -> "Kesiapan Rendah (3-4)"
            in 5..6 -> "Cukup Siap & Memahami (5-6)"
            in 7..8 -> "Siap Menghadapi Operasi (7-8)"
            else -> "Sangat Siap & Percaya Diri (9-10)"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_card"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Current Score Big Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = animatedColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedColor),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$currentScore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = animatedColor
                        )
                        Text(
                            text = "/10",
                            style = MaterialTheme.typography.labelSmall,
                            color = animatedColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 2.dp, top = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Score status text chip
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = animatedColor.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = scoreLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Discrete Numeric Rating Chips (1 to 10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..10).forEach { num ->
                    val isSelected = num == currentScore
                    val chipColor = if (isSelected) animatedColor else MaterialTheme.colorScheme.surfaceVariant
                    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(chipColor)
                            .clickable { onScoreSelected(num) }
                            .testTag("${testTagPrefix}_chip_$num"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$num",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Slider for smooth tactile feedback
            Slider(
                value = currentScore.toFloat(),
                onValueChange = { onScoreSelected(it.toInt().coerceIn(1, 10)) },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = animatedColor,
                    activeTrackColor = animatedColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("${testTagPrefix}_slider")
            )

            // Low vs High labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (scoreType == ScoreType.ANXIETY) "1 (Sangat Tenang)" else "1 (Belum Siap)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Text(
                    text = if (scoreType == ScoreType.ANXIETY) "10 (Sangat Cemas)" else "10 (Sangat Siap)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}
