package com.efthemiosprime.pasabayan.features.trips.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
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

@Composable
fun TripPackageProgressWidget(
    metrics: TripPackageProgressMetrics,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        metrics.deliveredLabel.resId,
                        *metrics.deliveredLabel.formatArgs.toTypedArray(),
                    ),
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = metrics.arrivalDateText,
                    style = PasabayanTextStyles.Caption.regular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
                style = PasabayanTextStyles.Caption.large,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun TripPackageProgressSkeleton(modifier: Modifier = Modifier) {
    PCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            PCircularProgress()
        }
    }
}

@Composable
fun TripPackageProgressEmpty(modifier: Modifier = Modifier) {
    PCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.trips_empty_no_packages),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun TripPackageProgressError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
            Text(
                text = message,
                style = PasabayanTextStyles.Body.small,
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
}

@Preview(showBackground = true, name = "TripProgress — light")
@Preview(showBackground = true, name = "TripProgress — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripPackageProgressPreview() {
    PasabayanTheme {
        TripPackageProgressWidget(
            metrics = TripPackageProgressMetrics(
                totalMatches = 5,
                deliveredMatches = 3,
                activeMatches = 2,
                arrivalDateText = "Apr 1, 2026",
            ),
            modifier = Modifier.padding(PasabayanSpacing.lg),
        )
    }
}
