package com.example.ui.screens.evaluation

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientSession
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.MedicalCanvasBg
import com.example.ui.theme.MedicalOutline
import com.example.ui.theme.MedicalOutlineVariant
import com.example.ui.theme.OceanBlueLight
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.ToskaLight
import com.example.ui.theme.ToskaSecondary
import com.example.ui.viewmodel.EvaluationSyncUiState

private data class ApaisQuestionSpec(
    val number: Int,
    val title: String,
    val isInformationNeed: Boolean
)

private val APAIS_QUESTIONS = listOf(
    ApaisQuestionSpec(1, "1. Saya takut dibius (Anestesi)", false),
    ApaisQuestionSpec(2, "2. Saya terus-menerus memikirkan tentang pembiusan", false),
    ApaisQuestionSpec(3, "3. Saya ingin tahu sebanyak mungkin tentang pembiusan", true),
    ApaisQuestionSpec(4, "4. Saya takut dioperasi (Pembedahan)", false),
    ApaisQuestionSpec(5, "5. Saya terus memikirkan tentang operasi", false),
    ApaisQuestionSpec(6, "6. Saya ingin tahu sebanyak mungkin tentang operasi", true)
)

private val LIKERT_SHORT_LABELS = mapOf(
    1 to "Sama sekali",
    2 to "Tidak",
    3 to "Sedikit",
    4 to "Agak",
    5 to "Sangat"
)

@Composable
fun EvaluationScreen(
    currentSession: PatientSession,
    syncUiState: EvaluationSyncUiState = EvaluationSyncUiState.Idle,
    onSaveEvaluation: (apaisAnswers: List<Int>, specificConcerns: String) -> Unit,
    onNavigateToInfoHub: () -> Unit
) {
    val context = LocalContext.current

    var q1 by rememberSaveable(currentSession.createdAt) { mutableIntStateOf(currentSession.apais1.coerceIn(1, 5)) }
    var q2 by rememberSaveable(currentSession.createdAt) { mutableIntStateOf(currentSession.apais2.coerceIn(1, 5)) }
    var q3 by rememberSaveable(currentSession.createdAt) { mutableIntStateOf(currentSession.apais3.coerceIn(1, 5)) }
    var q4 by rememberSaveable(currentSession.createdAt) { mutableIntStateOf(currentSession.apais4.coerceIn(1, 5)) }
    var q5 by rememberSaveable(currentSession.createdAt) { mutableIntStateOf(currentSession.apais5.coerceIn(1, 5)) }
    var q6 by rememberSaveable(currentSession.createdAt) { mutableIntStateOf(currentSession.apais6.coerceIn(1, 5)) }
    var specificConcerns by rememberSaveable(currentSession.createdAt) {
        mutableStateOf(currentSession.specificConcerns)
    }

    val anxietyScore = q1 + q2 + q4 + q5 // Rentang: 4 - 20
    val infoScore = q3 + q6              // Rentang: 2 - 10

    fun getAnswer(questionNumber: Int): Int = when (questionNumber) {
        1 -> q1
        2 -> q2
        3 -> q3
        4 -> q4
        5 -> q5
        6 -> q6
        else -> 1
    }

    fun setAnswer(questionNumber: Int, value: Int) {
        when (questionNumber) {
            1 -> q1 = value
            2 -> q2 = value
            3 -> q3 = value
            4 -> q4 = value
            5 -> q5 = value
            6 -> q6 = value
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("evaluation_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner: Instrumen Baku Skripsi - Kuesioner APAIS Versi Indonesia
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(IndigoAccent, OceanBluePrimary)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("evaluation_header_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INSTRUMEN BAKU SKRIPSI",
                            color = Color(0xFF99F6E4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "📊",
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kuesioner APAIS Versi Indonesia",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Amsterdam Preoperative Anxiety and Information Scale (Perdana et al., 2020). Berikan penilaian skala 1 sampai 5.",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        lineHeight = 17.5.sp
                    )
                }
            }
        }

        // Live Score Summary (.apais-score-summary)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OceanBlueLight,
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apais_score_summary")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Skor Kecemasan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$anxietyScore",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBluePrimary,
                            modifier = Modifier.testTag("apais_anxiety_score_text")
                        )
                        Text(
                            text = "(Rentang: 4 - 20)",
                            fontSize = 9.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Kebutuhan Informasi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$infoScore",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBluePrimary,
                            modifier = Modifier.testTag("apais_info_score_text")
                        )
                        Text(
                            text = "(Rentang: 2 - 10)",
                            fontSize = 9.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // APAIS Form Container (.card-gray)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("evaluation_form_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalCanvasBg),
                border = BorderStroke(1.dp, MedicalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Keterangan Skala Likert: 1 = Sama sekali tidak, 2 = Tidak terlalu, 3 = Sedikit, 4 = Agak, 5 = Sangat",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    // 6 Item Kuesioner APAIS
                    APAIS_QUESTIONS.forEach { question ->
                        ApaisQuestionCard(
                            question = question,
                            selectedValue = getAnswer(question.number),
                            onSelectValue = { setAnswer(question.number, it) }
                        )
                    }

                    // Catatan / Kekhawatiran Khusus Pasien
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Catatan / Kekhawatiran Khusus Pasien",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        OutlinedTextField(
                            value = specificConcerns,
                            onValueChange = { specificConcerns = it },
                            placeholder = {
                                Text(
                                    "Tuliskan keluhan atau hal lain yang dikhawatirkan pasien",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.5.sp
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = OceanBluePrimary,
                                unfocusedBorderColor = MedicalOutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("specific_concerns_input")
                        )
                    }

                    // Teal Save Button: Simpan & Otomatis Kirim Data (Google Sheets)
                    Button(
                        onClick = {
                            val answers = listOf(q1, q2, q3, q4, q5, q6)
                            onSaveEvaluation(answers, specificConcerns.trim())
                            Toast.makeText(
                                context,
                                "✅ Data berhasil tersimpan!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onNavigateToInfoHub()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("submit_evaluation_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = ToskaSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "💾 Simpan & Otomatis Kirim Data (Google Sheets)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Status Alert (.status-alert)
                    when (syncUiState) {
                        is EvaluationSyncUiState.Idle -> Unit
                        is EvaluationSyncUiState.Loading -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE0F2FE),
                                border = BorderStroke(1.dp, Color(0xFF7DD3FC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("evaluation_status_alert")
                            ) {
                                Text(
                                    text = syncUiState.message,
                                    color = Color(0xFF0369A1),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                        is EvaluationSyncUiState.Success -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("evaluation_status_alert")
                            ) {
                                Text(
                                    text = syncUiState.message,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApaisQuestionCard(
    question: ApaisQuestionSpec,
    selectedValue: Int,
    onSelectValue: (Int) -> Unit
) {
    val cardBg = if (question.isInformationNeed) ToskaLight else Color.White
    val cardBorder = if (question.isInformationNeed) Color(0xFF99F6E4) else MedicalOutline

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apais_question_card_${question.number}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = question.title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                fontSize = 12.5.sp,
                lineHeight = 17.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (scoreVal in 1..5) {
                    val isSelected = selectedValue == scoreVal
                    val optionBg = if (isSelected) OceanBlueLight else MedicalCanvasBg
                    val optionBorder = if (isSelected) OceanBluePrimary else MedicalOutline
                    val shortLabel = LIKERT_SHORT_LABELS[scoreVal].orEmpty()

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = optionBg,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, optionBorder),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(role = Role.RadioButton) { onSelectValue(scoreVal) }
                            .testTag("apais_q${question.number}_opt_${scoreVal}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) OceanBluePrimary else Color.White)
                                    .padding(if (isSelected) 3.dp else 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        border = BorderStroke(1.2.dp, Color(0xFF94A3B8)),
                                        modifier = Modifier.fillMaxSize()
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "$scoreVal",
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) OceanBluePrimary else Color(0xFF1E293B),
                                fontSize = 11.5.sp,
                                lineHeight = 13.sp
                            )

                            Text(
                                text = shortLabel,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) OceanBluePrimary else Color(0xFF334155),
                                fontSize = 9.5.sp,
                                lineHeight = 11.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
