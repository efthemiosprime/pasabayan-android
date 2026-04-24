package com.efthemiosprime.pasabayan.features.chat.model

data class ReverbConfig(
    val key: String,
    val host: String,
    val port: Int?,
    val scheme: String?,
) {
    val socketScheme: String
        get() = if (scheme == "https") "wss" else "ws"

    val socketPort: Int
        get() = port ?: 8080

    val socketUrl: String
        get() = "${socketScheme}://${host}:${socketPort}/app/${key}"

    fun authUrl(): String = "${scheme ?: "https"}://${host}:${socketPort}/broadcasting/auth"
}

