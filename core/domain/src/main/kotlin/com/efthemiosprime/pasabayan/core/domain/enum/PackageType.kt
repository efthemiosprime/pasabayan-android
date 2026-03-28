package com.efthemiosprime.pasabayan.core.domain.`enum`

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PackageType {
    @SerialName("general") GENERAL,
    @SerialName("electronics") ELECTRONICS,
    @SerialName("clothing") CLOTHING,
    @SerialName("books") BOOKS,
    @SerialName("food") FOOD,
    @SerialName("furniture") FURNITURE,
    @SerialName("medical") MEDICAL,
    @SerialName("documents") DOCUMENTS,
    @SerialName("document") DOCUMENT,
    @SerialName("fragile") FRAGILE,
    @SerialName("parcel") PARCEL,
    @SerialName("gifts") GIFTS,
    @SerialName("automotive") AUTOMOTIVE,
    @SerialName("beauty") BEAUTY,
    @SerialName("sports") SPORTS,
    @SerialName("toys") TOYS,
    @SerialName("household") HOUSEHOLD,
    @SerialName("jewelry") JEWELRY,
    @SerialName("art") ART,
    @SerialName("industrial") INDUSTRIAL,
    @SerialName("equipment") EQUIPMENT,
    @SerialName("other") OTHER;

    val icon: String
        get() = when (this) {
            GENERAL -> "📦"
            ELECTRONICS -> "💻"
            CLOTHING -> "👕"
            BOOKS -> "📚"
            FOOD -> "🍕"
            FURNITURE -> "🪑"
            MEDICAL -> "💊"
            DOCUMENTS, DOCUMENT -> "📄"
            FRAGILE -> "⚠️"
            PARCEL -> "📦"
            GIFTS -> "🎁"
            AUTOMOTIVE -> "🚗"
            BEAUTY -> "💄"
            SPORTS -> "⚽"
            TOYS -> "🧸"
            HOUSEHOLD -> "🏠"
            JEWELRY -> "💎"
            ART -> "🎨"
            INDUSTRIAL -> "🏭"
            EQUIPMENT -> "🔧"
            OTHER -> "📦"
        }
}
