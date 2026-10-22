package com.luna.cycle.ui

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.luna.cycle.data.CycleLog
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ExportReport {
    fun export(context: Context, logs: List<CycleLog>) {
        val document = PdfDocument()
        val titlePaint = Paint().apply { textSize = 22f; isFakeBoldText = true; color = android.graphics.Color.rgb(76, 55, 60) }
        val bodyPaint = Paint().apply { textSize = 12f; color = android.graphics.Color.rgb(76, 55, 60) }
        val entries = logs.sortedByDescending { it.date }
        val pages = entries.chunked(30).ifEmpty { listOf(emptyList()) }
        pages.forEachIndexed { index, pageEntries ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(612, 792, index + 1).create())
            val canvas = page.canvas
            canvas.drawText("Luna  ·  Cycle health summary", 48f, 58f, titlePaint)
            canvas.drawText("Generated ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}", 48f, 82f, bodyPaint)
            canvas.drawText("${logs.size} daily check-ins recorded", 48f, 112f, bodyPaint)
            pageEntries.forEachIndexed { row, log ->
                val y = 150f + row * 20f
                val date = LocalDate.parse(log.date).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                val details = listOfNotNull(
                    log.flow.takeIf(String::isNotBlank)?.let { "Flow: $it" },
                    log.mood.takeIf(String::isNotBlank)?.let { "Mood: $it" },
                    log.symptomSet().takeIf { it.isNotEmpty() }?.joinToString(", "),
                    log.activity.takeIf(String::isNotBlank)?.let { "Activity: $it" },
                    log.cervicalMucus.takeIf(String::isNotBlank)?.let { "Mucus: $it" }
                ).joinToString("  ·  ")
                canvas.drawText(date, 48f, y, bodyPaint)
                canvas.drawText(details.ifBlank { "Daily check-in" }.take(78), 160f, y, bodyPaint)
            }
            document.finishPage(page)
        }
        val file = File(context.cacheDir, "luna-cycle-summary.pdf")
        try {
            file.outputStream().use(document::writeTo)
        } catch (error: Exception) {
            document.close()
            throw error
        }
        document.close()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share cycle summary"))
    }
}
