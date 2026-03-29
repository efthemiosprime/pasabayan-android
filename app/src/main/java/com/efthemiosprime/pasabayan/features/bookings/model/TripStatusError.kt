package com.efthemiosprime.pasabayan.features.bookings.model

enum class TripStatusError(
    val userFriendlyTitle: String,
    val shouldRemoveFromList: Boolean,
) {
    PLANNING("Trip Not Ready", false),
    COMPLETED("Trip Completed", true),
    CANCELLED("Trip Cancelled", true),
    IN_TRANSIT("Trip In Transit", false),
    DEPARTED("Trip Departed", true),
    UNAVAILABLE("Trip Unavailable", true);

    companion object {
        fun from(message: String): TripStatusError? {
            val lower = message.lowercase()
            return when {
                lower.contains("completed") -> COMPLETED
                lower.contains("cancelled") || lower.contains("canceled") -> CANCELLED
                lower.contains("planning") -> PLANNING
                lower.contains("in transit") || lower.contains("in_transit") -> IN_TRANSIT
                lower.contains("departed") -> DEPARTED
                lower.contains("unavailable") -> UNAVAILABLE
                else -> null
            }
        }
    }
}
