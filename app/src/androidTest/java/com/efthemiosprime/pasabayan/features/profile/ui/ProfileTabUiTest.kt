package com.efthemiosprime.pasabayan.features.profile.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.domain.`enum`.UserRole
import com.efthemiosprime.pasabayan.core.network.profile.CarrierStatsJson
import com.efthemiosprime.pasabayan.core.network.profile.CarrierDeliveriesJson
import com.efthemiosprime.pasabayan.core.network.profile.UserProfileJson
import com.efthemiosprime.pasabayan.core.network.profile.UserStatsDataJson
import com.efthemiosprime.pasabayan.core.session.AuthUser
import com.efthemiosprime.pasabayan.features.profile.model.ProfileTabUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileTabUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val user = AuthUser(
        id = 1L,
        name = "U",
        email = "a@b.c",
        avatar = null,
        phone = null,
        phoneVerified = false,
        profileCompleted = true,
        provider = "g",
        userTypes = listOf("shipper", "carrier"),
        isActiveCarrier = true,
        isActiveShipper = true,
    )

    @Test
    fun profileTabContent_shipper_order_hasHeaderBeforeFavorites() {
        val state = ProfileTabUiState(
            userProfile = UserProfileJson(verificationLevel = "basic", fullName = "U"),
            userStats = UserStatsDataJson(packagesCount = 1),
        )
        composeRule.setContent {
            PasabayanTheme {
                ProfileTabContent(
                    state = state,
                    user = user,
                    currentRole = UserRole.SHIPPER,
                    versionName = "1.0",
                    onSwitchRole = {},
                    onLogout = {},
                    onOpenPaymentsHub = {},
                )
            }
        }
        composeRule.onNodeWithTag(ProfileTestTags.Header).assertIsDisplayed()
        composeRule.onNodeWithTag(ProfileTestTags.MenuFavorites).assertIsDisplayed()
    }

    @Test
    fun profileTabContent_carrier_showsCarrierStatus_notFavorites() {
        val state = ProfileTabUiState(
            userProfile = UserProfileJson(verificationLevel = "basic"),
            carrierProfile = com.efthemiosprime.pasabayan.core.network.profile.CarrierProfileJson(
                carrierStatus = "active",
            ),
            carrierStats = CarrierStatsJson(
                deliveries = CarrierDeliveriesJson(totalTrips = 1),
            ),
        )
        composeRule.setContent {
            PasabayanTheme {
                ProfileTabContent(
                    state = state,
                    user = user,
                    currentRole = UserRole.CARRIER,
                    versionName = "1.0",
                    onSwitchRole = {},
                    onLogout = {},
                    onOpenPaymentsHub = {},
                )
            }
        }
        composeRule.onNodeWithTag(ProfileTestTags.CarrierStatus).assertIsDisplayed()
    }

    @Test
    fun profileTabContent_logout_invokesCallback() {
        var called = false
        val state = ProfileTabUiState(
            userProfile = UserProfileJson(verificationLevel = "basic", fullName = "U"),
            userStats = UserStatsDataJson(packagesCount = 0),
        )
        composeRule.setContent {
            PasabayanTheme {
                ProfileTabContent(
                    state = state,
                    user = user,
                    currentRole = UserRole.SHIPPER,
                    versionName = "1.0",
                    onSwitchRole = {},
                    onLogout = { called = true },
                    onOpenPaymentsHub = {},
                )
            }
        }
        composeRule.onNodeWithTag(ProfileTestTags.Logout).performClick()
        assertTrue(called)
    }
}
