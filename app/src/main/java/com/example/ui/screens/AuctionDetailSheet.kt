package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuctionBid
import com.example.data.model.SneakerAuction
import com.example.ui.components.SneakerImage
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.formatCountdown
import com.example.ui.theme.MintVerified
import com.example.ui.theme.PaypalBlue
import com.example.ui.theme.SneakerOrange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailSheet(
    auction: SneakerAuction,
    bids: List<AuctionBid>,
    currentTimeMs: Long,
    isParticipant: Boolean,
    isAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onPayEntryFee: () -> Unit,
    onPlaceBid: (Double) -> Unit,
    onVerifyCertificate: () -> Unit
) {
    var customBidInput by remember { mutableStateOf("") }
    val remainingMs = auction.endTimeMs - currentTimeMs
    val formattedTime = formatCountdown(remainingMs, auction.status)

    val currentMinBid = auction.currentPrice + 1.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("auction_detail_sheet")
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (auction.status == "LIVE") Color(0xFFD32F2F) else Color(0xFF0288D1)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    VerifiedBadge(onClick = onVerifyCertificate)
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            val photoList = remember(auction) {
                val split = auction.referencePhotos.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (split.isNotEmpty()) split else listOf(auction.imageUrl)
            }
            var selectedPhotoIndex by remember(auction.id) { mutableStateOf(0) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                // Sneaker Image preview & gallery
                item {
                    val activeImage = photoList.getOrElse(selectedPhotoIndex) { auction.imageUrl }
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            SneakerImage(
                                imageUrl = activeImage,
                                contentDescription = auction.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (photoList.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fotos de Referencia (${photoList.size} Disponibles):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                photoList.forEachIndexed { index, photoUrl ->
                                    val isSelected = index == selectedPhotoIndex
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(55.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = if (isSelected) 2.5.dp else 0.dp,
                                                color = if (isSelected) SneakerOrange else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedPhotoIndex = index },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        SneakerImage(
                                            imageUrl = photoUrl,
                                            contentDescription = "Foto ${index + 1}",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Title & Details
                item {
                    Text(
                        text = auction.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Marca: ${auction.brand} • Talla: ${auction.size} • SKU: ${auction.sku}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Estado: ${auction.condition} • Caja: ${if (auction.originalBox) "Sí (Original)" else "No"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description & accessories
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = auction.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Accesorios incluidos: ${auction.accessories}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SneakerOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 30-Second Extension Rule Banner Notice
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261D15)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = SneakerOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚡ Regla de los 30s: Si realiza una oferta en los últimos 30 segundos, el tiempo se extenderá automáticamente 30s adicionales.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Pricing & Bidding Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Oferta Más Alta Actual:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", auction.currentPrice)} USD",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = SneakerOrange
                                    )
                                    Text(
                                        text = "Mejor Postor: ${auction.highestBidderAlias}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Inscripción PayPal:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", auction.entryFee)} USD",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PaypalBlue
                                    )
                                    Text(
                                        text = if (isParticipant) "✓ Inscrito" else "Mín. ${auction.minParticipants} part.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isParticipant) MintVerified else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
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
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Interactive Bidding Controls (LIVE vs UPCOMING)
                item {
                    val isLive = auction.status == "LIVE"
                    val isUpcoming = auction.status == "UPCOMING"

                    if (isAdmin) {
                        // Admin warning prompt
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("admin_no_bidding_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Modo Administrador Activo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Como administrador, no tienes permitido realizar ofertas en las subastas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    } else if (isUpcoming) {
                        // UPCOMING: Simple clean enrollment status or PayPal action button
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (isParticipant) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = MintVerified.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(14.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MintVerified,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "¡Estás inscrito en esta subasta!",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MintVerified
                                            )
                                            Text(
                                                text = "Las ofertas se abrirán cuando el administrador inicie la subasta.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = PaypalBlue.copy(alpha = 0.12f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Payment,
                                                contentDescription = null,
                                                tint = PaypalBlue,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Inscripción: $${String.format("%.2f", auction.entryFee)} USD",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PaypalBlue
                                                )
                                                Text(
                                                    text = "Inscríbete para participar cuando comience la subasta.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = onPayEntryFee,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PaypalBlue),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("INSCRIBIRSE CON PayPal", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isLive) {
                        // LIVE: Quick Bidding + Custom Bid UI
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "🔴 SUBASTA ACTIVA EN TIEMPO REAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SneakerOrange
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Increment Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onPlaceBid(auction.currentPrice + 5.0) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_bid_plus_5"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("+$5 USD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Button(
                                    onClick = { onPlaceBid(auction.currentPrice + 10.0) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_bid_plus_10"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("+$10 USD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Button(
                                    onClick = { onPlaceBid(auction.currentPrice + 25.0) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_bid_plus_25"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("+$25 USD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Bid / Auto-bid Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customBidInput,
                                    onValueChange = { customBidInput = it },
                                    placeholder = { Text("Monto min. $${String.format("%.0f", currentMinBid)}") },
                                    label = { Text("Puja Personalizada ($)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("custom_bid_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val amount = customBidInput.toDoubleOrNull()
                                        if (amount != null && amount > auction.currentPrice) {
                                            onPlaceBid(amount)
                                            customBidInput = ""
                                        }
                                    },
                                    enabled = customBidInput.toDoubleOrNull()?.let { it > auction.currentPrice } == true,
                                    modifier = Modifier
                                        .height(56.dp)
                                        .testTag("btn_confirm_custom_bid"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Max bid limit button (Instant Win!)
                            if (auction.maxBidLimit > auction.currentPrice) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onPlaceBid(auction.maxBidLimit) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MintVerified),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🎯 PUJA MÁXIMA / COMPRA DIRECTA ($${String.format("%.2f", auction.maxBidLimit)} USD)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else if (auction.status == "COMPLETED") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🏁 SUBASTA FINALIZADA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ganador de la subasta: ${auction.highestBidderAlias}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MintVerified,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Precio final adjudicado: $${String.format("%.2f", auction.currentPrice)} USD",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Bid History Feed Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Historial de Ofertas (${bids.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Bid List Items
                if (bids.isEmpty()) {
                    item {
                        Text(
                            text = "Aún no hay ofertas en esta subasta. ¡Sé el primero!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(bids) { bid ->
                        val dateStr = remember(bid.timestampMs) {
                            SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(bid.timestampMs))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(SneakerOrange.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bid.userAlias.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SneakerOrange
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = bid.userAlias,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "$${String.format("%.2f", bid.amount)} USD",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SneakerOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.QuickBidButton(
    label: String,
    targetAmount: Double,
    enabled: Boolean = true,
    onBid: (Double) -> Unit
) {
    Button(
        onClick = { onBid(targetAmount) },
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SneakerOrange.copy(alpha = 0.15f),
            contentColor = SneakerOrange,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
