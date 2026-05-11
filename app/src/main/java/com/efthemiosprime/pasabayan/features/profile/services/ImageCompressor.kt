package com.efthemiosprime.pasabayan.features.profile.services

/**
 * Compresses an image to a JPEG payload suitable for `POST /api/profile` multipart upload.
 * Implementation must enforce the size + quality budget from `09-profile-carrier-consent.md`
 * (max 512 px on the longest dimension, ~0.7 JPEG quality) so the request stays under the
 * backend's 2 MB / 413 ceiling.
 */
fun interface ImageCompressor {
    /**
     * @throws IllegalArgumentException if [input] cannot be decoded as an image.
     */
    fun compressToJpeg(input: ByteArray): ByteArray
}
