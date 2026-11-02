package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChecklistData
import com.example.data.model.PatientSession
import com.example.data.repository.PatientRepository
import com.example.data.sync.GoogleSheetsSyncService
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class FontScalePreset(val scale: Float, val label: String, val percentageLabel: String) {
    SMALL(0.90f, "Kecil", "90%"),
    NORMAL(1.00f, "Normal", "100%"),
    LARGE(1.15f, "Besar", "115%"),
    EXTRA_LARGE(1.30f, "Sangat Besar", "130%")
}

sealed interface EvaluationSyncUiState {
    data object Idle : EvaluationSyncUiState
    data class Loading(val message: String) : EvaluationSyncUiState
    data class Success(val message: String) : EvaluationSyncUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatientRepository
    private val videoPrefs = application.getSharedPreferences("siap_operasi_videos", Context.MODE_PRIVATE)
    private val persistMutex = Mutex()

    val allSessions: StateFlow<List<PatientSession>>

    private val _currentSession = MutableStateFlow(PatientSession())
    val currentSession: StateFlow<PatientSession> = _currentSession.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _evalSyncUiState = MutableStateFlow<EvaluationSyncUiState>(EvaluationSyncUiState.Idle)
    val evalSyncUiState: StateFlow<EvaluationSyncUiState> = _evalSyncUiState.asStateFlow()

    // Always start in NORMAL (100%) layout when the app is opened
    private val _fontScalePreset = MutableStateFlow(FontScalePreset.NORMAL)
    val fontScalePreset: StateFlow<FontScalePreset> = _fontScalePreset.asStateFlow()

    private val _videoUrls = MutableStateFlow(loadSavedVideoUrls())
    val videoUrls: StateFlow<Map<String, String>> = _videoUrls.asStateFlow()

    private val _webAccessUrl = MutableStateFlow(loadSavedWebAccessUrl())
    val webAccessUrl: StateFlow<String> = _webAccessUrl.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PatientRepository(database.patientDao())

        // Clear any legacy saved enlarged font preset so app always opens in Normal layout
        application.getSharedPreferences("siap_operasi_accessibility", Context.MODE_PRIVATE)
            .edit()
            .remove("font_scale_preset")
            .apply()

        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Restore uncompleted session once on cold start only
        viewModelScope.launch {
            val session = repository.latestSession.first()
            if (session != null && _currentSession.value.id == 0L && !session.isCompleted) {
                _currentSession.update { current ->
                    if (current.id == 0L && current.fullName.isBlank()) session else current
                }
            }
        }
    }

    private fun normalizeVideoUrl(rawUrl: String?, fallback: String): String {
        val trimmed = rawUrl?.trim().orEmpty()
        if (trimmed.isBlank()) return fallback
        return if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun loadSavedVideoUrls(): Map<String, String> {
        val defaultPainUrl = ChecklistData.DEFAULT_RELAXATION_VIDEO_URL
        return mapOf(
            "pain" to normalizeVideoUrl(
                videoPrefs.getString(PREF_KEY_RELAXATION_VIDEO, null),
                defaultPainUrl
            )
        )
    }

    fun saveCustomVideoUrl(painUrl: String) {
        val normalizedPainUrl = normalizeVideoUrl(
            painUrl,
            ChecklistData.DEFAULT_RELAXATION_VIDEO_URL
        )
        videoPrefs.edit()
            .putString(PREF_KEY_RELAXATION_VIDEO, normalizedPainUrl)
            .apply()
        _videoUrls.value = mapOf("pain" to normalizedPainUrl)
    }

    fun resetVideoUrlsToDefault() {
        videoPrefs.edit().remove(PREF_KEY_RELAXATION_VIDEO).apply()
        _videoUrls.value = ChecklistData.defaultVideoUrls
    }

    private fun loadSavedWebAccessUrl(): String {
        return QrCodeGenerator.normalizeWebUrl(
            videoPrefs.getString(PREF_KEY_WEB_ACCESS_URL, null),
            QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
        )
    }

    fun saveCustomWebAccessUrl(rawUrl: String) {
        val normalized = QrCodeGenerator.normalizeWebUrl(
            rawUrl,
            QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
        )
        videoPrefs.edit()
            .putString(PREF_KEY_WEB_ACCESS_URL, normalized)
            .apply()
        _webAccessUrl.value = normalized
    }

    fun resetWebAccessUrlToDefault() {
        videoPrefs.edit().remove(PREF_KEY_WEB_ACCESS_URL).apply()
        _webAccessUrl.value = QrCodeGenerator.DEFAULT_WEB_ACCESS_URL
    }

    companion object {
        private const val PREF_KEY_RELAXATION_VIDEO = "pain_relaxation_v2"
        private const val PREF_KEY_WEB_ACCESS_URL = "qr_web_access_url_v1"
    }

    fun setFontScalePreset(preset: FontScalePreset) {
        _fontScalePreset.value = preset
    }

    fun increaseFontSize() {
        val presets = FontScalePreset.entries
        val currentIndex = presets.indexOf(_fontScalePreset.value)
        if (currentIndex < presets.lastIndex) {
            setFontScalePreset(presets[currentIndex + 1])
        }
    }

    fun decreaseFontSize() {
        val presets = FontScalePreset.entries
        val currentIndex = presets.indexOf(_fontScalePreset.value)
        if (currentIndex > 0) {
            setFontScalePreset(presets[currentIndex - 1])
        }
    }

    fun resetFontSizeToNormal() {
        setFontScalePreset(FontScalePreset.NORMAL)
    }

    fun saveProfile(
        fullName: String,
        age: Int,
        gender: String,
        surgery: String,
        rmNumber: String
    ) {
        _currentSession.update { current ->
            current.copy(
                fullName = fullName,
                age = age,
                gender = gender,
                plannedSurgery = surgery,
                medicalRecordNumber = rmNumber
            )
        }
        persistCurrentSession()
    }

    fun toggleChecklist(itemId: String, isChecked: Boolean) {
        var shouldPersist = false
        _currentSession.update { current ->
            val updated = when (itemId) {
                "procedure" -> current.copy(checklistProcedure = isChecked)
                "prep" -> current.copy(checklistPrep = isChecked)
                "sideeffects" -> current.copy(checklistSideEffects = isChecked)
                "pain" -> current.copy(checklistPain = isChecked)
                else -> current
            }
            shouldPersist = updated.fullName.isNotBlank() || updated.id != 0L
            updated
        }
        if (shouldPersist) {
            persistCurrentSession()
        }
    }

    fun saveEvaluation(
        apaisAnswers: List<Int>,
        specificConcerns: String
    ) {
        val a1 = apaisAnswers.getOrElse(0) { 1 }.coerceIn(1, 5)
        val a2 = apaisAnswers.getOrElse(1) { 1 }.coerceIn(1, 5)
        val a3 = apaisAnswers.getOrElse(2) { 1 }.coerceIn(1, 5)
        val a4 = apaisAnswers.getOrElse(3) { 1 }.coerceIn(1, 5)
        val a5 = apaisAnswers.getOrElse(4) { 1 }.coerceIn(1, 5)
        val a6 = apaisAnswers.getOrElse(5) { 1 }.coerceIn(1, 5)

        val anxietyTotal = a1 + a2 + a4 + a5 // Subskala Kecemasan (Rentang: 4 - 20)
        val infoTotal = a3 + a6              // Subskala Kebutuhan Informasi (Rentang: 2 - 10)

        val savedWebhook = GoogleSheetsSyncService.getSavedWebhookUrl(getApplication())
        val hasWebhook = GoogleSheetsSyncService.isAutoSyncEnabled(getApplication()) &&
            savedWebhook.isNotBlank() &&
            !savedWebhook.contains("...")

        _evalSyncUiState.value = if (hasWebhook) {
            EvaluationSyncUiState.Loading("Mengirim data ke Google Sheets...")
        } else {
            EvaluationSyncUiState.Success("✅ Data berhasil tersimpan.")
        }

        lateinit var updatedSnapshot: PatientSession
        _currentSession.update { base ->
            updatedSnapshot = base.copy(
                fullName = base.fullName.ifBlank { "Pasien Pra-Bedah" },
                apais1 = a1,
                apais2 = a2,
                apais3 = a3,
                apais4 = a4,
                apais5 = a5,
                apais6 = a6,
                anxietyScore = anxietyTotal,
                infoScore = infoTotal,
                specificConcerns = specificConcerns,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            updatedSnapshot
        }
        persistCurrentSession { persistedSession ->
            if (hasWebhook) {
                syncCurrentSessionToCloud(persistedSession)
            }
        }
    }

    fun startNewPatientSession() {
        val nextTimestamp = maxOf(System.currentTimeMillis(), _currentSession.value.createdAt + 1L)
        _currentSession.value = PatientSession(createdAt = nextTimestamp)
        _evalSyncUiState.value = EvaluationSyncUiState.Idle
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            persistMutex.withLock {
                repository.deleteSession(sessionId)
                if (_currentSession.value.id == sessionId) {
                    val nextTimestamp = maxOf(System.currentTimeMillis(), _currentSession.value.createdAt + 1L)
                    _currentSession.value = PatientSession(createdAt = nextTimestamp)
                    _evalSyncUiState.value = EvaluationSyncUiState.Idle
                }
            }
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            persistMutex.withLock {
                repository.clearAll()
                val nextTimestamp = maxOf(System.currentTimeMillis(), _currentSession.value.createdAt + 1L)
                _currentSession.value = PatientSession(createdAt = nextTimestamp)
                _evalSyncUiState.value = EvaluationSyncUiState.Idle
            }
        }
    }

    private fun persistCurrentSession(onPersisted: ((PatientSession) -> Unit)? = null) {
        viewModelScope.launch {
            val finalSnapshot: PatientSession = persistMutex.withLock {
                val snapshot = _currentSession.value
                val savedId = repository.saveSession(snapshot)
                if (snapshot.id == 0L && savedId != 0L) {
                    val withId = snapshot.copy(id = savedId)
                    _currentSession.update { current ->
                        if (current.id == 0L && current.createdAt == snapshot.createdAt) {
                            current.copy(id = savedId)
                        } else {
                            current
                        }
                    }
                    withId
                } else {
                    snapshot
                }
            }
            onPersisted?.invoke(finalSnapshot)
        }
    }

    private fun syncCurrentSessionToCloud(session: PatientSession) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = GoogleSheetsSyncService.syncSessionToCloud(getApplication(), session)
            _isSyncing.value = false
            result.onSuccess { msg ->
                _evalSyncUiState.value = EvaluationSyncUiState.Success(msg)
                Toast.makeText(getApplication(), "Tersinkronisasi ke Google Sheets!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                _evalSyncUiState.value = EvaluationSyncUiState.Success(
                    "✅ Data aman tersimpan di perangkat (siap diekspor ke CSV/Excel)."
                )
                if (GoogleSheetsSyncService.getSavedWebhookUrl(getApplication()).isNotBlank()) {
                    Toast.makeText(getApplication(), "Sinkronisasi tertunda: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
