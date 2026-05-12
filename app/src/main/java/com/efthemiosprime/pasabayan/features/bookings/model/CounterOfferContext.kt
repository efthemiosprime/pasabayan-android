package com.efthemiosprime.pasabayan.features.bookings.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.InitiatedBy
import com.efthemiosprime.pasabayan.core.domain.`enum`.MatchStatus
import com.efthemiosprime.pasabayan.features.notifications.model.NotificationData

/**
 * Counter-offer presentation context. Parity with iOS
 * `Features/Bookings/Models/CounterOfferContext.swift`.
 *
 * Built locally from a [DeliveryMatch] or a push [NotificationData] — never
 * decoded directly from the wire. `priceDifference` and `direction` are
 * computed from `newPrice`/`originalPrice`, so a constructed context is
 * always self-consistent regardless of what (if any) values the server sent
 * for those fields.
 */
data class CounterOfferContext(
    val newPrice: Double,
    val originalPrice: Double,
    val counterOffererName: String,
    val counterOffererId: Int,
    val initiatedBy: InitiatedBy,
    val isCounterOffer: Boolean,
    val counterOfferRound: Int? = null,
    val remainingCounterOffers: Int? = null,
    val canCounterOffer: Boolean? = null,
) {
    val priceDifference: Double get() = newPrice - originalPrice

    val direction: String get() = if (priceDifference > 0) "up" else "down"

    val isPriceIncrease: Boolean get() = priceDifference > 0

    val isLowerOffer: Boolean get() = priceDifference < 0

    val isHigherOffer: Boolean get() = priceDifference > 0

    val formattedNewPrice: String get() = formatPrice(newPrice)

    val formattedOriginalPrice: String get() = formatPrice(originalPrice)

    /** Absolute formatted difference (no sign). e.g. `$15.00`. */
    val formattedDifference: String get() = formatPrice(kotlin.math.abs(priceDifference))

    val summaryKind: CounterOfferSummaryKind
        get() = when {
            isLowerOffer -> CounterOfferSummaryKind.LOWER
            isHigherOffer -> CounterOfferSummaryKind.HIGHER
            else -> CounterOfferSummaryKind.UNCHANGED
        }

    companion object {

        /**
         * Best-effort context from a match when no push payload is available.
         * Returns `null` when neither the structured `originalPrice` field nor
         * a parseable "(was $X.XX)" hint in the carrier/shipper message is
         * available — without an original we can't compute a comparison.
         */
        fun fromMatch(match: DeliveryMatch): CounterOfferContext? {
            val newPrice = match.agreedPrice

            val originalFromMatch = match.originalPriceValue
            val originalFromMessage = if (originalFromMatch == null) originalPriceFromMessage(match) else null
            val originalPrice = originalFromMatch ?: originalFromMessage ?: return null

            if (!match.isCounterOffer && originalFromMatch == null) {
                // Only the structured field signals a real counter-offer. A "was $X"
                // hint alone isn't enough to assume the match is a counter-offer.
                return null
            }

            val counterOffererName = match.counterOffererName?.takeIf { it.isNotBlank() }
                ?: fallbackCounterOffererName(match)
            val counterOffererId = match.counterOffererId ?: fallbackCounterOffererId(match)

            return CounterOfferContext(
                newPrice = newPrice,
                originalPrice = originalPrice,
                counterOffererName = counterOffererName,
                counterOffererId = counterOffererId,
                initiatedBy = match.initiatedBy,
                isCounterOffer = match.isCounterOffer,
                counterOfferRound = match.counterOfferRound,
                remainingCounterOffers = match.remainingCounterOffers,
                canCounterOffer = match.canCounterOffer,
            )
        }

        /**
         * Build a context from a decoded push payload. Required fields:
         * `newPrice` and `originalPrice`. Returns `null` if either is missing.
         */
        fun fromNotificationData(
            data: NotificationData,
            fallbackName: String = "",
        ): CounterOfferContext? {
            val newPrice = data.newPrice ?: return null
            val originalPrice = data.originalPrice ?: return null

            val initiated = data.initiatedBy
                ?.let { raw -> runCatching { InitiatedBy.valueOf(raw.uppercase()) }.getOrNull() }
                ?: InitiatedBy.UNKNOWN

            return CounterOfferContext(
                newPrice = newPrice,
                originalPrice = originalPrice,
                counterOffererName = data.counterOffererName?.takeIf { it.isNotBlank() } ?: fallbackName,
                counterOffererId = data.counterOffererId ?: 0,
                initiatedBy = initiated,
                isCounterOffer = true,
                counterOfferRound = data.counterOfferRound,
                remainingCounterOffers = data.remainingCounterOffers,
                canCounterOffer = data.canCounterOffer,
            )
        }

        /**
         * Tries to recover the original price from a chat message of the form
         * `(was $42.00)` or `was $42`. Mirrors the iOS regex in
         * `CounterOfferContext.originalPriceFromMessage`.
         */
        internal fun originalPriceFromMessage(match: DeliveryMatch): Double? {
            val combined = listOfNotNull(match.carrierMessage, match.shipperMessage)
                .joinToString(separator = " ")
                .trim()
            if (combined.isEmpty()) return null

            val groups = ORIGINAL_PRICE_REGEX.find(combined)?.groupValues ?: return null
            // groups[1] is the parenthesized form; groups[2] is the bare "was $X" form.
            return groups.drop(1).firstOrNull { it.isNotEmpty() }?.toDoubleOrNull()
        }

        private fun fallbackCounterOffererName(match: DeliveryMatch): String = when (match.initiatedBy) {
            InitiatedBy.SHIPPER -> match.shipper?.name.orEmpty()
            InitiatedBy.CARRIER -> match.carrier?.name.orEmpty()
            InitiatedBy.UNKNOWN -> if (match.matchStatus == MatchStatus.SHIPPER_REQUESTED) {
                match.shipper?.name.orEmpty()
            } else {
                match.carrier?.name.orEmpty()
            }
        }

        private fun fallbackCounterOffererId(match: DeliveryMatch): Int = when (match.initiatedBy) {
            InitiatedBy.SHIPPER -> match.shipper?.id ?: 0
            InitiatedBy.CARRIER -> match.carrier?.id ?: 0
            InitiatedBy.UNKNOWN -> if (match.matchStatus == MatchStatus.SHIPPER_REQUESTED) {
                match.shipper?.id ?: 0
            } else {
                match.carrier?.id ?: 0
            }
        }

        private fun formatPrice(value: Double): String = String.format("$%.2f", value)

        private val ORIGINAL_PRICE_REGEX = Regex(
            "(?:\\(\\s*was\\s*\\$?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s*\\)|\\bwas\\s*\\$?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\b)",
            RegexOption.IGNORE_CASE,
        )
    }
}

enum class CounterOfferSummaryKind { LOWER, HIGHER, UNCHANGED }
