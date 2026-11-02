package com.example.data.sync

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.PatientSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GoogleSheetsSyncService {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val PREFS_NAME = "siap_operasi_sync_prefs"
    private const val KEY_WEBHOOK_URL = "google_sheets_webhook_url"
    private const val KEY_AUTO_SYNC = "auto_sync_enabled"

    fun getSavedWebhookUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WEBHOOK_URL, "") ?: ""
    }

    fun saveWebhookUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WEBHOOK_URL, url.trim()).apply()
    }

    fun isAutoSyncEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_SYNC, true)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    suspend fun syncSessionToCloud(context: Context, session: PatientSession): Result<String> {
        return withContext(Dispatchers.IO) {
            val webhookUrl = getSavedWebhookUrl(context)
            if (webhookUrl.isBlank() || webhookUrl.contains("...")) {
                return@withContext Result.failure(Exception("URL Webhook Google Sheets belum diatur. Data tetap aman tersimpan di perangkat dan siap diekspor ke CSV/Excel."))
            }

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timestampStr = dateFormat.format(Date(if (session.completedAt > 0) session.completedAt else session.createdAt))
                val apaisJsonArray = org.json.JSONArray(session.apaisAnswersList)

                val json = JSONObject().apply {
                    // Keys matching standalone Netlify HTML payload
                    put("id", session.id)
                    put("fullName", session.fullName)
                    put("age", session.age)
                    put("gender", session.gender)
                    put("surgery", session.plannedSurgery)
                    put("rmNumber", session.medicalRecordNumber)
                    put("apaisAnswers", apaisJsonArray)
                    put("anxietyScore", session.anxietyScore)
                    put("infoScore", session.infoScore)
                    put("specificConcerns", session.specificConcerns)
                    put("timestamp", timestampStr)

                    // Indonesian snake_case keys for spreadsheet script compatibility
                    put("no_rekam_medis", session.medicalRecordNumber)
                    put("nama_pasien", session.fullName)
                    put("usia", session.age)
                    put("jenis_kelamin", session.gender)
                    put("rencana_operasi", session.plannedSurgery)
                    put("domain_1_prosedur_anestesi", if (session.checklistProcedure) "YA" else "TIDAK")
                    put("domain_2_persiapan_puasa", if (session.checklistPrep) "YA" else "TIDAK")
                    put("domain_3_risiko_efek_samping", if (session.checklistSideEffects) "YA" else "TIDAK")
                    put("domain_4_nyeri_koping", if (session.checklistPain) "YA" else "TIDAK")
                    put("total_domain_selesai", "${session.completedChecklistCount}/${session.totalChecklistCount}")
                    put("skor_kecemasan_apais", session.anxietyScore)
                    put("skor_info_apais", session.infoScore)
                    put("detail_jawaban_apais", "[${session.apaisAnswersList.joinToString(", ")}]")
                    put("kekhawatiran_khusus", session.specificConcerns)
                    put("status_evaluasi", if (session.isCompleted) "Selesai" else "Dalam Proses")
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code in 300..399) {
                        Result.success("✅ Data berhasil tersimpan & terkirim ke Google Sheets!")
                    } else {
                        Result.failure(Exception("Gagal mengirim data ke server (HTTP ${response.code})"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(Exception("Koneksi gagal: ${e.localizedMessage ?: "Terjadi kesalahan jaringan"}"))
            }
        }
    }

    fun generateCsv(sessions: List<PatientSession>): String {
        val sb = StringBuilder()
        sb.append("ID,Nama Lengkap,Usia,Gender,Tindakan Operasi,No RM,Skor Kecemasan APAIS,Skor Info APAIS,Detail Jawaban (1-6),Kekhawatiran,Waktu\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (s in sessions) {
            val dateStr = dateFormat.format(Date(if (s.completedAt > 0) s.completedAt else s.createdAt))
            val cleanName = s.fullName.replace("\"", "\"\"")
            val cleanSurgery = s.plannedSurgery.replace("\"", "\"\"")
            val cleanRm = s.medicalRecordNumber.replace("\"", "\"\"")
            val cleanNotes = s.specificConcerns
                .replace("\"", "\"\"")
                .replace("\r", " ")
                .replace("\n", " ")
            val detailAnswers = "[${s.apaisAnswersList.joinToString(", ")}]"

            sb.append("${s.id},")
            sb.append("\"$cleanName\",")
            sb.append("${s.age},")
            sb.append("\"${s.gender}\",")
            sb.append("\"$cleanSurgery\",")
            sb.append("\"$cleanRm\",")
            sb.append("${s.anxietyScore},")
            sb.append("${s.infoScore},")
            sb.append("\"$detailAnswers\",")
            sb.append("\"$cleanNotes\",")
            sb.append("\"$dateStr\"\n")
        }

        return sb.toString()
    }

    fun shareCsvIntent(context: Context, csvContent: String) {
        try {
            val exportDir = java.io.File(context.cacheDir, "csv_exports").apply { mkdirs() }
            val csvFile = java.io.File(exportDir, "Rekap_APAIS_SiapOperasi.csv")
            csvFile.writeText("\uFEFF" + csvContent, Charsets.UTF_8)
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, csvContent)
                putExtra(Intent.EXTRA_TITLE, "Rekap Data Riset APAIS Siap Operasi")
                putExtra(Intent.EXTRA_SUBJECT, "Rekap Data Riset APAIS Pra-Bedah - Siap Operasi")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Ekspor CSV/Excel APAIS ke Google Drive / WhatsApp / Email").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Tidak ditemukan aplikasi untuk membagikan file CSV.", Toast.LENGTH_SHORT).show()
        }
    }
}
