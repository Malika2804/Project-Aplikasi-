package com.example

import com.example.data.model.ChecklistData
import com.example.data.model.PatientSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun patientSession_initialValidationAndApaisDefaults() {
        val emptySession = PatientSession()
        assertFalse(emptySession.isProfileValid)
        assertEquals(0, emptySession.completedChecklistCount)
        assertEquals(4, emptySession.totalChecklistCount)
        assertEquals(4, emptySession.anxietyScore)
        assertEquals(2, emptySession.infoScore)
        assertEquals(listOf(1, 1, 1, 1, 1, 1), emptySession.apaisAnswersList)
    }

    @Test
    fun patientSession_profileFieldsAndValidation() {
        val session = PatientSession(
            fullName = "Ahmad Dahlan",
            age = 45,
            gender = "Laki-laki",
            plannedSurgery = "Apendiktomi",
            medicalRecordNumber = "RM-9941"
        )

        assertTrue(session.isProfileValid)
        assertEquals("Ahmad Dahlan", session.fullName)
        assertEquals(45, session.age)
        assertEquals("Laki-laki", session.gender)
        assertEquals("Apendiktomi", session.plannedSurgery)
        assertEquals("RM-9941", session.medicalRecordNumber)
    }

    @Test
    fun patientSession_checklistProgress() {
        val session = PatientSession(
            checklistProcedure = true,
            checklistPrep = true
        )
        assertEquals(2, session.completedChecklistCount)
        assertEquals(0.5f, session.checklistProgress, 0.001f)
        assertEquals(50, session.checklistPercentage)
    }

    @Test
    fun checklistDomains_onlyDomain4HasVideoAndDomains1To3HaveComprehensiveGuides() {
        val itemsById = ChecklistData.items.associateBy { it.id }
        assertEquals(4, itemsById.size)

        listOf("procedure", "prep", "sideeffects").forEach { id ->
            val item = itemsById.getValue(id)
            assertFalse("Domain $id should not have video", item.hasVideo)
            assertTrue("Domain $id should have multiple structured sections", item.detailSections.size >= 2)
            assertTrue("Domain $id should have comprehensive bullets", item.practicalSteps.size >= 6)
            assertNotNull("Domain $id should have a reassurance note", item.reassuranceNote)
        }

        val domain4 = itemsById.getValue("pain")
        assertTrue(domain4.hasVideo)
        assertEquals("https://www.youtube.com/watch?v=TPIEEQtAUjg", domain4.youtubeUrl)
        assertEquals(
            "https://www.youtube.com/watch?v=TPIEEQtAUjg",
            ChecklistData.defaultVideoUrls["pain"]
        )
    }
}
