package com.efthemiosprime.pasabayan.core.domain.`enum`

/**
 * Direction of an errand / service request.
 *
 * iOS parity with `ErrandDirection` in `PackageRequest.swift`. The API field
 * `direction` arrives as a raw `String?` on `AvailablePackage` and
 * `PackageRequest`; legacy delivery rows omit it. Use [fromApi] to normalise
 * mixed-case values — strict casing would let "Send" / "TASK" silently fall
 * through to [RECEIVE] and mask a backend bug.
 */
enum class ErrandDirection {
    /** Carrier picks up an item (e.g. groceries) and delivers it to the shipper. Default for legacy rows. */
    RECEIVE,

    /** Carrier picks up an item from the shipper and delivers it to a named recipient. */
    SEND,

    /** Custom task (e.g. "walk my dog"). No pickup/delivery pair. */
    TASK;

    /** Lowercase API value, e.g. `"receive"`, `"send"`, `"task"`. */
    val apiValue: String
        get() = name.lowercase()

    companion object {
        fun fromApi(value: String?): ErrandDirection = when (value?.trim()?.lowercase()) {
            "send" -> SEND
            "task" -> TASK
            else -> RECEIVE
        }
    }
}
