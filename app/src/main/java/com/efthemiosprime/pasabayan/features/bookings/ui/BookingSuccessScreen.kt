package com.efthemiosprime.pasabayan.features.bookings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton

@Composable
fun BookingSuccessScreen(
    message: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PasabayanSpacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = PasabayanColors.Success,
        )
        Text(
            text = stringResource(R.string.bookings_success_title),
            style = PasabayanTextStyles.Heading.h3,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = PasabayanSpacing.lg),
        )
        Text(
            text = message,
            style = PasabayanTextStyles.Body.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = PasabayanSpacing.sm),
        )
        PButton(
            text = stringResource(R.string.bookings_success_done),
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PasabayanSpacing.xxl),
        )
    }
}

@Preview(showBackground = true, name = "BookingSuccess — light")
@Preview(showBackground = true, name = "BookingSuccess — dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BookingSuccessPreview() {
    PasabayanTheme {
        BookingSuccessScreen(
            message = "Your booking has been confirmed. The carrier will pick up your package soon.",
            onDone = {},
        )
    }
}
