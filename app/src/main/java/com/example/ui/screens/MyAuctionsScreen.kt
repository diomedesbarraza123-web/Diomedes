package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentTransaction
import com.example.data.model.SneakerAuction
import com.example.ui.components.AuctionCard
import com.example.ui.theme.GoldWinner
import com.example.ui.theme.PaypalBlue
import com.example.ui.theme.SneakerOrange
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyAuctionsScreen(
    joinedAuctions: List<SneakerAuction>,
    transactions: List<PaymentTransaction>,
    currentUserId: Long,
    currentTimeMs: Long,
    onAuctionClick: (Long) -> Unit,
    onVerifyCertificate: (SneakerAuction) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Joined, 1: Won, 2: Payments

    val wonAuctions = remember(joinedAuctions, currentUserId) {
        joinedAuctions.filter { it.winnerUserId == currentUserId || (it.status == "COMPLETED" && it.highestBidderId == currentUserId) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_auctions_screen")
    ) {
        // Screen Top Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = SneakerOrange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Mi Actividad y Pagos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Tab Selector Bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SneakerOrange
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Inscritas (${joinedAuctions.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Ganadas (${wonAuctions.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Pagos PayPal", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    if (joinedAuctions.isEmpty()) {
                        item {
                            EmptyStateNotice("Aún no estás inscrito en ninguna subasta.", "Inscríbete pagando el costo de entrada vía PayPal.")
                        }
                    } else {
                        items(joinedAuctions) { auction ->
                            AuctionCard(
                                auction = auction,
                                currentTimeMs = currentTimeMs,
                                isParticipant = true,
                                onClick = { onAuctionClick(auction.id) },
                                onPayEntryFee = {},
                                onVerifyCertificate = { onVerifyCertificate(auction) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                1 -> {
                    if (wonAuctions.isEmpty()) {
                        item {
                            EmptyStateNotice("Aún no has ganado subastas.", "Mantente atento a los últimos segundos de la subasta para realizar la oferta ganadora.")
                        }
                    } else {
                        items(wonAuctions) { auction ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .border(1.dp, GoldWinner, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldWinner)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "¡SUBASTA GANADA!",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = GoldWinner
                                            )
                                        }
                                        Button(
                                            onClick = { onVerifyCertificate(auction) },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldWinner, contentColor = Color.Black),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Ver Certificado", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = auction.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Precio Final Adjudicado: $${String.format("%.2f", auction.currentPrice)} USD",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SneakerOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Certificado de Autenticidad ID: ${auction.certificateId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (transactions.isEmpty()) {
                        item {
                            EmptyStateNotice("No hay historial de pagos registrados.", "Tus comprobantes de PayPal aparecerán aquí.")
                        }
                    } else {
                        items(transactions) { tx ->
                            val dateStr = remember(tx.timestampMs) {
                                SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault()).format(Date(tx.timestampMs))
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tx.auctionTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Transacción PayPal: ${tx.paypalTxId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PaypalBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$${String.format("%.2f", tx.amount)} USD",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PaypalBlue
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = tx.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF00E676),
                                                fontWeight = FontWeight.Bold
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
    }
}

@Composable
private fun EmptyStateNotice(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Gavel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
