package com.efthemiosprime.pasabayan.features.profile.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R

/**
 * `preferred_package_types` enum (iOS `EditCarrierProfileSheet.packageTypeOptions`). The [raw]
 * string is what the backend stores — labels are localized via [labelRes].
 */
enum class CarrierPackageType(val raw: String, @StringRes val labelRes: Int) {
    ELECTRONICS("Electronics", R.string.profile_carrier_package_electronics),
    DOCUMENTS("Documents", R.string.profile_carrier_package_documents),
    CLOTHING("Clothing", R.string.profile_carrier_package_clothing),
    FOOD_ITEMS("Food Items", R.string.profile_carrier_package_food),
    FRAGILE_ITEMS("Fragile Items", R.string.profile_carrier_package_fragile),
    BOOKS("Books", R.string.profile_carrier_package_books),
    TOYS("Toys", R.string.profile_carrier_package_toys),
    SPORTS_EQUIPMENT("Sports Equipment", R.string.profile_carrier_package_sports),
    TOOLS("Tools", R.string.profile_carrier_package_tools),
    MEDICAL_SUPPLIES("Medical Supplies", R.string.profile_carrier_package_medical),
    ;

    companion object {
        fun fromRaw(value: String): CarrierPackageType? = entries.firstOrNull { it.raw == value }
    }
}

/**
 * `restricted_items` enum (iOS `EditCarrierProfileSheet.restrictedItemOptions`).
 */
enum class CarrierRestrictedItem(val raw: String, @StringRes val labelRes: Int) {
    HAZARDOUS("Hazardous Materials", R.string.profile_carrier_restricted_hazardous),
    LIQUIDS("Liquids", R.string.profile_carrier_restricted_liquids),
    PERISHABLES("Perishables", R.string.profile_carrier_restricted_perishables),
    LIVE_ANIMALS("Live Animals", R.string.profile_carrier_restricted_live_animals),
    WEAPONS("Weapons", R.string.profile_carrier_restricted_weapons),
    ALCOHOL("Alcohol", R.string.profile_carrier_restricted_alcohol),
    TOBACCO("Tobacco", R.string.profile_carrier_restricted_tobacco),
    FLAMMABLE("Flammable Items", R.string.profile_carrier_restricted_flammable),
    EXPLOSIVES("Explosive Materials", R.string.profile_carrier_restricted_explosives),
    ;

    companion object {
        fun fromRaw(value: String): CarrierRestrictedItem? =
            entries.firstOrNull { it.raw == value }
    }
}
