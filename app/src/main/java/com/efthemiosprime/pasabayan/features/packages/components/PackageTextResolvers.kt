package com.efthemiosprime.pasabayan.features.packages.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.domain.`enum`.PackageType
import com.efthemiosprime.pasabayan.core.domain.`enum`.UrgencyLevel
import com.efthemiosprime.pasabayan.features.packages.model.PackageRequest

@Composable
fun packageTypeLabel(type: PackageType): String = stringResource(
    when (type) {
        PackageType.GENERAL -> R.string.packages_type_general
        PackageType.ELECTRONICS -> R.string.packages_type_electronics
        PackageType.CLOTHING -> R.string.packages_type_clothing
        PackageType.BOOKS -> R.string.packages_type_books
        PackageType.FOOD -> R.string.packages_type_food
        PackageType.FURNITURE -> R.string.packages_type_furniture
        PackageType.MEDICAL -> R.string.packages_type_medical
        PackageType.FRAGILE -> R.string.packages_type_fragile
        PackageType.DOCUMENTS, PackageType.DOCUMENT -> R.string.packages_type_documents
        PackageType.PARCEL -> R.string.packages_type_parcel
        PackageType.GIFTS -> R.string.packages_type_gifts
        PackageType.AUTOMOTIVE -> R.string.packages_type_automotive
        PackageType.BEAUTY -> R.string.packages_type_beauty
        PackageType.SPORTS -> R.string.packages_type_sports
        PackageType.TOYS -> R.string.packages_type_toys
        PackageType.HOUSEHOLD -> R.string.packages_type_household
        PackageType.JEWELRY -> R.string.packages_type_jewelry
        PackageType.ART -> R.string.packages_type_art
        PackageType.INDUSTRIAL -> R.string.packages_type_industrial
        PackageType.EQUIPMENT -> R.string.packages_type_equipment
        PackageType.OTHER -> R.string.packages_type_other
    },
)

@Composable
fun urgencyLevelLabel(level: UrgencyLevel): String = stringResource(
    when (level) {
        UrgencyLevel.LOW -> R.string.packages_urgency_low
        UrgencyLevel.NORMAL -> R.string.packages_urgency_normal
        UrgencyLevel.HIGH -> R.string.packages_urgency_high
        UrgencyLevel.URGENT -> R.string.packages_urgency_urgent
        UrgencyLevel.EXPRESS -> R.string.packages_urgency_express
        UrgencyLevel.FLEXIBLE -> R.string.packages_urgency_flexible
    },
)

@Composable
fun packageDisplayTitle(pkg: PackageRequest): String {
    if (!pkg.packageDescription.isNullOrBlank()) return pkg.packageDescription
    val typeLabel = pkg.packageType?.let { packageTypeLabel(it) } ?: stringResource(R.string.packages_type_general)
    return stringResource(R.string.packages_detail_title_fallback, typeLabel)
}
