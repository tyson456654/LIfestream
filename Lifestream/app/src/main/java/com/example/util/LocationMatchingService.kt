package com.example.util

import android.util.Log
import com.example.data.model.User
import com.example.data.model.BloodRequest
import com.example.data.model.Hospital
import com.example.data.repository.BloodDonationRepository

/**
 * Service to store and update geographic coordinates for Users and Blood Banks,
 * enabling precise distance-based matching functionality across the LifeStream network.
 */
class LocationMatchingService(private val repository: BloodDonationRepository) {

    /**
     * Updates and persists the geographic coordinates for a given user or blood bank.
     * Keeps local state aligned with Room local persistence.
     */
    suspend fun updateLocation(userId: Int, latitude: Double, longitude: Double): Boolean {
        return try {
            val userList = repository.allUsers
            // In a suspend context, retrieve the specific user or search through the flow's current state
            var foundUser: User? = null
            repository.getPrimaryUser()?.let {
                if (it.id == userId) {
                    foundUser = it
                }
            }
            
            if (foundUser == null) {
                // If not primary user, we might need to get all users and find them
                // But normally the active logged-in user is primary. We can also handle this robustly.
                Log.d("LocationMatchingService", "Updating coordinates for user ID: $userId")
            }

            // Perform the update on database
            val currentUser = foundUser ?: repository.getPrimaryUser()
            if (currentUser != null && (currentUser.id == userId || userId == 0)) {
                val updatedUser = currentUser.copy(latitude = latitude, longitude = longitude)
                repository.updateUser(updatedUser)
                Log.i("LocationMatchingService", "Successfully saved coordinates (${latitude}, ${longitude}) for user: ${currentUser.name}")
                true
            } else {
                Log.w("LocationMatchingService", "User ID $userId not found or not primary user.")
                false
            }
        } catch (e: Exception) {
            Log.e("LocationMatchingService", "Error updating geographic coordinates", e)
            false
        }
    }

    /**
     * Core matching logic: Haversine distance calculation in kilometers.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Filters all eligible active donors or blood banks that are within a specified distance
     * and match the compatibility requirements of a specific blood request.
     */
    fun findNearbyMatches(
        centerLat: Double,
        centerLon: Double,
        allUsers: List<User>,
        bloodTypeNeeded: String,
        maxDistanceKm: Double = 25.0
    ): List<MatchedRecipient> {
        return allUsers
            .filter { user -> 
                // Only consider active users/donors or blood banks
                (user.role == "User" && user.status == "Active") || user.role == "Blood Bank"
            }
            .map { user ->
                val distance = calculateDistance(centerLat, centerLon, user.latitude, user.longitude)
                val isCompatible = isCompatibleType(user.getDecryptedBloodType(), bloodTypeNeeded)
                MatchedRecipient(user = user, distanceKm = distance, isCompatibleBloodType = isCompatible)
            }
            .filter { match ->
                match.distanceKm <= maxDistanceKm
            }
            .sortedBy { it.distanceKm }
    }

    /**
     * Determine compatibility between donor's blood type and the needed blood type.
     */
    private fun isCompatibleType(donorType: String, neededType: String): Boolean {
        if (donorType == neededType) return true
        if (donorType == "O-") return true // O- is universal donor
        if (neededType == "AB+") return true // AB+ is universal recipient

        return when (neededType) {
            "O+" -> donorType == "O-"
            "A+" -> donorType == "A-" || donorType == "O+" || donorType == "O-"
            "A-" -> donorType == "O-"
            "B+" -> donorType == "B-" || donorType == "O+" || donorType == "O-"
            "B-" -> donorType == "O-"
            "AB-" -> donorType == "A-" || donorType == "B-" || donorType == "O-"
            else -> false
        }
    }
}

/**
 * Data class representing a donor/blood bank match with their calculated distance.
 */
data class MatchedRecipient(
    val user: User,
    val distanceKm: Double,
    val isCompatibleBloodType: Boolean
)
