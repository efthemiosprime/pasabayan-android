package com.efthemiosprime.pasabayan.features.onboarding.model

import androidx.compose.runtime.Immutable

@Immutable
data class ConsentSelections(
    val pushNotifications: Boolean = false,
    val locationTracking: Boolean = false,
    val analytics: Boolean = false,
    val marketingCommunications: Boolean = false,
)
