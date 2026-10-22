package com.sevadiary.app.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.sevadiary.app.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AshramVisitPdfItem(
    val ashramName: String,
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val purpose: String,
    val sevaType: String,
    val notes: String
)

data class ChantingPdfItem(
    val date: String,
    val count: Int,
    val target: Int
)

class PdfReportGenerator(private val context: Context) {

    private val titlePaint = Paint().apply {
        color = Color.rgb(30, 77, 43) // ForestGreen
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val subtitlePaint = Paint().apply {
        color = Color.rgb(92, 100, 94)
        textSize = 11f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    private val sectionPaint = Paint().apply {
        color = Color.rgb(217, 130, 43) // SaffronGold
        textSize = 15f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val headerPaint = Paint().apply {
        color = Color.rgb(30, 77, 43)
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val bodyPaint = Paint().apply {
        color = Color.rgb(31, 36, 33)
        textSize = 10f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.rgb(227, 222, 212)
        strokeWidth = 1f
    }

    private val pageFooterPaint = Paint().apply {
        color = Color.rgb(150, 150, 150)
        textSize = 9f
        isAntiAlias = true
    }

    fun generateAshramVisitsPdf(
        records: List<AshramVisitPdfItem>,
        devotionalName: String
    ): File {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width in PostScript points (72 dpi)
        val pageHeight = 842 // Standard A4 height
        val groupedRecords = records.groupBy { it.ashramName }

        var pageNumber = 1
        val dateGenerated = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        if (groupedRecords.isEmpty()) {
            // Render Empty Report
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawReportHeader(canvas, "Satlok Ashram Visits Report", devotionalName, dateGenerated)
            canvas.drawText("No ashram visit records found for the selected period.", 40f, 160f, bodyPaint)
            drawPageFooter(canvas, pageWidth, pageHeight, pageNumber)

            document.finishPage(page)
        } else {
            for ((ashram, ashramRecords) in groupedRecords) {
                var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                var page = document.startPage(pageInfo)
                var canvas = page.canvas

                drawReportHeader(canvas, "Satlok Ashram Visits Report", devotionalName, dateGenerated)

                var yPos = 140f
                canvas.drawText(ashram, 40f, yPos, sectionPaint)
                yPos += 20f

                // Table Header
                drawTableHeader(canvas, yPos, listOf("Start Date", "End Date", "Days", "Purpose", "Seva Type"))
                yPos += 18f

                for (item in ashramRecords) {
                    if (yPos > pageHeight - 100) {
                        drawPageFooter(canvas, pageWidth, pageHeight, pageNumber)
                        document.finishPage(page)
                        pageNumber++

                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        drawReportHeader(canvas, "Satlok Ashram Visits Report (Contd.)", devotionalName, dateGenerated)
                        yPos = 130f
                        drawTableHeader(canvas, yPos, listOf("Start Date", "End Date", "Days", "Purpose", "Seva Type"))
                        yPos += 18f
                    }

                    canvas.drawText(item.startDate, 40f, yPos, bodyPaint)
                    canvas.drawText(item.endDate, 120f, yPos, bodyPaint)
                    canvas.drawText("${item.durationDays}", 200f, yPos, bodyPaint)
                    canvas.drawText(item.purpose, 240f, yPos, bodyPaint)
                    canvas.drawText(item.sevaType.ifBlank { "-" }, 370f, yPos, bodyPaint)
                    yPos += 16f

                    if (item.notes.isNotBlank()) {
                        canvas.drawText("Notes: ${item.notes}", 40f, yPos, subtitlePaint)
                        yPos += 16f
                    }
                    canvas.drawLine(40f, yPos - 8f, 555f, yPos - 8f, linePaint)
                    yPos += 6f
                }

                drawPageFooter(canvas, pageWidth, pageHeight, pageNumber)
                document.finishPage(page)
                pageNumber++
            }
        }

        val outputFile = File(context.cacheDir, "SevaDiary_Ashram_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    fun generateNaamJaapPdf(
        entries: List<ChantingPdfItem>,
        devotionalName: String,
        allTimeTotal: Int
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        val dateGenerated = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        drawReportHeader(canvas, "Naam Jaap Progress Report", devotionalName, dateGenerated)

        var yPos = 140f
        canvas.drawText("Summary Overview", 40f, yPos, sectionPaint)
        yPos += 20f
        canvas.drawText("All-Time Chanting Total: $allTimeTotal Counts", 40f, yPos, bodyPaint)
        yPos += 16f
        canvas.drawText("Total Days Logged: ${entries.size}", 40f, yPos, bodyPaint)
        yPos += 30f

        canvas.drawText("Daily Log History", 40f, yPos, sectionPaint)
        yPos += 20f
        drawTableHeader(canvas, yPos, listOf("Date", "Daily Count", "Target Count", "Status"))
        yPos += 18f

        for (entry in entries) {
            if (yPos > pageHeight - 80) {
                drawPageFooter(canvas, pageWidth, pageHeight, pageNumber)
                document.finishPage(page)
                pageNumber++

                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                drawReportHeader(canvas, "Naam Jaap Progress Report (Contd.)", devotionalName, dateGenerated)
                yPos = 130f
                drawTableHeader(canvas, yPos, listOf("Date", "Daily Count", "Target Count", "Status"))
                yPos += 18f
            }

            val status = if (entry.count >= entry.target && entry.target > 0) "Completed" else "In Progress"
            canvas.drawText(entry.date, 40f, yPos, bodyPaint)
            canvas.drawText("${entry.count}", 150f, yPos, bodyPaint)
            canvas.drawText("${entry.target}", 280f, yPos, bodyPaint)
            canvas.drawText(status, 420f, yPos, bodyPaint)

            yPos += 16f
            canvas.drawLine(40f, yPos - 6f, 555f, yPos - 6f, linePaint)
            yPos += 6f
        }

        drawPageFooter(canvas, pageWidth, pageHeight, pageNumber)
        document.finishPage(page)

        val outputFile = File(context.cacheDir, "SevaDiary_Jaap_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    private fun drawReportHeader(
        canvas: Canvas,
        title: String,
        devotionalName: String,
        dateGenerated: String
    ) {
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
            if (logoBitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(logoBitmap, 60, 60, true)
                canvas.drawBitmap(scaledBitmap, 495f, 32f, null)
            }
        } catch (_: Exception) {}

        canvas.drawText("Seva Diary — सेवा डायरी", 40f, 45f, titlePaint)
        canvas.drawText(title, 40f, 70f, sectionPaint)
        canvas.drawText("Devotee: ${devotionalName.ifBlank { "Devotee" }}", 40f, 88f, subtitlePaint)
        canvas.drawText("Generated: $dateGenerated", 40f, 102f, subtitlePaint)
        canvas.drawLine(40f, 114f, 555f, 114f, linePaint)
    }

    private fun drawTableHeader(canvas: Canvas, yPos: Float, headers: List<String>) {
        val xPositions = listOf(40f, 120f, 200f, 240f, 370f, 480f)
        for (i in headers.indices) {
            val x = if (i < xPositions.size) xPositions[i] else 40f + (i * 90f)
            canvas.drawText(headers[i], x, yPos, headerPaint)
        }
        canvas.drawLine(40f, yPos + 6f, 555f, yPos + 6f, linePaint)
    }

    private fun drawPageFooter(canvas: Canvas, pageWidth: Int, pageHeight: Int, pageNumber: Int) {
        val footerText = "Page $pageNumber — Seva Diary Personal Devotional Record"
        canvas.drawLine(40f, pageHeight - 40f, 555f, pageHeight - 40f, linePaint)
        canvas.drawText(footerText, 40f, pageHeight - 25f, pageFooterPaint)
    }
}
