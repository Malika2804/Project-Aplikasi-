package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MedicalCanvasBg
import com.example.ui.theme.MedicalOutline
import com.example.ui.theme.MedicalSurfaceVariant
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBlueLight
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.ToskaLight
import com.example.ui.theme.ToskaSecondary
import com.example.util.LocalServerStatus
import com.example.util.LocalWebServer
import com.example.util.QrCodeGenerator

private enum class QrConnectionMode {
    ONLINE_HOSTING,
    LOCAL_HOTSPOT
}

@Composable
fun QrAccessDialog(
    webAccessUrl: String,
    onRequestEditUrl: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var connectionMode by rememberSaveable { mutableStateOf(QrConnectionMode.ONLINE_HOSTING) }
    var localServerStatus by remember { mutableStateOf<LocalServerStatus?>(null) }

    val saveNetlifyZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { targetUri ->
        if (targetUri != null) {
            QrCodeGenerator.saveNetlifyZipToUri(context, targetUri)
        }
    }

    DisposableEffect(connectionMode) {
        if (connectionMode == QrConnectionMode.LOCAL_HOTSPOT) {
            localServerStatus = LocalWebServer.start(context)
        } else {
            LocalWebServer.stop()
            localServerStatus = null
        }
        onDispose {
            LocalWebServer.stop()
        }
    }

    val normalizedOnlineUrl = remember(webAccessUrl) {
        QrCodeGenerator.normalizeWebUrl(webAccessUrl)
    }
    val isUnconfiguredOnlineUrl = remember(normalizedOnlineUrl) {
        QrCodeGenerator.isUnconfiguredDefaultUrl(normalizedOnlineUrl)
    }
    val isNetlifyOnlineUrl = remember(normalizedOnlineUrl) {
        QrCodeGenerator.isNetlifyUrl(normalizedOnlineUrl)
    }

    val activeQrUrl = if (connectionMode == QrConnectionMode.LOCAL_HOTSPOT) {
        localServerStatus?.url ?: "http://192.168.43.1:8080"
    } else {
        normalizedOnlineUrl
    }

    val qrMatrix = remember(activeQrUrl) {
        QrCodeGenerator.encodeQrMatrix(activeQrUrl)
    }

    fun copyUrlToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Link Siap Operasi", activeQrUrl))
        Toast.makeText(context, "Tautan web berhasil disalin!", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .testTag("qr_access_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MedicalOutline),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ToskaLight,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = null,
                                    tint = ToskaSecondary,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "QR Code Akses Aplikasi",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                fontSize = 15.5.sp
                            )
                            Text(
                                text = "Langsung ke Google Chrome atau Safari",
                                color = ToskaSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_qr_dialog_btn")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MedicalSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MedicalOutline)
                Spacer(modifier = Modifier.height(10.dp))

                // 2-Mode Switcher: Link Internet (Hosting Gratis) vs Hotspot / Wi-Fi HP
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MedicalCanvasBg,
                    border = BorderStroke(1.dp, MedicalOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isOnline = connectionMode == QrConnectionMode.ONLINE_HOSTING
                        Button(
                            onClick = { connectionMode = QrConnectionMode.ONLINE_HOSTING },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOnline) OceanBluePrimary else Color.Transparent,
                                contentColor = if (isOnline) Color.White else Color(0xFF475569)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("qr_mode_online_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Link Online",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val isLocal = connectionMode == QrConnectionMode.LOCAL_HOTSPOT
                        Button(
                            onClick = { connectionMode = QrConnectionMode.LOCAL_HOTSPOT },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLocal) ToskaSecondary else Color.Transparent,
                                contentColor = if (isLocal) Color.White else Color(0xFF475569)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .heightIn(min = 48.dp)
                                .testTag("qr_mode_local_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Hotspot / Wi-Fi HP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Connection Status Banner
                if (connectionMode == QrConnectionMode.ONLINE_HOSTING) {
                    if (isUnconfiguredOnlineUrl) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_status_banner")
                        ) {
                            Column(modifier = Modifier.padding(11.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Siap Diunggah ke Netlify agar Jadi QR Code Publik",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF92400E),
                                        fontSize = 11.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Simpan paket siap-operasi-netlify.zip di bawah, unggah ke app.netlify.com/drop, lalu tempel link .netlify.app untuk menjadikannya QR Code permanen (atau buka menu QR langsung di halaman Netlify Anda).",
                                    color = Color(0xFF78350F),
                                    fontSize = 11.sp,
                                    lineHeight = 15.5.sp
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_status_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = if (isNetlifyOnlineUrl) {
                                        "Terhubung ke Netlify: HTML Aktif & Siap Di-scan Kamera HP Pasien!"
                                    } else {
                                        "Link Web Publik Aktif: Siap di-scan kamera HP pasien kapan saja."
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.5.sp
                                )
                            }
                        }
                    }
                } else {
                    val status = localServerStatus
                    val isVirtualIp = status?.isVirtualOrLoopbackIp == true
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isVirtualIp) Color(0xFFFFFBEB) else ToskaLight,
                        border = BorderStroke(
                            1.dp,
                            if (isVirtualIp) Color(0xFFFDE68A) else Color(0xFF99F6E4)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qr_status_banner")
                    ) {
                        Column(modifier = Modifier.padding(11.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WifiTethering,
                                        contentDescription = null,
                                        tint = if (isVirtualIp) Color(0xFFB45309) else ToskaSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (status?.isRunning == true) {
                                            "Server Wi-Fi / Hotspot Aktif (Port ${status.port})"
                                        } else {
                                            "Menyiapkan Server Wi-Fi Lokal..."
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isVirtualIp) Color(0xFF92400E) else Color(0xFF0F766E),
                                        fontSize = 11.5.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        LocalWebServer.stop()
                                        localServerStatus = LocalWebServer.start(context)
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("refresh_local_ip_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Perbarui IP Wi-Fi atau Hotspot",
                                        tint = if (isVirtualIp) Color(0xFFB45309) else ToskaSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isVirtualIp) {
                                    "Catatan Emulator: Saat ini aplikasi berjalan di emulator pratinjau (IP virtual ${status?.ipAddress ?: "-"}). Jika APK diinstal di HP Android fisik lalu Hotspot dinyalakan, pasien yang tersambung ke Hotspot/Wi-Fi HP Anda bisa langsung scan QR ini tanpa internet!"
                                } else {
                                    "Pastikan HP pasien tersambung ke Hotspot HP ini atau Wi-Fi ruangan yang sama, lalu biarkan layar QR ini tetap terbuka saat pasien melakukan scan."
                                },
                                color = if (isVirtualIp) Color(0xFF78350F) else Color(0xFF134E4A),
                                fontSize = 11.sp,
                                lineHeight = 15.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Crisp Vector QR Code Box with 4-module Quiet Zone
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .widthIn(max = 236.dp)
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        .testTag("qr_code_canvas_box")
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        val quietZone = 4
                        val totalModules = qrMatrix.size + quietZone * 2
                        val cellSize = size.minDimension / totalModules.toFloat()
                        val offsetX = (size.width - cellSize * totalModules) / 2f
                        val offsetY = (size.height - cellSize * totalModules) / 2f
                        val darkColor = Color(0xFF0F172A)

                        for (r in 0 until qrMatrix.size) {
                            for (c in 0 until qrMatrix.size) {
                                if (qrMatrix.isDark(r, c)) {
                                    drawRect(
                                        color = darkColor,
                                        topLeft = Offset(
                                            x = offsetX + (c + quietZone) * cellSize,
                                            y = offsetY + (r + quietZone) * cellSize
                                        ),
                                        size = Size(cellSize + 0.4f, cellSize + 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active URL Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedicalCanvasBg,
                    border = BorderStroke(1.dp, MedicalOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = activeQrUrl,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanBlueDark,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("qr_active_url_text")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clear Safari & Chrome Scan Guide
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OceanBlueLight,
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        Text(
                            text = "Cara Scan Pasien (Tanpa Instal Aplikasi):",
                            fontWeight = FontWeight.Bold,
                            color = OceanBlueDark,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• iPhone / iPad: Buka aplikasi Kamera bawaan, arahkan ke QR di atas, lalu ketuk notifikasi kuning untuk masuk ke Safari.",
                            color = Color(0xFF1E293B),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "• HP Android: Buka Kamera atau Google Lens, arahkan ke QR di atas, lalu ketuk tautan untuk masuk ke Google Chrome.",
                            color = Color(0xFF1E293B),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Action: Share / Save Printable PNG Card
                Button(
                    onClick = { QrCodeGenerator.sharePrintableQrPoster(context, activeQrUrl) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToskaSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("share_qr_poster_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bagikan / Simpan Kartu QR (PNG)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Actions: Copy Link & Open in Chrome/Browser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { copyUrlToClipboard() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MedicalOutline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("copy_qr_url_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = OceanBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Salin Link",
                            color = OceanBluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { QrCodeGenerator.openInChromeOrBrowser(context, activeQrUrl) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .heightIn(min = 48.dp)
                            .testTag("open_qr_browser_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Buka di Chrome",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (connectionMode == QrConnectionMode.ONLINE_HOSTING) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Dedicated 3-Step Netlify ZIP -> QR Code Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ToskaLight,
                        border = BorderStroke(1.dp, Color(0xFF99F6E4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Paket Siap Unggah ke Netlify -> Jadi QR Code:",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Netlify Drop membutuhkan format .zip (berisi index.html, _redirects, dan netlify.toml). Simpan paket ZIP di bawah lalu unggah ke Netlify Drop.",
                                color = Color(0xFF134E4A),
                                fontSize = 11.sp,
                                lineHeight = 15.5.sp
                            )
                            Spacer(modifier = Modifier.height(9.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        saveNetlifyZipLauncher.launch(QrCodeGenerator.NETLIFY_ZIP_FILENAME)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ToskaSecondary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1.35f)
                                        .heightIn(min = 48.dp)
                                        .testTag("save_netlify_zip_device_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "1. Simpan ZIP Netlify",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = { QrCodeGenerator.shareNetlifyZipBundle(context) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ToskaSecondary),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF0F766E)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(0.95f)
                                        .heightIn(min = 48.dp)
                                        .testTag("share_netlify_zip_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kirim ZIP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            OutlinedButton(
                                onClick = { QrCodeGenerator.openNetlifyDropInBrowser(context) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, OceanBluePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = OceanBluePrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("open_netlify_drop_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. Buka Netlify Drop (app.netlify.com/drop)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Staff-only Edit Target URL Button (Password Protected)
                    OutlinedButton(
                        onClick = onRequestEditUrl,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = OceanBlueLight,
                            contentColor = OceanBlueDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("edit_qr_url_staff_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ubah Link Netlify QR (Petugas)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Optional raw index.html export
                    OutlinedButton(
                        onClick = { QrCodeGenerator.shareStandaloneWebHtml(context) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MedicalCanvasBg,
                            contentColor = Color(0xFF475569)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("export_web_html_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilePresent,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bagikan File index.html Tunggal (GitHub / Lainnya)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
