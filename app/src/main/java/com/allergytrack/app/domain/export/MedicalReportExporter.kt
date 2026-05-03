package com.allergytrack.app.domain.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.FoodTrigger
import com.allergytrack.app.ui.util.labelRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MedicalReportExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun generatePdfReport(
        correlations: List<Correlation>,
        foodTriggers: List<FoodTrigger>,
        aiSummary: String
    ): File? = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        var currentY = 50f
        val margin = 50f
        val pageWidth = 595f

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText(context.getString(R.string.export_pdf_header), margin, currentY, paint)
        currentY += 40f

        // Date
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        val dateStr = SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText(context.getString(R.string.export_pdf_date_label, dateStr), margin, currentY, paint)
        currentY += 40f

        // AI Summary Section
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 16f
        canvas.drawText(context.getString(R.string.export_pdf_summary_header), margin, currentY, paint)
        currentY += 25f

        paint.isFakeBoldText = false
        paint.textSize = 12f
        val summaryLines = wrapText(aiSummary, paint, pageWidth - 2 * margin)
        summaryLines.forEach { line ->
            canvas.drawText(line, margin, currentY, paint)
            currentY += 20f
        }
        currentY += 30f

        // Identified Correlations Section
        if (correlations.isNotEmpty()) {
            paint.isFakeBoldText = true
            paint.textSize = 16f
            canvas.drawText(context.getString(R.string.export_pdf_correlations_header), margin, currentY, paint)
            currentY += 30f

            correlations.take(5).forEach { correlation ->
                drawCorrelationItem(canvas, paint, margin, currentY, pageWidth, correlation)
                currentY += 75f
            }
            currentY += 20f
        }

        // Food Triggers Section
        if (foodTriggers.isNotEmpty()) {
            if (currentY > 600f) { // Simple page break check
                pdfDocument.finishPage(page)
                val newPage = pdfDocument.startPage(pageInfo)
                val newCanvas = newPage.canvas
                currentY = 50f
                drawFoodTriggers(newCanvas, paint, margin, currentY, pageWidth, foodTriggers, pdfDocument, newPage)
            } else {
                drawFoodTriggers(canvas, paint, margin, currentY, pageWidth, foodTriggers, pdfDocument, page)
            }
        } else {
            pdfDocument.finishPage(page)
        }

        val fileName = "AllergyReport_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            saveToPublicMediaStore(file)
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawCorrelationItem(
        canvas: Canvas,
        paint: Paint,
        margin: Float,
        y: Float,
        pageWidth: Float,
        correlation: Correlation
    ) {
        // Background box
        val rectPaint = Paint()
        rectPaint.color = Color.rgb(245, 245, 245)
        canvas.drawRoundRect(margin - 5, y - 20, pageWidth - margin + 5, y + 50, 10f, 10f, rectPaint)

        paint.isFakeBoldText = true
        paint.textSize = 14f
        paint.color = Color.BLACK
        canvas.drawText(correlation.trigger, margin + 10, y + 10, paint)

        paint.isFakeBoldText = false
        paint.textSize = 11f
        paint.color = Color.DKGRAY
        val symptomLabel = context.getString(correlation.symptomType.labelRes())
        canvas.drawText(
            context.getString(R.string.export_pdf_confidence_label, (correlation.score * 100).toInt()) +
                    " | " + context.getString(R.string.export_pdf_symptom_label, symptomLabel),
            margin + 10, y + 30, paint
        )

        val details = context.getString(R.string.export_pdf_occurrences_label, correlation.occurrences, correlation.totalEvents)
        canvas.drawText(details, margin + 10, y + 45, paint)
    }

    private fun drawFoodTriggers(
        canvas: Canvas,
        paint: Paint,
        margin: Float,
        y: Float,
        pageWidth: Float,
        foodTriggers: List<FoodTrigger>,
        pdfDocument: PdfDocument,
        currentPage: PdfDocument.Page
    ) {
        var currentY = y
        paint.isFakeBoldText = true
        paint.textSize = 16f
        paint.color = Color.BLACK
        canvas.drawText(context.getString(R.string.export_pdf_food_triggers_header), margin, currentY, paint)
        currentY += 30f

        foodTriggers.take(8).forEach { trigger ->
            paint.isFakeBoldText = true
            paint.textSize = 12f
            canvas.drawText("${trigger.triggerEmoji} ${trigger.triggerName}", margin + 10, currentY, paint)

            paint.isFakeBoldText = false
            paint.textSize = 10f
            paint.color = Color.GRAY
            val reactionInfo = context.getString(
                R.string.export_pdf_reaction_label,
                trigger.reactionCount,
                trigger.totalCount,
                (trigger.correlationScore * 100).toInt()
            )
            canvas.drawText(reactionInfo, margin + 150, currentY, paint)

            if (trigger.dominantSymptoms.isNotEmpty()) {
                currentY += 15f
                paint.textSize = 9f
                canvas.drawText(
                    context.getString(R.string.export_pdf_symptoms_list_label, trigger.dominantSymptoms.joinToString(", ")),
                    margin + 30,
                    currentY,
                    paint
                )
            }

            currentY += 25f
        }

        // Footer (simplified for current page)
        paint.textSize = 9f
        paint.color = Color.LTGRAY
        canvas.drawText(context.getString(R.string.export_pdf_disclaimer), margin, 810f, paint)

        pdfDocument.finishPage(currentPage)
    }

    private fun saveToPublicMediaStore(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/AllergyTrack")
                }
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                uri?.let { targetUri ->
                    resolver.openOutputStream(targetUri)?.use { output ->
                        file.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }
}
