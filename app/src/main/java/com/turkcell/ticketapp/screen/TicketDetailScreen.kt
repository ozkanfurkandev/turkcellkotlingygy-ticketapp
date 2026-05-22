package com.turkcell.ticketapp.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.ticket.TicketStatus
import com.turkcell.core.domain.ticket.UserTicket
import com.turkcell.core.ui.theme.SuccessGreen
import com.turkcell.core.ui.theme.WarnAmber
import com.turkcell.core.util.formatEventDate
import com.turkcell.core.util.formatPriceCents
import com.turkcell.ticketapp.ui.QrCodeImage
import com.turkcell.ticketapp.viewmodel.TicketDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    onBack: () -> Unit,
    viewModel: TicketDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bilet Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::loadTicket) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            }

            state.ticket != null -> {
                TicketDetailContent(
                    ticket = state.ticket!!,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun TicketDetailContent(
    ticket: UserTicket,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    brush = detailGradient(ticket.eventName),
                    shape = RoundedCornerShape(20.dp),
                ),
            contentAlignment = Alignment.BottomStart,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                DetailStatusBadge(status = ticket.status)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ticket.eventName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }

        DetailInfoCard(title = "Bilet Türü", value = ticket.ticketTypeName)
        DetailInfoCard(title = "Mekan", value = ticket.venue)
        DetailInfoCard(title = "Tarih", value = formatEventDate(ticket.startsAt))
        DetailInfoCard(title = "Fiyat", value = formatPriceCents(ticket.priceCents))

        ticket.usedAt?.let { usedAt ->
            DetailInfoCard(title = "Kullanım Zamanı", value = formatEventDate(usedAt))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Giriş QR Kodu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                ) {
                    QrCodeImage(
                        content = ticket.qrCode,
                        modifier = Modifier
                            .size(220.dp)
                            .padding(16.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = ticket.qrCode,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Kapı görevlisi bu kodu tarayarak girişinizi doğrular.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun DetailStatusBadge(status: TicketStatus) {
    val (label, color) = when (status) {
        TicketStatus.VALID -> "Geçerli" to SuccessGreen
        TicketStatus.USED -> "Kullanıldı" to WarnAmber
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.18f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private val detailGradientPalette = listOf(
    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
    listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
    listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4)),
    listOf(Color(0xFF10B981), Color(0xFF059669)),
)

private fun detailGradient(key: String): Brush {
    val index = (key.hashCode() and Int.MAX_VALUE) % detailGradientPalette.size
    val colors = detailGradientPalette[index]
    return Brush.linearGradient(colors)
}
