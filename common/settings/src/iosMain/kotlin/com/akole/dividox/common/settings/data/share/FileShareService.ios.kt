package com.akole.dividox.common.settings.data.share

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual class FileShareService {
    actual fun share(fileName: String, csvContent: String) {
        val tempDir = NSTemporaryDirectory()
        val filePath = "$tempDir$fileName"
        @Suppress("UNCHECKED_CAST")
        (csvContent as NSString).writeToFile(
            path = filePath,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null,
        )
        val fileUrl = NSURL.fileURLWithPath(filePath)
        dispatch_async(dispatch_get_main_queue()) {
            val controller = UIActivityViewController(
                activityItems = listOf(fileUrl),
                applicationActivities = null,
            )
            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?.presentViewController(controller, animated = true, completion = null)
        }
    }
}
