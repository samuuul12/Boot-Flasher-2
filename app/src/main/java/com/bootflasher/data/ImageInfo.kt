package com.bootflasher.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class ImageInfo(
    val name: String,
    val size: Long,
    val uri: Uri
) {
    companion object {
        fun fromUri(context: Context, uri: Uri): ImageInfo? {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    OpenableColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE
                ),
                null,
                null,
                null
            )

            cursor?.use {
                if (!it.moveToFirst()) return null

                val name = it.getString(0) ?: return null
                val size = if (it.isNull(1)) 0L else it.getLong(1)

                if (!name.endsWith(".img", ignoreCase = true)) {
                    return null
                }

                return ImageInfo(name, size, uri)
            }

            return null
        }
    }
}
