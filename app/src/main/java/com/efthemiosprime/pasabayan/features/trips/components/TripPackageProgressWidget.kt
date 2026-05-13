package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCircularProgress
import com.efthemiosprime.pasabayan.core.designsystem.component.PLinearProgress

enum class TripProgressStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETE,
}

data class ProgressLabel(
    val resId: Int,
    val formatArgs: List<Any> = emptyList(),
)

data class TripPackageProgressMetrics(
    val totalMatches: Int,
    val deliveredMatches: Int,
    val activeMatches: Int,
    val arrivalDateText: String,
) {
    val deliveredRatio: Float
        get() = if (totalMatches > 0) deliveredMatches.toFloat() / totalMatches else 0f

    val deliveredLabel: ProgressLabel
        get() = ProgressLabel(
            resId = R.string.trips_progress_delivered_label,
            formatArgs = listOf(deliveredMatches, totalMatches),
        )

    val status: TripProgressStatus
        get() = when {
            totalMatches <= 0 -> TripProgressStatus.NOT_STARTED
            deliveredMatches >= totalMatches -> TripProgressStatus.COMPLETE
            deliveredMatches > 0 || activeMatches > 0 -> TripProgressStatus.IN_PROGRESS
            else -> TripProgressStatus.NOT_STARTED
        }

    val nextActionLabel: ProgressLabel
        get() = if (activeMatches > 0) {
            ProgressLabel(
                resId = R.string.trips_progress_next_action_active,
                formatArgs = listOf(activeMatches),
            )
        } else {
            ProgressLabel(resId = R.string.trips_progress_next_action_none)
        }
}

/**
 * Flat (no card chrome) — embeds inside a host card (e.g. [TripCard]).
 * iOS parity: `TripPackageProgressComponents.swift:5-71`.
 */
@Composable
fun TripPackageProgressWidget(
    metrics: TripPackageProgressMetrics,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    onDeliveredHistoryTap: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xs),
    ) {
        // Title + status chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.trips_progress_packages),
                style = PasabayanTextStyles.Body.small,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ProgressStatusChip(status = metrics.status)
        }

        Text(
            text = stringResource(
                metrics.deliveredLabel.resId,
                *metrics.deliveredLabel.formatArgs.toTypedArray(),
            ),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurface,
        )

        PLinearProgress(
            progress = metrics.deliveredRatio,
            modifier = Modifier.fillMaxWidth(),
            indicatorColor = PasabayanColors.Success,
        )

        Text(
            text = stringResource(
                metrics.nextActionLabel.resId,
                *metrics.nextActionLabel.formatArgs.toTypedArray(),
            ),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (metrics.arrivalDateText.isNotBlank()) {
            Text(
                text = stringResource(R.string.trips_progress_arrives, metrics.arrivalDateText),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProgressStatusChip(status: TripProgressStatus) {
    val foreground = when (status) {
        TripProgressStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
        TripProgressStatus.IN_PROGRESS -> PasabayanColors.Warning
        TripProgressStatus.COMPLETE -> PasabayanColors.Success
    }
    val background = when (status) {
        TripProgressStatus.NOT_STARTED -> MaterialTheme.colorScheme.surfaceVariant
        TripProgressStatus.IN_PROGRESS -> PasabayanColors.Warning.copy(alpha = 0.15f)
        TripProgressStatus.COMPLETE -> PasabayanColors.Success.copy(alpha = 0.15f)
    }
    val label = when (status) {
        TripProgressStatus.NOT_STARTED -> stringResource(R.string.trips_progress_status_not_started)
        TripProgressStatus.IN_PROGRESS -> stringResource(R.string.trips_progress_status_in_progress)
        TripProgressStatus.COMPLETE -> stringResource(R.string.trips_progress_status_complete)
    }
    Text(
        text = label,
        style = PasabayanTextStyles.Caption.regular,
        fontWeight = FontWeight.Medium,
        color = foreground,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(background)
            .padding(horizontal = PasabayanSpacing.sm, vertical = 2.dp),
    )
}

@Composable
fun TripPackageProgressSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        PCircularProgress()
    }
}

@Composable
fun TripPackageProgressEmpty(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.trips_empty_no_packages),
        style = PasabayanTextStyles.Body.small,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
fun TripPackageProgressError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.trips_progress_unavailable),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = message,
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.error,
        )
        PButton(
            text = stringResource(com.efthemiosprime.pasabayan.core.designsystem.R.string.ds_retry),
            onClick = onRetry,
            style = PButtonStyle.Secondary,
            size = PButtonSize.Small,
        )
    }
}

@Preview(showBackground = true, name = "TripProgress — in progress, light")
@Preview(showBackground = true, name = "TripProgress — in progress, dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripPackageProgressInProgressPreview() {
    PasabayanTheme {
        TripPackageProgressWidget(
            metrics = TripPackageProgressMetrics(
                totalMatches = 2,
                deliveredMatches = 1,
                activeMatches = 1,
                arrivalDateText = "Mar 1, 2026",
            ),
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "TripProgress — complete")
@Composable
private fun TripPackageProgressCompletePreview() {
    PasabayanTheme {
        TripPackageProgressWidget(
            metrics = TripPackageProgressMetrics(
                totalMatches = 3,
                deliveredMatches = 3,
                activeMatches = 0,
                arrivalDateText = "Apr 4, 2026",
            ),
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}

@Preview(showBackground = true, name = "TripProgress — not started")
@Composable
private fun TripPackageProgressNotStartedPreview() {
    PasabayanTheme {
        TripPackageProgressWidget(
            metrics = TripPackageProgressMetrics(
                totalMatches = 2,
                deliveredMatches = 0,
                activeMatches = 0,
                arrivalDateText = "Apr 4, 2026",
            ),
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
