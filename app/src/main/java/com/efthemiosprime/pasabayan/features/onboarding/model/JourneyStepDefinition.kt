package com.efthemiosprime.pasabayan.features.onboarding.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector
import com.efthemiosprime.pasabayan.R

/**
 * Static journey content — parity with iOS `JourneyStep` in `OnboardingModels.swift`.
 * Decorative icons use Material symbols as SF Symbol equivalents (no iOS raster assets in this feature).
 */
data class JourneyStepDefinition(
    val stepNumber: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val tagResIds: List<Int>,
    val mainIcon: ImageVector,
    val floatingIcons: List<ImageVector>,
)

object JourneyStepDefinitions {

    private val carrierSteps: List<JourneyStepDefinition> = listOf(
        JourneyStepDefinition(
            stepNumber = 1,
            titleRes = R.string.onboarding_carrier_step1_title,
            descriptionRes = R.string.onboarding_carrier_step1_description,
            tagResIds = listOf(
                R.string.onboarding_carrier_step1_tag1,
                R.string.onboarding_carrier_step1_tag2,
                R.string.onboarding_carrier_step1_tag3,
                R.string.onboarding_carrier_step1_tag4,
            ),
            mainIcon = Icons.Outlined.Send,
            floatingIcons = listOf(
                Icons.Outlined.Flight,
                Icons.Outlined.Public,
                Icons.Outlined.AttachMoney,
            ),
        ),
        JourneyStepDefinition(
            stepNumber = 2,
            titleRes = R.string.onboarding_carrier_step2_title,
            descriptionRes = R.string.onboarding_carrier_step2_description,
            tagResIds = listOf(
                R.string.onboarding_carrier_step2_tag1,
                R.string.onboarding_carrier_step2_tag2,
                R.string.onboarding_carrier_step2_tag3,
                R.string.onboarding_carrier_step2_tag4,
            ),
            mainIcon = Icons.Outlined.Search,
            floatingIcons = listOf(
                Icons.Outlined.Inventory2,
                Icons.Outlined.VerifiedUser,
                Icons.Outlined.FormatListBulleted,
            ),
        ),
        JourneyStepDefinition(
            stepNumber = 3,
            titleRes = R.string.onboarding_carrier_step3_title,
            descriptionRes = R.string.onboarding_carrier_step3_description,
            tagResIds = listOf(
                R.string.onboarding_carrier_step3_tag1,
                R.string.onboarding_carrier_step3_tag2,
                R.string.onboarding_carrier_step3_tag3,
                R.string.onboarding_carrier_step3_tag4,
            ),
            mainIcon = Icons.Outlined.VolunteerActivism,
            floatingIcons = listOf(
                Icons.Outlined.ChatBubbleOutline,
                Icons.Outlined.LocationOn,
                Icons.Outlined.People,
            ),
        ),
        JourneyStepDefinition(
            stepNumber = 4,
            titleRes = R.string.onboarding_carrier_step4_title,
            descriptionRes = R.string.onboarding_carrier_step4_description,
            tagResIds = listOf(
                R.string.onboarding_carrier_step4_tag1,
                R.string.onboarding_carrier_step4_tag2,
                R.string.onboarding_carrier_step4_tag3,
                R.string.onboarding_carrier_step4_tag4,
            ),
            mainIcon = Icons.Outlined.Payments,
            floatingIcons = listOf(
                Icons.Outlined.AccountBalance,
                Icons.Outlined.Star,
                Icons.Outlined.Autorenew,
            ),
        ),
    )

    private val shipperSteps: List<JourneyStepDefinition> = listOf(
        JourneyStepDefinition(
            stepNumber = 1,
            titleRes = R.string.onboarding_sender_step1_title,
            descriptionRes = R.string.onboarding_sender_step1_description,
            tagResIds = listOf(
                R.string.onboarding_sender_step1_tag1,
                R.string.onboarding_sender_step1_tag2,
                R.string.onboarding_sender_step1_tag3,
                R.string.onboarding_sender_step1_tag4,
            ),
            mainIcon = Icons.Outlined.EditNote,
            floatingIcons = listOf(
                Icons.Outlined.PhotoCamera,
                Icons.Outlined.Inventory2,
                Icons.Outlined.CalendarMonth,
            ),
        ),
        JourneyStepDefinition(
            stepNumber = 2,
            titleRes = R.string.onboarding_sender_step2_title,
            descriptionRes = R.string.onboarding_sender_step2_description,
            tagResIds = listOf(
                R.string.onboarding_sender_step2_tag1,
                R.string.onboarding_sender_step2_tag2,
                R.string.onboarding_sender_step2_tag3,
                R.string.onboarding_sender_step2_tag4,
            ),
            mainIcon = Icons.Outlined.VerifiedUser,
            floatingIcons = listOf(
                Icons.Outlined.Star,
                Icons.Outlined.VerifiedUser,
                Icons.Outlined.AccountTree,
            ),
        ),
        JourneyStepDefinition(
            stepNumber = 3,
            titleRes = R.string.onboarding_sender_step3_title,
            descriptionRes = R.string.onboarding_sender_step3_description,
            tagResIds = listOf(
                R.string.onboarding_sender_step3_tag1,
                R.string.onboarding_sender_step3_tag2,
                R.string.onboarding_sender_step3_tag3,
            ),
            mainIcon = Icons.Outlined.Lock,
            floatingIcons = listOf(
                Icons.Outlined.CreditCard,
                Icons.Outlined.Shield,
                Icons.Outlined.AttachMoney,
            ),
        ),
        JourneyStepDefinition(
            stepNumber = 4,
            titleRes = R.string.onboarding_sender_step4_title,
            descriptionRes = R.string.onboarding_sender_step4_description,
            tagResIds = listOf(
                R.string.onboarding_sender_step4_tag1,
                R.string.onboarding_sender_step4_tag2,
                R.string.onboarding_sender_step4_tag3,
            ),
            mainIcon = Icons.Outlined.LocationOn,
            floatingIcons = listOf(
                Icons.Outlined.LocationOn,
                Icons.Outlined.QrCode2,
                Icons.Outlined.RateReview,
            ),
        ),
    )

    fun stepsFor(role: OnboardingRole): List<JourneyStepDefinition> = when (role) {
        OnboardingRole.Carrier -> carrierSteps
        OnboardingRole.Shipper -> shipperSteps
    }

    /** Parity with iOS `FloatingIconsBackground` on role selection. */
    val roleSelectionFloatingIcons: List<ImageVector> = listOf(
        Icons.Outlined.Flight,
        Icons.Outlined.Inventory2,
        Icons.Outlined.Public,
        Icons.Outlined.AttachMoney,
        Icons.Outlined.DirectionsCar,
        Icons.Outlined.LocalShipping,
    )
}
