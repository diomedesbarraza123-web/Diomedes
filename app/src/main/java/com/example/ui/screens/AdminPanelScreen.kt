package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrandCategory
import com.example.data.model.PaymentTransaction
import com.example.data.model.SneakerAuction
import com.example.data.model.UserProfile
import com.example.ui.theme.MintVerified
import com.example.ui.theme.PaypalBlue
import com.example.ui.theme.SneakerOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    auctions: List<SneakerAuction>,
    brands: List<BrandCategory>,
    users: List<UserProfile>,
    transactions: List<PaymentTransaction>,
    onCreateAuction: (SneakerAuction) -> Unit,
    onCancelAuction: (Long, String) -> Unit,
    onStartAuction: (Long) -> Unit,
    onAddBrand: (String) -> Unit,
    onSendStartReminder: (Long) -> Unit = {}
) {
    var isWizardOpen by remember { mutableStateOf(false) }
    var isAddBrandOpen by remember { mutableStateOf(false) }
    var cancelReasonAuctionId by remember { mutableStateOf<Long?>(null) }
    var cancelReasonInput by remember { mutableStateOf("") }

    val totalRevenue = remember(transactions) {
        transactions.sumOf { it.amount }
    }
    val activeAuctionsCount = remember(auctions) {
        auctions.count { it.status == "LIVE" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_panel_screen"),
        contentPadding = PaddingValues(bottom = 90.dp, start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        // Admin Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SneakerOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "PANEL ADMINISTRATIVO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = SneakerOrange
                        )
                        Text(
                            text = "Gestión centralizada de subastas, publicaciones y marcas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Metrics Dashboard Cards
        item {
            Text(
                text = "ESTADÍSTICAS Y MÉTRICAS EN TIEMPO REAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Total Usuarios", "${users.size}", Icons.Default.Group, Modifier.weight(1f))
                    MetricCard("Subastas Totales", "${auctions.size}", Icons.Default.Gavel, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Activas en Vivo", "$activeAuctionsCount", Icons.Default.PlayArrow, Modifier.weight(1f), color = SneakerOrange)
                    MetricCard("Ingresos PayPal", "$${String.format("%.0f", totalRevenue)} USD", Icons.Default.MonetizationOn, Modifier.weight(1f), color = PaypalBlue)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { isWizardOpen = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_create_auction_wizard"),
                    colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CREAR SUBASTA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { isAddBrandOpen = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_add_brand_admin"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AGREGAR MARCA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Auction Management List
        item {
            Text(
                text = "GESTIÓN DE SUBASTAS (${auctions.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(auctions) { auction ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = auction.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = when (auction.status) {
                                "LIVE" -> Color(0xFFD32F2F)
                                "UPCOMING" -> Color(0xFF0288D1)
                                else -> Color(0xFF616161)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = auction.status,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Precio Actual: $${String.format("%.2f", auction.currentPrice)} USD • Inscripción: $${String.format("%.2f", auction.entryFee)} USD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Participantes: ${auction.currentParticipants} (Mín. ${auction.minParticipants}) • Certificado: ${auction.certificateId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (auction.maxBidLimit > 0.0) {
                        Text(
                            text = "🎯 Costo Máximo / Cierre Instantáneo: $${String.format("%.2f", auction.maxBidLimit)} USD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SneakerOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (auction.status == "LIVE" || auction.status == "UPCOMING") {
                            FilledTonalButton(
                                onClick = { onSendStartReminder(auction.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("btn_notify_start_30min_${auction.id}")
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📢 Notificar Inicio (30 min)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (auction.status == "UPCOMING") {
                            Button(
                                onClick = { onStartAuction(auction.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Iniciar AHORA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (auction.status == "LIVE" || auction.status == "UPCOMING") {
                            OutlinedButton(
                                onClick = {
                                    cancelReasonAuctionId = auction.id
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancelar", color = Color(0xFFE53935), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Wizard: Create Auction
    if (isWizardOpen) {
        CreateAuctionWizardModal(
            brands = brands,
            onDismiss = { isWizardOpen = false },
            onCreate = { newAuction ->
                onCreateAuction(newAuction)
                isWizardOpen = false
            }
        )
    }

    // Modal: Add Brand
    if (isAddBrandOpen) {
        AddBrandModal(
            onDismiss = { isAddBrandOpen = false },
            onAdd = { brandName ->
                onAddBrand(brandName)
                isAddBrandOpen = false
            }
        )
    }

    // Dialog: Cancel Auction Reason
    cancelReasonAuctionId?.let { auctionId ->
        AlertDialog(
            onDismissRequest = { cancelReasonAuctionId = null },
            title = { Text("Cancelar Subasta") },
            text = {
                Column {
                    Text("Ingresa el motivo de la cancelación de esta subasta:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReasonInput,
                        onValueChange = { cancelReasonInput = it },
                        placeholder = { Text("Motivo de cancelación...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelAuction(auctionId, cancelReasonInput.ifEmpty { "Cancelado por Administrador" })
                        cancelReasonAuctionId = null
                        cancelReasonInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Confirmar Cancelación")
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelReasonAuctionId = null }) {
                    Text("Volver")
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAuctionWizardModal(
    brands: List<BrandCategory>,
    onDismiss: () -> Unit,
    onCreate: (SneakerAuction) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) } // 1: Product, 2: Photos (3-5), 3: Pricing, 4: Confirmation

    // Form State
    var title by remember { mutableStateOf("Nike Air Jordan 4 Retro") }
    var selectedBrand by remember { mutableStateOf("Jordan") }
    var model by remember { mutableStateOf("Air Jordan 4") }
    var sku by remember { mutableStateOf("DH6927-111") }
    var colorway by remember { mutableStateOf("White / Military Blue") }
    var size by remember { mutableStateOf("10 US (28 CM)") }
    var condition by remember { mutableStateOf("Nuevo en Caja Original (DS)") }

    // Reference Photos (Min 3, Max 5)
    var photo1 by remember { mutableStateOf("img_hero_sneaker_1785539290252") }
    var photo2 by remember { mutableStateOf("img_sneaker_jordan1_1785539300440") }
    var photo3 by remember { mutableStateOf("img_sneaker_dunk_1785539311716") }
    var photo4 by remember { mutableStateOf("") }
    var photo5 by remember { mutableStateOf("") }

    var startingPriceInput by remember { mutableStateOf("200") }
    var maxBidLimitInput by remember { mutableStateOf("450") }
    var entryFeeInput by remember { mutableStateOf("10") }
    var minParticipantsInput by remember { mutableStateOf("3") }
    var description by remember { mutableStateOf("Par 100% Auténtico de colección con sello de verificación e inspección detallada.") }

    val validPhotos = remember(photo1, photo2, photo3, photo4, photo5) {
        listOf(photo1, photo2, photo3, photo4, photo5)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    val isPhotoCountValid = validPhotos.size in 3..5

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("create_auction_wizard_modal")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Publicar Subasta (Paso $currentStep/4)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = SneakerOrange
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (currentStep) {
                1 -> {
                    // Step 1: Product Details
                    Text(
                        text = "PASO 1: DETALLES DEL SNEAKER ORIGINAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la Publicación") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = selectedBrand,
                            onValueChange = { selectedBrand = it },
                            label = { Text("Marca") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = size,
                            onValueChange = { size = it },
                            label = { Text("Talla US/CM") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU del Producto") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = condition,
                            onValueChange = { condition = it },
                            label = { Text("Estado del Par") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { currentStep = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Siguiente: Fotos de Referencia (3 a 5) →", fontWeight = FontWeight.Bold)
                    }
                }
                2 -> {
                    // Step 2: Reference Photos (Min 3, Max 5)
                    Text(
                        text = "PASO 2: FOTOS DE REFERENCIA (MÍNIMO 3, MÁXIMO 5)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SneakerOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Agrega fotos de alta resolución del producto (ángulos, suela, etiquetas o caja) para que los compradores verifiquen el sneaker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = photo1,
                        onValueChange = { photo1 = it },
                        label = { Text("Foto 1: Vista Frontal / Principal (Obligatoria)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = photo2,
                        onValueChange = { photo2 = it },
                        label = { Text("Foto 2: Perfil / Ángulo Lateral (Obligatoria)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = photo3,
                        onValueChange = { photo3 = it },
                        label = { Text("Foto 3: Suela y Talón / Etiqueta (Obligatoria)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = photo4,
                        onValueChange = { photo4 = it },
                        label = { Text("Foto 4: Caja y Accesorios (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = photo5,
                        onValueChange = { photo5 = it },
                        label = { Text("Foto 5: Holograma / Certificado (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isPhotoCountValid) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Debes ingresar como mínimo 3 fotos de referencia (Llevas ${validPhotos.size}/3 requeridas).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MintVerified.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ ${validPhotos.size} fotos de referencia registradas correctamente.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintVerified,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Atrás")
                        }
                        Button(
                            onClick = { currentStep = 3 },
                            enabled = isPhotoCountValid,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange)
                        ) {
                            Text("Siguiente: Precios →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                3 -> {
                    // Step 3: Pricing & Entry Fee
                    Text(
                        text = "PASO 3: VALORES E INSCRIPCIÓN PAYPAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = startingPriceInput,
                        onValueChange = { startingPriceInput = it },
                        label = { Text("Precio Inicial de Oferta ($ USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = maxBidLimitInput,
                        onValueChange = { maxBidLimitInput = it },
                        label = { Text("Costo Máximo de Cierre Instantáneo ($ USD)") },
                        supportingText = { Text("Primer usuario que alcance o supere esta cifra ganará la subasta automáticamente.", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = entryFeeInput,
                        onValueChange = { entryFeeInput = it },
                        label = { Text("Costo de Inscripción PayPal ($ USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = minParticipantsInput,
                        onValueChange = { minParticipantsInput = it },
                        label = { Text("Número Mínimo de Participantes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { currentStep = 2 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Atrás")
                        }
                        Button(
                            onClick = { currentStep = 4 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange)
                        ) {
                            Text("Siguiente: Confirmar →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                4 -> {
                    // Step 4: Confirmation & Verified Seal
                    Text(
                        text = "PASO 4: CONFIRMACIÓN Y SELLO DE AUTENTICIDAD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = title, fontWeight = FontWeight.Bold)
                            Text(text = "Marca: $selectedBrand • SKU: $sku • Talla: $size", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Fotos de Referencia: ${validPhotos.size} adjuntadas", style = MaterialTheme.typography.bodySmall, color = SneakerOrange)
                            Text(text = "Precio Inicial: $$startingPriceInput USD • Inscripción PayPal: $$entryFeeInput USD", style = MaterialTheme.typography.bodySmall, color = SneakerOrange, fontWeight = FontWeight.Bold)
                            if ((maxBidLimitInput.toDoubleOrNull() ?: 0.0) > 0.0) {
                                Text(text = "🎯 Tope Máximo (Gana Automático): $$maxBidLimitInput USD", style = MaterialTheme.typography.bodySmall, color = SneakerOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MintVerified.copy(alpha = 0.15f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MintVerified)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Se adjuntará automáticamente el Sello 'Producto Original Verificado' con Certificado ID.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintVerified,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val startPx = startingPriceInput.toDoubleOrNull() ?: 150.0
                            val maxLimitPx = maxBidLimitInput.toDoubleOrNull() ?: 0.0
                            val feePx = entryFeeInput.toDoubleOrNull() ?: 10.0
                            val minPart = minParticipantsInput.toIntOrNull() ?: 3
                            val now = System.currentTimeMillis()

                            val mainImg = validPhotos.firstOrNull() ?: "img_hero_sneaker_1785539290252"
                            val photoJoined = validPhotos.joinToString(",")

                            val created = SneakerAuction(
                                title = title,
                                brand = selectedBrand,
                                model = model,
                                sku = sku,
                                colorway = colorway,
                                size = size,
                                condition = condition,
                                startingPrice = startPx,
                                currentPrice = startPx,
                                maxBidLimit = maxLimitPx,
                                entryFee = feePx,
                                minParticipants = minPart,
                                currentParticipants = 0,
                                startTimeMs = now,
                                endTimeMs = now + (30 * 60_1000L), // 30 mins
                                status = "LIVE",
                                imageUrl = mainImg,
                                referencePhotos = photoJoined,
                                verifiedOriginal = true,
                                originalBox = true,
                                description = description
                            )
                            onCreate(created)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_publish_auction_final"),
                        colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PUBLICAR SUBASTA EN TIEMPO REAL", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AddBrandModal(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var newBrandName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Nueva Marca Deportiva") },
        text = {
            Column {
                Text("Ingresa el nombre de la nueva marca autorizada:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newBrandName,
                    onValueChange = { newBrandName = it },
                    placeholder = { Text("Ej. Salomon, Off-White, Balenciaga") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newBrandName.isNotBlank()) {
                        onAdd(newBrandName.trim())
                    }
                },
                enabled = newBrandName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SneakerOrange)
            ) {
                Text("Guardar Marca")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
