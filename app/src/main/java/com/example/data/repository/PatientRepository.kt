package com.example.data.repository

import com.example.data.local.PatientDao
import com.example.data.model.PatientSession
import kotlinx.coroutines.flow.Flow

class PatientRepository(private val patientDao: PatientDao) {

    val latestSession: Flow<PatientSession?> = patientDao.getLatestSession()
    val allSessions: Flow<List<PatientSession>> = patientDao.getAllSessions()

    fun getSessionById(id: Long): Flow<PatientSession?> = patientDao.getSessionById(id)

    suspend fun saveSession(session: PatientSession): Long {
        return patientDao.insertSession(session)
    }

    suspend fun updateSession(session: PatientSession) {
        patientDao.updateSession(session)
    }

    suspend fun deleteSession(id: Long) {
        patientDao.deleteSessionById(id)
    }

    suspend fun clearAll() {
        patientDao.clearAll()
    }
}
