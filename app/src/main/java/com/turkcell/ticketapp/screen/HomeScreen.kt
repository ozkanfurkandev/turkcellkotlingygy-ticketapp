package com.turkcell.ticketapp.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.ticket.TicketStatus
import com.turkcell.core.domain.ticket.UserTicket
import com.turkcell.core.ui.theme.SuccessGreen
import com.turkcell.core.ui.theme.WarnAmber
import com.turkcell.core.util.formatEventDate
import com.turkcell.core.util.formatEventDateShort
import com.turkcell.core.util.formatPriceCents
import com.turkcell.core.util.formatPriceRangeCents
import com.turkcell.ticketapp.R
import com.turkcell.ticketapp.viewmodel.HomeTab
import com.turkcell.ticketapp.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

private val gradientPalette = listOf(
    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
    listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
    listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4)),
    listOf(Color(0xFF10B981), Color(0xFF059669)),
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onTicketClick: (String) -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToStaff: () -> Unit,
    showStaffEntry: Boolean = false,
    openTicketsTab: Boolean = false,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(openTicketsTab) {
        if (openTicketsTab) {
            viewModel.openTicketsTab()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                actions = {
                    IconButton(onClick = onNavigateToPurchases) {
                        Icon(Icons.Default.Receipt, contentDescription = stringResource(R.string.tab_purchases))
                    }
                    if (showStaffEntry) {
                        IconButton(onClick = onNavigateToStaff) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = stringResource(R.string.staff_checkin),
                            )
                        }
                    }
                    IconButton(onClick = viewModel::logout, enabled = !state.isLoggingOut) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout))
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HomeTabRow(
                selectedTab = state.selectedTab,
                eventsCount = state.events.size,
                ticketsCount = state.tickets.size,
                onTabSelected = viewModel::onTabSelected,
            )

            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "home_tab_content",
            ) { tab ->
                when (tab) {
                    HomeTab.Events -> EventsTabContent(
                        isLoading = state.isEventsLoading,
                        error = state.eventsError,
                        events = state.events,
                        onRetry = { viewModel.loadEvents(force = true) },
                        onEventClick = onEventClick,
                        isRefreshing = state.isEventsLoading,
                        onRefresh = { viewModel.loadEvents(force = true) },
                    )

                    HomeTab.Tickets -> TicketsTabContent(
                        isLoading = state.isTicketsLoading,
                        error = state.ticketsError,
                        tickets = state.tickets,
                        onRetry = { viewModel.loadTickets(force = true) },
                        onTicketClick = onTicketClick,
                        isRefreshing = state.isTicketsLoading,
                        onRefresh = { viewModel.loadTickets(force = true) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTabRow(
    selectedTab: HomeTab,
    eventsCount: Int,
    ticketsCount: Int,
    onTabSelected: (HomeTab) -> Unit,
) {
    val tabs = listOf(
        HomeTab.Events to "${stringResource(R.string.tab_events)} ($eventsCount)",
        HomeTab.Tickets to "${stringResource(R.string.tab_tickets)} ($ticketsCount)",
    )

    PrimaryTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = label,
                        fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

@Composable
private fun EventsTabContent(
    isLoading: Boolean,
    error: String?,
    events: List<Event>,
    onRetry: () -> Unit,
    onEventClick: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            isLoading && events.isEmpty() -> LoadingState(message = stringResource(R.string.loading_events))
            error != null && events.isEmpty() -> ErrorState(message = error, onRetry = onRetry)
            events.isEmpty() -> EmptyState(
                title = stringResource(R.string.empty_events_title),
                subtitle = stringResource(R.string.empty_events_subtitle),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items = events, key = { it.id }) { event ->
                    ModernEventCard(event = event, onClick = { onEventClick(event.id) })
                }
            }
        }
    }
}

@Composable
private fun TicketsTabContent(
    isLoading: Boolean,
    error: String?,
    tickets: List<UserTicket>,
    onRetry: () -> Unit,
    onTicketClick: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
        isLoading -> LoadingState(message = stringResource(R.string.loading_tickets))
        error != null -> ErrorState(message = error, onRetry = onRetry)
        tickets.isEmpty() -> EmptyState(
            title = stringResource(R.string.empty_tickets_title),
            subtitle = stringResource(R.string.empty_tickets_subtitle),
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = tickets, key = { it.id }) { ticket ->
                ModernTicketCard(
                    ticket = ticket,
                    onClick = { onTicketClick(ticket.id) },
                )
            }
        }
        }
    }
}

@Composable
private fun ModernEventCard(
    event: Event,
    onClick: () -> Unit,
) {
    val prices = event.ticketTypes.map { it.priceCents }
    val minPrice = prices.minOrNull()
    val maxPrice = prices.maxOrNull()
    val totalRemaining = event.ticketTypes.sumOf { it.remaining }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(brushForKey(event.name)),
                contentAlignment = Alignment.BottomStart,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.92f),
                    ) {
                        Text(
                            text = formatEventDateShort(event.startsAt),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    if (totalRemaining > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.92f),
                        ) {
                            Text(
                                text = "$totalRemaining bilet kaldı",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = formatEventDate(event.startsAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (event.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (minPrice != null && maxPrice != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = formatPriceRangeCents(minPrice, maxPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernTicketCard(
    ticket: UserTicket,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brushForKey(ticket.eventName)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ticket.eventName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = ticket.eventName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    TicketStatusBadge(status = ticket.status)
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = ticket.ticketTypeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = ticket.venue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = formatEventDate(ticket.startsAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatPriceCents(ticket.priceCents),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "QR",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketStatusBadge(status: TicketStatus) {
    val (label, color) = when (status) {
        TicketStatus.VALID -> "Geçerli" to SuccessGreen
        TicketStatus.USED -> "Kullanıldı" to WarnAmber
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LoadingState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Tekrar Dene")
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun brushForKey(key: String): Brush {
    val index = (key.hashCode() and Int.MAX_VALUE) % gradientPalette.size
    val colors = gradientPalette[index]
    return Brush.linearGradient(colors)
}
