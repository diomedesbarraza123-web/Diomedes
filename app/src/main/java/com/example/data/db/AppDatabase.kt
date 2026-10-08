package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SneakerAuction::class,
        AuctionBid::class,
        AuctionParticipant::class,
        UserProfile::class,
        PaymentTransaction::class,
        AppNotification::class,
        BrandCategory::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun auctionDao(): AuctionDao
    abstract fun userDao(): UserDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun brandDao(): BrandDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sneaker_auctions_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // 1. Initial Brands
                val brands = listOf(
                    "Nike", "Jordan", "Adidas", "Puma", "New Balance",
                    "ASICS", "Converse", "Vans", "Mizuno", "Saucony", "Reebok", "Under Armour"
                )
                brands.forEach { brandName ->
                    db.brandDao().insertBrand(BrandCategory(name = brandName, isSystem = true))
                }

                // 2. Initial Users (Standard User and Admin User)
                val standardUserId = db.userDao().insertUser(
                    UserProfile(
                        id = 1,
                        name = "Carlos Mendoza",
                        email = "carlos.sneakerhead@gmail.com",
                        phone = "+52 55 1234 5678",
                        avatarUrl = "",
                        totalSpent = 320.0,
                        auctionsJoined = 3,
                        auctionsWon = 1,
                        isAdmin = false
                    )
                )

                val adminUserId = db.userDao().insertUser(
                    UserProfile(
                        id = 2,
                        name = "Admin Principal (Verificador)",
                        email = "admin@subastasneakers.com",
                        phone = "+52 55 9876 5432",
                        avatarUrl = "",
                        totalSpent = 0.0,
                        auctionsJoined = 0,
                        auctionsWon = 0,
                        isAdmin = true
                    )
                )

                // 3. Initial Auctions
                val now = System.currentTimeMillis()
                val hour = 3600_000L

                val jordan1Id = db.auctionDao().insertAuction(
                    SneakerAuction(
                        id = 101,
                        title = "Air Jordan 1 High OG Chicago (2022)",
                        brand = "Jordan",
                        model = "Jordan 1 Retro High OG",
                        sku = "DZ5485-612",
                        colorway = "Varsity Red / Black / Sail",
                        size = "10.5 US (28.5 CM)",
                        condition = "Nuevo en Caja Original (DS)",
                        startingPrice = 250.0,
                        currentPrice = 320.0,
                        entryFee = 10.0,
                        minParticipants = 5,
                        currentParticipants = 7,
                        highestBidderAlias = "SneakerCollector_99",
                        highestBidderId = standardUserId,
                        startTimeMs = now - (30 * 60_1000L),
                        endTimeMs = now + (15 * 60_1000L), // 15 mins remaining
                        status = "LIVE",
                        imageUrl = "img_sneaker_jordan1_1785539300440",
                        verifiedOriginal = true,
                        originalBox = true,
                        accessories = "Agujetas extras rojas y negras, Sticker Nike Air",
                        certificateId = "CERT-2026-AJ1-8842",
                        description = "El icónico Air Jordan 1 High en su colorway original Chicago Lost & Found. 100% Auténtico, inspeccionado minuciosamente con holograma de autenticidad y factura de tienda oficial.",
                        maxBidLimit = 450.0
                    )
                )

                val dunkPandaId = db.auctionDao().insertAuction(
                    SneakerAuction(
                        id = 102,
                        title = "Nike Dunk Low Retro Panda",
                        brand = "Nike",
                        model = "Dunk Low",
                        sku = "DD1391-100",
                        colorway = "White / Black",
                        size = "9.5 US (27.5 CM)",
                        condition = "Nuevo en Caja Original",
                        startingPrice = 100.0,
                        currentPrice = 145.0,
                        entryFee = 5.0,
                        minParticipants = 3,
                        currentParticipants = 4,
                        highestBidderAlias = "KicksMaster_23",
                        highestBidderId = 0,
                        startTimeMs = now - (10 * 60_1000L),
                        endTimeMs = now + (8 * 60_1000L), // 8 mins remaining
                        status = "LIVE",
                        imageUrl = "img_sneaker_dunk_1785539311716",
                        verifiedOriginal = true,
                        originalBox = true,
                        accessories = "Etiquetas de autenticidad",
                        certificateId = "CERT-2026-DUNK-1102",
                        description = "Un clásico atemporal. El silueta Nike Dunk Low Panda en impecable estado de fábrica. Garantía de autenticidad garantizada por Subastas Sneakers."
                    )
                )

                val yeezyId = db.auctionDao().insertAuction(
                    SneakerAuction(
                        id = 103,
                        title = "Adidas Yeezy Boost 350 V2 Zebra",
                        brand = "Adidas",
                        model = "Yeezy Boost 350 V2",
                        sku = "CP9654",
                        colorway = "White / Core Black / Red",
                        size = "11 US (29 CM)",
                        condition = "Nuevo en Caja Original",
                        startingPrice = 200.0,
                        currentPrice = 200.0,
                        entryFee = 8.0,
                        minParticipants = 4,
                        currentParticipants = 2,
                        highestBidderAlias = "Sin ofertas",
                        highestBidderId = 0,
                        startTimeMs = now + (2 * hour), // Starts in 2 hours
                        endTimeMs = now + (3 * hour),
                        status = "UPCOMING",
                        imageUrl = "img_hero_sneaker_1785539290252",
                        verifiedOriginal = true,
                        originalBox = true,
                        accessories = "Suela Boost impecable, etiquetas de inventario",
                        certificateId = "CERT-2026-YZY-9021",
                        description = "Cajas selladas de la colección Yeezy. Tejido Primeknit superior y tecnología Boost en la mediasuela. Inspeccionado por nuestros expertos."
                    )
                )

                val nbId = db.auctionDao().insertAuction(
                    SneakerAuction(
                        id = 104,
                        title = "New Balance 9060 Rain Cloud",
                        brand = "New Balance",
                        model = "9060 Retro",
                        sku = "U9060ECA",
                        colorway = "Rain Cloud / Castlerock",
                        size = "9 US (27 CM)",
                        condition = "Nuevo en Caja Original",
                        startingPrice = 140.0,
                        currentPrice = 170.0,
                        entryFee = 5.0,
                        minParticipants = 3,
                        currentParticipants = 5,
                        highestBidderAlias = "Carlos Mendoza",
                        highestBidderId = standardUserId,
                        startTimeMs = now - (45 * 60_1000L),
                        endTimeMs = now + (25 * 60_1000L),
                        status = "LIVE",
                        imageUrl = "img_hero_sneaker_1785539290252",
                        verifiedOriginal = true,
                        originalBox = true,
                        accessories = "Laces crema extras",
                        certificateId = "CERT-2026-NB-3391",
                        description = "Futurismo y máxima comodidad. Modelo New Balance 9060 en su célebre tono gris Rain Cloud. 100% Original."
                    )
                )

                // 4. Initial Bids for Jordan 1
                db.auctionDao().insertBid(
                    AuctionBid(
                        auctionId = jordan1Id,
                        userId = 1,
                        userAlias = "Carlos Mendoza",
                        amount = 260.0,
                        timestampMs = now - (25 * 60_1000L)
                    )
                )
                db.auctionDao().insertBid(
                    AuctionBid(
                        auctionId = jordan1Id,
                        userId = 3,
                        userAlias = "JordanFanatic",
                        amount = 290.0,
                        timestampMs = now - (15 * 60_1000L)
                    )
                )
                db.auctionDao().insertBid(
                    AuctionBid(
                        auctionId = jordan1Id,
                        userId = 4,
                        userAlias = "SneakerCollector_99",
                        amount = 320.0,
                        timestampMs = now - (5 * 60_1000L)
                    )
                )

                // 5. Initial Participants
                db.auctionDao().insertParticipant(
                    AuctionParticipant(
                        auctionId = jordan1Id,
                        userId = standardUserId,
                        paidEntryFee = true,
                        paymentTxId = "PAYPAL-INSCR-992182",
                        timestampMs = now - (28 * 60_1000L)
                    )
                )

                // 6. Initial Payment Transaction
                db.paymentDao().insertTransaction(
                    PaymentTransaction(
                        userId = standardUserId,
                        auctionId = jordan1Id,
                        auctionTitle = "Air Jordan 1 High OG Chicago (2022)",
                        amount = 10.0,
                        timestampMs = now - (28 * 60_1000L),
                        status = "CONFIRMED",
                        paypalTxId = "PAYPAL-INSCR-992182",
                        gateway = "PayPal"
                    )
                )

                // 7. Initial Notifications
                db.notificationDao().insertNotification(
                    AppNotification(
                        userId = standardUserId,
                        title = "¡Registro exitoso!",
                        message = "Bienvenido a Subastas Sneakers. Tu cuenta ha sido verificada.",
                        type = "REGISTRATION",
                        timestampMs = now - (100 * 60_1000L),
                        isRead = true
                    )
                )
                db.notificationDao().insertNotification(
                    AppNotification(
                        userId = standardUserId,
                        title = "Inscripción confirmada vía PayPal",
                        message = "Se confirmó tu pago de $10.00 USD para la subasta 'Air Jordan 1 High OG Chicago'. ¡Buena suerte!",
                        type = "PAYMENT",
                        timestampMs = now - (28 * 60_1000L),
                        isRead = false
                    )
                )
            }
        }
    }
}
