package com.efthemiosprime.pasabayan.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.core.designsystem.component.PCardVariant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PasabayanThemeAndroidTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pasabayanTheme_renders_content() {
        composeRule.setContent {
            PasabayanTheme {
                Text("PasabayanSmoke")
            }
        }
        composeRule.onNodeWithText("PasabayanSmoke").assertExists()
    }

    @Test
    fun lightTheme_primary_matches_brand_black() {
        var primary: Color = Color.Unspecified
        composeRule.setContent {
            PasabayanTheme(darkTheme = false) {
                primary = MaterialTheme.colorScheme.primary
                Text("x")
            }
        }
        composeRule.runOnIdle {
            assertEquals(PasabayanColors.PrimaryBlack, primary)
        }
    }

    @Test
    fun bodyLarge_fontSize_is_16sp() {
        var size: TextUnit = 0.sp
        composeRule.setContent {
            PasabayanTheme {
                size = MaterialTheme.typography.bodyLarge.fontSize
                Text("x")
            }
        }
        composeRule.runOnIdle {
            assertEquals(16f, size.value, 0.01f)
        }
    }

    @Test
    fun pButton_click_invokes_callback() {
        var clicks = 0
        composeRule.setContent {
            PasabayanTheme {
                PButton(text = "TapMe", onClick = { clicks++ })
            }
        }
        composeRule.onNodeWithText("TapMe").performClick()
        composeRule.runOnIdle {
            assertEquals(1, clicks)
        }
    }

    @Test
    fun pButton_loading_does_not_invoke_click() {
        var clicks = 0
        composeRule.setContent {
            PasabayanTheme {
                PButton(
                    text = "Load",
                    onClick = { clicks++ },
                    isLoading = true,
                )
            }
        }
        composeRule.onNodeWithText("Load").performClick()
        composeRule.runOnIdle {
            assertEquals(0, clicks)
        }
    }

    @Test
    fun pCard_shows_child_text() {
        composeRule.setContent {
            PasabayanTheme {
                PCard(variant = PCardVariant.Primary) {
                    Text("InsideCard")
                }
            }
        }
        composeRule.onNodeWithText("InsideCard").assertExists()
    }

    @Test
    fun pButton_secondary_shows_label() {
        composeRule.setContent {
            PasabayanTheme {
                PButton(
                    text = "Secondary",
                    onClick = {},
                    style = PButtonStyle.Secondary,
                )
            }
        }
        composeRule.onNodeWithText("Secondary").assertExists()
    }
}
