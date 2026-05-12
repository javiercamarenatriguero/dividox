package com.akole.dividox.common.settings.data.share

import android.content.Intent
import androidx.core.content.FileProvider
import com.akole.dividox.common.settings.data.biometric.ActivityHolder
import java.io.File

actual class FileShareService {
    actual fun share(fileName: String, csvContent: String) {
        val activity = ActivityHolder.get() ?: return
        val cacheDir = File(activity.cacheDir, "exports").also { it.mkdirs() }
        val file = File(cacheDir, fileName).also { it.writeText(csvContent) }
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(Intent.createChooser(intent, fileName))
    }
}
