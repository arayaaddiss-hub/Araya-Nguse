package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "pet_stats")
data class PetStats(
    @PrimaryKey val id: Int = 1,
    val hunger: Int = 60,       // 0 = starving, 100 = full (satiety)
    val thirst: Int = 75,       // 0 = parched, 100 = quenched (hydration)
    val energy: Int = 80,       // 0 = tired, 100 = energetic
    val happiness: Int = 70,    // 0 = sad, 100 = ecstatic
    val bond: Int = 10,         // 0 to 1000
    val level: Int = 1
)

@Entity(tableName = "care_logs")
data class CareLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val actionType: String, // "FEED", "PLAY", "SLEEP", "PET", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "lula"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// 2. DAOs
@Dao
interface PetStatsDao {
    @Query("SELECT * FROM pet_stats WHERE id = 1 LIMIT 1")
    fun getPetStatsFlow(): Flow<PetStats?>

    @Query("SELECT * FROM pet_stats WHERE id = 1 LIMIT 1")
    suspend fun getPetStats(): PetStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePetStats(stats: PetStats)
}

@Dao
interface CareLogDao {
    @Query("SELECT * FROM care_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentLogsFlow(): Flow<List<CareLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CareLog)

    @Query("DELETE FROM care_logs")
    suspend fun clearLogs()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

// 3. Database
@Database(
    entities = [PetStats::class, CareLog::class, ChatMessage::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petStatsDao(): PetStatsDao
    abstract fun careLogDao(): CareLogDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lula_cub_care_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
