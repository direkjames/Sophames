package com.direkjames.sophames.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Copies picked photos into app-private storage, downscaled to ~1600px and re-encoded
 * as JPEG (80%). ImageDecoder also applies EXIF rotation, so photos are never sideways.
 */
class PhotoStore(context: Context) {

    private val resolver = context.contentResolver
    private val dir = File(context.filesDir, "photos").apply { mkdirs() }

    fun fileFor(name: String): File = File(dir, name)

    suspend fun importPhoto(uri: Uri): String = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(resolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longest = max(info.size.width, info.size.height)
            if (longest > MAX_EDGE) {
                val scale = MAX_EDGE.toFloat() / longest
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt(),
                    (info.size.height * scale).roundToInt(),
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        val name = "${UUID.randomUUID()}.jpg"
        FileOutputStream(fileFor(name)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        bitmap.recycle()
        name
    }

    fun delete(name: String?) {
        if (name == null) return
        runCatching { fileFor(name).delete() }
    }

    private companion object {
        const val MAX_EDGE = 1600
        const val JPEG_QUALITY = 80
    }
}
