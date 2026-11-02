package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun profile_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.profile.ProfileScreen(
          currentSession = com.example.data.model.PatientSession(
            fullName = "Ny. Siti Rahmawati",
            age = 62,
            gender = "Perempuan",
            plannedSurgery = "Seksio Sesarea",
            medicalRecordNumber = "RM-98231"
          ),
          onSaveProfile = { _, _, _, _, _ -> },
          onNavigateToChecklist = {},
          onResetPatient = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
