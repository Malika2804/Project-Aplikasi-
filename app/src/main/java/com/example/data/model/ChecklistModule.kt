package com.example.data.model

/**
 * 4 Pre-Operative Counseling Domains based on Dawod et al. (2024) Protocol
 */
data class StepBullet(
    val boldPrefix: String? = null,
    val text: String
)

data class EducationSection(
    val sectionTitle: String,
    val introText: String? = null,
    val bullets: List<StepBullet>
)

data class ChecklistItem(
    val id: String,
    val category: String,
    val title: String,
    val shortSummary: String,
    val detailSections: List<EducationSection>,
    val reassuranceNote: String? = null,
    val hasVideo: Boolean = false,
    val videoButtonLabel: String = "",
    val youtubeUrl: String = "",
    val youtubeTitle: String = "",
    val hasInteractiveTool: Boolean = false,
    val interactiveToolType: String = ""
) {
    val practicalSteps: List<String>
        get() = detailSections.flatMap { section ->
            section.bullets.map { bullet ->
                if (bullet.boldPrefix != null) "${bullet.boldPrefix} ${bullet.text}" else bullet.text
            }
        }
}

object ChecklistData {
    const val DEFAULT_RELAXATION_VIDEO_URL = "https://www.youtube.com/watch?v=TPIEEQtAUjg"

    val defaultVideoUrls: Map<String, String> = mapOf(
        "pain" to DEFAULT_RELAXATION_VIDEO_URL
    )

    val items: List<ChecklistItem> = listOf(
        ChecklistItem(
            id = "procedure",
            category = "Domain 1: Prosedur & Anestesi",
            title = "Prosedur Operasi & Cara Kerja Pembiusan",
            shortSummary = "Memahami urutan kegiatan sejak masuk ruang operasi hingga bangun kembali akan membantu pikiran jauh lebih tenang. Selama tindakan berlangsung, dokter spesialis anestesi dan perawat anestesi selalu mendampingi Anda setiap detik.",
            detailSections = listOf(
                EducationSection(
                    sectionTitle = "1. Alur Tahapan Anda di Kamar Operasi:",
                    bullets = listOf(
                        StepBullet(
                            boldPrefix = "Ruang Penerimaan (Sign-In):",
                            text = "Perawat mencocokkan gelang identitas, rencana lokasi operasi, riwayat alergi obat, serta memastikan waktu puasa Anda sudah aman."
                        ),
                        StepBullet(
                            boldPrefix = "Pemasangan Alat Pantau:",
                            text = "Di ruang operasi yang sejuk, petugas memasang manset tensi di lengan, stiker rekam jantung di dada, dan penjepit sensor oksigen di ujung jari (sama sekali tidak terasa sakit)."
                        ),
                        StepBullet(
                            boldPrefix = "Pemberian Obat Bius:",
                            text = "Dokter anestesi mulai memberikan obat bius sesuai kondisi medis Anda hingga Anda benar-benar bebas nyeri sebelum operasi dimulai."
                        ),
                        StepBullet(
                            boldPrefix = "Ruang Pemulihan (Recovery Room):",
                            text = "Seusai tindakan, Anda dipantau ketat selama 1 sampai 2 jam hingga kesadaran, napas, dan tekanan darah stabil sebelum kembali ke kamar rawat."
                        )
                    )
                ),
                EducationSection(
                    sectionTitle = "2. Jenis Pembiusan yang Perlu Dipahami:",
                    bullets = listOf(
                        StepBullet(
                            boldPrefix = "Bius Umum (Bius Tidur):",
                            text = "Obat diberikan lewat selang infus atau masker oksigen sehingga Anda tertidur pulas dalam hitungan detik. Selama operasi berlangsung, Anda tidak merasakan nyeri, tidak mendengar suara alat, dan tidak mengingat jalannya tindakan."
                        ),
                        StepBullet(
                            boldPrefix = "Bius Regional / Spinal (Setengah Badan):",
                            text = "Suntikan halus di punggung bawah membuat saraf perut hingga kaki kebas total dari rasa sakit, sementara Anda tetap bernapas lega dan rileks."
                        )
                    )
                )
            ),
            reassuranceNote = "Pesan Tenang: Dosis obat bius diatur secara presisi mengikuti tanda vital tubuh Anda sepanjang waktu, sehingga Anda tidak akan terbangun kesakitan di tengah operasi.",
            hasVideo = false
        ),
        ChecklistItem(
            id = "prep",
            category = "Domain 2: Persiapan Pra-Bedah",
            title = "Persiapan Fisik & Aturan Puasa Aman",
            shortSummary = "Persiapan tubuh dan kepatuhan jadwal puasa sebelum operasi sangat penting untuk melindungi paru-paru Anda saat pembiusan serta menjaga kesterilan area pembedahan.",
            detailSections = listOf(
                EducationSection(
                    sectionTitle = "1. Mengapa Harus Puasa & Berapa Lama?",
                    introText = "Saat obat bius bekerja, otot lambung dan refleks menelan ikut rileks. Jika lambung masih terisi makanan, isi lambung dapat naik ke kerongkongan dan masuk ke paru-paru (aspirasi). Patuhi batas waktu berikut:",
                    bullets = listOf(
                        StepBullet(
                            boldPrefix = "Makanan Berat / Berlemak (Stop 8 Jam):",
                            text = "Hentikan nasi, lauk bersantan, gorengan, daging, atau susu minimal 8 jam sebelum jadwal bius karena paling lambat dicerna lambung."
                        ),
                        StepBullet(
                            boldPrefix = "Makanan Ringan / Bubur (Stop 6 Jam):",
                            text = "Hentikan roti tawar, biskuit, atau bubur halus minimal 6 jam sebelum tindakan."
                        ),
                        StepBullet(
                            boldPrefix = "Air Putih Jernih (Hingga 2 Jam Sebelum Bius):",
                            text = "Air putih bening (tanpa gula/susu) masih boleh diminum secukupnya hingga 2 jam sebelum operasi agar tenggorokan tidak kering, kecuali ada instruksi khusus dokter."
                        )
                    )
                ),
                EducationSection(
                    sectionTitle = "2. Kebersihan Tubuh, Aksesoris & Obat Rutin:",
                    bullets = listOf(
                        StepBullet(
                            boldPrefix = "Mandi & Gaun Khusus Operasi:",
                            text = "Mandi bersih untuk mengurangi kuman kulit, lalu ganti seluruh pakaian (termasuk pakaian dalam) menggunakan baju steril rumah sakit."
                        ),
                        StepBullet(
                            boldPrefix = "Lepas Logam & Gigi Palsu:",
                            text = "Lepaskan cincin, kalung, anting, jam tangan, kacamata, lensa kontak, serta gigi palsu lepasan agar jalan napas tetap aman."
                        ),
                        StepBullet(
                            boldPrefix = "Bersihkan Cat Kuku & Riasan:",
                            text = "Hapus kuteks dan lipstik tebal karena tim medis perlu melihat warna alami kuku/bibir serta membaca kadar oksigen darah lewat sensor jari."
                        ),
                        StepBullet(
                            boldPrefix = "Obat Rutin Harian:",
                            text = "Sampaikan kepada dokter atau perawat bila Anda rutin minum obat tensi, jantung, gula (diabetes), atau pengencer darah. Obat tertentu tetap perlu diminum dengan seteguk air putih sesuai arahan dokter."
                        )
                    )
                )
            ),
            reassuranceNote = "Penting Demi Keselamatan: Bila tidak sengaja terminum atau termakan sesuatu mendekati jam operasi, segera jujur memberi tahu perawat agar jadwal bius disesuaikan dengan aman.",
            hasVideo = false
        ),
        ChecklistItem(
            id = "sideeffects",
            category = "Domain 3: Antisipasi Efek Samping",
            title = "Mengenali Sensasi Wajar Pasca-Bius & Penanganannya",
            shortSummary = "Ketika efek obat bius mulai berkurang di ruang pemulihan, tubuh dapat merasakan beberapa sensasi sementara. Mengetahui mana reaksi yang wajar akan mencegah rasa panik saat baru terbangun.",
            detailSections = listOf(
                EducationSection(
                    sectionTitle = "1. Sensasi Sementara yang Wajar & Cara Mengatasinya:",
                    bullets = listOf(
                        StepBullet(
                            boldPrefix = "Mengantuk Berat & Pusing Ringan:",
                            text = "Sisa obat bius membuat kepala terasa berat seperti baru bangun tidur lelap. Tetap berbaring tenang, jangan langsung duduk mendadak, dan pastikan pagar pengaman tempat tidur terpasang."
                        ),
                        StepBullet(
                            boldPrefix = "Rasa Dingin / Menggigil (Shivering):",
                            text = "Suhu kamar operasi yang sejuk dan respons obat bius sering memicu rasa menggigil tanpa demam. Perawat akan segera memasangkan selimut penghangat khusus hingga tubuh nyaman."
                        ),
                        StepBullet(
                            boldPrefix = "Mual atau Ingin Muntah (PONV):",
                            text = "Jika perut terasa mual, miringkan kepala ke samping, tarik napas perlahan lewat hidung, dan beritahu perawat agar segera diberikan obat antimual melalui selang infus."
                        ),
                        StepBullet(
                            boldPrefix = "Tenggorokan Kering / Sedikit Serak:",
                            text = "Wajar terjadi sementara setelah pemasangan alat bantu napas pada bius umum dan berangsur membaik dalam 1 sampai 2 hari setelah diizinkan minum bertahap."
                        ),
                        StepBullet(
                            boldPrefix = "Kebas / Kesemutan Berkurang (Pasca Bius Spinal):",
                            text = "Kedua kaki akan terasa berat selama 2 sampai 4 jam pertama. Gerakkan jari kaki perlahan saat rasa kebas mulai berkurang sesuai arahan perawat."
                        )
                    )
                ),
                EducationSection(
                    sectionTitle = "2. Kapan Harus Segera Menekan Bel Perawat?",
                    bullets = listOf(
                        StepBullet(
                            boldPrefix = "Tanda Lapor Segera:",
                            text = "Segera panggil perawat bila muncul sesak napas, jantung berdebar kencang, muntah berulang, pusing berputar hebat, atau balutan perban operasi tampak merembes darah segar."
                        )
                    )
                )
            ),
            reassuranceNote = "Tip Keselamatan Pasien: Jangan turun dari tempat tidur atau berjalan ke kamar mandi sendirian pada 6 sampai 12 jam pertama setelah operasi. Selalu minta pendampingan perawat atau keluarga.",
            hasVideo = false
        ),
        ChecklistItem(
            id = "pain",
            category = "Domain 4: Nyeri & Relaksasi",
            title = "Manajemen Nyeri & Relaksasi Napas Dalam",
            shortSummary = "Rasa nyeri setelah operasi dapat dikendalikan secara efektif melalui kombinasi obat antinyeri terjadwal dari tim medis dan latihan relaksasi napas dalam mandiri untuk menenangkan saraf.",
            detailSections = listOf(
                EducationSection(
                    sectionTitle = "Langkah Relaksasi Napas Dalam (Pola 4-2-6):",
                    bullets = listOf(
                        StepBullet(
                            boldPrefix = "1. Posisi Nyaman:",
                            text = "Rilekskan bahu di atas bantal. Bila luka operasi di perut/dada, sangga lembut area tersebut dengan bantal kecil saat bernapas."
                        ),
                        StepBullet(
                            boldPrefix = "2. Tarik Napas (4 Detik):",
                            text = "Hirup udara perlahan dan dalam melalui hidung selama 4 hitungan sambil merasakan udara masuk dengan tenang."
                        ),
                        StepBullet(
                            boldPrefix = "3. Tahan Sejenak (2 Detik):",
                            text = "Tahan napas dengan rileks selama 2 hitungan tanpa mengejan."
                        ),
                        StepBullet(
                            boldPrefix = "4. Hembuskan Perlahan (6 Detik):",
                            text = "Keluarkan udara perlahan lewat bibir sedikit mengerucut (seperti meniup lilin pelan) selama 6 hitungan. Ulangi 5 sampai 10 siklus."
                        ),
                        StepBullet(
                            boldPrefix = "5. Laporkan Skala Nyeri (0 sampai 10):",
                            text = "Jangan menahan nyeri sendirian. Sebutkan angka nyeri Anda (0 = tidak nyeri hingga 10 = sangat nyeri) kepada perawat."
                        )
                    )
                )
            ),
            hasVideo = true,
            videoButtonLabel = "Tonton Video Relaksasi Napas Dalam",
            youtubeUrl = DEFAULT_RELAXATION_VIDEO_URL,
            youtubeTitle = "Video Relaksasi Napas Dalam",
            hasInteractiveTool = true,
            interactiveToolType = "BREATHING"
        )
    )
}
