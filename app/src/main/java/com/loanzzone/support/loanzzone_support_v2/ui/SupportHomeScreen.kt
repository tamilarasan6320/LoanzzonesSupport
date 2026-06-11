package com.loanzzone.support.loanzzone_support_v2.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loanzzone.support.loanzzone_support_v2.R
import com.loanzzone.support.loanzzone_support_v2.data.ContactRow
import com.loanzzone.support.loanzzone_support_v2.data.SupportRepository
import com.loanzzone.support.loanzzone_support_v2.data.SupportTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun filterContactRows(rows: List<ContactRow>, query: String): List<ContactRow> {
    if (query.isBlank()) return rows
    return rows.filter { r ->
        fun matches(s: String) = s.contains(query, ignoreCase = true)
        val lf = r.loanFields
        if (lf != null) {
            matches(lf.bankNbfc) || matches(lf.companyName) || matches(lf.code)
        } else {
            matches(r.title) || matches(r.subtitle)
        }
    }
}

@Composable
private fun SupportSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                shape = RoundedCornerShape(28.dp),
            ),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    stringResource(R.string.search_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun tabAccentColor(tab: SupportTab): Color = when (tab) {
    SupportTab.BanksSupport -> MaterialTheme.colorScheme.primary
    SupportTab.PersonalLoan -> MaterialTheme.colorScheme.tertiary
    SupportTab.BusinessLoan -> MaterialTheme.colorScheme.secondary
    SupportTab.ClientHomeLoan -> Color(0xFF7B1FA2)
    SupportTab.ClientLap -> Color(0xFF00695C)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SupportHomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val baseUrl = stringResource(R.string.api_base_url)
    val repo = remember(baseUrl) { SupportRepository(baseUrl = baseUrl) }
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { SupportTab.entries.size },
    )
    val rowsByTab = remember { mutableStateMapOf<SupportTab, List<ContactRow>>() }
    val errorByTab = remember { mutableStateMapOf<SupportTab, String>() }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showShareAppSheet by remember { mutableStateOf(false) }
    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(pagerState.settledPage) {
        val tab = SupportTab.entries[pagerState.settledPage]
        query = ""
        errorByTab.remove(tab)
        loading = true
        val result = withContext(Dispatchers.IO) { repo.fetchForTab(tab) }
        loading = false
        result.fold(
            onSuccess = { rowsByTab[tab] = it },
            onFailure = {
                rowsByTab[tab] = emptyList()
                errorByTab[tab] = it.message ?: context.getString(R.string.load_error)
            },
        )
    }

    val activeTab = SupportTab.entries[pagerState.settledPage]
    val error = errorByTab[activeTab]

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.loanzzones_logo_full),
                    contentDescription = stringResource(R.string.cd_loanzzones_logo),
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(max = 180.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    onClick = { showShareAppSheet = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.cd_share_app),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.share_app_top_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                CatSearchBanner(
                    onOpenPlay = { openCatSearchPlayStore(context) },
                    modifier = Modifier.padding(top = 12.dp, bottom = 10.dp),
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 1.dp,
                ) {
                    PrimaryScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        edgePadding = 10.dp,
                        divider = {},
                    ) {
                        SupportTab.entries.forEachIndexed { index, item ->
                            val selected = index == pagerState.currentPage
                            Tab(
                                selected = selected,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.78f,
                                ),
                                text = {
                                    Text(
                                        text = item.label,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    )
                                },
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    SupportSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (error != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp),
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) { page ->
                        TabPagerPage(
                            page = page,
                            query = query,
                            rowsByTab = rowsByTab,
                            loading = loading,
                            settledPage = pagerState.settledPage,
                            context = context,
                        )
                    }
                }
            }
        }

        if (showShareAppSheet) {
            ModalBottomSheet(
                onDismissRequest = { showShareAppSheet = false },
                sheetState = shareSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                ShareAppSheetContent(
                    appLink = "https://play.google.com/store/apps/details?id=${context.packageName}",
                    onShare = { link ->
                        shareAppLink(context, link)
                        showShareAppSheet = false
                    },
                    onCopy = { link ->
                        copyAppLinkToClipboard(context, link)
                    },
                )
            }
        }
    }
}

@Composable
private fun ShareAppSheetContent(
    appLink: String,
    onShare: (String) -> Unit,
    onCopy: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.share_app_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.share_app_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
        ) {
            Text(
                text = appLink,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { onCopy(appLink) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.share_app_copy_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = { onShare(appLink) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.share_app_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun shareAppLink(context: android.content.Context, link: String) {
    val body = context.getString(R.string.share_app_message, link)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(
        Intent.createChooser(send, context.getString(R.string.share_app_chooser_title)),
    )
}

private fun copyAppLinkToClipboard(context: android.content.Context, link: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), link))
    Toast.makeText(context, context.getString(R.string.share_app_link_copied), Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabPagerPage(
    page: Int,
    query: String,
    rowsByTab: Map<SupportTab, List<ContactRow>>,
    loading: Boolean,
    settledPage: Int,
    context: android.content.Context,
) {
    val pageTab = SupportTab.entries[page]
    val pageRows = rowsByTab[pageTab] ?: emptyList()
    val pageFiltered = filterContactRows(pageRows, query)
    val accentColor = tabAccentColor(pageTab)

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading && settledPage == page -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 4.dp,
                    )
                }
            }

            pageFiltered.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (query.isNotBlank()) "No results for \"$query\"" else "No data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp),
                ) {
                    items(
                        items = pageFiltered,
                        key = { "${pageTab.ordinal}_${it.stableId}" },
                    ) { row ->
                        ContactCard(
                            row = row,
                            accentColor = accentColor,
                            onCall = { digits ->
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${Uri.encode(digits)}")
                                }
                                context.startActivity(intent)
                            },
                            onShare = pageTab.loanCategory?.let {
                                {
                                    shareContact(
                                        context = context,
                                        sectionLabel = pageTab.label,
                                        row = row,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun openCatSearchPlayStore(context: android.content.Context) {
    val pkg = "com.greymatter.blms"
    val web = Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
    val market = Uri.parse("market://details?id=$pkg")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, market))
    } catch (_: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, web))
    }
}

/** Full-width promo image; tap opens Play Store for `com.greymatter.blms`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatSearchBanner(
    onOpenPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onOpenPlay,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        shadowElevation = 1.dp,
    ) {
        Image(
            painter = painterResource(R.drawable.promo_cat_search_banner),
            contentDescription = stringResource(R.string.cd_promo_cat_search_banner),
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
    }
}

private fun shareContact(
    context: android.content.Context,
    sectionLabel: String,
    row: ContactRow,
) {
    val body = buildString {
        append(sectionLabel)
        append('\n')
        val lf = row.loanFields
        if (lf != null) {
            append(context.getString(R.string.share_line_loan_type))
            append(sectionLabel.trim())
            append("\n\n")
            append(context.getString(R.string.share_line_bank_nbfc))
            append(' ')
            append(lf.bankNbfc.trim())
            append("\n\n")
            append(context.getString(R.string.share_line_channel_name))
            append(' ')
            append(lf.companyName.trim())
            append("\n\n")
            append(context.getString(R.string.share_line_channel_code))
            append(' ')
            append(lf.code.trim())
        } else {
            append(row.title)
            if (row.subtitle.isNotBlank()) {
                append('\n')
                append(row.subtitle)
            }
        }
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_SUBJECT,
            context.getString(R.string.share_subject, sectionLabel),
        )
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(
        Intent.createChooser(send, context.getString(R.string.share_chooser_title)),
    )
}

@Composable
private fun LoanDetailSection(
    label: String,
    value: String,
    accentColor: Color,
    showDividerBelow: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accentColor.copy(alpha = 0.75f),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 3.dp),
        )
        if (showDividerBelow) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactCard(
    row: ContactRow,
    accentColor: Color,
    onCall: (String) -> Unit,
    onShare: (() -> Unit)?,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            // Left accent strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                val lf = row.loanFields
                if (lf != null) {
                    LoanDetailSection(
                        label = stringResource(R.string.card_label_bank),
                        value = lf.bankNbfc,
                        accentColor = accentColor,
                        showDividerBelow = true,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LoanDetailSection(
                        label = stringResource(R.string.card_label_company),
                        value = lf.companyName,
                        accentColor = accentColor,
                        showDividerBelow = true,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Code as a tinted badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.card_label_code).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor.copy(alpha = 0.75f),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = lf.code,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                } else {
                    Text(
                        text = row.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                    )
                    if (row.subtitle.isNotBlank()) {
                        Text(
                            text = row.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                // Action row — call + share buttons
                val dial = row.dialDigits
                if (dial != null || onShare != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (onShare != null) {
                            OutlinedButton(
                                onClick = onShare,
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = accentColor,
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_share_vector),
                                    contentDescription = stringResource(R.string.cd_share),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        if (dial != null) {
                            if (onShare != null) Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                onClick = { onCall(dial) },
                                shape = CircleShape,
                                color = accentColor,
                                contentColor = Color.White,
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 18.dp,
                                        vertical = 10.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_phone_white_vector),
                                        contentDescription = stringResource(R.string.cd_call),
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Call",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
