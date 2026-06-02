package com.turkcell.ticketapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.TicketType
import com.turkcell.core.util.formatEventDate
import com.turkcell.core.util.formatPriceCents
import com.turkcell.ticketapp.R
import com.turkcell.ticketapp.viewmodel.EventDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    viewModel: EventDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.purchaseSucceeded) {
        if (state.purchaseSucceeded) {
            viewModel.consumePurchaseSuccess()
            onPurchaseSuccess()
        }
    }

    if (state.showPaymentDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPaymentDialog,
            title = { Text(stringResource(R.string.payment_confirm_title)) },
            text = { Text(stringResource(R.string.payment_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmPayment,
                    enabled = !state.isProcessing,
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPaymentDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.event_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (state.event != null) {
                PurchaseBottomBar(
                    totalCents = state.totalCents(state.event),
                    enabled = state.hasSelection && !state.isProcessing,
                    isLoading = state.isProcessing,
                    onBuyClick = viewModel::onBuyClick,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::loadEvent) { Text(stringResource(R.string.retry)) }
                    }
                }
            }

            state.event != null -> {
                EventDetailContent(
                    event = state.event!!,
                    state = state,
                    modifier = Modifier.padding(padding),
                    onIncrement = viewModel::incrementQuantity,
                    onDecrement = viewModel::decrementQuantity,
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    state: com.turkcell.ticketapp.viewmodel.EventDetailUiState,
    modifier: Modifier = Modifier,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(event.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(event.venue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatEventDate(event.startsAt), style = MaterialTheme.typography.bodyMedium)
        if (event.description.isNotBlank()) {
            Text(event.description, style = MaterialTheme.typography.bodyMedium)
        }

        Text(stringResource(R.string.ticket_types), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        event.ticketTypes.forEach { ticketType ->
            TicketTypeRow(
                ticketType = ticketType,
                quantity = state.quantityFor(ticketType.id),
                onIncrement = { onIncrement(ticketType.id) },
                onDecrement = { onDecrement(ticketType.id) },
            )
        }

        state.actionError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun TicketTypeRow(
    ticketType: TicketType,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ticketType.name, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.remaining_capacity, ticketType.remaining, ticketType.capacity),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatPriceCents(ticketType.priceCents),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            FilledTonalIconButton(onClick = onDecrement, enabled = quantity > 0) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(8.dp))
            Text(quantity.toString(), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = onIncrement,
                enabled = quantity < minOf(20, ticketType.remaining.toInt()),
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun PurchaseBottomBar(
    totalCents: Long,
    enabled: Boolean,
    isLoading: Boolean,
    onBuyClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(stringResource(R.string.total), style = MaterialTheme.typography.labelMedium)
                Text(
                    formatPriceCents(totalCents),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(onClick = onBuyClick, enabled = enabled && !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.buy))
                }
            }
        }
    }
}
