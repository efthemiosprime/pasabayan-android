package com.efthemiosprime.pasabayan.features.bookings.model

/**
 * Source of the resolved automatic offered price for a compatible trip.
 * Mirrors iOS `CompatibleTripsOfferedPriceSource`.
 */
enum class CompatibleTripsOfferedPriceSource {
    /** `flat_trip_price` (land transport / explicit flat) or `pricePerKg × packageWeight`. */
    CALCULATED,

    /** No rate × weight available — fell back to the package's max budget. */
    PACKAGE_BUDGET,

    /** No flat price, no rate × weight, and no package budget. UI must require manual entry. */
    UNAVAILABLE,
}

/** Result of validating a manual offered price (the "Your offer" field on a compatible trip). */
sealed interface CompatibleTripsManualPriceValidation {
    data class Valid(val price: Double) : CompatibleTripsManualPriceValidation
    object Invalid : CompatibleTripsManualPriceValidation
    data class OverBudget(val limit: Double) : CompatibleTripsManualPriceValidation
    data class TooHigh(val maxAllowed: Double) : CompatibleTripsManualPriceValidation
}

/**
 * Result of parsing the optional "Your offer" field on compatible trips.
 * Empty input means the automatic price should be used.
 */
sealed interface CompatibleTripsOptionalOfferResolution {
    object UseAutomatic : CompatibleTripsOptionalOfferResolution
    data class OverridePrice(val price: Double) : CompatibleTripsOptionalOfferResolution
    object InvalidInput : CompatibleTripsOptionalOfferResolution
    data class BelowMinimum(val offered: Double) : CompatibleTripsOptionalOfferResolution
    data class OverBudget(val limit: Double) : CompatibleTripsOptionalOfferResolution
    data class TooHigh(val maxAllowed: Double) : CompatibleTripsOptionalOfferResolution
}

/**
 * Mirrors iOS `CompatibleTripsPricingPolicy` (CompatibleTripsView.swift).
 * Pure value logic that decides the offered price when a shipper requests
 * a compatible trip — and validates manual price overrides.
 */
object CompatibleTripsPricingPolicy {

    const val MAX_ALLOWED_OFFERED_PRICE: Double = 9_999.99

    /**
     * Cap for optional manual offers: `max(packageMaxBudget, automaticOfferedPrice)`,
     * so the field is never stricter than leaving it blank (which sends `automaticOfferedPrice`).
     * Returns `null` when there is no budget — no cap applies beyond [MAX_ALLOWED_OFFERED_PRICE].
     */
    fun effectiveBudgetLimitForOptionalOffer(
        packageMaxBudget: Double?,
        automaticOfferedPrice: Double,
    ): Double? {
        val budget = packageMaxBudget ?: return null
        if (budget <= 0.0) return null
        return maxOf(budget, automaticOfferedPrice)
    }

    /**
     * Compute the automatic offered price for a trip.
     *
     * Priority:
     *  1. `flatTripPrice` if positive (flat-priced / land transport trips)
     *  2. `pricePerKg × packageWeightKg` if positive
     *  3. `packageMaxBudget` if positive
     *  4. otherwise [CompatibleTripsOfferedPriceSource.UNAVAILABLE], price = 0.0
     */
    fun resolveOfferedPrice(
        tripPricePerKg: String,
        packageWeightKg: Double?,
        packageMaxBudget: Double?,
        flatTripPrice: Double? = null,
    ): Pair<Double, CompatibleTripsOfferedPriceSource> {
        if (flatTripPrice != null && flatTripPrice > 0.0) {
            return flatTripPrice to CompatibleTripsOfferedPriceSource.CALCULATED
        }

        val pricePerKg = tripPricePerKg.trim().toDoubleOrNull() ?: 0.0
        val packageWeight = packageWeightKg ?: 0.0
        val calculatedTotal = pricePerKg * packageWeight

        if (calculatedTotal > 0.0) {
            return calculatedTotal to CompatibleTripsOfferedPriceSource.CALCULATED
        }

        if (packageMaxBudget != null && packageMaxBudget > 0.0) {
            return packageMaxBudget to CompatibleTripsOfferedPriceSource.PACKAGE_BUDGET
        }

        return 0.0 to CompatibleTripsOfferedPriceSource.UNAVAILABLE
    }

    /** Parse a user-entered price string. Accepts comma as decimal separator. */
    fun parseManualPriceInput(input: String): Double? =
        input.trim().replace(',', '.').toDoubleOrNull()

    fun validateManualOfferedPrice(
        offeredPrice: Double,
        budgetLimit: Double?,
    ): CompatibleTripsManualPriceValidation {
        if (offeredPrice <= 0.0) return CompatibleTripsManualPriceValidation.Invalid
        if (budgetLimit != null && offeredPrice > budgetLimit) {
            return CompatibleTripsManualPriceValidation.OverBudget(limit = budgetLimit)
        }
        if (offeredPrice > MAX_ALLOWED_OFFERED_PRICE) {
            return CompatibleTripsManualPriceValidation.TooHigh(maxAllowed = MAX_ALLOWED_OFFERED_PRICE)
        }
        return CompatibleTripsManualPriceValidation.Valid(offeredPrice)
    }

    /**
     * Resolve the optional "Your offer" field. Blank → [CompatibleTripsOptionalOfferResolution.UseAutomatic].
     * Otherwise parses, checks against the platform minimum delivery price, the budget limit,
     * and the global maximum.
     */
    fun resolveOptionalShipperOffer(
        input: String,
        budgetLimit: Double?,
        minimumDeliveryPrice: Double,
    ): CompatibleTripsOptionalOfferResolution {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return CompatibleTripsOptionalOfferResolution.UseAutomatic

        val parsed = parseManualPriceInput(trimmed)
            ?: return CompatibleTripsOptionalOfferResolution.InvalidInput

        if (parsed < minimumDeliveryPrice) {
            return CompatibleTripsOptionalOfferResolution.BelowMinimum(offered = parsed)
        }

        return when (val v = validateManualOfferedPrice(parsed, budgetLimit)) {
            is CompatibleTripsManualPriceValidation.Valid ->
                CompatibleTripsOptionalOfferResolution.OverridePrice(price = v.price)
            CompatibleTripsManualPriceValidation.Invalid ->
                CompatibleTripsOptionalOfferResolution.InvalidInput
            is CompatibleTripsManualPriceValidation.OverBudget ->
                CompatibleTripsOptionalOfferResolution.OverBudget(limit = v.limit)
            is CompatibleTripsManualPriceValidation.TooHigh ->
                CompatibleTripsOptionalOfferResolution.TooHigh(maxAllowed = v.maxAllowed)
        }
    }
}
