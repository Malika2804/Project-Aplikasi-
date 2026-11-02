package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PatientSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patient_sessions ORDER BY id DESC LIMIT 1")
    fun getLatestSession(): Flow<PatientSession?>

    @Query("SELECT * FROM patient_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<PatientSession>>

    @Query("SELECT * FROM patient_sessions WHERE id = :id")
    fun getSessionById(id: Long): Flow<PatientSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PatientSession): Long

    @Update
    suspend fun updateSession(session: PatientSession)

    @Query("DELETE FROM patient_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM patient_sessions")
    suspend fun clearAll()
}
