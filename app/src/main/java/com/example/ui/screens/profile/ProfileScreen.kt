package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientSession
import com.example.ui.theme.MedicalCanvasBg
import com.example.ui.theme.MedicalOutline
import com.example.ui.theme.MedicalOutlineVariant
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.ToskaDark
import com.example.ui.theme.ToskaLight

private data class SurgeryChipOption(
    val label: String,
    val value: String,
    val tag: String
)

private val QUICK_SURGERY_OPTIONS = listOf(
    SurgeryChipOption("+ Apendiktomi", "Apendiktomi", "surgery_chip_apendiktomi"),
    SurgeryChipOption("+ Seksio Sesarea", "Seksio Sesarea (SC)", "surgery_chip_sc"),
    SurgeryChipOption("+ ORIF Fraktur", "ORIF Fraktur", "surgery_chip_orif"),
    SurgeryChipOption("+ Laparoskopi", "Laparoskopi", "surgery_chip_laparoskopi")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    currentSession: PatientSession,
    onSaveProfile: (fullName: String, age: Int, gender: String, surgery: String, rmNumber: String) -> Unit,
    onNavigateToChecklist: () -> Unit,
    onResetPatient: () -> Unit
) {
    val context = LocalContext.current

    var nameInput by rememberSaveable(currentSession.createdAt, currentSession.fullName) {
        mutableStateOf(currentSession.fullName)
    }
    var ageInput by rememberSaveable(currentSession.createdAt) {
        mutableStateOf(if (currentSession.age > 0) currentSession.age.toString() else "")
    }
    var genderInput by rememberSaveable(currentSession.createdAt) {
        mutableStateOf(currentSession.gender.ifBlank { "Perempuan" })
    }
    var surgeryInput by rememberSaveable(currentSession.createdAt) {
        mutableStateOf(currentSession.plannedSurgery)
    }
    var rmNumberInput by rememberSaveable(currentSession.createdAt) {
        mutableStateOf(currentSession.medicalRecordNumber)
    }

    var genderMenuExpanded by remember { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner: Tahap 1 Registrasi Pra-Bedah
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OceanBluePrimary, ToskaDark)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("profile_hero_banner")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TAHAP 1: REGISTRASI PRA-BEDAH",
                            color = Color(0xFF99F6E4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "★",
                            color = Color(0xFF99F6E4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Identitas Pasien",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lengkapi data diri pasien sebelum memulai checklist edukasi persiapan anestesi.",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        lineHeight = 17.5.sp
                    )
                }
            }
        }

        // Patient Identity Form Card (.card-gray)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("patient_form_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalCanvasBg),
                border = BorderStroke(1.dp, MedicalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    // Nama Lengkap Pasien *
                    Column {
                        FieldLabel("Nama Lengkap Pasien *")
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = {
                                Text(
                                    "Contoh: Ny. Siti Rahmawati",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = OceanBluePrimary,
                                unfocusedBorderColor = MedicalOutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("patient_name_input")
                        )
                    }

                    // 2-Column Grid: Usia (Tahun) * & Jenis Kelamin *
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("Usia (Tahun) *")
                            OutlinedTextField(
                                value = ageInput,
                                onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) ageInput = it },
                                placeholder = {
                                    Text(
                                        "Contoh: 62",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = OceanBluePrimary,
                                    unfocusedBorderColor = MedicalOutlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("patient_age_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("Jenis Kelamin *")
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, MedicalOutlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .clickable { genderMenuExpanded = true }
                                        .testTag("patient_gender_select")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = genderInput,
                                            fontSize = 14.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Pilih Jenis Kelamin",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = genderMenuExpanded,
                                    onDismissRequest = { genderMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Perempuan") },
                                        onClick = {
                                            genderInput = "Perempuan"
                                            genderMenuExpanded = false
                                        },
                                        modifier = Modifier.testTag("gender_female_option")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Laki-laki") },
                                        onClick = {
                                            genderInput = "Laki-laki"
                                            genderMenuExpanded = false
                                        },
                                        modifier = Modifier.testTag("gender_male_option")
                                    )
                                }
                            }
                        }
                    }

                    // Rencana Tindakan Operasi * + Quick Chips
                    Column {
                        FieldLabel("Rencana Tindakan Operasi *")
                        OutlinedTextField(
                            value = surgeryInput,
                            onValueChange = { surgeryInput = it },
                            placeholder = {
                                Text(
                                    "Contoh: Seksio Sesarea (SC)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = OceanBluePrimary,
                                unfocusedBorderColor = MedicalOutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("patient_surgery_input")
                        )

                        Spacer(modifier = Modifier.height(7.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QUICK_SURGERY_OPTIONS.forEach { chip ->
                                OutlinedButton(
                                    onClick = { surgeryInput = chip.value },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF1E293B)
                                    ),
                                    border = BorderStroke(1.dp, MedicalOutlineVariant),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .heightIn(min = 38.dp)
                                        .testTag(chip.tag)
                                ) {
                                    Text(
                                        text = chip.label,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // No. Rekam Medis (RM) / Kamar
                    Column {
                        FieldLabel("No. Rekam Medis (RM) / Kamar")
                        OutlinedTextField(
                            value = rmNumberInput,
                            onValueChange = { rmNumberInput = it },
                            placeholder = {
                                Text(
                                    "Contoh: RM-98231 / Kamar Melati 3",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = OceanBluePrimary,
                                unfocusedBorderColor = MedicalOutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("patient_rm_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Primary Button: Simpan & Lanjut ke Edukasi ➔
                    Button(
                        onClick = {
                            if (nameInput.isBlank()) {
                                Toast.makeText(context, "Mohon isi nama lengkap pasien.", Toast.LENGTH_SHORT).show()
                            } else {
                                val ageVal = ageInput.toIntOrNull() ?: 0
                                onSaveProfile(
                                    nameInput.trim(),
                                    ageVal,
                                    genderInput,
                                    surgeryInput.trim(),
                                    rmNumberInput.trim()
                                )
                                onNavigateToChecklist()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("save_and_continue_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Simpan & Lanjut ke Edukasi",
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

                    if (currentSession.fullName.isNotBlank()) {
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("reset_patient_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ToskaLight,
                                contentColor = ToskaDark
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFF5EEAD4)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mulai Sesi Pasien Baru", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Mulai Sesi Pasien Baru?") },
            text = { Text("Formulir akan dikosongkan untuk pasien berikutnya.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetPatient()
                        nameInput = ""
                        ageInput = ""
                        genderInput = "Perempuan"
                        surgeryInput = ""
                        rmNumberInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary)
                ) {
                    Text("Ya, Mulai Baru")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}
