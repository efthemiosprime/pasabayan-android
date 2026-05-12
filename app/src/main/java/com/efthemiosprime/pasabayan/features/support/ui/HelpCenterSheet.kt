package com.efthemiosprime.pasabayan.features.support.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanColors
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PDetailSheetCard
import com.efthemiosprime.pasabayan.features.support.model.HelpArticle
import com.efthemiosprime.pasabayan.features.support.services.HelpArticleCatalog
import android.content.Intent

/**
 * Help Center landing page — iOS parity with `HelpCenterView`. Renders a Popular
 * Articles list (each tap opens the HTML article via [ArticleDetailSheet]) and a
 * Contact Support section with Live Chat / Submit Request / Phone Support rows,
 * then a footer mailto link. Host inside a `PModalBottomSheet`.
 *
 * Submitting a support request hands off to the parent via [onOpenSupportTicketForm]
 * so the existing [SupportTicketFormScreen] sheet remains the single source of truth
 * for the ticket flow.
 */
@Composable
fun HelpCenterSheet(
    onClose: () -> Unit,
    onOpenArticle: (HelpArticle) -> Unit,
    onOpenSupportTicketForm: () -> Unit,
    modifier: Modifier = Modifier,
    articles: List<HelpArticle> = HelpArticleCatalog.popularArticles,
) {
    val context = LocalContext.current
    val phoneNumber = stringResource(R.string.support_help_center_phone_number)
    val footerEmail = stringResource(R.string.support_help_center_footer_email)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TitleBar(onClose = onClose)
        Column(
            modifier = Modifier.padding(PasabayanSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.xl),
        ) {
            PopularArticlesSection(
                articles = articles,
                onOpenArticle = onOpenArticle,
            )
            ContactSupportSection(
                onLiveChat = {
                    // TODO: Open live chat — iOS currently no-ops here too.
                },
                onSubmitRequest = onOpenSupportTicketForm,
                onPhoneSupport = {
                    runCatching {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri())
                        context.startActivity(intent)
                    }
                },
            )
            HelpCenterFooter(
                onEmailTap = {
                    runCatching {
                        val intent = Intent(Intent.ACTION_SENDTO, "mailto:$footerEmail".toUri())
                        context.startActivity(intent)
                    }
                },
            )
        }
    }
}

@Composable
private fun TitleBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = PasabayanSpacing.screenPadding,
                vertical = PasabayanSpacing.sm,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.support_help_center_title),
            style = PasabayanTextStyles.Heading.h4,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.support_help_center_close),
                tint = PasabayanColors.Info,
            )
        }
    }
}

@Composable
private fun PopularArticlesSection(
    articles: List<HelpArticle>,
    onOpenArticle: (HelpArticle) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        Text(
            text = stringResource(R.string.support_help_center_popular_articles),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            articles.forEachIndexed { index, article ->
                ArticleRow(article = article, onClick = { onOpenArticle(article) })
                if (index < articles.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 50.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(
    article: HelpArticle,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = article.icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(article.titleRes),
                style = PasabayanTextStyles.Body.medium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(article.readingTimeRes),
                style = PasabayanTextStyles.Caption.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContactSupportSection(
    onLiveChat: () -> Unit,
    onSubmitRequest: () -> Unit,
    onPhoneSupport: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.md)) {
        Text(
            text = stringResource(R.string.support_help_center_contact_support),
            style = PasabayanTextStyles.Heading.h5,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        PDetailSheetCard(modifier = Modifier.fillMaxWidth()) {
            SupportOptionRow(
                icon = Icons.Filled.QuestionAnswer,
                title = stringResource(R.string.support_help_center_live_chat_title),
                badge = stringResource(R.string.support_help_center_badge_available),
                subtitle = stringResource(R.string.support_help_center_live_chat_subtitle),
                onClick = onLiveChat,
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 50.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            SupportOptionRow(
                icon = Icons.Filled.Email,
                title = stringResource(R.string.support_help_center_submit_title),
                badge = stringResource(R.string.support_help_center_badge_available),
                subtitle = stringResource(R.string.support_help_center_submit_subtitle),
                onClick = onSubmitRequest,
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 50.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            SupportOptionRow(
                icon = Icons.Filled.Phone,
                title = stringResource(R.string.support_help_center_phone_title),
                badge = null,
                subtitle = stringResource(R.string.support_help_center_phone_subtitle),
                onClick = onPhoneSupport,
            )
        }
    }
}

@Composable
private fun SupportOptionRow(
    icon: ImageVector,
    title: String,
    badge: String?,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PasabayanSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                badge?.let { Badge(text = it) }
            }
            Text(
                text = subtitle,
                style = PasabayanTextStyles.Caption.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = PasabayanTextStyles.Caption.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun HelpCenterFooter(onEmailTap: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PasabayanSpacing.md),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = stringResource(R.string.support_help_center_footer),
            style = PasabayanTextStyles.Caption.regular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.support_help_center_footer_email),
            style = PasabayanTextStyles.Caption.regular,
            color = PasabayanColors.Info,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onEmailTap),
        )
        Spacer(modifier = Modifier.height(PasabayanSpacing.md))
    }
}

@Preview(showBackground = true, name = "HelpCenter — light", heightDp = 1100)
@Preview(
    showBackground = true,
    name = "HelpCenter — dark",
    heightDp = 1100,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HelpCenterSheetPreview() {
    PasabayanTheme {
        HelpCenterSheet(
            onClose = {},
            onOpenArticle = {},
            onOpenSupportTicketForm = {},
        )
    }
}
