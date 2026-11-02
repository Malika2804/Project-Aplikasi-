package com.example.ui.screens.infohub

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalmingNavy
import com.example.ui.theme.CalmingNavyDark
import com.example.ui.theme.MedicalCanvasBg
import com.example.ui.theme.MedicalOutline
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.ToskaDark
import com.example.ui.theme.ToskaLight
import com.example.ui.theme.ToskaSecondary

@Composable
fun InfoHubScreen(
    onOpenQrAccess: () -> Unit = {},
    onOpenVideoSettings: () -> Unit = {},
    onOpenResearchPanel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("infohub_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner: Pusat Informasi & Bantuan
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CalmingNavy, CalmingNavyDark)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("infohub_header_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PUSAT INFORMASI & BANTUAN",
                            color = Color(0xFF99F6E4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "ℹ",
                            color = Color(0xFF99F6E4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Informasi & Kontak Tim Medis",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "QR akses cepat browser, panduan puasa kamar operasi, dan kontak perawat.",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        lineHeight = 17.5.sp
                    )
                }
            }
        }

        // Card 1: QR Code Akses Cepat (Chrome & Safari)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { onOpenQrAccess() }
                    .testTag("open_qr_access_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ToskaLight),
                border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📱 QR Code Akses Cepat (Netlify)",
                            fontWeight = FontWeight.Bold,
                            color = ToskaDark,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Scan kamera HP pasien untuk membuka aplikasi langsung",
                            color = Color(0xFF334155),
                            fontSize = 11.5.sp,
                            lineHeight = 16.5.sp
                        )
                    }
                    Text(
                        text = "➔",
                        color = ToskaSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Card 2: Atur Link Video YouTube Sendiri (Khusus Petugas)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { onOpenVideoSettings() }
                    .testTag("open_video_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MedicalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Atur Link Video YouTube Sendiri",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔒 Ubah URL YouTube Relaksasi Napas Dalam (Khusus Petugas)",
                            color = Color(0xFF475569),
                            fontSize = 11.5.sp
                        )
                    }
                    Text(
                        text = "➔",
                        color = OceanBluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Card 3: Aturan Puasa Pre-Operasi (ASA / ERAS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fasting_guide_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MedicalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🍽️ Aturan Puasa Pre-Operasi (ASA / ERAS)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 13.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Makanan Berat / Lemak: Stop 8 Jam sebelum operasi.\n" +
                            "• Makanan Ringan / Bubur: Stop 6 Jam sebelum operasi.\n" +
                            "• Air Putih Jernih: Maksimal 2 Jam sebelum bius.",
                        color = Color(0xFF334155),
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Card 4: Kontak Darurat Ruang Perawatan
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_contact_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MedicalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🚨 Kontak Darurat Ruang Perawatan",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 13.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Jika mengalami keluhan nyeri hebat atau mual mendadak:",
                        color = Color(0xFF334155),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MedicalCanvasBg,
                        border = BorderStroke(1.dp, MedicalOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pos Perawat (Nurse Station):",
                                color = Color(0xFF1E293B),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Ext. 119 / Bel Pasien",
                                color = OceanBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Card 5: Rekap Data Riset APAIS & Ekspor CSV (Khusus Petugas)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { onOpenResearchPanel() }
                    .testTag("open_research_panel_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MedicalOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rekap Data Riset APAIS & Ekspor CSV",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔒 Lihat skor APAIS & ekspor CSV (Khusus Petugas)",
                            color = Color(0xFF475569),
                            fontSize = 11.5.sp
                        )
                    }
                    Text(
                        text = "➔",
                        color = ToskaSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
