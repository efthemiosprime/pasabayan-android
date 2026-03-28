package com.efthemiosprime.pasabayan.features.packages.model

import java.util.UUID

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val item: String,
    val quantity: String,
    val notes: String? = null,
)
