package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import com.example.data.model.Hospital
import com.example.data.model.BloodRequest
import com.example.data.model.DonationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getPrimaryUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospitals")
    fun getAllHospitalsFlow(): Flow<List<Hospital>>

    @Query("SELECT * FROM hospitals")
    suspend fun getAllHospitals(): List<Hospital>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospital(hospital: Hospital)

    @Query("DELETE FROM hospitals")
    suspend fun clearAll()
}

@Dao
interface BloodRequestDao {
    @Query("SELECT * FROM blood_requests ORDER BY createdAt DESC")
    fun getAllRequestsFlow(): Flow<List<BloodRequest>>

    @Query("SELECT * FROM blood_requests ORDER BY createdAt DESC")
    suspend fun getAllRequests(): List<BloodRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BloodRequest)

    @Update
    suspend fun updateRequest(request: BloodRequest)

    @Query("DELETE FROM blood_requests WHERE id = :id")
    suspend fun deleteRequestById(id: Int)

    @Query("DELETE FROM blood_requests")
    suspend fun clearAll()
}

@Dao
interface DonationRecordDao {
    @Query("SELECT * FROM donation_records WHERE userId = :userId ORDER BY donationDate DESC")
    fun getRecordsByUserIdFlow(userId: Int): Flow<List<DonationRecord>>

    @Query("SELECT * FROM donation_records ORDER BY donationDate DESC")
    suspend fun getAllRecords(): List<DonationRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DonationRecord)

    @Query("DELETE FROM donation_records")
    suspend fun clearAll()
}

@Database(entities = [User::class, Hospital::class, BloodRequest::class, DonationRecord::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun hospitalDao(): HospitalDao
    abstract fun bloodRequestDao(): BloodRequestDao
    abstract fun donationRecordDao(): DonationRecordDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifestream_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
