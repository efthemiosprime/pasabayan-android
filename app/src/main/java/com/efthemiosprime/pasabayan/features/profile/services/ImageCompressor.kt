package com.efthemiosprime.pasabayan.features.profile.services

/**
 * Compresses an image to a JPEG payload suitable for multipart uploads.
 *
 * The single-arg overload uses the **avatar profile** (max 512 px / quality 70)
 * required by `09-profile-carrier-consent.md`. Callers with different size
 * budgets (e.g. service-match receipts at 1024 px / quality 80, matching iOS
 * `UIImage.optimizedForUpload(maxSize: 1024, quality: 0.8)`) should call the
 * parameterized overload.
 */
fun interface ImageCompressor {
    /**
     * Avatar profile — 512 px max dimension, JPEG quality 70.
     * @throws IllegalArgumentException if [input] cannot be decoded as an image.
     */
    fun compressToJpeg(input: ByteArray): ByteArray

    /**
     * Custom size budget. Defaults fall back to the single-arg overload so
     * passthrough/test fakes don't need to override.
     *
     * @param maxDimension longest-edge ceiling in pixels.
     * @param jpegQuality 0–100, passed to `Bitmap.compress`.
     */
    fun compressToJpeg(input: ByteArray, maxDimension: Int, jpegQuality: Int): ByteArray =
        compressToJpeg(input)
}
