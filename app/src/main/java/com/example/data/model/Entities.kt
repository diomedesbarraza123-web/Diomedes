package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sneaker_auctions")
data class SneakerAuction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val brand: String,
    val model: String,
    val sku: String,
    val colorway: String,
    val size: String,
    val condition: String, // e.g. "Nuevo en Caja Original"
    val startingPrice: Double,
    val currentPrice: Double,
    val entryFee: Double,
    val minParticipants: Int,
    val currentParticipants: Int = 0,
    val highestBidderAlias: String = "Sin ofertas",
    val highestBidderId: Long = 0,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val status: String = "LIVE", // "LIVE", "UPCOMING", "COMPLETED", "CANCELLED"
    val winnerUserId: Long = 0,
    val winnerDeclared: Boolean = false,
    val imageUrl: String,
    val verifiedOriginal: Boolean = true,
    val originalBox: Boolean = true,
    val accessories: String = "Laces extra, Sticker pack",
    val certificateId: String = "CERT-ORIGINAL-SNEAKER",
    val description: String = "",
    val referencePhotos: String = "",
    val maxBidLimit: Double = 0.0
)

@Entity(tableName = "auction_bids")
data class AuctionBid(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val auctionId: Long,
    val userId: Long,
    val userAlias: String,
    val amount: Double,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "auction_participants")
data class AuctionParticipant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val auctionId: Long,
    val userId: Long,
    val paidEntryFee: Boolean = true,
    val paymentTxId: String,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String = "",
    val totalSpent: Double = 0.0,
    val auctionsJoined: Int = 0,
    val auctionsWon: Int = 0,
    val isAdmin: Boolean = false
)

@Entity(tableName = "payment_transactions")
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val auctionId: Long,
    val auctionTitle: String,
    val amount: Double,
    val timestampMs: Long = System.currentTimeMillis(),
    val status: String = "CONFIRMED",
    val paypalTxId: String,
    val gateway: String = "PayPal"
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String, // "REGISTRATION", "PAYMENT", "AUCTION_START", "OUTBID", "AUCTION_WON", "AUCTION_CANCELLED"
    val timestampMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "brand_categories")
data class BrandCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isSystem: Boolean = true
)
