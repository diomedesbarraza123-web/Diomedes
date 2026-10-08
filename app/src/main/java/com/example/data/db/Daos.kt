package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuctionDao {
    @Query("SELECT * FROM sneaker_auctions ORDER BY id DESC")
    fun getAllAuctions(): Flow<List<SneakerAuction>>

    @Query("SELECT * FROM sneaker_auctions WHERE status = :status ORDER BY endTimeMs ASC")
    fun getAuctionsByStatus(status: String): Flow<List<SneakerAuction>>

    @Query("SELECT * FROM sneaker_auctions WHERE id = :id")
    fun getAuctionById(id: Long): Flow<SneakerAuction?>

    @Query("SELECT * FROM sneaker_auctions WHERE id = :id")
    suspend fun getAuctionByIdSync(id: Long): SneakerAuction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuction(auction: SneakerAuction): Long

    @Update
    suspend fun updateAuction(auction: SneakerAuction)

    @Delete
    suspend fun deleteAuction(auction: SneakerAuction)

    @Query("DELETE FROM sneaker_auctions WHERE id = :id")
    suspend fun deleteAuctionById(id: Long)

    // Bids
    @Query("SELECT * FROM auction_bids WHERE auctionId = :auctionId ORDER BY amount DESC, timestampMs DESC")
    fun getBidsForAuction(auctionId: Long): Flow<List<AuctionBid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: AuctionBid): Long

    // Participants
    @Query("SELECT * FROM auction_participants WHERE auctionId = :auctionId AND userId = :userId LIMIT 1")
    suspend fun getParticipant(auctionId: Long, userId: Long): AuctionParticipant?

    @Query("SELECT COUNT(*) FROM auction_participants WHERE auctionId = :auctionId")
    suspend fun getParticipantCount(auctionId: Long): Int

    @Query("SELECT * FROM auction_participants WHERE auctionId = :auctionId")
    suspend fun getParticipantsForAuction(auctionId: Long): List<AuctionParticipant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: AuctionParticipant): Long

    @Query("SELECT * FROM sneaker_auctions WHERE id IN (SELECT auctionId FROM auction_participants WHERE userId = :userId)")
    fun getAuctionsForUser(userId: Long): Flow<List<SneakerAuction>>
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun getUserById(id: Long): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserByIdSync(id: Long): UserProfile?

    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile): Long

    @Update
    suspend fun updateUser(user: UserProfile)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_transactions WHERE userId = :userId ORDER BY timestampMs DESC")
    fun getTransactionsForUser(userId: Long): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payment_transactions ORDER BY timestampMs DESC")
    fun getAllTransactions(): Flow<List<PaymentTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransaction): Long
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM app_notifications WHERE userId = :userId ORDER BY timestampMs DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE app_notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Long)
}

@Dao
interface BrandDao {
    @Query("SELECT * FROM brand_categories ORDER BY name ASC")
    fun getAllBrands(): Flow<List<BrandCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrand(brand: BrandCategory): Long
}
