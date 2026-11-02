package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.PatientSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Siap Operasi", appName)
  }

  @Test
  fun `insert and retrieve patient session from Room`() = runBlocking {
    val session = PatientSession(
      fullName = "Siti Nurhaliza",
      age = 32,
      gender = "Perempuan",
      plannedSurgery = "Seksio Sesarea (SC)",
      medicalRecordNumber = "RM-5502"
    )

    val id = db.patientDao().insertSession(session)
    val retrieved = db.patientDao().getLatestSession().first()

    assertNotNull(retrieved)
    assertEquals("Siti Nurhaliza", retrieved?.fullName)
    assertEquals(32, retrieved?.age)
    assertEquals("Perempuan", retrieved?.gender)
    assertEquals("Seksio Sesarea (SC)", retrieved?.plannedSurgery)
    assertEquals("RM-5502", retrieved?.medicalRecordNumber)
  }

  @Test
  fun `save APAIS evaluation scores and concerns in Room`() = runBlocking {
    val initial = PatientSession(
      fullName = "Budi Santoso",
      age = 50,
      gender = "Laki-laki",
      plannedSurgery = "Laparoskopi Kolesistektomi",
      medicalRecordNumber = "RM-8821"
    )
    val id = db.patientDao().insertSession(initial)

    val updated = initial.copy(
      id = id,
      apais1 = 3,
      apais2 = 2,
      apais3 = 4,
      apais4 = 3,
      apais5 = 2,
      apais6 = 5,
      anxietyScore = 10,
      infoScore = 9,
      specificConcerns = "Khawatir rasa dingin di ruang operasi pasca anestesi",
      isCompleted = true,
      completedAt = System.currentTimeMillis()
    )
    db.patientDao().updateSession(updated)

    val retrieved = db.patientDao().getSessionById(id).first()
    assertNotNull(retrieved)
    assertEquals(10, retrieved?.anxietyScore)
    assertEquals(9, retrieved?.infoScore)
    assertEquals(listOf(3, 2, 4, 3, 2, 5), retrieved?.apaisAnswersList)
    assertEquals("Khawatir rasa dingin di ruang operasi pasca anestesi", retrieved?.specificConcerns)
    assertEquals(true, retrieved?.isCompleted)
  }
}
