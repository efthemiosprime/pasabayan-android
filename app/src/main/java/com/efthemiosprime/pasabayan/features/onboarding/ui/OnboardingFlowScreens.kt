package com.efthemiosprime.pasabayan.features.onboarding.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.core.os.LocaleListCompat
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanBorder
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanRadius
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.dsShadowOnboardingCard
import com.efthemiosprime.pasabayan.features.onboarding.components.OnboardingFloatingIconsBackground
import com.efthemiosprime.pasabayan.features.onboarding.model.JourneyStepDefinition
import com.efthemiosprime.pasabayan.features.onboarding.model.JourneyStepDefinitions
import com.efthemiosprime.pasabayan.features.onboarding.model.OnboardingRole

@Composable
fun OnboardingRoleSelectionScreen(
    hasViewedCarrier: Boolean,
    hasViewedSender: Boolean,
    onSelectCarrier: () -> Unit,
    onSelectSender: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PasabayanColors.OnboardingPrimary
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = PasabayanSpacing.lg),
    ) {
        OnboardingFloatingIconsBackground(
            icons = JourneyStepDefinitions.roleSelectionFloatingIcons,
            accentColor = accent,
            modifier = Modifier.matchParentSize(),
        )
        // fillMaxSize + verticalScroll breaks measurement (IDE preview shows "Render problem").
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                OnboardingLanguageToggle()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PasabayanSpacing.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_welcome_title),
                    style = PasabayanTextStyles.Heading.h1.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.onboarding_welcome_subtitle),
                    style = PasabayanTextStyles.Body.large,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OnboardingRoleCard(
                    title = stringResource(R.string.onboarding_role_carrier_title),
                    description = stringResource(R.string.onboarding_role_carrier_description),
                    tagLabels = listOf(
                        stringResource(R.string.onboarding_tag_earnincome),
                        stringResource(R.string.onboarding_tag_flexible),
                        stringResource(R.string.onboarding_tag_travel),
                    ),
                    icon = Icons.Outlined.DirectionsCar,
                    accent = OnboardingRole.Carrier.accentColor,
                    hasViewed = hasViewedCarrier,
                    onClick = onSelectCarrier,
                )
                OnboardingRoleCard(
                    title = stringResource(R.string.onboarding_role_sender_title),
                    description = stringResource(R.string.onboarding_role_sender_description),
                    tagLabels = listOf(
                        stringResource(R.string.onboarding_tag_fastdelivery),
                        stringResource(R.string.onboarding_tag_affordable),
                        stringResource(R.string.onboarding_tag_secure),
                    ),
                    icon = Icons.Outlined.Inventory2,
                    accent = OnboardingRole.Shipper.accentColor,
                    hasViewed = hasViewedSender,
                    onClick = onSelectSender,
                )
            }
            Text(
                text = stringResource(R.string.onboarding_welcome_footer),
                style = PasabayanTextStyles.Body.small.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = PasabayanSpacing.lg),
            )
        }
    }
}

@Composable
private fun OnboardingLanguageToggle() {
    val lang = LocalConfiguration.current.locales[0]?.language ?: "en"
    Surface(
        onClick = {
            val next = if (lang == "fr") {
                LocaleListCompat.forLanguageTags("en")
            } else {
                LocaleListCompat.forLanguageTags("fr")
            }
            AppCompatDelegate.setApplicationLocales(next)
        },
        shape = RoundedCornerShape(16.dp),
        color = PasabayanColors.OnboardingPrimary.copy(alpha = 0.1f),
        modifier = Modifier.heightIn(min = 36.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = PasabayanColors.OnboardingPrimary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = if (lang == "en") {
                    stringResource(R.string.onboarding_language_switch_label_fr)
                } else {
                    stringResource(R.string.onboarding_language_switch_label_en)
                },
                style = PasabayanTextStyles.Body.medium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = PasabayanColors.OnboardingPrimary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingRoleCard(
    title: String,
    description: String,
    tagLabels: List<String>,
    icon: ImageVector,
    accent: Color,
    hasViewed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(PasabayanRadius.lg)
    val borderColor = if (hasViewed) {
        PasabayanColors.Success.copy(alpha = 0.3f)
    } else {
        accent.copy(alpha = 0.2f)
    }
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .dsShadowOnboardingCard(shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(PasabayanBorder.widthStrong, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(PasabayanSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = PasabayanTextStyles.Heading.h5.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = PasabayanTextStyles.Body.small.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tagLabels.forEach { tag ->
                        OnboardingSmallTag(text = tag, accent = accent)
                    }
                }
            }
            if (hasViewed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PasabayanColors.Success,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun OnboardingSmallTag(text: String, accent: Color) {
    val shape = RoundedCornerShape(10.dp)
    Surface(
        shape = shape,
        color = accent.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
    ) {
        Text(
            text = text,
            style = PasabayanTextStyles.Caption.large.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun OnboardingJourneyScreen(
    definition: JourneyStepDefinition,
    role: OnboardingRole,
    stepIndex: Int,
    totalSteps: Int,
    isLastStep: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onSeeSummary: () -> Unit,
    onSkipToApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = role.accentColor
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        OnboardingFloatingIconsBackground(
            icons = definition.floatingIcons,
            accentColor = accent,
            modifier = Modifier.matchParentSize(),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OnboardingJourneyTopBar(
                currentStepIndex = stepIndex,
                totalSteps = totalSteps,
                accent = accent,
                onBack = onBack,
                onSkip = onSkip,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        if (role == OnboardingRole.Carrier) {
                            R.string.onboarding_badge_forcarriers
                        } else {
                            R.string.onboarding_badge_forsenders
                        },
                    ),
                    style = PasabayanTextStyles.Caption.large.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                    ),
                    color = accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(role.accentLightColor())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
                Spacer(modifier = Modifier.height(20.dp))
                OnboardingStepNumberBadge(
                    number = definition.stepNumber,
                    accent = accent,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Icon(
                    imageVector = definition.mainIcon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(definition.titleRes),
                    style = PasabayanTextStyles.Heading.h2.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = PasabayanSpacing.screenPadding),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(definition.descriptionRes),
                    style = PasabayanTextStyles.Body.large,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(20.dp))
                OnboardingFeatureTagsFlow(
                    tags = definition.tagResIds.map { stringResource(it) },
                    accent = accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (isLastStep) {
                    OnboardingAccentButton(
                        text = stringResource(R.string.onboarding_nav_seesummary),
                        accent = accent,
                        onClick = onSeeSummary,
                    )
                    TextButton(onClick = onSkipToApp) {
                        Text(
                            text = stringResource(R.string.onboarding_nav_skiptoapp),
                            style = PasabayanTextStyles.Body.medium.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    OnboardingAccentButton(
                        text = stringResource(R.string.onboarding_nav_nextstep),
                        accent = accent,
                        onClick = onNext,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.onboarding_nav_stepof,
                        definition.stepNumber,
                        totalSteps,
                    ),
                    style = PasabayanTextStyles.Caption.large.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OnboardingJourneyTopBar(
    currentStepIndex: Int,
    totalSteps: Int,
    accent: Color,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(horizontal = PasabayanSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.onboarding_nav_back),
                style = PasabayanTextStyles.Body.medium.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
        ) {
            OnboardingProgressDots(
                totalSteps = totalSteps,
                currentStepIndex = currentStepIndex,
                accent = accent,
            )
        }
        TextButton(onClick = onSkip) {
            Text(
                text = stringResource(R.string.onboarding_nav_skip),
                style = PasabayanTextStyles.Body.medium.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OnboardingProgressDots(
    totalSteps: Int,
    currentStepIndex: Int,
    accent: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val w = if (index == currentStepIndex) 24.dp else 6.dp
            val color = when {
                index == currentStepIndex -> accent
                index < currentStepIndex -> accent.copy(alpha = 0.5f)
                else -> PasabayanColors.Border
            }
            Box(
                modifier = Modifier
                    .width(w)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun OnboardingStepNumberBadge(number: Int, accent: Color) {
    val shape = RoundedCornerShape(PasabayanRadius.lg)
    Box(
        modifier = Modifier
            .size(60.dp)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                spotColor = accent.copy(alpha = 0.3f),
                ambientColor = accent.copy(alpha = 0.15f),
            )
            .clip(shape)
            .background(accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = PasabayanTextStyles.Heading.h2.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = contentColorFor(accent),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingFeatureTagsFlow(
    tags: List<String>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accent.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
            ) {
                Text(
                    text = tag,
                    style = PasabayanTextStyles.Body.medium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = accent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun OnboardingAccentButton(
    text: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(PasabayanRadius.md)
    Surface(
        onClick = onClick,
        shape = shape,
        color = accent,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = PasabayanTextStyles.Button.large.copy(fontSize = 16.sp),
                color = contentColorFor(accent),
            )
        }
    }
}

@Composable
fun OnboardingCompletionScreen(
    completedRole: OnboardingRole,
    hasViewedOppositeJourney: Boolean,
    hasViewedBothJourneys: Boolean,
    onContinueToApp: () -> Unit,
    onExploreOtherRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val other = completedRole.opposite
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(PasabayanColors.Success.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PasabayanColors.Success,
                    modifier = Modifier.size(56.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_completion_title),
            style = PasabayanTextStyles.Heading.h1.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.onboarding_completion_description,
                stringResource(completedRole.displayNameRes),
            ),
            style = PasabayanTextStyles.Body.large,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OnboardingCompletionActionCard(
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                iconColor = PasabayanColors.OnboardingPrimary,
                title = stringResource(R.string.onboarding_completion_continuetitle),
                description = stringResource(R.string.onboarding_completion_continuedescription),
                onClick = onContinueToApp,
            )
            if (!hasViewedOppositeJourney) {
                val otherLabel = stringResource(other.displayNameRes)
                OnboardingCompletionActionCard(
                    icon = if (other == OnboardingRole.Carrier) Icons.Outlined.DirectionsCar else Icons.Outlined.Inventory2,
                    iconColor = other.accentColor,
                    title = stringResource(R.string.onboarding_completion_exploretitle, otherLabel),
                    description = stringResource(R.string.onboarding_completion_exploredescription),
                    onClick = onExploreOtherRole,
                )
            }
            if (hasViewedBothJourneys) {
                OnboardingAllCaughtUpBadge()
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnboardingCompletionActionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(PasabayanRadius.lg)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .dsShadowOnboardingCard(shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(PasabayanBorder.width, PasabayanColors.Border),
    ) {
        Row(
            modifier = Modifier.padding(PasabayanSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = PasabayanTextStyles.Body.large.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = PasabayanTextStyles.Caption.large.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun OnboardingAllCaughtUpBadge() {
    val shape = RoundedCornerShape(PasabayanRadius.lg)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PasabayanColors.OnboardingPrimary.copy(alpha = 0.1f))
            .border(
                width = PasabayanBorder.widthStrong,
                color = PasabayanColors.OnboardingPrimary.copy(alpha = 0.3f),
                shape = shape,
            )
            .padding(PasabayanSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = null,
                tint = PasabayanColors.OnboardingPrimary,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.onboarding_completion_alldonetitle),
                    style = PasabayanTextStyles.Body.large.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.onboarding_completion_alldonedescription),
                    style = PasabayanTextStyles.Caption.large.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(PasabayanColors.Border),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(999.dp))
                    .background(PasabayanColors.OnboardingPrimary),
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "Role selection — light",
    widthDp = 360,
    heightDp = 800,
)
@Preview(
    showBackground = true,
    name = "Role selection — dark",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingRoleSelectionScreenPreview() {
    PasabayanTheme {
        OnboardingRoleSelectionScreen(
            hasViewedCarrier = false,
            hasViewedSender = true,
            onSelectCarrier = {},
            onSelectSender = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Journey — carrier step 1 — light",
    widthDp = 360,
    heightDp = 800,
)
@Preview(
    showBackground = true,
    name = "Journey — carrier step 1 — dark",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingJourneyScreenPreview() {
    PasabayanTheme {
        OnboardingJourneyScreen(
            definition = JourneyStepDefinitions.stepsFor(OnboardingRole.Carrier).first(),
            role = OnboardingRole.Carrier,
            stepIndex = 0,
            totalSteps = 4,
            isLastStep = false,
            onBack = {},
            onSkip = {},
            onNext = {},
            onSeeSummary = {},
            onSkipToApp = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Journey — last step — light",
    widthDp = 360,
    heightDp = 800,
)
@Preview(
    showBackground = true,
    name = "Journey — last step — dark",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingJourneyScreenLastStepPreview() {
    val defs = JourneyStepDefinitions.stepsFor(OnboardingRole.Shipper)
    PasabayanTheme {
        OnboardingJourneyScreen(
            definition = defs.last(),
            role = OnboardingRole.Shipper,
            stepIndex = defs.lastIndex,
            totalSteps = defs.size,
            isLastStep = true,
            onBack = {},
            onSkip = {},
            onNext = {},
            onSeeSummary = {},
            onSkipToApp = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Completion — explore other role — light",
    widthDp = 360,
    heightDp = 800,
)
@Preview(
    showBackground = true,
    name = "Completion — explore other role — dark",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingCompletionScreenPreview() {
    PasabayanTheme {
        OnboardingCompletionScreen(
            completedRole = OnboardingRole.Shipper,
            hasViewedOppositeJourney = false,
            hasViewedBothJourneys = false,
            onContinueToApp = {},
            onExploreOtherRole = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Completion — all journeys — light",
    widthDp = 360,
    heightDp = 800,
)
@Preview(
    showBackground = true,
    name = "Completion — all journeys — dark",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingCompletionScreenAllDonePreview() {
    PasabayanTheme {
        OnboardingCompletionScreen(
            completedRole = OnboardingRole.Carrier,
            hasViewedOppositeJourney = true,
            hasViewedBothJourneys = true,
            onContinueToApp = {},
            onExploreOtherRole = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
