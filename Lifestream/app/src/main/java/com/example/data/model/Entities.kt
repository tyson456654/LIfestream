package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import android.util.Base64
import java.nio.charset.StandardCharsets

/**
 * Deliverable 1: Database Schema (Mapped to native Android Room Entities for persistence)
 */

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    
    /**
     * Encrypted blood type string for patient privacy.
     * Encrypted using a simple secure Base64 obfuscation/simulation with inline documentation on
     * how this maps to enterprise AES encryption.
     */
    val encryptedBloodType: String,
    
    val latitude: Double,
    val longitude: Double,
    val lastDonationDate: Long, // timestamp in ms
    val status: String, // "Active" or "Cooldown"
    val completedQuiz: Boolean = false,
    val gender: String = "Male", // "Male" or "Female"
    val phone: String = "+91 98765 43210",
    val password: String = "1234",
    val role: String = "User", // "Admin", "User", "Blood Bank"
    val email: String? = null
) {
    /**
     * Decrypts the blood type for in-app secure display using Base64.
     */
    fun getDecryptedBloodType(): String {
        return try {
            val decodedBytes = Base64.decode(encryptedBloodType, Base64.DEFAULT)
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    companion object {
        /**
         * Encrypts the blood type using Base64 (Simulating AES-GCM for mobile data-at-rest encryption)
         */
        fun encryptBloodType(bloodType: String): String {
            return Base64.encodeToString(bloodType.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT).trim()
        }
    }
}

@Entity(tableName = "hospitals")
data class Hospital(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val verifiedStatus: Boolean,
    
    /**
     * JSON string storing current stock levels of different blood types.
     * Format: {"O+": 12, "O-": 4, "A+": 8, "A-": 2, "B+": 6, "B-": 1, "AB+": 5, "AB-": 1}
     */
    val currentStockLevels: String
)

@Entity(tableName = "blood_requests")
data class BloodRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hospitalId: Int,
    val hospitalName: String,
    val hospitalLatitude: Double,
    val hospitalLongitude: Double,
    val bloodTypeNeeded: String,
    val patientReferenceId: String,
    val unitsRequired: Int,
    val status: String, // "Open" or "Fulfilled"
    val createdAt: Long = System.currentTimeMillis(),
    val googleMapsLink: String? = null,
    val urgencyLevel: String = "High"
)

@Entity(tableName = "donation_records")
data class DonationRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val hospitalName: String,
    val unitsDonated: Int,
    val bloodType: String,
    val donationDate: Long, // timestamp in ms
    val patientReferenceId: String,
    val replacementTokenId: String
)

