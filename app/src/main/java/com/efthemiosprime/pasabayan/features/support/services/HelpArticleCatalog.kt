package com.efthemiosprime.pasabayan.features.support.services

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.features.support.model.HelpArticle

/**
 * Canonical Popular Articles list — iOS parity with `PopularArticlesSection.articles`
 * in `HelpCenterView.swift`. Order matches iOS (rough popularity descending).
 */
object HelpArticleCatalog {
    val popularArticles: List<HelpArticle> = listOf(
        HelpArticle(
            id = "getting-started",
            icon = Icons.Filled.AutoAwesome,
            titleRes = R.string.support_help_center_article_getting_started_title,
            readingTimeRes = R.string.support_help_center_article_getting_started_reading_time,
            htmlBaseFilename = "getting-started",
        ),
        HelpArticle(
            id = "create-delivery-request",
            icon = Icons.Filled.Description,
            titleRes = R.string.support_help_center_article_create_delivery_title,
            readingTimeRes = R.string.support_help_center_article_create_delivery_reading_time,
            htmlBaseFilename = "how-to-create-delivery-request",
        ),
        HelpArticle(
            id = "payment-billing",
            icon = Icons.Filled.CreditCard,
            titleRes = R.string.support_help_center_article_payment_billing_title,
            readingTimeRes = R.string.support_help_center_article_payment_billing_reading_time,
            htmlBaseFilename = "payment-methods-billing",
        ),
        HelpArticle(
            id = "verifying-account",
            icon = Icons.Filled.VerifiedUser,
            titleRes = R.string.support_help_center_article_verifying_account_title,
            readingTimeRes = R.string.support_help_center_article_verifying_account_reading_time,
            htmlBaseFilename = "verifying-your-account",
        ),
        HelpArticle(
            id = "carrier-safety",
            icon = Icons.Filled.WarningAmber,
            titleRes = R.string.support_help_center_article_carrier_safety_title,
            readingTimeRes = R.string.support_help_center_article_carrier_safety_reading_time,
            htmlBaseFilename = "safety-guidelines-for-carriers",
        ),
        HelpArticle(
            id = "tracking-updates",
            icon = Icons.Filled.LocationOn,
            titleRes = R.string.support_help_center_article_tracking_updates_title,
            readingTimeRes = R.string.support_help_center_article_tracking_updates_reading_time,
            htmlBaseFilename = "package-tracking-updates",
        ),
        HelpArticle(
            id = "switching-roles",
            icon = Icons.Filled.SwapHoriz,
            titleRes = R.string.support_help_center_article_switching_roles_title,
            readingTimeRes = R.string.support_help_center_article_switching_roles_reading_time,
            htmlBaseFilename = "switching-roles",
        ),
        HelpArticle(
            id = "accepting-deliveries",
            icon = Icons.Filled.LocalShipping,
            titleRes = R.string.support_help_center_article_accepting_deliveries_title,
            readingTimeRes = R.string.support_help_center_article_accepting_deliveries_reading_time,
            htmlBaseFilename = "accepting-completing-deliveries",
        ),
        HelpArticle(
            id = "carrier-payouts",
            icon = Icons.Filled.Payments,
            titleRes = R.string.support_help_center_article_carrier_payouts_title,
            readingTimeRes = R.string.support_help_center_article_carrier_payouts_reading_time,
            htmlBaseFilename = "carrier-payouts-earnings",
        ),
        HelpArticle(
            id = "create-trip",
            icon = Icons.Filled.Flight,
            titleRes = R.string.support_help_center_article_create_trip_title,
            readingTimeRes = R.string.support_help_center_article_create_trip_reading_time,
            htmlBaseFilename = "how-to-create-trip",
        ),
    )
}
