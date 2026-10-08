package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SneakerAuction
import com.example.ui.theme.MintVerified
import com.example.ui.theme.PaypalBlue
import com.example.ui.theme.SneakerOrange

@Composable
fun AuctionCard(
    auction: SneakerAuction,
    currentTimeMs: Long,
    isParticipant: Boolean,
    onClick: () -> Unit,
    onPayEntryFee: () -> Unit,
    onVerifyCertificate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingMs = auction.endTimeMs - currentTimeMs
    val formattedTime = formatCountdown(remainingMs, auction.status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("auction_card_${auction.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Sneaker Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                SneakerImage(
                    imageUrl = auction.imageUrl,
                    contentDescription = auction.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay gradient dark tint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )

                // Status Badge Top Left
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (auction.status) {
                                "LIVE" -> Color(0xFFD32F2F)
                                "UPCOMING" -> Color(0xFF0288D1)
                                else -> Color(0xFF616161)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Verified Original Badge Top Right
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    VerifiedBadge(onClick = onVerifyCertificate)
                }

                // Brand Pill Bottom Left
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = auction.brand,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Card Body Info
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = auction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Talla: ${auction.size} • SKU: ${auction.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${auction.currentParticipants} part.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                // Price and Bidding Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (auction.status == "LIVE") "Oferta Más Alta Actual:" else "Precio Inicial:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", auction.currentPrice)} USD",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = SneakerOrange
                        )
                        Text(
                            text = "Líder: ${auction.highestBidderAlias}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (auction.maxBidLimit > 0.0) {
                            Text(
                                text = "🎯 Tope Máximo: $${String.format("%.2f", auction.maxBidLimit)} USD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SneakerOrange
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Inscripción:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", auction.entryFee)} USD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PaypalBlue
                        )

                        if (isParticipant) {
                            Text(
                                text = "Inscrito ✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintVerified,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Mín. ${auction.minParticipants} part.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button
                if (auction.status == "LIVE") {
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_bid_now_${auction.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isParticipant) "OFERTAR (ACTIVA)" else "OFERTAR / INSCRIBIRSE (ACTIVA)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (auction.status == "UPCOMING") {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VER DETALLES Y RESERVAR", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Subasta Finalizada • Ganador: ${auction.highestBidderAlias}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatCountdown(remainingMs: Long, status: String): String {
    if (status != "LIVE") return if (status == "UPCOMING") "⏳ PRÓXIMAMENTE" else "🏁 FINALIZADA"
    if (remainingMs <= 0) return "🔴 FINALIZANDO..."

    val totalSeconds = remainingMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("🔴 ACTIVA %02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("🔴 ACTIVA %02d:%02d", minutes, seconds)
    }
}
