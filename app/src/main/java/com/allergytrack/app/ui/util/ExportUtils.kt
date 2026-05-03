package com.allergytrack.app.ui.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allergytrack.app.R
import com.allergytrack.app.ui.screens.analysis.AnalysisViewModel
import java.io.File

/**
 * Extension function to safely share a PDF file using system share sheet.
 */
fun Context.sharePdfFile(file: File) {
    val appContext = applicationContext
    try {
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooserTitle = appContext.getString(R.string.export_share_chooser_title)
        val chooserIntent = Intent.createChooser(shareIntent, chooserTitle).apply {
            if (this@sharePdfFile !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        
        startActivity(chooserIntent)
        Toast.makeText(appContext, appContext.getString(R.string.export_save_confirmation), Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(appContext, appContext.getString(R.string.export_error_msg), Toast.LENGTH_SHORT).show()
    }
}

/**
 * Common observer for Analysis export events.
 * Can be placed in any Screen or Scaffold that uses AnalysisViewModel.
 */
@Composable
fun AnalysisExportHandler(viewModel: AnalysisViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.exportedFile) {
        uiState.exportedFile?.let { file ->
            context.sharePdfFile(file)
            viewModel.dismissExport()
        }
    }
}
