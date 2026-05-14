package com.efthemiosprime.pasabayan.features.packages.services

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.efthemiosprime.pasabayan.core.network.packages.CreatePackageRequestJson
import com.efthemiosprime.pasabayan.core.network.packages.PackageUpdateRequestJson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
open class MultipartFormDataFactory {
    @Inject
    constructor(@ApplicationContext appContext: Context) {
        this.appContext = appContext
    }

    constructor()

    private var appContext: Context? = null

    open fun createPackageFields(request: CreatePackageRequestJson): Map<String, RequestBody> = buildMap {
        put("pickup_address", request.pickupAddress.toPlainTextBody())
        put("pickup_city", request.pickupCity.toPlainTextBody())
        put("pickup_country", request.pickupCountry.toPlainTextBody())
        put("delivery_address", request.deliveryAddress.toPlainTextBody())
        put("delivery_city", request.deliveryCity.toPlainTextBody())
        put("delivery_country", request.deliveryCountry.toPlainTextBody())
        put("package_weight_kg", request.packageWeightKg.toString().toPlainTextBody())
        put("package_type", request.packageType.toPlainTextBody())
        put("fragile", request.fragile.toString().toPlainTextBody())
        put("urgency_level", request.urgencyLevel.toPlainTextBody())
        put("pickup_date_preferred", request.pickupDatePreferred.toPlainTextBody())
        put("pickup_date_flexible", request.pickupDateFlexible.toString().toPlainTextBody())
        put("delivery_date_needed", request.deliveryDateNeeded.toPlainTextBody())

        request.packageValue?.let { put("package_value", it.toString().toPlainTextBody()) }
        request.packageDescription?.takeIf { it.isNotBlank() }?.let {
            put("package_description", it.toPlainTextBody())
        }
        request.maxPriceBudget?.let { put("max_price_budget", it.toString().toPlainTextBody()) }
        request.pickupTimePreferred?.takeIf { it.isNotBlank() }?.let {
            put("pickup_time_preferred", it.toPlainTextBody())
        }
        request.deliveryTimeNeeded?.takeIf { it.isNotBlank() }?.let {
            put("delivery_time_needed", it.toPlainTextBody())
        }
        request.specialHandlingRequirements?.takeIf { it.isNotBlank() }?.let {
            put("special_handling_requirements", it.toPlainTextBody())
        }
        request.pickupCityId?.let { put("pickup_city_id", it.toString().toPlainTextBody()) }
        request.deliveryCityId?.let { put("delivery_city_id", it.toString().toPlainTextBody()) }
    }

    /**
     * Build multipart fields for an update — only non-null fields are emitted,
     * mirroring iOS `updatePackageDetailsWithImages` which sends just the changed
     * fields. Booleans are emitted as `"1"`/`"0"` for backend parity.
     */
    open fun createPackageUpdateFields(request: PackageUpdateRequestJson): Map<String, RequestBody> = buildMap {
        request.maxPriceBudget?.let { put("max_price_budget", it.toString().toPlainTextBody()) }
        request.urgencyLevel?.let { put("urgency_level", it.toPlainTextBody()) }
        request.pickupDateFlexible?.let {
            put("pickup_date_flexible", if (it) "1".toPlainTextBody() else "0".toPlainTextBody())
        }
        request.deliveryDateNeeded?.let { put("delivery_date_needed", it.toPlainTextBody()) }
        request.specialHandlingRequirements?.let {
            put("special_handling_requirements", it.toPlainTextBody())
        }
        request.requestStatus?.let { put("request_status", it.toPlainTextBody()) }
    }

    open fun createImageParts(imageUris: List<Uri>): List<MultipartBody.Part> {
        val context = requireNotNull(appContext) { "Context is required for image multipart conversion" }
        return imageUris.mapIndexed { index, uri ->
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val fileName = resolveDisplayName(uri) ?: "image_$index.jpg"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("Unable to read selected image")
            MultipartBody.Part.createFormData(
                "images[]",
                fileName,
                bytes.toRequestBody(mimeType.toMediaTypeOrNull()),
            )
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        val context = appContext ?: return null
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return null
    }

    private fun String.toPlainTextBody(): RequestBody = toRequestBody("text/plain".toMediaType())
}
