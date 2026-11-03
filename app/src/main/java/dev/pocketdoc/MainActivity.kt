package dev.pocketdoc

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hslf.usermodel.HSLFGroupShape
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextShape
import org.apache.poi.ss.usermodel.DataFormatter
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketDocScreen()
        }
    }
}

private data class SelectedDocument(
    val uri: Uri,
    val name: String,
    val mimeType: String?,
)

private sealed interface DocumentContent {
    data class Pdf(val session: PdfDocumentSession) : DocumentContent
    data class Text(val text: String) : DocumentContent
    data class Image(val bitmap: Bitmap) : DocumentContent
    data class Spreadsheet(val sheets: List<SpreadsheetSheet>) : DocumentContent
    data class Presentation(val slides: List<PresentationSlide>) : DocumentContent
    data class Error(val message: String) : DocumentContent
}

private data class SpreadsheetSheet(
    val name: String,
    val rows: List<List<String>>,
)

private data class PresentationSlide(
    val title: String?,
    val paragraphs: List<String>,
)

@Composable
private fun PocketDocScreen() {
    val context = LocalContext.current
    var selectedDocument by remember { mutableStateOf<SelectedDocument?>(null) }
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // The picker may grant temporary access only.
            }
            selectedDocument = SelectedDocument(
                uri = uri,
                name = displayName(context, uri),
                mimeType = context.contentResolver.getType(uri),
            )
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF285A49),
            background = Color(0xFFF5F7F2),
            surface = Color.White,
        ),
    ) {
        val document = selectedDocument
        if (document == null) {
            HomeScreen(
                onChooseFile = { documentPicker.launch(arrayOf("*/*")) },
            )
        } else {
            DocumentScreen(
                document = document,
                onBack = { selectedDocument = null },
                onChooseAnother = { documentPicker.launch(arrayOf("*/*")) },
            )
        }
    }
}

@Composable
private fun HomeScreen(onChooseFile: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("PocketDoc", style = MaterialTheme.typography.titleLarge)
            Text("Open a document", style = MaterialTheme.typography.headlineMedium)
            Text(
                "View PDF, PowerPoint (.ppt), Excel (.xls, .csv), text, and image files stored on your device.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onChooseFile,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Choose file")
            }
        }
    }
}

@Composable
private fun DocumentScreen(
    document: SelectedDocument,
    onBack: () -> Unit,
    onChooseAnother: () -> Unit,
) {
    val context = LocalContext.current
    var content by remember(document.uri) { mutableStateOf<DocumentContent?>(null) }

    LaunchedEffect(document.uri) {
        content = withContext(Dispatchers.IO) {
            try {
                openDocument(context, document)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                DocumentContent.Error(
                    exception.message ?: "This file could not be opened.",
                )
            }
        }
    }

    val pdfSession = (content as? DocumentContent.Pdf)?.session
    DisposableEffect(pdfSession) {
        onDispose { pdfSession?.close() }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onBack) { Text("Back") }
                Text(
                    text = document.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onChooseAnother) { Text("Open another") }
            }

            when (val opened = content) {
                null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is DocumentContent.Pdf -> PdfViewer(opened.session)
                is DocumentContent.Text -> TextViewer(opened.text)
                is DocumentContent.Image -> ImageViewer(opened.bitmap)
                is DocumentContent.Spreadsheet -> SpreadsheetViewer(opened.sheets)
                is DocumentContent.Presentation -> PresentationViewer(opened.slides, document.uri)
                is DocumentContent.Error -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        opened.message,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun PresentationViewer(slides: List<PresentationSlide>, presentationUri: Uri) {
    var selectedSlideIndex by remember(presentationUri) { mutableIntStateOf(0) }
    val slide = slides[selectedSlideIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Slide ${selectedSlideIndex + 1} of ${slides.size}",
            modifier = Modifier.padding(vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (slide.title == null && slide.paragraphs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("This slide contains no readable text.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    slide.title?.let { title ->
                        item {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                    items(slide.paragraphs) { paragraph ->
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                enabled = selectedSlideIndex > 0,
                onClick = { selectedSlideIndex-- },
            ) {
                Text("Previous")
            }
            TextButton(
                enabled = selectedSlideIndex < slides.lastIndex,
                onClick = { selectedSlideIndex++ },
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun PdfViewer(session: PdfDocumentSession) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val pageWidth = constraints.maxWidth
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(session.pageCount) { pageIndex ->
                val page by produceState<PdfPageResult>(
                    initialValue = PdfPageResult.Loading,
                    session,
                    pageIndex,
                    pageWidth,
                ) {
                    value = withContext(Dispatchers.IO) {
                        try {
                            PdfPageResult.Loaded(session.renderPage(pageIndex, pageWidth))
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            PdfPageResult.Error(
                                exception.message ?: "This page could not be rendered.",
                            )
                        }
                    }
                }

                when (val result = page) {
                    PdfPageResult.Loading -> Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.75f),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is PdfPageResult.Loaded -> Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Image(
                                bitmap = result.page.bitmap.asImageBitmap(),
                                contentDescription = "Page ${pageIndex + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(result.page.aspectRatio),
                                contentScale = ContentScale.Fit,
                            )
                            Text(
                                text = "Page ${pageIndex + 1} of ${session.pageCount}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    is PdfPageResult.Error -> Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Page ${pageIndex + 1}: ${result.message}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextViewer(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ImageViewer(bitmap: Bitmap) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected image",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
    }
}

private sealed interface PdfPageResult {
    data object Loading : PdfPageResult
    data class Loaded(val page: RenderedPdfPage) : PdfPageResult
    data class Error(val message: String) : PdfPageResult
}

private data class RenderedPdfPage(
    val bitmap: Bitmap,
    val aspectRatio: Float,
)

private class PdfDocumentSession(private val descriptor: ParcelFileDescriptor) : AutoCloseable {
    private val renderer = PdfRenderer(descriptor)
    val pageCount: Int
        get() = renderer.pageCount

    @Synchronized
    fun renderPage(index: Int, requestedWidth: Int): RenderedPdfPage {
        val page = renderer.openPage(index)
        try {
            val width = requestedWidth.coerceAtLeast(1)
            val height = (width * page.height.toFloat() / page.width)
                .toInt()
                .coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return RenderedPdfPage(bitmap, page.width.toFloat() / page.height)
        } finally {
            page.close()
        }
    }

    @Synchronized
    override fun close() {
        renderer.close()
        descriptor.close()
    }
}

@Composable
private fun SpreadsheetViewer(sheets: List<SpreadsheetSheet>) {
    var selectedSheetIndex by remember(sheets) { mutableIntStateOf(0) }
    val sheet = sheets[selectedSheetIndex]
    val horizontalScrollState = rememberScrollState()
    val columnCount = sheet.rows.maxOfOrNull { it.size } ?: 0

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sheets.forEachIndexed { index, item ->
                TextButton(onClick = { selectedSheetIndex = index }) {
                    Text(
                        text = if (index == selectedSheetIndex) "${item.name}  •" else item.name,
                        color = if (index == selectedSheetIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }

        Text(
            text = "${sheet.rows.size} rows · $columnCount columns",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )

        if (columnCount == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("This spreadsheet is empty.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(sheet.rows.size) { rowIndex ->
                    val row = sheet.rows[rowIndex]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = (rowIndex + 1).toString(),
                            modifier = Modifier.width(36.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(horizontalScrollState),
                        ) {
                            repeat(columnCount) { columnIndex ->
                                Text(
                                    text = row.getOrElse(columnIndex) { "" },
                                    modifier = Modifier
                                        .width(SPREADSHEET_COLUMN_WIDTH)
                                        .heightIn(min = 44.dp)
                                        .border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                        )
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openDocument(context: Context, document: SelectedDocument): DocumentContent {
    val type = document.mimeType.orEmpty().lowercase()
    val extension = document.name.substringAfterLast('.', "").lowercase()

    return when {
        type == "application/pdf" || extension == "pdf" -> {
            val descriptor = context.contentResolver.openFileDescriptor(document.uri, "r")
                ?: throw IOException("The PDF file could not be opened.")
            try {
                DocumentContent.Pdf(PdfDocumentSession(descriptor))
            } catch (exception: Exception) {
                descriptor.close()
                throw exception
            }
        }

        type.startsWith("image/") ||
            extension in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp") -> {
            val bitmap = decodeImage(context, document.uri)
            DocumentContent.Image(bitmap)
        }

        extension == "xls" ||
            (type == "application/vnd.ms-excel" && extension != "csv") -> {
            val bytes = readDocumentBytes(
                context,
                document.uri,
                MAX_XLS_BYTES,
                "Excel files larger than 20 MB are not supported.",
            )
            DocumentContent.Spreadsheet(readXls(bytes))
        }

        extension == "ppt" || type == "application/vnd.ms-powerpoint" -> {
            val bytes = readDocumentBytes(
                context,
                document.uri,
                MAX_PPT_BYTES,
                "PowerPoint files larger than 20 MB are not supported.",
            )
            DocumentContent.Presentation(readPpt(bytes))
        }

        extension == "csv" || type == "text/csv" -> {
            val bytes = readDocumentBytes(
                context,
                document.uri,
                MAX_SPREADSHEET_BYTES,
                "Spreadsheet files larger than 5 MB are not supported.",
            )
            DocumentContent.Spreadsheet(readCsv(bytes))
        }

        type.startsWith("text/") ||
            extension in setOf("txt", "md", "tsv", "json", "xml", "log") -> {
            val bytes = readDocumentBytes(
                context,
                document.uri,
                MAX_TEXT_BYTES,
                "Text files larger than 5 MB are not supported.",
            )
            DocumentContent.Text(String(bytes, Charsets.UTF_8))
        }

        else -> DocumentContent.Error(
            "This file format is not supported yet. Open a PDF, PPT, XLS, CSV, text file, or image.",
        )
    }
}

private const val MAX_TEXT_BYTES = 5 * 1024 * 1024
private const val MAX_SPREADSHEET_BYTES = 5 * 1024 * 1024
private const val MAX_XLS_BYTES = 20 * 1024 * 1024
private const val MAX_PPT_BYTES = 20 * 1024 * 1024
private const val MAX_SPREADSHEET_ROWS = 10_000
private const val MAX_SPREADSHEET_CELLS = 100_000
private val SPREADSHEET_COLUMN_WIDTH = 144.dp
private const val MAX_PRESENTATION_SLIDES = 500
private const val MAX_PRESENTATION_SHAPES = 20_000
private const val MAX_PRESENTATION_TEXT_CHARS = 2 * 1024 * 1024

private fun readDocumentBytes(
    context: Context,
    uri: Uri,
    maxBytes: Int,
    tooLargeMessage: String,
): ByteArray {
    return context.contentResolver.openInputStream(uri)?.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > maxBytes) throw IOException(tooLargeMessage)
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    } ?: throw IOException("This file could not be opened.")
}

private fun readCsv(bytes: ByteArray): List<SpreadsheetSheet> {
    val rows = mutableListOf<List<String>>()
    var cellCount = 0
    CSVParser.parse(
        InputStreamReader(ByteArrayInputStream(bytes), Charsets.UTF_8),
        CSVFormat.DEFAULT,
    ).use { parser ->
        for (record in parser) {
            if (rows.size >= MAX_SPREADSHEET_ROWS || record.size() > MAX_SPREADSHEET_CELLS) {
                throw IOException("This CSV has too many rows or columns to display.")
            }
            cellCount += record.size()
            if (cellCount > MAX_SPREADSHEET_CELLS) {
                throw IOException("This CSV has too many cells to display.")
            }
            rows.add(record.toList())
        }
    }
    return listOf(SpreadsheetSheet("Sheet 1", rows))
}

private fun readXls(bytes: ByteArray): List<SpreadsheetSheet> {
    HSSFWorkbook(ByteArrayInputStream(bytes)).use { workbook ->
        if (workbook.numberOfSheets > MAX_XLS_SHEETS) {
            throw IOException("This workbook has too many sheets to display.")
        }

        var totalCellCount = 0
        val formatter = DataFormatter(Locale.getDefault())
        val evaluator = workbook.creationHelper.createFormulaEvaluator()
        return (0 until workbook.numberOfSheets).map { sheetIndex ->
            val sheet = workbook.getSheetAt(sheetIndex)
            val rowCount = sheet.lastRowNum + 1
            if (rowCount > MAX_SPREADSHEET_ROWS) {
                throw IOException("A worksheet has too many rows to display.")
            }

            var columnCount = 0
            for (rowIndex in 0 until rowCount) {
                val row = sheet.getRow(rowIndex)
                if (row != null) columnCount = maxOf(columnCount, row.lastCellNum.toInt())
            }
            totalCellCount += rowCount * columnCount
            if (totalCellCount > MAX_SPREADSHEET_CELLS) {
                throw IOException("This workbook has too many cells to display.")
            }

            val rows = (0 until rowCount).map { rowIndex ->
                val row = sheet.getRow(rowIndex)
                (0 until columnCount).map { columnIndex ->
                    val cell = row?.getCell(columnIndex)
                    if (cell == null) "" else formatter.formatCellValue(cell, evaluator)
                }
            }
            SpreadsheetSheet(sheet.sheetName, rows)
        }
    }
}

private fun readPpt(bytes: ByteArray): List<PresentationSlide> {
    HSLFSlideShow(ByteArrayInputStream(bytes)).use { presentation ->
        val sourceSlides = presentation.slides
        if (sourceSlides.isEmpty()) throw IOException("This presentation contains no slides.")
        if (sourceSlides.size > MAX_PRESENTATION_SLIDES) {
            throw IOException("This presentation has too many slides to display.")
        }

        var shapeCount = 0
        var totalTextLength = 0
        return sourceSlides.map { slide ->
            val slideText = mutableListOf<String>()
            val shapes = ArrayDeque(slide.shapes.asReversed())
            while (shapes.isNotEmpty()) {
                val shape = shapes.removeLast()
                shapeCount++
                if (shapeCount > MAX_PRESENTATION_SHAPES) {
                    throw IOException("This presentation has too many elements to display.")
                }
                when (shape) {
                    is HSLFTextShape -> {
                        val text = shape.text?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            totalTextLength += text.length
                            if (totalTextLength > MAX_PRESENTATION_TEXT_CHARS) {
                                throw IOException("This presentation contains too much text to display.")
                            }
                            slideText.add(text)
                        }
                    }

                    is HSLFGroupShape -> shapes.addAll(shape.shapes.asReversed())
                }
            }

            val title = slide.title?.trim()?.takeIf(String::isNotEmpty)
            val paragraphs = if (title == null) {
                slideText
            } else {
                slideText.filterNot { it == title }
            }
            PresentationSlide(title, paragraphs)
        }
    }
}

private const val MAX_XLS_SHEETS = 50

private fun decodeImage(context: Context, uri: Uri): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, bounds)
    } ?: throw IOException("This image could not be opened.")
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        throw IOException("This image could not be decoded.")
    }

    val maxWidth = context.resources.displayMetrics.widthPixels * 2
    val maxPixels = 8L * 1024 * 1024
    var sampleSize = 1
    while (
        bounds.outWidth / sampleSize > maxWidth ||
        (bounds.outWidth / sampleSize).toLong() * (bounds.outHeight / sampleSize) > maxPixels
    ) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: throw IOException("This image could not be opened.")
}

private fun displayName(context: Context, uri: Uri): String {
    val queriedName = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameColumn >= 0) cursor.getString(nameColumn) else null
    }

    return queriedName ?: uri.lastPathSegment ?: "Selected document"
}
