package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.AuthenticityCertificateModal
import com.example.ui.components.PayPalModal
import com.example.ui.screens.*
import com.example.ui.theme.SneakerAuctionTheme
import com.example.ui.theme.SneakerOrange

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SneakerAuctionTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    val allAuctions by viewModel.allAuctions.collectAsStateWithLifecycle()
    val allBrands by viewModel.allBrands.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    val myAuctions by viewModel.myAuctions.collectAsStateWithLifecycle()
    val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val userNotifications by viewModel.userNotifications.collectAsStateWithLifecycle()

    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()

    val selectedAuction by viewModel.selectedAuction.collectAsStateWithLifecycle()
    val selectedAuctionBids by viewModel.selectedAuctionBids.collectAsStateWithLifecycle()
    val isParticipantInSelected by viewModel.isUserParticipantInSelected.collectAsStateWithLifecycle()

    val paypalAuction by viewModel.paypalPaymentAuction.collectAsStateWithLifecycle()
    val authenticityAuction by viewModel.authenticityCertAuction.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explorar") },
                    label = { Text("Explorar", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SneakerOrange,
                        selectedTextColor = SneakerOrange,
                        indicatorColor = SneakerOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_explore")
                )

                if (!isAdminMode) {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.Gavel, contentDescription = "Mis Subastas") },
                        label = { Text("Mis Subastas", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SneakerOrange,
                            selectedTextColor = SneakerOrange,
                            indicatorColor = SneakerOrange.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_my_auctions")
                    )
                }

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        BadgedBox(badge = {
                            if (userNotifications.any { !it.isRead }) {
                                Badge(containerColor = SneakerOrange) {
                                    Text("${userNotifications.count { !it.isRead }}")
                                }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    },
                    label = { Text("Avisos", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SneakerOrange,
                        selectedTextColor = SneakerOrange,
                        indicatorColor = SneakerOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_notifications")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SneakerOrange,
                        selectedTextColor = SneakerOrange,
                        indicatorColor = SneakerOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )

                if (isAdminMode) {
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { viewModel.selectTab(4) },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                        label = { Text("Panel Admin", fontWeight = FontWeight.Bold, color = SneakerOrange) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SneakerOrange,
                            selectedTextColor = SneakerOrange,
                            indicatorColor = SneakerOrange.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_tab_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ExploreScreen(
                    auctions = allAuctions,
                    brands = allBrands,
                    selectedBrand = selectedBrand,
                    selectedStatus = selectedStatus,
                    currentTimeMs = currentTimeMs,
                    isAdminMode = isAdminMode,
                    onBrandSelected = { viewModel.setBrandFilter(it) },
                    onStatusSelected = { viewModel.setStatusFilter(it) },
                    onAuctionClick = { auctionId -> viewModel.openAuctionDetail(auctionId) },
                    onPayEntryFee = { auction -> viewModel.openPayPalModal(auction) },
                    onVerifyCertificate = { auction -> viewModel.openAuthenticityModal(auction) },
                    onToggleAdminMode = { viewModel.toggleAdminMode() }
                )
                1 -> MyAuctionsScreen(
                    joinedAuctions = myAuctions,
                    transactions = userTransactions,
                    currentUserId = currentUser?.id ?: 1L,
                    currentTimeMs = currentTimeMs,
                    onAuctionClick = { auctionId -> viewModel.openAuctionDetail(auctionId) },
                    onVerifyCertificate = { auction -> viewModel.openAuthenticityModal(auction) }
                )
                2 -> NotificationsScreen(
                    notifications = userNotifications
                )
                3 -> ProfileScreen(
                    user = currentUser,
                    isAdminMode = isAdminMode,
                    onToggleAdminMode = { viewModel.toggleAdminMode() },
                    onUpdateProfile = { name, email, phone ->
                        viewModel.updateUserProfile(name, email, phone)
                    }
                )
                4 -> AdminPanelScreen(
                    auctions = allAuctions,
                    brands = allBrands,
                    users = allUsers,
                    transactions = allTransactions,
                    onCreateAuction = { auction -> viewModel.createAuction(auction) },
                    onCancelAuction = { id, reason -> viewModel.cancelAuction(id, reason) },
                    onStartAuction = { id -> viewModel.startAuction(id) },
                    onAddBrand = { brandName -> viewModel.addBrand(brandName) },
                    onSendStartReminder = { id -> viewModel.sendStartReminderNotification(id) }
                )
            }
        }
    }

    // Modal Bidding Sheet
    selectedAuction?.let { auction ->
        AuctionDetailSheet(
            auction = auction,
            bids = selectedAuctionBids,
            currentTimeMs = currentTimeMs,
            isParticipant = isParticipantInSelected,
            isAdmin = isAdminMode || currentUser?.isAdmin == true || currentUser?.id == 2L,
            onDismiss = { viewModel.closeAuctionDetail() },
            onPayEntryFee = { viewModel.openPayPalModal(auction) },
            onPlaceBid = { amount -> viewModel.placeBid(auction.id, amount) },
            onVerifyCertificate = { viewModel.openAuthenticityModal(auction) }
        )
    }

    // Modal PayPal Payment Gateway
    paypalAuction?.let { auction ->
        PayPalModal(
            auction = auction,
            userEmail = currentUser?.email ?: "usuario@ejemplo.com",
            onDismiss = { viewModel.closePayPalModal() },
            onConfirmPayment = { auctionId -> viewModel.confirmPayPalPayment(auctionId) }
        )
    }

    // Modal Certificate of Authenticity
    authenticityAuction?.let { auction ->
        AuthenticityCertificateModal(
            auction = auction,
            onDismiss = { viewModel.closeAuthenticityModal() }
        )
    }
}
