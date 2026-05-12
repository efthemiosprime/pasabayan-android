package com.efthemiosprime.pasabayan.core.domain.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.VerificationLevel
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleBoolSerializer
import com.efthemiosprime.pasabayan.core.domain.util.FlexibleStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Unified user subset — replaces iOS's scattered CarrierInfo, UserInfo,
 * AvailablePackageShipper, CarrierBasicInfo. All fields optional except id + name.
 *
 * Used across trips (Trip.carrier), packages (AvailablePackage.shipper,
 * CompatibleTrip.carrier), and bookings (DeliveryMatch.carrier/shipper).
 */
@Serializable
data class UserSummary(
    val id: Int,
    val name: String,
    val email: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
    @SerialName("phone_verified")
    @Serializable(with = FlexibleBoolSerializer::class)
    val phoneVerified: Boolean? = null,
    @SerialName("profile_completed")
    @Serializable(with = FlexibleBoolSerializer::class)
    val profileCompleted: Boolean? = null,
    @SerialName("user_types")
    @Serializable(with = UserTypesSerializer::class)
    val userTypes: List<String>? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val rating: String? = null,
    @SerialName("total_ratings")
    val totalRatings: Int? = null,
    @SerialName("verification_level")
    val verificationLevel: String? = null,
    @SerialName("is_active_carrier")
    @Serializable(with = FlexibleBoolSerializer::class)
    val isActiveCarrier: Boolean? = null,
    @SerialName("is_active_shipper")
    @Serializable(with = FlexibleBoolSerializer::class)
    val isActiveShipper: Boolean? = null,
    /**
     * Account-creation timestamp from the server (ISO-8601). Drives the
     * "Member since" / "Carrier since" row on
     * [com.efthemiosprime.pasabayan.features.profile.ui.UserProfilePopover].
     * Optional — many wire surfaces omit it.
     */
    @SerialName("created_at") val createdAt: String? = null,
) {
    val effectiveVerificationLevel: VerificationLevel
        get() = VerificationLevel.normalized(verificationLevel)

    val ratingValue: Double?
        get() = rating?.toDoubleOrNull()

    val formattedRating: String
        get() {
            val value = ratingValue ?: return "No rating"
            return String.format("%.1f", value)
        }

    val isVerified: Boolean
        get() = effectiveVerificationLevel.isVerified

    val isPremium: Boolean
        get() = effectiveVerificationLevel.isPremium

    /**
     * Account-creation date formatted for the popover ("May 2023"). Returns
     * `null` when [createdAt] is missing or unparseable so the UI hides the
     * row entirely rather than rendering a malformed label.
     */
    val memberSinceLabel: String?
        get() {
            val raw = createdAt?.takeIf { it.isNotBlank() } ?: return null
            return runCatching {
                val instant = java.time.OffsetDateTime.parse(raw).toInstant()
                java.time.format.DateTimeFormatter.ofPattern("MMM yyyy")
                    .withZone(java.time.ZoneId.systemDefault())
                    .format(instant)
            }.getOrNull()
        }
}

/**
 * Handles `user_types` arriving as either `["shipper","carrier"]` (array)
 * or `{"0":"shipper","1":"carrier"}` / `{"shipper":true}` (dictionary).
 */
object UserTypesSerializer : KSerializer<List<String>?> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("UserTypes")

    override fun deserialize(decoder: Decoder): List<String>? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> {
                jsonDecoder.json.decodeFromJsonElement(
                    ListSerializer(String.serializer()),
                    element,
                )
            }
            is JsonObject -> {
                // Dict format: keys are indices or role names, values are role strings or booleans
                // Try extracting string values first ({"0":"shipper"} format)
                val stringValues = element.values.mapNotNull { v ->
                    try {
                        v.jsonPrimitive.content
                    } catch (_: Exception) {
                        null
                    }
                }.filter { it.isNotBlank() }

                if (stringValues.isNotEmpty() && stringValues.all { it in listOf("shipper", "carrier") }) {
                    stringValues
                } else {
                    // Key-based format ({"shipper":true,"carrier":false})
                    element.keys.toList()
                }
            }
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>?) {
        if (value != null) {
            encoder.encodeSerializableValue(ListSerializer(String.serializer()), value)
        } else {
            @Suppress("UNCHECKED_CAST")
            (encoder as? kotlinx.serialization.json.JsonEncoder)
                ?.encodeJsonElement(kotlinx.serialization.json.JsonNull)
        }
    }
}
