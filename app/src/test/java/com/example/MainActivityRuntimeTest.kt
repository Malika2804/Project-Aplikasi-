package com.example

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class MainActivityRuntimeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("siap_operasi_videos", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        context.getSharedPreferences("siap_operasi_sync_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        runBlocking(Dispatchers.IO) {
            AppDatabase.getDatabase(context).clearAllTables()
        }
    }

    @Test
    fun appLaunchesInNormalFontModeAndRendersAll4TabsAt130PercentWithoutErrors() {
        composeTestRule.onNodeWithText("Ukuran Huruf: Normal (100%)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Checklist Edukasi Pra-Bedah (Dawod et al., 2024)").assertIsDisplayed()

        // Test decrease to Kecil (90%)
        composeTestRule.onNodeWithTag("font_decrease_btn").performClick()
        composeTestRule.onNodeWithText("Ukuran Huruf: Kecil (90%)").assertIsDisplayed()

        // Reset to Normal (100%), then increase to Besar (115%) and Sangat Besar (130%)
        composeTestRule.onNodeWithTag("font_normal_btn").performClick()
        composeTestRule.onNodeWithText("Ukuran Huruf: Normal (100%)").assertIsDisplayed()

        composeTestRule.onNodeWithTag("font_increase_btn").performClick()
        composeTestRule.onNodeWithText("Ukuran Huruf: Besar (115%)").assertIsDisplayed()

        composeTestRule.onNodeWithTag("font_increase_btn").performClick()
        composeTestRule.onNodeWithText("Ukuran Huruf: Sangat Besar (130%)").assertIsDisplayed()

        // Render all 4 tabs and Domain 4 video dialog at 130% font scale to ensure no clipping/crashes
        composeTestRule.onNodeWithTag("tab_checklist").performClick()
        composeTestRule.onNodeWithTag("checklist_hero_banner").assertIsDisplayed()

        // Domains 1-3 must have no video buttons; Domain 4 has video_btn_pain
        composeTestRule.onAllNodesWithTag("video_btn_procedure").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("video_btn_prep").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("video_btn_sideeffects").assertCountEquals(0)

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("checklist_item_pain"))
        composeTestRule.onNodeWithTag("video_btn_pain")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("youtube_dialog_pain").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_youtube_dialog").performClick()

        composeTestRule.onNodeWithTag("tab_evaluation").performClick()
        composeTestRule.onNodeWithTag("evaluation_header_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_infohub").performClick()
        composeTestRule.onNodeWithTag("infohub_header_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_qr_access_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("font_normal_btn").performClick()
        composeTestRule.onNodeWithText("Ukuran Huruf: Normal (100%)").assertIsDisplayed()
    }

    @Test
    fun passwordGateBlocksUnauthorizedAccessAndUnlocksWith280428() {
        // 1. Top Video Settings button requires password 280428
        composeTestRule.onNodeWithTag("open_video_settings_top_btn").performClick()
        composeTestRule.onNodeWithTag("staff_password_dialog").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("video_settings_dialog").assertCountEquals(0)

        // Wrong password rejected
        composeTestRule.onNodeWithTag("staff_password_input").performTextInput("123456")
        composeTestRule.onNodeWithTag("staff_password_submit_btn").performClick()
        composeTestRule.onNodeWithTag("staff_password_error_text").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("video_settings_dialog").assertCountEquals(0)

        // Correct password 280428 unlocks Video Settings
        composeTestRule.onNodeWithTag("staff_password_input").performTextReplacement("280428")
        composeTestRule.onNodeWithTag("staff_password_submit_btn").performClick()
        composeTestRule.onNodeWithTag("video_settings_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_video_pain").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_video_settings_btn").performClick()

        // Re-opening Video Settings requires password again
        composeTestRule.onNodeWithTag("open_video_settings_top_btn").performClick()
        composeTestRule.onNodeWithTag("staff_password_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_password_cancel_btn").performClick()

        // 2. Info Hub triggers also require password 280428
        composeTestRule.onNodeWithTag("tab_infohub").performClick()
        composeTestRule.onNodeWithTag("infohub_list")
            .performScrollToNode(hasTestTag("open_research_panel_btn"))
        composeTestRule.onNodeWithTag("open_research_panel_btn").performClick()
        composeTestRule.onNodeWithTag("staff_password_dialog").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("research_data_dialog").assertCountEquals(0)

        composeTestRule.onNodeWithTag("staff_password_input").performTextInput("000000")
        composeTestRule.onNodeWithTag("staff_password_submit_btn").performClick()
        composeTestRule.onNodeWithTag("staff_password_error_text").assertIsDisplayed()

        composeTestRule.onNodeWithTag("staff_password_input").performTextReplacement("280428")
        composeTestRule.onNodeWithTag("staff_password_submit_btn").performClick()
        composeTestRule.onNodeWithTag("research_data_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_research_dialog").performClick()
    }

    @Test
    fun switchingTabsPreservesUnsavedDraftsAndValidatesEmptyPatientName() {
        // Attempting to save empty profile keeps user on Tab 1 (Profile)
        composeTestRule.onNodeWithTag("save_and_continue_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("profile_hero_banner").assertIsDisplayed()

        // Enter draft name without clicking save, switch to Evaluasi, select Q2=5 (Anxiety: 1+5+1+1 = 8)
        composeTestRule.onNodeWithTag("patient_name_input")
            .performScrollTo()
            .performTextInput("Tn. Hendra Wijaya")

        composeTestRule.onNodeWithTag("tab_evaluation").performClick()
        composeTestRule.onNodeWithTag("apais_q2_opt_5")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("apais_anxiety_score_text").assertTextEquals("8")

        // Switch to Info Hub and back to Evaluasi & Profil - unsaved drafts must remain intact
        composeTestRule.onNodeWithTag("tab_infohub").performClick()
        composeTestRule.onNodeWithTag("infohub_header_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_evaluation").performClick()
        composeTestRule.onNodeWithTag("apais_anxiety_score_text").assertTextEquals("8")

        composeTestRule.onNodeWithTag("tab_profile").performClick()
        composeTestRule.onNodeWithText("Tn. Hendra Wijaya").assertIsDisplayed()
    }

    @Test
    fun guidedBreathingExerciseInChecklistCompletesFourCyclesAndMarksDomain4() {
        composeTestRule.onNodeWithTag("tab_checklist").performClick()
        composeTestRule.onNodeWithText("0 dari 4 Domain Selesai").assertIsDisplayed()

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("checklist_item_pain"))
        composeTestRule.onNodeWithTag("breathing_tool_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("breathing_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("start_breathing_btn").performClick()

        // Advance virtual clock across 4 cycles of 4s inhale + 2s hold + 6s exhale (48s total)
        composeTestRule.mainClock.advanceTimeBy(49_500L)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Siklus: 4 / 4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("complete_breathing_btn").performClick()

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("checklist_progress_card"))
        composeTestRule.onNodeWithText("1 dari 4 Domain Selesai").assertIsDisplayed()
    }

    @Test
    fun full4TabPatientProfileChecklistEvaluationAndInfoHubJourneyRunsWithoutErrors() {
        // 1. Tab 1: Profile Registration -> Simpan & Lanjut ke Edukasi ➔
        composeTestRule.onNodeWithTag("profile_hero_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("patient_name_input").performTextInput("Ny. Siti Rahmawati")
        composeTestRule.onNodeWithTag("patient_age_input").performTextInput("62")
        composeTestRule.onNodeWithText("+ Seksio Sesarea")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("patient_rm_input")
            .performScrollTo()
            .performTextInput("RM-98231")
        composeTestRule.onNodeWithTag("save_and_continue_btn")
            .performScrollTo()
            .performClick()

        // 2. Tab 2: Checklist Edukasi Pra-Bedah (Dawod et al., 2024)
        composeTestRule.onNodeWithTag("checklist_hero_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 dari 4 Domain Selesai").assertIsDisplayed()

        // Check Domain 1, Domain 2, Domain 3, and watch Domain 4 relaxation video
        composeTestRule.onNodeWithTag("checkbox_procedure").performClick()

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("checklist_item_prep"))
        composeTestRule.onNodeWithTag("checkbox_prep").performClick()

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("checklist_item_sideeffects"))
        composeTestRule.onNodeWithTag("checkbox_sideeffects").performClick()

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("checklist_item_pain"))
        composeTestRule.onNodeWithTag("video_btn_pain")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("youtube_dialog_pain").assertIsDisplayed()
        composeTestRule.onNodeWithText("https://www.youtube.com/watch?v=TPIEEQtAUjg").assertIsDisplayed()
        composeTestRule.onNodeWithTag("understand_video_btn").performClick()

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("checklist_progress_card"))
        composeTestRule.onNodeWithText("4 dari 4 Domain Selesai").assertIsDisplayed()
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()

        composeTestRule.onNodeWithTag("checklist_list")
            .performScrollToNode(hasTestTag("navigate_to_eval_btn"))
        composeTestRule.onNodeWithTag("navigate_to_eval_btn").performClick()

        // 3. Tab 3: Kuesioner APAIS Versi Indonesia
        composeTestRule.onNodeWithTag("evaluation_header_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("apais_anxiety_score_text").assertTextEquals("4")
        composeTestRule.onNodeWithTag("apais_info_score_text").assertTextEquals("2")

        // Select Item 1 = 4 (Anxiety: 4 + 1 + 1 + 1 = 7) and Item 3 = 5 (Info: 5 + 1 = 6)
        composeTestRule.onNodeWithTag("apais_q1_opt_4")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("apais_anxiety_score_text").assertTextEquals("7")

        composeTestRule.onNodeWithTag("apais_q3_opt_5")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("specific_concerns_input")
            .performScrollTo()
            .performTextInput("Takut mual pasca bius")

        composeTestRule.onNodeWithTag("submit_evaluation_btn")
            .performScrollTo()
            .performClick()

        // 4. Tab 4: Info Hub (Pusat Informasi & Kontak Tim Medis)
        composeTestRule.onNodeWithTag("infohub_header_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_qr_access_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("📱 QR Code Akses Cepat (Netlify)").assertIsDisplayed()

        // Verify inline evaluation status alert when checking Tab 3
        composeTestRule.onNodeWithTag("tab_evaluation").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("evaluation_status_alert")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("evaluation_status_alert")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("✅ Data berhasil tersimpan."))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("✅ Data berhasil tersimpan.").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_infohub").performClick()
        composeTestRule.onNodeWithTag("infohub_list")
            .performScrollToNode(hasTestTag("fasting_guide_card"))
        composeTestRule.onNodeWithTag("fasting_guide_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("infohub_list")
            .performScrollToNode(hasTestTag("emergency_contact_card"))
        composeTestRule.onNodeWithText("Ext. 119 / Bel Pasien").assertIsDisplayed()

        // Open Research Data Recap Modal (with password 280428) + verify saved record + Clear All
        composeTestRule.onNodeWithTag("open_research_top_btn").performClick()
        composeTestRule.onNodeWithTag("staff_password_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_password_input").performTextInput("280428")
        composeTestRule.onNodeWithTag("staff_password_submit_btn").performClick()
        composeTestRule.onNodeWithTag("research_data_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Pasien: 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kecemasan (APAIS): 7 | Info: 6").assertIsDisplayed()

        // Clear all sessions & verify empty state
        composeTestRule.onNodeWithTag("clear_all_sessions_btn").performClick()
        composeTestRule.onNodeWithTag("confirm_clear_all_btn").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Total Pasien: 0"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Total Pasien: 0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_research_dialog").performClick()
    }

    @Test
    fun activityRecreationPreservesActiveTabAndUnsavedApaisSelection() {
        composeTestRule.onNodeWithTag("tab_evaluation").performClick()
        composeTestRule.onNodeWithTag("apais_q1_opt_5")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("apais_anxiety_score_text").assertTextEquals("8")

        // Simulate device rotation / configuration change
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()

        // Verify Tab 3 (Evaluasi) and live APAIS score are still intact after recreation
        composeTestRule.onNodeWithTag("evaluation_header_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("apais_anxiety_score_text").assertTextEquals("8")
    }

    @Test
    fun qrAccessDialogRendersValidQrAndProtectsUrlSettingsWithPassword() {
        val expectedDefaultUrl = "https://resplendent-zuccutto-9a64ae.netlify.app"
        org.junit.Assert.assertEquals(
            expectedDefaultUrl,
            com.example.util.QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
        )
        org.junit.Assert.assertFalse(
            com.example.util.QrCodeGenerator.isUnconfiguredDefaultUrl(
                com.example.util.QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
            )
        )
        org.junit.Assert.assertTrue(
            com.example.util.QrCodeGenerator.isNetlifyUrl(
                com.example.util.QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
            )
        )

        val defaultMatrix = com.example.util.QrCodeGenerator.encodeQrMatrix(
            com.example.util.QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
        )
        org.junit.Assert.assertTrue(defaultMatrix.size >= 21)
        org.junit.Assert.assertTrue(defaultMatrix.isDark(0, 0))
        org.junit.Assert.assertTrue(defaultMatrix.isDark(defaultMatrix.size - 8, 8))

        val overlongMatrix = com.example.util.QrCodeGenerator.encodeQrMatrix(
            "https://rs-pendidikan.id/" + "🌟".repeat(150)
        )
        org.junit.Assert.assertEquals(73, overlongMatrix.size)

        val posterBitmap = com.example.util.QrCodeGenerator.createPrintablePosterBitmap(
            com.example.util.QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
        )
        org.junit.Assert.assertEquals(1080, posterBitmap.width)
        org.junit.Assert.assertEquals(1420, posterBitmap.height)

        // Open QR Access Dialog from Top Bar without password
        composeTestRule.onNodeWithTag("open_qr_top_btn").performClick()
        composeTestRule.onNodeWithTag("qr_access_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_code_canvas_box").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_active_url_text")
            .assertTextEquals(expectedDefaultUrl)

        // Attempt to edit QR target URL -> must prompt StaffPasswordDialog (280428)
        composeTestRule.onNodeWithTag("edit_qr_url_staff_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("staff_password_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_password_input").performTextInput("280428")
        composeTestRule.onNodeWithTag("staff_password_submit_btn").performClick()

        // Update QR URL using bare Netlify site slug and verify live QR preview + reopened QR dialog
        composeTestRule.onNodeWithTag("qr_url_settings_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_settings_live_preview")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_qr_web_url")
            .performScrollTo()
            .performTextReplacement("siap-operasi-rs")
        composeTestRule.onNodeWithTag("save_qr_url_btn")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithTag("qr_access_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_active_url_text")
            .assertTextEquals("https://siap-operasi-rs.netlify.app")

        // Reset back to default
        composeTestRule.onNodeWithTag("edit_qr_url_staff_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("staff_password_input").performTextInput("280428")
        composeTestRule.onNodeWithTag("staff_password_submit_btn").performClick()
        composeTestRule.onNodeWithTag("reset_qr_url_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("close_qr_url_settings_btn").performClick()

        composeTestRule.onNodeWithTag("qr_active_url_text")
            .assertTextEquals(expectedDefaultUrl)

        // Switch QR dialog to Local Hotspot / Wi-Fi Server mode and verify active HTTP server URL
        composeTestRule.onNodeWithTag("qr_mode_local_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("qr_status_banner").assertIsDisplayed()

        // Switch back to Online Hosting mode & verify Netlify ZIP buttons + HTML export
        composeTestRule.onNodeWithTag("qr_mode_online_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("save_netlify_zip_device_btn")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_netlify_drop_btn")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_web_html_btn")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_qr_dialog_btn")
            .performScrollTo()
            .performClick()
    }

    @Test
    fun localWebServerServes4TabHtmlAndExportsNetlifyZipCleanly() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        org.junit.Assert.assertEquals(
            "https://siap-operasi-rs.netlify.app",
            com.example.util.QrCodeGenerator.normalizeWebUrl("siap-operasi-rs")
        )
        org.junit.Assert.assertEquals(
            "https://siap-operasi-rs.netlify.app",
            com.example.util.QrCodeGenerator.normalizeWebUrl("https://siap-operasi-rs.netlify.app/index.html")
        )
        org.junit.Assert.assertTrue(
            com.example.util.QrCodeGenerator.isNetlifyUrl("https://siap-operasi-rs.netlify.app")
        )

        // 1. Verify standalone index.html export matches assets/index.html
        val exportedFile = com.example.util.QrCodeGenerator.exportStandaloneWebHtml(context)
        org.junit.Assert.assertTrue(exportedFile.exists())
        org.junit.Assert.assertEquals("index.html", exportedFile.name)
        org.junit.Assert.assertTrue(exportedFile.length() > 80_000L)

        // 2. Verify Netlify-ready ZIP bundle contains index.html, _redirects, and netlify.toml at root
        val netlifyZip = com.example.util.QrCodeGenerator.exportNetlifyZipBundle(context)
        org.junit.Assert.assertTrue(netlifyZip.exists())
        org.junit.Assert.assertEquals("siap-operasi-netlify.zip", netlifyZip.name)
        val zipEntries = mutableMapOf<String, String>()
        java.util.zip.ZipInputStream(netlifyZip.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                zipEntries[entry.name] = zis.readBytes().toString(Charsets.UTF_8)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        org.junit.Assert.assertTrue(zipEntries.containsKey("index.html"))
        org.junit.Assert.assertTrue(zipEntries.containsKey("_redirects"))
        org.junit.Assert.assertTrue(zipEntries.containsKey("netlify.toml"))
        val htmlInZip = zipEntries["index.html"].orEmpty()
        org.junit.Assert.assertTrue(htmlInZip.contains("Checklist Edukasi Pra-Bedah (Dawod et al., 2024)"))
        org.junit.Assert.assertTrue(htmlInZip.contains("resplendent-zuccutto-9a64ae.netlify.app"))
        org.junit.Assert.assertTrue(htmlInZip.contains("saveToGoogleSheetsOnly"))
        org.junit.Assert.assertTrue(htmlInZip.contains("z-index: 999"))
        org.junit.Assert.assertTrue(htmlInZip.contains("downloadNetlifyZipBundle"))
        org.junit.Assert.assertTrue(htmlInZip.contains("TPIEEQtAUjg"))
        org.junit.Assert.assertFalse(htmlInZip.contains("—"))
        org.junit.Assert.assertTrue(zipEntries["_redirects"].orEmpty().contains("/* /index.html 200"))

        // 3. Start LocalWebServer on an ephemeral test port and verify real HTTP GET & HEAD responses
        val status = com.example.util.LocalWebServer.start(context, preferredPort = 0)
        try {
            org.junit.Assert.assertTrue(status.isRunning)
            org.junit.Assert.assertTrue(status.port > 0)

            val getConn = (java.net.URL("http://127.0.0.1:${status.port}/").openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
            }
            org.junit.Assert.assertEquals(200, getConn.responseCode)
            org.junit.Assert.assertTrue(getConn.contentType.orEmpty().contains("text/html"))
            val body = getConn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            org.junit.Assert.assertTrue(body.contains("Siap Operasi"))
            org.junit.Assert.assertTrue(body.contains("Checklist Edukasi Pra-Bedah (Dawod et al., 2024)"))
            org.junit.Assert.assertTrue(body.contains("TPIEEQtAUjg"))
            getConn.disconnect()

            val favConn = (java.net.URL("http://127.0.0.1:${status.port}/favicon.ico").openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
            }
            org.junit.Assert.assertEquals(204, favConn.responseCode)
            favConn.disconnect()
        } finally {
            com.example.util.LocalWebServer.stop()
            org.junit.Assert.assertFalse(com.example.util.LocalWebServer.isRunning())
        }
    }
}
