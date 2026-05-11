package com.efthemiosprime.pasabayan.features.profile.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Production [ImageCompressor]. Scales the longest side to 512 px (preserving aspect ratio) and
 * encodes JPEG at quality 70 — matches iOS `UIImage.optimizedForUpload(maxSize:512, quality:0.7)`.
 */
class BitmapImageCompressor @Inject constructor() : ImageCompressor {

    override fun compressToJpeg(input: ByteArray): ByteArray {
        require(input.isNotEmpty()) { "empty image bytes" }
        val decoded = BitmapFactory.decodeByteArray(input, 0, input.size)
            ?: throw IllegalArgumentException("could not decode image")
        val scaled = scaleToMaxDimension(decoded, MAX_DIMENSION)
        return ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.toByteArray()
        }
    }

    private fun scaleToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val ratio = width.toFloat() / height
        val (targetWidth, targetHeight) = if (width >= height) {
            maxDimension to (maxDimension / ratio).toInt().coerceAtLeast(1)
        } else {
            (maxDimension * ratio).toInt().coerceAtLeast(1) to maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private companion object {
        const val MAX_DIMENSION = 512
        const val JPEG_QUALITY = 70
    }
}
