package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AuctionRepository
import com.example.data.repository.BidResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AuctionRepository(
        auctionDao = db.auctionDao(),
        userDao = db.userDao(),
        paymentDao = db.paymentDao(),
        notificationDao = db.notificationDao(),
        brandDao = db.brandDao()
    )

    // Current time ticker flow (emits every second for live countdowns)
    private val _currentTimeMs = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    // Navigation & Tab State
    private val _selectedTab = MutableStateFlow(0) // 0: Explore, 1: My Auctions, 2: Notifications, 3: Profile, 4: Admin
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Filters
    private val _selectedBrand = MutableStateFlow("Todas")
    val selectedBrand: StateFlow<String> = _selectedBrand.asStateFlow()

    private val _selectedStatus = MutableStateFlow("LIVE") // "LIVE", "UPCOMING", "COMPLETED", "ALL"
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    // Active User State
    private val _currentUserId = MutableStateFlow(1L) // 1: Standard User, 2: Admin User
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserProfile?> = _currentUserId.flatMapLatest { id ->
        repository.getUserById(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Admin Toggle
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Selected Auction for Detail Sheet/Screen
    private val _selectedAuctionId = MutableStateFlow<Long?>(null)
    val selectedAuctionId: StateFlow<Long?> = _selectedAuctionId.asStateFlow()

    val selectedAuction: StateFlow<SneakerAuction?> = _selectedAuctionId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getAuctionById(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val selectedAuctionBids: StateFlow<List<AuctionBid>> = _selectedAuctionId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getBidsForAuction(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // All Auctions & Brands
    val allAuctions: StateFlow<List<SneakerAuction>> = repository.allAuctions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allBrands: StateFlow<List<BrandCategory>> = repository.allBrands.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val userTransactions: StateFlow<List<PaymentTransaction>> = _currentUserId.flatMapLatest { id ->
        repository.getTransactionsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<AppNotification>> = _currentUserId.flatMapLatest { id ->
        repository.getNotificationsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myAuctions: StateFlow<List<SneakerAuction>> = _currentUserId.flatMapLatest { id ->
        repository.getAuctionsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Stats
    val allUsers: StateFlow<List<UserProfile>> = repository.allUsers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allTransactions: StateFlow<List<PaymentTransaction>> = repository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Dialog & Sheet States
    private val _paypalPaymentAuction = MutableStateFlow<SneakerAuction?>(null)
    val paypalPaymentAuction: StateFlow<SneakerAuction?> = _paypalPaymentAuction.asStateFlow()

    private val _authenticityCertAuction = MutableStateFlow<SneakerAuction?>(null)
    val authenticityCertAuction: StateFlow<SneakerAuction?> = _authenticityCertAuction.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isUserParticipantInSelected = MutableStateFlow(false)
    val isUserParticipantInSelected: StateFlow<Boolean> = _isUserParticipantInSelected.asStateFlow()

    init {
        // Live Ticker Coroutine
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTimeMs.value = System.currentTimeMillis()
                checkAndAutoFinalize()
            }
        }
    }

    private suspend fun checkAndAutoFinalize() {
        val now = System.currentTimeMillis()
        allAuctions.value.filter { it.status == "LIVE" && now >= it.endTimeMs }.forEach { auction ->
            repository.finalizeAuctionInternal(auction)
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setBrandFilter(brand: String) {
        _selectedBrand.value = brand
    }

    fun setStatusFilter(status: String) {
        _selectedStatus.value = status
    }

    fun openAuctionDetail(auctionId: Long) {
        _selectedAuctionId.value = auctionId
        checkUserParticipant(auctionId)
    }

    fun closeAuctionDetail() {
        _selectedAuctionId.value = null
    }

    private fun checkUserParticipant(auctionId: Long) {
        viewModelScope.launch {
            _isUserParticipantInSelected.value = repository.isUserParticipant(auctionId, _currentUserId.value)
        }
    }

    fun openPayPalModal(auction: SneakerAuction) {
        if (_isAdminMode.value || _currentUserId.value == 2L || currentUser.value?.isAdmin == true) {
            _snackbarMessage.value = "Los administradores no pueden inscribirse ni participar en las subastas."
            return
        }
        _paypalPaymentAuction.value = auction
    }

    fun closePayPalModal() {
        _paypalPaymentAuction.value = null
    }

    fun openAuthenticityModal(auction: SneakerAuction) {
        _authenticityCertAuction.value = auction
    }

    fun closeAuthenticityModal() {
        _authenticityCertAuction.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun toggleAdminMode() {
        val nextMode = !_isAdminMode.value
        _isAdminMode.value = nextMode
        _currentUserId.value = if (nextMode) 2L else 1L
        if (nextMode && _selectedTab.value == 1) {
            _selectedTab.value = 4
        }
        _snackbarMessage.value = if (nextMode) "Modo Administrador Activado" else "Modo Usuario Estándar Activado"
    }

    fun confirmPayPalPayment(auctionId: Long) {
        viewModelScope.launch {
            if (_isAdminMode.value || _currentUserId.value == 2L || currentUser.value?.isAdmin == true) {
                _snackbarMessage.value = "Los administradores no pueden inscribirse ni ofertar en las subastas."
                closePayPalModal()
                return@launch
            }
            val user = currentUser.value
            val email = user?.email ?: "usuario@ejemplo.com"
            val tx = repository.payEntryFeeViaPayPal(auctionId, _currentUserId.value, email)
            closePayPalModal()
            if (tx != null) {
                _snackbarMessage.value = "¡Pago de inscripción de $${tx.amount} USD confirmado vía PayPal!"
                checkUserParticipant(auctionId)
            } else {
                _snackbarMessage.value = "Error al procesar la inscripción con PayPal"
            }
        }
    }

    fun placeBid(auctionId: Long, bidAmount: Double) {
        viewModelScope.launch {
            if (_isAdminMode.value || _currentUserId.value == 2L || currentUser.value?.isAdmin == true) {
                _snackbarMessage.value = "Los administradores no pueden realizar ofertas en las subastas."
                return@launch
            }
            val user = currentUser.value
            val userAlias = user?.name ?: "Usuario"
            when (val result = repository.placeBid(auctionId, _currentUserId.value, userAlias, bidAmount)) {
                is BidResult.Success -> {
                    val msg = if (result.extendedTime) {
                        "¡Oferta realizada de $${String.format("%.2f", result.newAmount)} USD! ⏱️ ¡Regla 30s: Se extendió la subasta 30s!"
                    } else {
                        "¡Oferta de $${String.format("%.2f", result.newAmount)} USD aceptada correctamente!"
                    }
                    _snackbarMessage.value = msg
                }
                is BidResult.Error -> {
                    _snackbarMessage.value = result.message
                }
            }
        }
    }

    // Admin Actions
    fun createAuction(auction: SneakerAuction) {
        viewModelScope.launch {
            repository.createAuctionByAdmin(auction)
            _snackbarMessage.value = "¡Subasta de '${auction.title}' publicada con sello de autenticidad!"
        }
    }

    fun cancelAuction(auctionId: Long, reason: String) {
        viewModelScope.launch {
            repository.cancelAuctionByAdmin(auctionId, reason)
            _snackbarMessage.value = "Subasta cancelada correctamente."
        }
    }

    fun startAuction(auctionId: Long) {
        viewModelScope.launch {
            repository.startAuctionByAdmin(auctionId)
            _snackbarMessage.value = "¡Subasta iniciada en vivo!"
        }
    }

    fun addBrand(brandName: String) {
        viewModelScope.launch {
            repository.addBrand(brandName)
            _snackbarMessage.value = "Marca '$brandName' agregada al catálogo oficial."
        }
    }

    fun sendStartReminderNotification(auctionId: Long) {
        viewModelScope.launch {
            val ok = repository.sendAuctionStart30MinNotification(auctionId)
            if (ok) {
                _snackbarMessage.value = "📢 Notificación enviada: La subasta inicia en 30 minutos a todos los inscritos."
            } else {
                _snackbarMessage.value = "No se pudo enviar la notificación."
            }
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updated = user.copy(name = name, email = email, phone = phone)
            repository.updateUserProfile(updated)
            _snackbarMessage.value = "Perfil actualizado correctamente."
        }
    }
}
