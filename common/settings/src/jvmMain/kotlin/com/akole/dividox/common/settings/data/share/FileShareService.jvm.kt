package com.akole.dividox.common.settings.data.share

import java.awt.Desktop
import java.io.File

actual class FileShareService {
    actual fun share(fileName: String, csvContent: String) {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "dividox-exports").also { it.mkdirs() }
        val file = File(tempDir, fileName).also { it.writeText(csvContent) }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }
}
