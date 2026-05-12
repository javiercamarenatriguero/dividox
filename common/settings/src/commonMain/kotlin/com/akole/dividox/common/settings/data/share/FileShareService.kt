package com.akole.dividox.common.settings.data.share

expect class FileShareService {
    fun share(fileName: String, csvContent: String)
}
