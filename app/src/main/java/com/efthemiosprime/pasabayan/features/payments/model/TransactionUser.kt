package com.efthemiosprime.pasabayan.features.payments.model

data class TransactionUser(
    val id: Int,
    val name: String,
    val email: String? = null,
    val avatar: String? = null,
)
