package com.efthemiosprime.pasabayan.core.domain.`enum`

enum class RefundReason(
    val displayText: String,
    val requiresCustomInput: Boolean,
) {
    DAMAGED("Package was damaged during delivery", false),
    NOT_DELIVERED("Package was never delivered", false),
    WRONG_ITEM("Wrong item was delivered", false),
    LATE_DELIVERY("Delivery was significantly late", false),
    PARTIAL_DELIVERY("Only partial items were delivered", false),
    OTHER("Other", true),
}
