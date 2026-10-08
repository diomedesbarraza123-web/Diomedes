package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

sealed class BidResult {
    data class Success(val newAmount: Double, val extendedTime: Boolean) : BidResult()
    data class Error(val message: String) : BidResult()
}

class AuctionRepository(
    private val auctionDao: AuctionDao,
    private val userDao: UserDao,
    private val paymentDao: PaymentDao,
    private val notificationDao: NotificationDao,
    private val brandDao: BrandDao
) {
    val allAuctions: Flow<List<SneakerAuction>> = auctionDao.getAllAuctions()
    val liveAuctions: Flow<List<SneakerAuction>> = auctionDao.getAuctionsByStatus("LIVE")
    val upcomingAuctions: Flow<List<SneakerAuction>> = auctionDao.getAuctionsByStatus("UPCOMING")
    val completedAuctions: Flow<List<SneakerAuction>> = auctionDao.getAuctionsByStatus("COMPLETED")
    val allBrands: Flow<List<BrandCategory>> = brandDao.getAllBrands()
    val allUsers: Flow<List<UserProfile>> = userDao.getAllUsers()
    val allTransactions: Flow<List<PaymentTransaction>> = paymentDao.getAllTransactions()

    fun getAuctionById(id: Long): Flow<SneakerAuction?> = auctionDao.getAuctionById(id)
    fun getBidsForAuction(auctionId: Long): Flow<List<AuctionBid>> = auctionDao.getBidsForAuction(auctionId)
    fun getUserById(userId: Long): Flow<UserProfile?> = userDao.getUserById(userId)
    fun getTransactionsForUser(userId: Long): Flow<List<PaymentTransaction>> = paymentDao.getTransactionsForUser(userId)
    fun getNotificationsForUser(userId: Long): Flow<List<AppNotification>> = notificationDao.getNotificationsForUser(userId)
    fun getAuctionsForUser(userId: Long): Flow<List<SneakerAuction>> = auctionDao.getAuctionsForUser(userId)

    suspend fun isUserParticipant(auctionId: Long, userId: Long): Boolean {
        return auctionDao.getParticipant(auctionId, userId) != null
    }

    suspend fun payEntryFeeViaPayPal(
        auctionId: Long,
        userId: Long,
        userEmail: String
    ): PaymentTransaction? {
        val auction = auctionDao.getAuctionByIdSync(auctionId) ?: return null
        val user = userDao.getUserByIdSync(userId) ?: return null

        if (user.isAdmin || userId == 2L) {
            // Administrators are restricted from joining auctions as regular users
            return null
        }

        val paypalTxId = "PAYPAL-INSCR-${System.currentTimeMillis().toString().takeLast(6)}-${(100..999).random()}"

        // Insert Participant
        auctionDao.insertParticipant(
            AuctionParticipant(
                auctionId = auctionId,
                userId = userId,
                paidEntryFee = true,
                paymentTxId = paypalTxId,
                timestampMs = System.currentTimeMillis()
            )
        )

        // Increment participants count
        val newParticipantCount = auction.currentParticipants + 1
        val updatedAuction = auction.copy(
            currentParticipants = newParticipantCount
        )
        auctionDao.updateAuction(updatedAuction)

        // Insert Payment Transaction
        val transaction = PaymentTransaction(
            userId = userId,
            auctionId = auctionId,
            auctionTitle = auction.title,
            amount = auction.entryFee,
            timestampMs = System.currentTimeMillis(),
            status = "CONFIRMED",
            paypalTxId = paypalTxId,
            gateway = "PayPal"
        )
        val txId = paymentDao.insertTransaction(transaction)

        // Send Notification to participant
        notificationDao.insertNotification(
            AppNotification(
                userId = userId,
                title = "Pago Confirmado con PayPal",
                message = "Inscripción aprobada ($${String.format("%.2f", auction.entryFee)} USD) para '${auction.title}'. Transacción PayPal: $paypalTxId",
                type = "PAYMENT",
                timestampMs = System.currentTimeMillis()
            )
        )

        // Send Enrollment Confirmation Notification
        notificationDao.insertNotification(
            AppNotification(
                userId = userId,
                title = "🎉 ¡Inscripción Exitosa a la Subasta!",
                message = "Te has inscrito exitosamente en '${auction.title}'. Ya puedes ver el panel de ofertas en la aplicación. Ten en cuenta que las pujas se activarán únicamente cuando la subasta inicie oficialmente.",
                type = "REGISTRATION_SUCCESS",
                timestampMs = System.currentTimeMillis() + 10
            )
        )

        // Check if all required participants have enrolled (quota complete!)
        if (newParticipantCount >= auction.minParticipants) {
            val nowMs = System.currentTimeMillis()

            // 1. Send Notification to Administrator that quota is full
            notificationDao.insertNotification(
                AppNotification(
                    userId = 2L,
                    title = "¡Cupo de participantes completado! 👥",
                    message = "¡Atención Administrador! Todos los ${auction.minParticipants} participantes requeridos se han inscrito en la subasta '${auction.title}'. La subasta está lista para iniciar.",
                    type = "REGISTRATION_COMPLETE",
                    timestampMs = nowMs
                )
            )

            // 2. Automatically send 30-minute start notification to enrolled participants
            sendAuctionStart30MinNotification(auctionId)
        }

        // Update User stats
        userDao.updateUser(
            user.copy(
                auctionsJoined = user.auctionsJoined + 1,
                totalSpent = user.totalSpent + auction.entryFee
            )
        )

        return transaction.copy(id = txId)
    }

    suspend fun sendAuctionStart30MinNotification(auctionId: Long): Boolean {
        val auction = auctionDao.getAuctionByIdSync(auctionId) ?: return false
        val participants = auctionDao.getParticipantsForAuction(auctionId)
        val nowMs = System.currentTimeMillis()

        // Notify enrolled participants
        participants.forEach { participant ->
            notificationDao.insertNotification(
                AppNotification(
                    userId = participant.userId,
                    title = "⏰ ¡La subasta inicia en 30 minutos!",
                    message = "El administrador te informa que la subasta '${auction.title}' iniciará en 30 minutos de forma automática. ¡Prepara tus ofertas!",
                    type = "AUCTION_START",
                    timestampMs = nowMs
                )
            )
        }

        // Send confirmation alert to Admin
        notificationDao.insertNotification(
            AppNotification(
                userId = 2L,
                title = "Notificación de inicio enviada 📢",
                message = "Se notificó automáticamente a los ${participants.size} participantes de '${auction.title}' que la subasta iniciará en 30 minutos.",
                type = "ADMIN_ALERT",
                timestampMs = nowMs
            )
        )
        return true
    }

    suspend fun placeBid(
        auctionId: Long,
        userId: Long,
        userAlias: String,
        bidAmount: Double
    ): BidResult {
        val user = userDao.getUserByIdSync(userId)
        if (user?.isAdmin == true || userId == 2L) {
            return BidResult.Error("Los administradores no tienen permitido realizar ofertas en las subastas.")
        }

        val auction = auctionDao.getAuctionByIdSync(auctionId)
            ?: return BidResult.Error("La subasta no existe.")

        if (auction.status != "LIVE") {
            return BidResult.Error("La subasta no está activa.")
        }

        val now = System.currentTimeMillis()
        if (now >= auction.endTimeMs) {
            // Auto finalize
            finalizeAuctionInternal(auction)
            return BidResult.Error("La subasta ya ha finalizado.")
        }

        // Verify participant - Auto-enroll if needed when placing bid
        var isParticipant = isUserParticipant(auctionId, userId)
        if (!isParticipant) {
            val email = user?.email ?: "usuario@ejemplo.com"
            payEntryFeeViaPayPal(auctionId, userId, email)
            isParticipant = true
        }

        // Rule: Bid must be strictly higher than current price
        if (bidAmount <= auction.currentPrice) {
            return BidResult.Error("Tu oferta debe ser superior al precio actual ($${String.format("%.2f", auction.currentPrice)} USD).")
        }

        // Rule: 30-Second Extension
        val timeRemainingMs = auction.endTimeMs - now
        var extended = false
        var newEndTimeMs = auction.endTimeMs

        if (timeRemainingMs in 1..30_000L) {
            // Extend auction time by 30 seconds
            newEndTimeMs = now + 30_000L
            extended = true
        }

        val previousHighestBidderId = auction.highestBidderId
        val isMaxLimitReached = auction.maxBidLimit > 0.0 && bidAmount >= auction.maxBidLimit

        // Insert Bid
        auctionDao.insertBid(
            AuctionBid(
                auctionId = auctionId,
                userId = userId,
                userAlias = userAlias,
                amount = bidAmount,
                timestampMs = now
            )
        )

        if (isMaxLimitReached) {
            // Instant Win: Max Bid Limit Reached!
            val updatedAuction = auction.copy(
                currentPrice = bidAmount,
                highestBidderAlias = userAlias,
                highestBidderId = userId,
                status = "COMPLETED",
                winnerUserId = userId,
                winnerDeclared = true
            )
            auctionDao.updateAuction(updatedAuction)

            // Update Winner Profile Stats
            val winner = userDao.getUserByIdSync(userId)
            if (winner != null) {
                userDao.updateUser(
                    winner.copy(
                        auctionsWon = winner.auctionsWon + 1,
                        totalSpent = winner.totalSpent + bidAmount
                    )
                )
            }

            // Notification to Winner
            notificationDao.insertNotification(
                AppNotification(
                    userId = userId,
                    title = "🏆 ¡GANASTE LA SUBASTA INSTANTÁNEAMENTE!",
                    message = "¡Felicidades $userAlias! Tu oferta de $${String.format("%.2f", bidAmount)} USD alcanzó el tope máximo de $${String.format("%.2f", auction.maxBidLimit)} USD establecido por el administrador para '${auction.title}'. ¡El producto es tuyo!",
                    type = "AUCTION_WON",
                    timestampMs = now
                )
            )

            // Notification to Admin
            notificationDao.insertNotification(
                AppNotification(
                    userId = 2L,
                    title = "🎉 ¡Tope Máximo Alcanzado y Ganador Declarado!",
                    message = "El comprador $userAlias alcanzó el costo máximo de $${String.format("%.2f", auction.maxBidLimit)} USD para '${auction.title}'. La subasta finalizó automáticamente.",
                    type = "ADMIN_ALERT",
                    timestampMs = now
                )
            )

            // Outbid notification if another user was leading
            if (previousHighestBidderId != 0L && previousHighestBidderId != userId) {
                notificationDao.insertNotification(
                    AppNotification(
                        userId = previousHighestBidderId,
                        title = "Subasta Finalizada - Tope Alcanzado",
                        message = "Un comprador ha alcanzado el precio tope de $${String.format("%.2f", auction.maxBidLimit)} USD en '${auction.title}' y la subasta ha concluido.",
                        type = "OUTBID",
                        timestampMs = now
                    )
                )
            }

            return BidResult.Success(newAmount = bidAmount, extendedTime = false)
        } else {
            // Standard Bid Update
            val updatedAuction = auction.copy(
                currentPrice = bidAmount,
                highestBidderAlias = userAlias,
                highestBidderId = userId,
                endTimeMs = newEndTimeMs
            )
            auctionDao.updateAuction(updatedAuction)

            // Outbid notification to previous highest bidder
            if (previousHighestBidderId != 0L && previousHighestBidderId != userId) {
                notificationDao.insertNotification(
                    AppNotification(
                        userId = previousHighestBidderId,
                        title = "¡Superado en oferta!",
                        message = "Un participante ha superado tu oferta en '${auction.title}' con $${String.format("%.2f", bidAmount)} USD. ¡Haz otra oferta!",
                        type = "OUTBID",
                        timestampMs = now
                    )
                )
            }

            return BidResult.Success(newAmount = bidAmount, extendedTime = extended)
        }
    }

    suspend fun createAuctionByAdmin(auction: SneakerAuction): Long {
        val now = System.currentTimeMillis()
        val newAuction = auction.copy(
            verifiedOriginal = true,
            certificateId = "CERT-${(1000..9999).random()}-${auction.brand.take(3).uppercase()}"
        )
        val id = auctionDao.insertAuction(newAuction)

        // Notify all users about new auction
        val users = userDao.getAllUsers()
        // Send alert
        notificationDao.insertNotification(
            AppNotification(
                userId = 1, // standard user
                title = "Nueva Subasta Programada",
                message = "El administrador ha publicado el sneaker original '${auction.title}'. Talla: ${auction.size}.",
                type = "AUCTION_START",
                timestampMs = now
            )
        )
        return id
    }

    suspend fun cancelAuctionByAdmin(auctionId: Long, reason: String) {
        val auction = auctionDao.getAuctionByIdSync(auctionId) ?: return
        val updated = auction.copy(status = "CANCELLED")
        auctionDao.updateAuction(updated)

        notificationDao.insertNotification(
            AppNotification(
                userId = 1,
                title = "Subasta Cancelada por el Administrador",
                message = "La subasta '${auction.title}' ha sido cancelada. Motivo: $reason",
                type = "AUCTION_CANCELLED",
                timestampMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun startAuctionByAdmin(auctionId: Long) {
        val auction = auctionDao.getAuctionByIdSync(auctionId) ?: return
        val now = System.currentTimeMillis()
        val durationMs = 30 * 60_1000L // Strictly 30 minutes duration
        val updated = auction.copy(
            status = "LIVE",
            startTimeMs = now,
            endTimeMs = now + durationMs
        )
        auctionDao.updateAuction(updated)

        notificationDao.insertNotification(
            AppNotification(
                userId = 1,
                title = "¡Subasta Iniciada y Activa!",
                message = "La subasta '${auction.title}' ha comenzado y está ACTIVA por 30 minutos. ¡Ingresa ahora para ofertar!",
                type = "AUCTION_START",
                timestampMs = now
            )
        )
    }

    suspend fun finalizeAuctionInternal(auction: SneakerAuction) {
        if (auction.status == "COMPLETED") return
        val winnerUserId = auction.highestBidderId
        val updated = auction.copy(
            status = "COMPLETED",
            winnerUserId = winnerUserId,
            winnerDeclared = true
        )
        auctionDao.updateAuction(updated)

        if (winnerUserId != 0L) {
            val winner = userDao.getUserByIdSync(winnerUserId)
            if (winner != null) {
                userDao.updateUser(
                    winner.copy(
                        auctionsWon = winner.auctionsWon + 1,
                        totalSpent = winner.totalSpent + auction.currentPrice
                    )
                )
            }
            notificationDao.insertNotification(
                AppNotification(
                    userId = winnerUserId,
                    title = "¡Felicidades! Has ganado la subasta 🎉",
                    message = "Has ganado '${auction.title}' por $${String.format("%.2f", auction.currentPrice)} USD. Tu certificado de autenticidad es ${auction.certificateId}.",
                    type = "AUCTION_WON",
                    timestampMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun addBrand(brandName: String) {
        brandDao.insertBrand(BrandCategory(name = brandName.trim(), isSystem = false))
    }

    suspend fun registerUser(name: String, email: String, phone: String): UserProfile {
        val newUser = UserProfile(
            name = name,
            email = email,
            phone = phone,
            isAdmin = false
        )
        val id = userDao.insertUser(newUser)
        val created = newUser.copy(id = id)

        notificationDao.insertNotification(
            AppNotification(
                userId = id,
                title = "¡Registro Completado!",
                message = "Bienvenido $name a Subastas Sneakers. Cuenta verificada correctamente.",
                type = "REGISTRATION",
                timestampMs = System.currentTimeMillis()
            )
        )
        return created
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userDao.updateUser(profile)
    }
}
