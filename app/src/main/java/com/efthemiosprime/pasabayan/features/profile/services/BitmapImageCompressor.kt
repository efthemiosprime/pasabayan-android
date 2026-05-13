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

    override fun compressToJpeg(input: ByteArray): ByteArray =
        compressToJpeg(input, maxDimension = DEFAULT_MAX_DIMENSION, jpegQuality = DEFAULT_JPEG_QUALITY)

    override fun compressToJpeg(input: ByteArray, maxDimension: Int, jpegQuality: Int): ByteArray {
        require(input.isNotEmpty()) { "empty image bytes" }
        val decoded = BitmapFactory.decodeByteArray(input, 0, input.size)
            ?: throw IllegalArgumentException("could not decode image")
        val scaled = scaleToMaxDimension(decoded, maxDimension)
        return ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
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
        const val DEFAULT_MAX_DIMENSION = 512
        const val DEFAULT_JPEG_QUALITY = 70
    }
}
