package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data Model for Patient Pre-Op Counseling and APAIS Evaluation Session
 * - 4 Pre-Operative Counseling Domains (Dawod et al., 2024 Protocol)
 * - Kuesioner APAIS Versi Indonesia (Perdana et al., 2020)
 */
@Entity(tableName = "patient_sessions")
data class PatientSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicalRecordNumber: String = "", // No. Rekam Medis (RM) / Kamar
    val fullName: String = "",            // Nama Lengkap Pasien
    val age: Int = 0,                     // Usia (Tahun)
    val gender: String = "Perempuan",     // Jenis Kelamin (Perempuan / Laki-laki)
    val plannedSurgery: String = "",      // Rencana Tindakan Operasi

    // 4 Domain Konseling Utama Pra-Bedah (Dawod et al., 2024)
    val checklistProcedure: Boolean = false,   // Domain 1: Prosedur Operasi & Pembiusan Umum
    val checklistPrep: Boolean = false,        // Domain 2: Persiapan Pre-Operasi & Puasa
    val checklistSideEffects: Boolean = false, // Domain 3: Antisipasi Efek Samping (PONV, Shivering, Pusing)
    val checklistPain: Boolean = false,        // Domain 4: Manajemen Nyeri & Relaksasi Napas Dalam

    // Kuesioner APAIS Versi Indonesia (Skala Likert 1 - 5)
    val apais1: Int = 1,                       // 1. Saya takut dibius (Anestesi)
    val apais2: Int = 1,                       // 2. Saya terus-menerus memikirkan tentang pembiusan
    val apais3: Int = 1,                       // 3. Saya ingin tahu sebanyak mungkin tentang pembiusan (Info)
    val apais4: Int = 1,                       // 4. Saya takut dioperasi (Pembedahan)
    val apais5: Int = 1,                       // 5. Saya terus memikirkan tentang operasi
    val apais6: Int = 1,                       // 6. Saya ingin tahu sebanyak mungkin tentang operasi (Info)

    // Subskala Skor APAIS
    val anxietyScore: Int = 4,                 // Item 1 + 2 + 4 + 5 (Rentang: 4 - 20)
    val infoScore: Int = 2,                    // Item 3 + 6 (Rentang: 2 - 10)
    val specificConcerns: String = "",         // Catatan / Kekhawatiran Khusus Pasien

    // Metadata
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val apaisAnswersList: List<Int>
        get() = listOf(apais1, apais2, apais3, apais4, apais5, apais6)

    val totalChecklistCount: Int get() = 4

    val completedChecklistCount: Int
        get() = listOf(
            checklistProcedure,
            checklistPrep,
            checklistSideEffects,
            checklistPain
        ).count { it }

    val checklistProgress: Float
        get() = if (totalChecklistCount > 0) completedChecklistCount.toFloat() / totalChecklistCount else 0f

    val checklistPercentage: Int
        get() = ((completedChecklistCount.toFloat() / totalChecklistCount) * 100).toInt()

    val isProfileValid: Boolean
        get() = fullName.isNotBlank()
}
