package dev.pocketdoc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketDocScreen()
        }
    }
}

@Composable
private fun PocketDocScreen() {
    val context = LocalContext.current
    var selectedFileName by rememberSaveable { mutableStateOf<String?>(null) }
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
            }
            selectedFileName = displayName(context, uri)
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF285A49),
            background = Color(0xFFF5F7F2),
            surface = Color.White,
        ),
    ) {
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

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (selectedFileName == null) "No file selected" else "Selected file",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = selectedFileName ?: "Choose a document stored on your device.",
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { documentPicker.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text("Choose file")
                }
            }
        }
    }
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
