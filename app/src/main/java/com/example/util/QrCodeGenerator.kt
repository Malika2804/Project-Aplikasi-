package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.abs

data class QrMatrix(
    val size: Int,
    val modules: Array<BooleanArray>
) {
    fun isDark(row: Int, col: Int): Boolean = modules[row][col]
}

/**
 * Pure Kotlin ISO/IEC 18004 QR Code Generator (Byte Mode, Error Correction Level M - 15%).
 * Supports Versions 1 through 14 (URLs up to 362 UTF-8 bytes) completely offline.
 */
object QrCodeGenerator {

    const val DEFAULT_WEB_ACCESS_URL =
        "https://resplendent-zuccutto-9a64ae.netlify.app"
    const val NETLIFY_DROP_URL = "https://app.netlify.com/drop"
    const val NETLIFY_ZIP_FILENAME = "siap-operasi-netlify.zip"

    private data class VersionSpec(
        val version: Int,
        val ecCodewordsPerBlock: Int,
        val group1Blocks: Int,
        val group1DataCw: Int,
        val group2Blocks: Int,
        val group2DataCw: Int,
        val alignCoords: IntArray
    ) {
        val totalDataCodewords: Int
            get() = group1Blocks * group1DataCw + group2Blocks * group2DataCw
    }

    private val VERSION_SPECS_M = arrayOf(
        VersionSpec(1, 10, 1, 16, 0, 0, intArrayOf()),
        VersionSpec(2, 16, 1, 28, 0, 0, intArrayOf(6, 18)),
        VersionSpec(3, 26, 1, 44, 0, 0, intArrayOf(6, 22)),
        VersionSpec(4, 18, 2, 32, 0, 0, intArrayOf(6, 26)),
        VersionSpec(5, 24, 2, 43, 0, 0, intArrayOf(6, 30)),
        VersionSpec(6, 16, 4, 27, 0, 0, intArrayOf(6, 34)),
        VersionSpec(7, 18, 4, 31, 0, 0, intArrayOf(6, 22, 38)),
        VersionSpec(8, 22, 2, 38, 2, 39, intArrayOf(6, 24, 42)),
        VersionSpec(9, 22, 3, 36, 2, 37, intArrayOf(6, 26, 46)),
        VersionSpec(10, 26, 4, 43, 1, 44, intArrayOf(6, 28, 50)),
        VersionSpec(11, 30, 1, 50, 4, 51, intArrayOf(6, 30, 54)),
        VersionSpec(12, 22, 6, 36, 2, 37, intArrayOf(6, 32, 58)),
        VersionSpec(13, 22, 8, 37, 1, 38, intArrayOf(6, 34, 62)),
        VersionSpec(14, 24, 4, 40, 5, 41, intArrayOf(6, 26, 46, 66))
    )

    fun normalizeWebUrl(rawUrl: String?, fallback: String = DEFAULT_WEB_ACCESS_URL): String {
        val trimmed = rawUrl?.trim().orEmpty()
        if (trimmed.isBlank()) return fallback

        val isLocalHostOrIp = trimmed.startsWith("localhost", ignoreCase = true) ||
            trimmed.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}(:\\d+)?(/.*)?$"))

        var candidate = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            isLocalHostOrIp -> "http://$trimmed"
            !trimmed.contains('.') && !trimmed.contains(':') && !trimmed.contains('/') ->
                "https://${trimmed.lowercase()}.netlify.app"
            else -> "https://$trimmed"
        }

        if (candidate.endsWith("/index.html", ignoreCase = true)) {
            candidate = candidate.dropLast("/index.html".length)
        }
        if (candidate.matches(Regex("^https?://[^/]+/$", RegexOption.IGNORE_CASE))) {
            candidate = candidate.dropLast(1)
        }
        return candidate
    }

    fun isNetlifyUrl(rawUrl: String?): Boolean {
        val lower = rawUrl?.trim()?.lowercase().orEmpty()
        return lower.contains(".netlify.app") || lower.contains(".netlify.live")
    }

    /**
     * Returns true when the URL is still pointing to the internal AI Studio workspace preview
     * (.run.app) or an unhosted example domain, which external patient phones cannot open directly.
     */
    fun isUnconfiguredDefaultUrl(rawUrl: String?): Boolean {
        val lower = rawUrl?.trim()?.lowercase().orEmpty()
        return lower.isBlank() ||
            lower.contains(".run.app") ||
            lower.contains("siap-operasi-edukasi.web.app")
    }

    private fun truncateUtf8AtCharBoundary(utf8: ByteArray, maxBytes: Int): ByteArray {
        if (utf8.size <= maxBytes) return utf8
        var end = maxBytes
        // Step back over UTF-8 continuation bytes (10xxxxxx)
        while (end > 0 && (utf8[end].toInt() and 0xC0) == 0x80) {
            end--
        }
        return utf8.copyOf(end)
    }

    fun encodeQrMatrix(text: String): QrMatrix {
        val payload = text.ifBlank { DEFAULT_WEB_ACCESS_URL }
        val rawUtf8 = payload.toByteArray(Charsets.UTF_8)

        val matchedSpec = VERSION_SPECS_M.firstOrNull { candidate ->
            val countBits = if (candidate.version <= 9) 8 else 16
            val neededBits = 4 + countBits + rawUtf8.size * 8
            neededBits <= candidate.totalDataCodewords * 8
        }

        val spec = matchedSpec ?: VERSION_SPECS_M.last()
        val utf8 = if (matchedSpec != null) {
            rawUtf8
        } else {
            val maxBytes = spec.totalDataCodewords - 3
            truncateUtf8AtCharBoundary(rawUtf8, maxBytes)
        }

        val dataCw = buildDataCodewords(utf8, spec)
        val finalCodewords = interleaveWithEcc(dataCw, spec)

        val size = 17 + 4 * spec.version
        val baseModules = Array(size) { BooleanArray(size) }
        val isFunction = Array(size) { BooleanArray(size) }

        drawFunctionPatterns(spec, size, baseModules, isFunction)
        placeCodewords(finalCodewords, size, baseModules, isFunction)

        var bestMask = 0
        var lowestPenalty = Int.MAX_VALUE
        var bestGrid = baseModules

        for (mask in 0..7) {
            val candidate = Array(size) { r -> baseModules[r].clone() }
            applyMask(mask, size, candidate, isFunction)
            drawFormatBits(mask, size, candidate, isFunction)
            val penalty = computePenalty(size, candidate)
            if (penalty < lowestPenalty) {
                lowestPenalty = penalty
                bestMask = mask
                bestGrid = candidate
            }
        }

        // Ensure format bits match bestMask
        drawFormatBits(bestMask, size, bestGrid, isFunction)
        return QrMatrix(size = size, modules = bestGrid)
    }

    private fun buildDataCodewords(utf8: ByteArray, spec: VersionSpec): IntArray {
        val totalBitsCapacity = spec.totalDataCodewords * 8
        val bits = ArrayList<Int>(totalBitsCapacity)

        fun appendBits(value: Int, len: Int) {
            for (i in len - 1 downTo 0) {
                bits.add((value ushr i) and 1)
            }
        }

        // Byte mode indicator (0100)
        appendBits(0b0100, 4)
        val countBits = if (spec.version <= 9) 8 else 16
        appendBits(utf8.size, countBits)
        for (b in utf8) {
            appendBits(b.toInt() and 0xFF, 8)
        }

        // Terminator (up to 4 zero bits)
        val terminatorLen = minOf(4, totalBitsCapacity - bits.size)
        appendBits(0, terminatorLen)

        // Pad to byte boundary
        val remBits = (8 - (bits.size % 8)) % 8
        appendBits(0, remBits)

        // Pad alternating 0xEC and 0x11
        var padToggle = true
        while (bits.size < totalBitsCapacity) {
            appendBits(if (padToggle) 0xEC else 0x11, 8)
            padToggle = !padToggle
        }

        val result = IntArray(spec.totalDataCodewords)
        for (i in result.indices) {
            var byteVal = 0
            for (b in 0..7) {
                byteVal = (byteVal shl 1) or bits[i * 8 + b]
            }
            result[i] = byteVal
        }
        return result
    }

    private fun interleaveWithEcc(dataCw: IntArray, spec: VersionSpec): IntArray {
        val totalBlocks = spec.group1Blocks + spec.group2Blocks
        val dataBlocks = ArrayList<IntArray>(totalBlocks)
        val eccBlocks = ArrayList<IntArray>(totalBlocks)

        var offset = 0
        for (i in 0 until spec.group1Blocks) {
            val block = dataCw.copyOfRange(offset, offset + spec.group1DataCw)
            offset += spec.group1DataCw
            dataBlocks.add(block)
            eccBlocks.add(rsComputeRemainder(block, spec.ecCodewordsPerBlock))
        }
        for (i in 0 until spec.group2Blocks) {
            val block = dataCw.copyOfRange(offset, offset + spec.group2DataCw)
            offset += spec.group2DataCw
            dataBlocks.add(block)
            eccBlocks.add(rsComputeRemainder(block, spec.ecCodewordsPerBlock))
        }

        val totalCw = spec.totalDataCodewords + totalBlocks * spec.ecCodewordsPerBlock
        val out = IntArray(totalCw)
        var pos = 0

        val maxDataLen = maxOf(spec.group1DataCw, spec.group2DataCw)
        for (col in 0 until maxDataLen) {
            for (b in 0 until totalBlocks) {
                if (col < dataBlocks[b].size) {
                    out[pos++] = dataBlocks[b][col]
                }
            }
        }
        for (col in 0 until spec.ecCodewordsPerBlock) {
            for (b in 0 until totalBlocks) {
                out[pos++] = eccBlocks[b][col]
            }
        }
        return out
    }

    private fun gfMultiply(x: Int, y: Int): Int {
        var z = 0
        for (i in 7 downTo 0) {
            z = (z shl 1) xor ((z ushr 7) * 0x11D)
            z = z xor (((y ushr i) and 1) * x)
        }
        return z
    }

    private fun rsComputeDivisor(degree: Int): IntArray {
        val result = IntArray(degree)
        result[degree - 1] = 1
        var root = 1
        repeat(degree) {
            for (j in 0 until degree) {
                result[j] = gfMultiply(result[j], root)
                if (j + 1 < degree) {
                    result[j] = result[j] xor result[j + 1]
                }
            }
            root = gfMultiply(root, 0x02)
        }
        return result
    }

    private fun rsComputeRemainder(data: IntArray, degree: Int): IntArray {
        val divisor = rsComputeDivisor(degree)
        val remainder = IntArray(degree)
        for (b in data) {
            val factor = b xor remainder[0]
            System.arraycopy(remainder, 1, remainder, 0, degree - 1)
            remainder[degree - 1] = 0
            for (j in 0 until degree) {
                remainder[j] = remainder[j] xor gfMultiply(divisor[j], factor)
            }
        }
        return remainder
    }

    private fun drawFunctionPatterns(
        spec: VersionSpec,
        size: Int,
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>
    ) {
        fun setFunc(r: Int, c: Int, dark: Boolean) {
            modules[r][c] = dark
            isFunction[r][c] = true
        }

        fun drawFinder(r0: Int, c0: Int) {
            for (dy in -1..7) {
                for (dx in -1..7) {
                    val r = r0 + dy
                    val c = c0 + dx
                    if (r in 0 until size && c in 0 until size) {
                        val dark = (dx in 0..6 && (dy == 0 || dy == 6)) ||
                            (dy in 0..6 && (dx == 0 || dx == 6)) ||
                            (dx in 2..4 && dy in 2..4)
                        setFunc(r, c, dark)
                    }
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, size - 7)
        drawFinder(size - 7, 0)

        for (i in 8 until size - 8) {
            setFunc(6, i, i % 2 == 0)
            setFunc(i, 6, i % 2 == 0)
        }

        val aligns = spec.alignCoords
        for (rCenter in aligns) {
            for (cCenter in aligns) {
                val isCorner = (rCenter == 6 && cCenter == 6) ||
                    (rCenter == 6 && cCenter == aligns.last()) ||
                    (rCenter == aligns.last() && cCenter == 6)
                if (isCorner) continue
                for (dy in -2..2) {
                    for (dx in -2..2) {
                        val dist = maxOf(abs(dx), abs(dy))
                        setFunc(rCenter + dy, cCenter + dx, dist != 1)
                    }
                }
            }
        }

        // Reserve format bits & dark module
        drawFormatBits(0, size, modules, isFunction)

        // Draw version info for v >= 7
        if (spec.version >= 7) {
            var rem = spec.version shl 12
            for (i in 5 downTo 0) {
                if ((rem and (1 shl (i + 12))) != 0) {
                    rem = rem xor (0x1F25 shl i)
                }
            }
            val vBits = (spec.version shl 12) or rem
            for (i in 0 until 18) {
                val bit = ((vBits ushr i) and 1) != 0
                val a = size - 11 + (i % 3)
                val b = i / 3
                setFunc(a, b, bit)
                setFunc(b, a, bit)
            }
        }
    }

    private fun drawFormatBits(
        mask: Int,
        size: Int,
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>
    ) {
        // ECC Level M indicator bits = 00
        val data = mask and 0x7
        var rem = data shl 10
        for (i in 4 downTo 0) {
            if ((rem and (1 shl (i + 10))) != 0) {
                rem = rem xor (0x537 shl i)
            }
        }
        val bits = ((data shl 10) or rem) xor 0x5412

        fun setFunc(r: Int, c: Int, dark: Boolean) {
            modules[r][c] = dark
            isFunction[r][c] = true
        }

        for (i in 0..5) setFunc(i, 8, ((bits ushr i) and 1) != 0)
        setFunc(7, 8, ((bits ushr 6) and 1) != 0)
        setFunc(8, 8, ((bits ushr 7) and 1) != 0)
        setFunc(8, 7, ((bits ushr 8) and 1) != 0)
        for (i in 9..14) setFunc(8, 14 - i, ((bits ushr i) and 1) != 0)

        for (i in 0..7) setFunc(8, size - 1 - i, ((bits ushr i) and 1) != 0)
        for (i in 8..14) setFunc(size - 15 + i, 8, ((bits ushr i) and 1) != 0)
        setFunc(size - 8, 8, true) // Dark module
    }

    private fun placeCodewords(
        codewords: IntArray,
        size: Int,
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>
    ) {
        var bitIdx = 0
        val totalBits = codewords.size * 8
        var right = size - 1
        while (right >= 1) {
            if (right == 6) right = 5
            val upward = ((right + 1) and 2) == 0
            for (vert in 0 until size) {
                val r = if (upward) size - 1 - vert else vert
                for (j in 0..1) {
                    val c = right - j
                    if (!isFunction[r][c]) {
                        val dark = if (bitIdx < totalBits) {
                            ((codewords[bitIdx ushr 3] ushr (7 - (bitIdx and 7))) and 1) != 0
                        } else {
                            false
                        }
                        modules[r][c] = dark
                        bitIdx++
                    }
                }
            }
            right -= 2
        }
    }

    private fun applyMask(
        mask: Int,
        size: Int,
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>
    ) {
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (isFunction[r][c]) continue
                val invert = when (mask) {
                    0 -> (r + c) % 2 == 0
                    1 -> r % 2 == 0
                    2 -> c % 3 == 0
                    3 -> (r + c) % 3 == 0
                    4 -> ((r / 2) + (c / 3)) % 2 == 0
                    5 -> ((r * c) % 2) + ((r * c) % 3) == 0
                    6 -> (((r * c) % 2) + ((r * c) % 3)) % 2 == 0
                    7 -> (((r + c) % 2) + ((r * c) % 3)) % 2 == 0
                    else -> false
                }
                if (invert) {
                    modules[r][c] = !modules[r][c]
                }
            }
        }
    }

    private fun computePenalty(size: Int, modules: Array<BooleanArray>): Int {
        var penalty = 0

        // N1: Adjacent modules in row/col of same color (5+ run)
        for (r in 0 until size) {
            var runColor = modules[r][0]
            var runLen = 1
            for (c in 1 until size) {
                if (modules[r][c] == runColor) {
                    runLen++
                    if (runLen == 5) penalty += 3
                    else if (runLen > 5) penalty += 1
                } else {
                    runColor = modules[r][c]
                    runLen = 1
                }
            }
        }
        for (c in 0 until size) {
            var runColor = modules[0][c]
            var runLen = 1
            for (r in 1 until size) {
                if (modules[r][c] == runColor) {
                    runLen++
                    if (runLen == 5) penalty += 3
                    else if (runLen > 5) penalty += 1
                } else {
                    runColor = modules[r][c]
                    runLen = 1
                }
            }
        }

        // N2: 2x2 blocks of same color
        for (r in 0 until size - 1) {
            for (c in 0 until size - 1) {
                val v = modules[r][c]
                if (v == modules[r][c + 1] && v == modules[r + 1][c] && v == modules[r + 1][c + 1]) {
                    penalty += 3
                }
            }
        }

        // N4: Balance of dark modules
        var darkCount = 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (modules[r][c]) darkCount++
            }
        }
        val total = size * size
        val k = (abs(darkCount * 20 - total * 10) + total - 1) / total - 1
        if (k > 0) penalty += k * 10

        return penalty
    }

    fun createQrBitmap(
        url: String,
        targetSizePx: Int = 720,
        quietZoneModules: Int = 4
    ): Bitmap {
        val matrix = encodeQrMatrix(url)
        val totalModules = matrix.size + quietZoneModules * 2
        val modulePx = maxOf(4, targetSizePx / totalModules)
        val actualPx = totalModules * modulePx

        val bitmap = Bitmap.createBitmap(actualPx, actualPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val darkPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        for (r in 0 until matrix.size) {
            for (c in 0 until matrix.size) {
                if (matrix.isDark(r, c)) {
                    val left = (c + quietZoneModules) * modulePx.toFloat()
                    val top = (r + quietZoneModules) * modulePx.toFloat()
                    canvas.drawRect(left, top, left + modulePx, top + modulePx, darkPaint)
                }
            }
        }
        return bitmap
    }

    /**
     * Creates a clean, printable 1080x1420 px patient scan card PNG with QR code & browser instructions.
     */
    fun createPrintablePosterBitmap(url: String): Bitmap {
        val width = 1080
        val height = 1420
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Outer soft medical background
        canvas.drawColor(Color.parseColor("#F1F5F9"))

        // Inner white card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val cardRect = RectF(48f, 48f, width - 48f, height - 48f)
        canvas.drawRoundRect(cardRect, 44f, 44f, cardPaint)
        canvas.drawRoundRect(cardRect, 44f, 44f, borderPaint)

        // Top medical header banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0284C7")
            style = Paint.Style.FILL
        }
        val headerRect = RectF(88f, 88f, width - 88f, 300f)
        canvas.drawRoundRect(headerRect, 28f, 28f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("SIAP OPERASI", width / 2f, 178f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0F2FE")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Edukasi Anestesi Pra-Bedah & Kuesioner APAIS", width / 2f, 232f, subPaint)
        canvas.drawText("Scan QR untuk Buka di Google Chrome atau Safari", width / 2f, 272f, subPaint)

        // QR Code Box
        val qrBitmap = createQrBitmap(url, targetSizePx = 680, quietZoneModules = 4)
        val qrLeft = (width - qrBitmap.width) / 2f
        val qrTop = 345f
        val qrFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(
            RectF(qrLeft - 16f, qrTop - 16f, qrLeft + qrBitmap.width + 16f, qrTop + qrBitmap.height + 16f),
            24f,
            24f,
            qrFramePaint
        )
        canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)

        // Instructions below QR
        val guideTop = qrTop + qrBitmap.height + 64f
        val guideHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Arahkan Kamera HP ke Kode QR di Atas",
            width / 2f,
            guideTop,
            guideHeaderPaint
        )

        val guideBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "iPhone: Otomatis diarahkan ke Safari  |  Android: Otomatis ke Google Chrome",
            width / 2f,
            guideTop + 48f,
            guideBodyPaint
        )

        // URL Box at bottom
        val urlTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0369A1")
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val maxTextWidth = width - 200
        val staticLayout = StaticLayout.Builder
            .obtain(url, 0, url.length, urlTextPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(2)
            .build()

        canvas.save()
        canvas.translate(100f, guideTop + 90f)
        staticLayout.draw(canvas)
        canvas.restore()

        return bitmap
    }

    fun sharePrintableQrPoster(context: Context, url: String) {
        try {
            val poster = createPrintablePosterBitmap(url)
            val dir = File(context.cacheDir, "qr_cards").apply { mkdirs() }
            val outFile = File(dir, "Kartu_QR_Siap_Operasi.png")
            FileOutputStream(outFile).use { fos ->
                poster.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Scan QR Code atau klik tautan berikut untuk membuka aplikasi Siap Operasi di Google Chrome / Safari:\n$url"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Bagikan atau Simpan Kartu QR").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal membagikan gambar QR: ${e.localizedMessage ?: "Error"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun buildNetlifyRedirectsContent(): String = "/* /index.html 200\n"

    fun buildNetlifyTomlContent(): String =
        """
        [build]
          publish = "."

        [[redirects]]
          from = "/*"
          to = "/index.html"
          status = 200

        [[headers]]
          for = "/*"
          [headers.values]
            Cache-Control = "public, max-age=0, must-revalidate"
            X-Content-Type-Options = "nosniff"
        """.trimIndent() + "\n"

    /**
     * Packages `index.html`, `_redirects`, and `netlify.toml` at the root of a standard ZIP archive
     * so it can be uploaded directly to Netlify Drop (`app.netlify.com/drop`), which requires a ZIP
     * or folder rather than a bare `.html` file.
     */
    fun writeNetlifyBundleZip(context: Context, outputStream: OutputStream) {
        val htmlBytes = context.assets.open("index.html").use { it.readBytes() }
        val redirectsBytes = buildNetlifyRedirectsContent().toByteArray(Charsets.UTF_8)
        val tomlBytes = buildNetlifyTomlContent().toByteArray(Charsets.UTF_8)

        ZipOutputStream(outputStream).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("index.html"))
            zipOut.write(htmlBytes)
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("_redirects"))
            zipOut.write(redirectsBytes)
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("netlify.toml"))
            zipOut.write(tomlBytes)
            zipOut.closeEntry()

            zipOut.finish()
        }
    }

    fun exportNetlifyZipBundle(context: Context): File {
        val dir = File(context.cacheDir, "web_bundle").apply { mkdirs() }
        val outFile = File(dir, NETLIFY_ZIP_FILENAME)
        FileOutputStream(outFile).use { fos ->
            writeNetlifyBundleZip(context, fos)
        }
        return outFile
    }

    fun saveNetlifyZipToUri(context: Context, targetUri: Uri): Boolean {
        return try {
            val stream = context.contentResolver.openOutputStream(targetUri) ?: return false
            stream.use { out ->
                writeNetlifyBundleZip(context, out)
            }
            Toast.makeText(
                context,
                "Paket $NETLIFY_ZIP_FILENAME berhasil disimpan! Unggah file ZIP ini ke app.netlify.com/drop",
                Toast.LENGTH_LONG
            ).show()
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal menyimpan paket ZIP Netlify: ${e.localizedMessage ?: "Error"}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    fun shareNetlifyZipBundle(context: Context) {
        try {
            val zipFile = exportNetlifyZipBundle(context)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Paket Siap Upload Netlify ($NETLIFY_ZIP_FILENAME)"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Langkah memproses HTML Siap Operasi menjadi QR Code di Netlify:\n" +
                        "1. Simpan file $NETLIFY_ZIP_FILENAME ini.\n" +
                        "2. Buka $NETLIFY_DROP_URL lalu unggah file ZIP tersebut.\n" +
                        "3. Halaman Netlify yang terbit otomatis memiliki QR Code siap scan, atau tempel link .netlify.app di menu Ubah Link QR (Password: 280428)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Bagikan atau Simpan Paket ZIP Netlify").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal membagikan paket ZIP Netlify: ${e.localizedMessage ?: "Error"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openNetlifyDropInBrowser(context: Context) {
        openInChromeOrBrowser(context, NETLIFY_DROP_URL)
    }

    fun exportStandaloneWebHtml(context: Context): File {
        val dir = File(context.cacheDir, "web_bundle").apply { mkdirs() }
        val outFile = File(dir, "index.html")
        context.assets.open("index.html").use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        return outFile
    }

    fun shareStandaloneWebHtml(context: Context) {
        try {
            val htmlFile = exportStandaloneWebHtml(context)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                htmlFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "File Web Siap Operasi (index.html)"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Untuk unggah ke Netlify Drop ($NETLIFY_DROP_URL), disarankan menggunakan Paket ZIP ($NETLIFY_ZIP_FILENAME) agar diterima langsung oleh Netlify."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Simpan atau Kirim File index.html").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal mengekspor file index.html: ${e.localizedMessage ?: "Error"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openInChromeOrBrowser(context: Context, rawUrl: String) {
        val normalized = normalizeWebUrl(rawUrl)
        val uri = Uri.parse(normalized)
        // Prefer Google Chrome explicitly on Android if installed, otherwise default browser
        val chromeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.android.chrome")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chromeIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    "Tidak dapat membuka browser. Salin tautan: $normalized",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
