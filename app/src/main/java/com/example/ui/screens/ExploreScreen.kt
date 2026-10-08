package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import com.example.data.model.BrandCategory
import com.example.data.model.SneakerAuction
import com.example.ui.components.AuctionCard
import com.example.ui.components.SneakerImage
import com.example.ui.theme.MintVerified
import com.example.ui.theme.SneakerOrange

@Composable
fun ExploreScreen(
    auctions: List<SneakerAuction>,
    brands: List<BrandCategory>,
    selectedBrand: String,
    selectedStatus: String,
    currentTimeMs: Long,
    isAdminMode: Boolean,
    onBrandSelected: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onAuctionClick: (Long) -> Unit,
    onPayEntryFee: (SneakerAuction) -> Unit,
    onVerifyCertificate: (SneakerAuction) -> Unit,
    onToggleAdminMode: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("Todas") }

    val sizes = listOf("Todas", "US 8", "US 8.5", "US 9", "US 9.5", "US 10", "US 10.5", "US 11", "US 11.5", "US 12")

    val filteredAuctions = remember(auctions, selectedBrand, selectedStatus, selectedSize, searchQuery) {
        auctions.filter { auction ->
            val matchBrand = (selectedBrand == "Todas" || auction.brand.equals(selectedBrand, ignoreCase = true))
            val matchStatus = (selectedStatus == "ALL" || auction.status == selectedStatus)
            val matchSize = (selectedSize == "Todas" || auction.size.contains(selectedSize, ignoreCase = true))
            val matchSearch = searchQuery.isEmpty() ||
                    auction.title.contains(searchQuery, ignoreCase = true) ||
                    auction.sku.contains(searchQuery, ignoreCase = true) ||
                    auction.model.contains(searchQuery, ignoreCase = true)
            matchBrand && matchStatus && matchSize && matchSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("explore_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // App Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                SneakerImage(
                    imageUrl = "img_hero_sneaker_1785539290252",
                    contentDescription = "Subastas Sneakers Banner",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SUBASTAS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = SneakerOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SNEAKERS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        // Admin Toggle Quick Pill
                        Surface(
                            onClick = onToggleAdminMode,
                            color = if (isAdminMode) SneakerOrange else Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("btn_toggle_admin_quick")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAdminMode) "Modo Admin" else "Modo Usuario",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MintVerified,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% Calzado Deportivo Original Verificado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MintVerified
                            )
                        }
                        Text(
                            text = "Participa en subastas en tiempo real de pares auténticos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por modelo, marca o SKU (ej. Jordan 1)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SneakerOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )
        }

        // Status Tabs (EN VIVO, PRÓXIMAS, FINALIZADAS, TODAS)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip("LIVE", "🔴 Activas", selectedStatus, onStatusSelected)
                StatusChip("UPCOMING", "⏳ Próximas", selectedStatus, onStatusSelected)
                StatusChip("COMPLETED", "🏁 Finalizadas", selectedStatus, onStatusSelected)
                StatusChip("ALL", "Todas", selectedStatus, onStatusSelected)
            }
        }

        // Brand Category Selector
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "MARCAS DEPORTIVAS OFICIALES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedBrand == "Todas",
                            onClick = { onBrandSelected("Todas") },
                            label = { Text("Todas") },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    items(brands) { brand ->
                        FilterChip(
                            selected = selectedBrand.equals(brand.name, ignoreCase = true),
                            onClick = { onBrandSelected(brand.name) },
                            label = { Text(brand.name) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "TALLA US (SNEAKERS)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sizes) { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size },
                            label = { Text(size, fontWeight = if (selectedSize == size) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SneakerOrange.copy(alpha = 0.85f),
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedStatus) {
                        "LIVE" -> "Subastas Activas en Vivo (${filteredAuctions.size})"
                        "UPCOMING" -> "Próximos Lanzamientos (${filteredAuctions.size})"
                        "COMPLETED" -> "Subastas Finalizadas (${filteredAuctions.size})"
                        else -> "Catálogo de Subastas (${filteredAuctions.size})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // List of Auctions
        if (filteredAuctions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron subastas en esta categoría.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredAuctions, key = { it.id }) { auction ->
                AuctionCard(
                    auction = auction,
                    currentTimeMs = currentTimeMs,
                    isParticipant = false, // Handled dynamically in ViewModel
                    onClick = { onAuctionClick(auction.id) },
                    onPayEntryFee = { onPayEntryFee(auction) },
                    onVerifyCertificate = { onVerifyCertificate(auction) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    statusKey: String,
    label: String,
    currentStatus: String,
    onSelect: (String) -> Unit
) {
    val selected = currentStatus == statusKey
    FilterChip(
        selected = selected,
        onClick = { onSelect(statusKey) },
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SneakerOrange,
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    )
}
