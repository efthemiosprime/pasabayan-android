package com.efthemiosprime.pasabayan.features.bookings.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PCard
import com.efthemiosprime.pasabayan.features.bookings.model.IncomingRequestContext

object IncomingRequestSnackbarTestTags {
    const val Root = "incoming_request_snackbar"
    const val Title = "incoming_request_snackbar_title"
    const val Dismiss = "incoming_request_snackbar_dismiss"
}

/**
 * Single review-section item rendered above the match list (see
 * `android-spec/05-bookings-matches.md` § "Snackbar section"). Tapping the body opens the
 * full match detail; the trailing close icon dismisses the snackbar locally for the rest
 * of the session via [onDismiss] → `MatchingViewModel.markIncomingRequestReviewed`.
 *
 * Built from `PCard` + DS tokens — no Material `Snackbar` (the spec name predates the
 * decision to render as a stack of cards). Compact-density variant of a match summary,
 * not a full `MatchCard`.
 *
 * **Android-only surface** — no iOS counterpart.
 *
 * @param isCarrier `true` when the user is in carrier mode (incoming = shipper requests);
 *   `false` for shipper mode (incoming = carrier offers). Drives the title copy.
 */
@Composable
fun IncomingRequestSnackbar(
    item: IncomingRequestContext,
    isCarrier: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(IncomingRequestSnackbarTestTags.Root),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(end = PasabayanSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = PasabayanColors.BadgeBlue,
                modifier = Modifier.size(20.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(titleRes(isCarrier), item.requesterName),
                    style = PasabayanTextStyles.Body.medium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(IncomingRequestSnackbarTestTags.Title),
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(IncomingRequestSnackbarTestTags.Dismiss),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.bookings_incoming_snackbar_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun titleRes(isCarrier: Boolean): Int =
    if (isCarrier) {
        R.string.bookings_incoming_snackbar_title_shipper_to_carrier
    } else {
        R.string.bookings_incoming_snackbar_title_carrier_to_shipper
    }

@Preview(showBackground = true, name = "IncomingRequestSnackbar — carrier light")
@Preview(
    showBackground = true,
    name = "IncomingRequestSnackbar — carrier dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun IncomingRequestSnackbarCarrierPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            IncomingRequestSnackbar(
                item = IncomingRequestContext(matchId = 42, requesterName = "Jane Shipper"),
                isCarrier = true,
                onOpen = {},
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "IncomingRequestSnackbar — shipper light")
@Preview(
    showBackground = true,
    name = "IncomingRequestSnackbar — shipper dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun IncomingRequestSnackbarShipperPreview() {
    PasabayanTheme {
        Column(
            modifier = Modifier.padding(PasabayanSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            IncomingRequestSnackbar(
                item = IncomingRequestContext(
                    matchId = 17,
                    requesterName = "Carlos Carrier with a Very Long Name That Should Truncate",
                ),
                isCarrier = false,
                onOpen = {},
                onDismiss = {},
            )
        }
    }
}
