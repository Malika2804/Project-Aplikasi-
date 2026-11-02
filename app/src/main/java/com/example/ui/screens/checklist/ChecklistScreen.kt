package com.example.ui.screens.checklist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChecklistData
import com.example.data.model.ChecklistItem
import com.example.data.model.PatientSession
import com.example.ui.components.BreathingExerciseDialog
import com.example.ui.components.YouTubeVideoDialog
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DangerCoralBorder
import com.example.ui.theme.DangerCoralContainer
import com.example.ui.theme.MedicalCanvasBg
import com.example.ui.theme.MedicalOutline
import com.example.ui.theme.OceanBlueContainer
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBlueLight
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.ToskaDark
import com.example.ui.theme.ToskaLight
import com.example.ui.theme.ToskaSecondary

@Composable
fun ChecklistScreen(
    currentSession: PatientSession,
    videoUrls: Map<String, String> = ChecklistData.defaultVideoUrls,
    onToggleChecklist: (itemId: String, isChecked: Boolean) -> Unit,
    onNavigateToEvaluation: () -> Unit
) {
    var activeVideoItem by remember { mutableStateOf<ChecklistItem?>(null) }
    var showBreathingDialog by remember { mutableStateOf(false) }

    fun isChecked(itemId: String): Boolean {
        return when (itemId) {
            "procedure" -> currentSession.checklistProcedure
            "prep" -> currentSession.checklistPrep
            "sideeffects" -> currentSession.checklistSideEffects
            "pain" -> currentSession.checklistPain
            else -> false
        }
    }

    val totalItems = ChecklistData.items.size
    val checkedCount = ChecklistData.items.count { isChecked(it.id) }
    val progressPercent = if (totalItems > 0) ((checkedCount.toFloat() / totalItems) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("checklist_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner: Dawod et al. (2024) Protocol
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ToskaSecondary, OceanBluePrimary)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("checklist_hero_banner")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAWOD ET AL. (2024) PROTOCOL",
                            color = Color(0xFF99F6E4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "✓",
                            color = Color(0xFF99F6E4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Checklist Edukasi Pra-Bedah",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "4 Domain Konseling Utama untuk memastikan kesiapan dan menurunkan kecemasan.",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        lineHeight = 17.5.sp
                    )
                }
            }
        }

        // Progress Card: Kemajuan Edukasi Pasien
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checklist_progress_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MedicalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kemajuan Edukasi Pasien",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$checkedCount dari $totalItems Domain Selesai",
                            color = Color(0xFF475569),
                            fontSize = 11.5.sp
                        )
                    }
                    Text(
                        text = "$progressPercent%",
                        fontWeight = FontWeight.ExtraBold,
                        color = OceanBluePrimary,
                        fontSize = 17.sp
                    )
                }
            }
        }

        // 4 Domain Checklist Cards
        items(ChecklistData.items, key = { it.id }) { item ->
            val checked = isChecked(item.id)
            val cardBorderColor by animateColorAsState(
                targetValue = if (checked) ToskaSecondary.copy(alpha = 0.65f) else MedicalOutline,
                label = "domainCardBorder"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checklist_item_${item.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (checked) ToskaLight.copy(alpha = 0.35f) else Color.White
                ),
                border = BorderStroke(1.5.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header: Checkbox + Badge + Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { onToggleChecklist(item.id, !checked) },
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onToggleChecklist(item.id, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ToskaSecondary,
                                checkmarkColor = Color.White,
                                uncheckedColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                                .testTag("checkbox_${item.id}")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = ToskaLight,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = item.category.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaDark,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = item.title,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Domain Overview Summary
                    Text(
                        text = item.shortSummary,
                        color = Color(0xFF334155),
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Structured Education Sections
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.detailSections.forEach { section ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MedicalCanvasBg,
                                border = BorderStroke(1.dp, MedicalOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = section.sectionTitle,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.5.sp
                                    )

                                    section.introText?.let { intro ->
                                        Text(
                                            text = intro,
                                            color = Color(0xFF334155),
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )
                                    }

                                    section.bullets.forEach { bullet ->
                                        Text(
                                            text = buildAnnotatedString {
                                                append("• ")
                                                if (bullet.boldPrefix != null) {
                                                    withStyle(
                                                        SpanStyle(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                    ) {
                                                        append(bullet.boldPrefix)
                                                    }
                                                    append(" ")
                                                }
                                                append(bullet.text)
                                            },
                                            color = Color(0xFF1E293B),
                                            fontSize = 12.sp,
                                            lineHeight = 18.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reassuring Clinical Callout Note (Domains 1-3)
                    item.reassuranceNote?.let { note ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OceanBlueLight,
                            border = BorderStroke(1.dp, OceanBlueContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = OceanBluePrimary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = note,
                                    color = OceanBlueDark,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.8.sp,
                                    lineHeight = 17.5.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Action Buttons (Only when Domain has Video or Interactive Tool - Domain 4)
                    val showBreathingTool = item.hasInteractiveTool && item.interactiveToolType == "BREATHING"
                    if (item.hasVideo || showBreathingTool) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (item.hasVideo) {
                                OutlinedButton(
                                    onClick = { activeVideoItem = item },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = DangerCoralContainer,
                                        contentColor = DangerCoral
                                    ),
                                    border = BorderStroke(1.5.dp, DangerCoralBorder),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("video_btn_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.videoButtonLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (showBreathingTool) {
                                OutlinedButton(
                                    onClick = { showBreathingDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = ToskaLight,
                                        contentColor = ToskaDark
                                    ),
                                    border = BorderStroke(1.5.dp, Color(0xFF5EEAD4)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("breathing_tool_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Air,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Panduan Latihan Napas Dalam Interaktif",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Continue Button
        item {
            Spacer(modifier = Modifier.height(2.dp))
            Button(
                onClick = onNavigateToEvaluation,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("navigate_to_eval_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Lanjut ke Evaluasi Kuesioner APAIS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    activeVideoItem?.let { item ->
        YouTubeVideoDialog(
            item = item,
            videoUrl = videoUrls[item.id] ?: item.youtubeUrl,
            onDismiss = { activeVideoItem = null },
            onMarkUnderstood = {
                onToggleChecklist(item.id, true)
            }
        )
    }

    if (showBreathingDialog) {
        BreathingExerciseDialog(
            onDismiss = { showBreathingDialog = false },
            onComplete = {
                onToggleChecklist("pain", true)
            }
        )
    }
}
