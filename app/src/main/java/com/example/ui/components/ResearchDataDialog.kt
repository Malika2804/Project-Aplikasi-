package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PatientSession
import com.example.data.sync.GoogleSheetsSyncService
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DangerCoralBorder
import com.example.ui.theme.DangerCoralContainer
import com.example.ui.theme.MedicalCanvasBg
import com.example.ui.theme.MedicalSurfaceVariant
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ToskaDark
import com.example.ui.theme.ToskaLight
import com.example.ui.theme.ToskaSecondary
import kotlinx.coroutines.launch

@Composable
fun ResearchDataDialog(
    allSessions: List<PatientSession>,
    currentSession: PatientSession,
    onDismiss: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onClearAllSessions: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showCloudSyncSection by remember { mutableStateOf(false) }
    var webhookUrl by remember { mutableStateOf(GoogleSheetsSyncService.getSavedWebhookUrl(context)) }
    var isAutoSync by remember { mutableStateOf(GoogleSheetsSyncService.isAutoSyncEnabled(context)) }
    var isTestingSync by remember { mutableStateOf(false) }
    var syncStatusMessage by remember { mutableStateOf<String?>(null) }
    var syncSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("research_data_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rekap Data Riset APAIS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.5.sp,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_research_dialog")
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
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                // Action Row: Ekspor CSV/Excel & Hapus Semua
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (allSessions.isEmpty()) {
                                Toast.makeText(context, "Belum ada data untuk diekspor.", Toast.LENGTH_SHORT).show()
                            } else {
                                val csv = GoogleSheetsSyncService.generateCsv(allSessions)
                                GoogleSheetsSyncService.shareCsvIntent(context, csv)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ToskaLight,
                            contentColor = ToskaDark
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF5EEAD4)),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("export_csv_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Ekspor CSV/Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            if (allSessions.isNotEmpty()) {
                                showClearConfirm = true
                            } else {
                                Toast.makeText(context, "Data sudah kosong.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DangerCoralContainer,
                            contentColor = DangerCoral
                        ),
                        border = BorderStroke(1.5.dp, DangerCoralBorder),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("clear_all_sessions_btn")
                    ) {
                        Text("Hapus Semua", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total Pasien: ${allSessions.size}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Records List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 80.dp, max = 230.dp)
                ) {
                    if (allSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada data pasien tersimpan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allSessions, key = { it.id }) { s ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MedicalCanvasBg,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${s.fullName.ifBlank { "Pasien" }} (${if (s.age > 0) "${s.age} th, " else ""}${s.gender})",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 12.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Kecemasan (APAIS): ${s.anxietyScore} | Info: ${s.infoScore}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = ToskaSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "${s.plannedSurgery.ifBlank { "-" }} • RM: ${s.medicalRecordNumber.ifBlank { "-" }} • Domain: ${s.completedChecklistCount}/4",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteSession(s.id) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("delete_session_${s.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = DangerCoral.copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Collapsible Cloud Sync Section
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedicalCanvasBg,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCloudSyncSection = !showCloudSyncSection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = OceanBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sinkronisasi Google Sheets (Opsional)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OceanBluePrimary
                                )
                            }
                            Icon(
                                imageVector = if (showCloudSyncSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = showCloudSyncSection) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedTextField(
                                    value = webhookUrl,
                                    onValueChange = {
                                        webhookUrl = it
                                        GoogleSheetsSyncService.saveWebhookUrl(context, it)
                                    },
                                    label = { Text("URL Webhook Apps Script", fontSize = 11.sp) },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("webhook_url_input"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Kirim Otomatis Saat Simpan", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                    Switch(
                                        checked = isAutoSync,
                                        onCheckedChange = {
                                            isAutoSync = it
                                            GoogleSheetsSyncService.setAutoSyncEnabled(context, it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = ToskaSecondary
                                        )
                                    )
                                }
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isTestingSync = true
                                            syncStatusMessage = null
                                            val sample = if (currentSession.fullName.isNotBlank()) currentSession else PatientSession(
                                                medicalRecordNumber = "RM-TEST-01",
                                                fullName = "Pasien Uji Coba",
                                                age = 55,
                                                gender = "Perempuan",
                                                plannedSurgery = "Seksio Sesarea (SC)",
                                                checklistProcedure = true,
                                                checklistPrep = true,
                                                checklistSideEffects = true,
                                                checklistPain = true,
                                                anxietyScore = 4,
                                                infoScore = 2,
                                                isCompleted = true,
                                                completedAt = System.currentTimeMillis()
                                            )
                                            val res = GoogleSheetsSyncService.syncSessionToCloud(context, sample)
                                            isTestingSync = false
                                            res.onSuccess { msg ->
                                                syncStatusMessage = msg
                                                syncSuccess = true
                                            }.onFailure { err ->
                                                syncStatusMessage = err.message
                                                syncSuccess = false
                                            }
                                        }
                                    },
                                    enabled = !isTestingSync,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 38.dp)
                                        .testTag("test_sync_button")
                                ) {
                                    if (isTestingSync) {
                                        CircularProgressIndicator(modifier = Modifier.size(15.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Uji Kirim ke Spreadsheet", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (syncStatusMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = syncStatusMessage ?: "",
                                        color = if (syncSuccess) SuccessGreen else DangerCoral,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Hapus Semua Data?") },
            text = { Text("Hapus seluruh riwayat data APAIS pasien di perangkat ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearAllSessions()
                        Toast.makeText(context, "Seluruh riwayat pasien dihapus.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCoral),
                    modifier = Modifier.testTag("confirm_clear_all_btn")
                ) {
                    Text("Hapus Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
