package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ProtectedMenuTarget
import com.example.ui.components.QrAccessDialog
import com.example.ui.components.QrUrlSettingsDialog
import com.example.ui.components.ResearchDataDialog
import com.example.ui.components.StaffPasswordDialog
import com.example.ui.components.VideoSettingsDialog
import com.example.ui.screens.checklist.ChecklistScreen
import com.example.ui.screens.evaluation.EvaluationScreen
import com.example.ui.screens.infohub.InfoHubScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.theme.MedicalCanvasBg
import com.example.ui.theme.MedicalOutline
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OceanBlueContainer
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBlueLight
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.ToskaLight
import com.example.ui.theme.ToskaSecondary
import com.example.ui.viewmodel.FontScalePreset
import com.example.ui.viewmodel.MainViewModel

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
                val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
                val fontScalePreset by viewModel.fontScalePreset.collectAsStateWithLifecycle()
                val videoUrls by viewModel.videoUrls.collectAsStateWithLifecycle()
                val webAccessUrl by viewModel.webAccessUrl.collectAsStateWithLifecycle()
                val evalSyncUiState by viewModel.evalSyncUiState.collectAsStateWithLifecycle()

                var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
                var showQrAccessDialog by rememberSaveable { mutableStateOf(false) }
                var showQrUrlSettingsDialog by rememberSaveable { mutableStateOf(false) }
                var showResearchDialog by rememberSaveable { mutableStateOf(false) }
                var showVideoSettingsDialog by rememberSaveable { mutableStateOf(false) }
                var pendingProtectedTarget by rememberSaveable { mutableStateOf<ProtectedMenuTarget?>(null) }

                BackHandler(enabled = selectedTabIndex != 0) {
                    selectedTabIndex = 0
                }

                val baseDensity = LocalDensity.current
                val contentDensity = remember(baseDensity, fontScalePreset) {
                    Density(
                        density = baseDensity.density,
                        fontScale = baseDensity.fontScale * fontScalePreset.scale
                    )
                }

                val tabs = listOf(
                    NavTabItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile"),
                    NavTabItem("Checklist", Icons.Filled.FactCheck, Icons.Outlined.FactCheck, "tab_checklist"),
                    NavTabItem("Evaluasi", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, "tab_evaluation"),
                    NavTabItem("Info Hub", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "tab_infohub")
                )

                val safeTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White,
                    topBar = {
                        Surface(
                            color = Color.White,
                            shadowElevation = 0.dp
                        ) {
                            Column(modifier = Modifier.statusBarsPadding()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = ToskaLight,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.FactCheck,
                                                    contentDescription = null,
                                                    tint = ToskaSecondary,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(9.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Siap Operasi",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = OceanBluePrimary,
                                                fontSize = 15.sp,
                                                lineHeight = 18.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Checklist Edukasi Pra-Bedah (Dawod et al., 2024)",
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.5.sp,
                                                lineHeight = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { showQrAccessDialog = true },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("open_qr_top_btn")
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = ToskaLight,
                                                border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.QrCode2,
                                                        contentDescription = "QR Code Akses",
                                                        tint = ToskaSecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { pendingProtectedTarget = ProtectedMenuTarget.VIDEO_SETTINGS },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("open_video_settings_top_btn")
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MedicalCanvasBg,
                                                border = BorderStroke(1.dp, MedicalOutline),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.VideoLibrary,
                                                        contentDescription = "Pengaturan Video",
                                                        tint = OceanBluePrimary,
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { pendingProtectedTarget = ProtectedMenuTarget.RESEARCH_DATA },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("open_research_top_btn")
                                        ) {
                                            BadgedBox(
                                                badge = {
                                                    if (allSessions.isNotEmpty()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(10.dp),
                                                            color = ToskaSecondary
                                                        ) {
                                                            Text(
                                                                text = "${allSessions.size}",
                                                                fontSize = 9.5.sp,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MedicalCanvasBg,
                                                    border = BorderStroke(1.dp, MedicalOutline),
                                                    modifier = Modifier.size(38.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.CloudDone,
                                                            contentDescription = "Rekap Data Riset",
                                                            tint = OceanBluePrimary,
                                                            modifier = Modifier.size(19.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                AccessibilityFontControlBar(
                                    currentPreset = fontScalePreset,
                                    onDecrease = { viewModel.decreaseFontSize() },
                                    onResetNormal = { viewModel.resetFontSizeToNormal() },
                                    onIncrease = { viewModel.increaseFontSize() }
                                )

                                HorizontalDivider(thickness = 1.dp, color = MedicalOutline)
                            }
                        }
                    },
                    bottomBar = {
                        Surface(
                            color = Color.White,
                            border = BorderStroke(1.dp, MedicalOutline)
                        ) {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 0.dp
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    val isSelected = safeTabIndex == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { selectedTabIndex = index },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                fontSize = 11.5.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = OceanBluePrimary,
                                            selectedTextColor = OceanBluePrimary,
                                            indicatorColor = OceanBlueLight,
                                            unselectedIconColor = Color(0xFF475569),
                                            unselectedTextColor = Color(0xFF475569)
                                        ),
                                        modifier = Modifier.testTag(tab.tag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    CompositionLocalProvider(LocalDensity provides contentDensity) {
                        val saveableStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            saveableStateHolder.SaveableStateProvider(safeTabIndex) {
                                when (safeTabIndex) {
                                    0 -> ProfileScreen(
                                        currentSession = currentSession,
                                        onSaveProfile = { name, age, gender, surgery, rm ->
                                            viewModel.saveProfile(name, age, gender, surgery, rm)
                                        },
                                        onNavigateToChecklist = { selectedTabIndex = 1 },
                                        onResetPatient = { viewModel.startNewPatientSession() }
                                    )
                                    1 -> ChecklistScreen(
                                        currentSession = currentSession,
                                        videoUrls = videoUrls,
                                        onToggleChecklist = { itemId, isChecked ->
                                            viewModel.toggleChecklist(itemId, isChecked)
                                        },
                                        onNavigateToEvaluation = { selectedTabIndex = 2 }
                                    )
                                    2 -> EvaluationScreen(
                                        currentSession = currentSession,
                                        syncUiState = evalSyncUiState,
                                        onSaveEvaluation = { apaisAnswers, concerns ->
                                            viewModel.saveEvaluation(apaisAnswers, concerns)
                                        },
                                        onNavigateToInfoHub = { selectedTabIndex = 3 }
                                    )
                                    3 -> InfoHubScreen(
                                        onOpenQrAccess = {
                                            showQrAccessDialog = true
                                        },
                                        onOpenVideoSettings = {
                                            pendingProtectedTarget = ProtectedMenuTarget.VIDEO_SETTINGS
                                        },
                                        onOpenResearchPanel = {
                                            pendingProtectedTarget = ProtectedMenuTarget.RESEARCH_DATA
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // QR Code Access Dialog (Open for Quick Scanning to Chrome / Safari)
                if (showQrAccessDialog) {
                    CompositionLocalProvider(LocalDensity provides contentDensity) {
                        QrAccessDialog(
                            webAccessUrl = webAccessUrl,
                            onRequestEditUrl = {
                                showQrAccessDialog = false
                                pendingProtectedTarget = ProtectedMenuTarget.QR_LINK_SETTINGS
                            },
                            onDismiss = { showQrAccessDialog = false }
                        )
                    }
                }

                // Staff Password Gate Dialog (Protects Video, Research Data, and QR Target URL)
                pendingProtectedTarget?.let { target ->
                    CompositionLocalProvider(LocalDensity provides contentDensity) {
                        StaffPasswordDialog(
                            target = target,
                            onUnlocked = {
                                pendingProtectedTarget = null
                                when (target) {
                                    ProtectedMenuTarget.VIDEO_SETTINGS -> showVideoSettingsDialog = true
                                    ProtectedMenuTarget.RESEARCH_DATA -> showResearchDialog = true
                                    ProtectedMenuTarget.QR_LINK_SETTINGS -> showQrUrlSettingsDialog = true
                                }
                            },
                            onDismiss = { pendingProtectedTarget = null }
                        )
                    }
                }

                // Staff QR Target URL Customization Dialog
                if (showQrUrlSettingsDialog) {
                    CompositionLocalProvider(LocalDensity provides contentDensity) {
                        QrUrlSettingsDialog(
                            currentWebUrl = webAccessUrl,
                            onSaveUrl = { newUrl ->
                                viewModel.saveCustomWebAccessUrl(newUrl)
                                showQrUrlSettingsDialog = false
                                showQrAccessDialog = true
                            },
                            onResetDefault = {
                                viewModel.resetWebAccessUrlToDefault()
                            },
                            onDismiss = {
                                showQrUrlSettingsDialog = false
                                showQrAccessDialog = true
                            }
                        )
                    }
                }

                // Custom YouTube Video Link Dialog (Relaksasi Napas Dalam)
                if (showVideoSettingsDialog) {
                    CompositionLocalProvider(LocalDensity provides contentDensity) {
                        VideoSettingsDialog(
                            currentVideoUrls = videoUrls,
                            onSaveUrl = { painUrl ->
                                viewModel.saveCustomVideoUrl(painUrl)
                            },
                            onResetDefault = { viewModel.resetVideoUrlsToDefault() },
                            onDismiss = { showVideoSettingsDialog = false }
                        )
                    }
                }

                // Research Recap & CSV Export Dialog
                if (showResearchDialog) {
                    CompositionLocalProvider(LocalDensity provides contentDensity) {
                        ResearchDataDialog(
                            allSessions = allSessions,
                            currentSession = currentSession,
                            onDismiss = { showResearchDialog = false },
                            onDeleteSession = { id -> viewModel.deleteSession(id) },
                            onClearAllSessions = { viewModel.clearAllSessions() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityFontControlBar(
    currentPreset: FontScalePreset,
    onDecrease: () -> Unit,
    onResetNormal: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        color = OceanBlueLight,
        border = BorderStroke(0.5.dp, OceanBlueContainer),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("accessibility_font_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Text(
                        text = "Aa",
                        fontWeight = FontWeight.ExtraBold,
                        color = OceanBluePrimary,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ukuran Huruf: ${currentPreset.label} (${currentPreset.percentageLabel})",
                    fontWeight = FontWeight.Bold,
                    color = OceanBlueDark,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDecrease,
                    enabled = currentPreset != FontScalePreset.SMALL,
                    shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = OceanBluePrimary
                    ),
                    border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp),
                    modifier = Modifier
                        .heightIn(min = 30.dp)
                        .testTag("font_decrease_btn")
                ) {
                    Text(
                        text = "A-",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                val isNormal = currentPreset == FontScalePreset.NORMAL
                FilledTonalButton(
                    onClick = onResetNormal,
                    shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isNormal) OceanBluePrimary else Color.White,
                        contentColor = if (isNormal) Color.White else OceanBluePrimary
                    ),
                    border = if (!isNormal) BorderStroke(1.dp, Color(0xFF93C5FD)) else null,
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp),
                    modifier = Modifier
                        .heightIn(min = 30.dp)
                        .testTag("font_normal_btn")
                ) {
                    Text(
                        text = "Normal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                FilledTonalButton(
                    onClick = onIncrease,
                    enabled = currentPreset != FontScalePreset.EXTRA_LARGE,
                    shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ToskaSecondary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .heightIn(min = 30.dp)
                        .testTag("font_increase_btn")
                ) {
                    Text(
                        text = "A+",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
