package com.example.data.repository

import android.util.Log
import com.example.data.db.UserDao
import com.example.data.db.HospitalDao
import com.example.data.db.BloodRequestDao
import com.example.data.db.DonationRecordDao
import com.example.data.model.User
import com.example.data.model.Hospital
import com.example.data.model.BloodRequest
import com.example.data.model.DonationRecord
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

data class MatchingNotification(
    val recipientName: String,
    val recipientId: Int,
    val distanceKm: Double,
    val deviceToken: String,
    val payload: String, // JSON payload simulation
    val gender: String = "Male",
    val phone: String = "+91 98765 43210",
    val bloodType: String = "",
    val email: String? = null
)

class BloodDonationRepository(
    private val userDao: UserDao,
    private val hospitalDao: HospitalDao,
    private val bloodRequestDao: BloodRequestDao,
    private val donationRecordDao: DonationRecordDao
) {
    val allUsers: Flow<List<User>> = userDao.getAllUsersFlow()
    val allHospitals: Flow<List<Hospital>> = hospitalDao.getAllHospitalsFlow()
    val allRequests: Flow<List<BloodRequest>> = bloodRequestDao.getAllRequestsFlow()

    fun getDonationRecordsFlow(userId: Int): Flow<List<DonationRecord>> = donationRecordDao.getRecordsByUserIdFlow(userId)
    suspend fun insertDonationRecord(record: DonationRecord) = donationRecordDao.insertRecord(record)
    suspend fun clearAllDonationRecords() = donationRecordDao.clearAll()

    suspend fun getPrimaryUser(): User? = userDao.getPrimaryUser()

    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun deleteUser(userId: Int) = userDao.deleteUserById(userId)
    
    suspend fun insertRequest(request: BloodRequest) = bloodRequestDao.insertRequest(request)
    suspend fun updateRequest(request: BloodRequest) = bloodRequestDao.updateRequest(request)
    suspend fun deleteRequest(requestId: Int) = bloodRequestDao.deleteRequestById(requestId)

    suspend fun insertHospital(hospital: Hospital) = hospitalDao.insertHospital(hospital)

    /**
     * Checks if a user has completed their cooldown.
     * Whole blood donation has a minimum cooldown of 90 days for men and 120 days for women.
     */
    fun isDonorActive(lastDonationDate: Long, gender: String = "Male"): Boolean {
        if (lastDonationDate == 0L) return true // Never donated, eligible
        val diffMs = System.currentTimeMillis() - lastDonationDate
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
        val interval = if (gender.equals("Female", ignoreCase = true)) 120 else 90
        return diffDays >= interval
    }

    /**
     * Helper to compute days remaining.
     */
    fun getCooldownDaysRemaining(lastDonationDate: Long, gender: String = "Male"): Int {
        if (lastDonationDate == 0L) return 0
        val diffMs = System.currentTimeMillis() - lastDonationDate
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
        val interval = if (gender.equals("Female", ignoreCase = true)) 120 else 90
        val remaining = interval - diffDays
        return if (remaining > 0) remaining else 0
    }

    /**
     * Deliverable 3: Location & Matching Logic
     * Matches active users within a 15km radius of the requesting hospital with compatible blood type.
     */
    suspend fun triggerBloodRequestCreatedMatching(request: BloodRequest): List<MatchingNotification> {
        val users = userDao.getAllUsers()
        val matchedNotifications = mutableListOf<MatchingNotification>()

        for (user in users) {
            // Decrypt the user's encrypted blood type
            val donorBloodType = user.getDecryptedBloodType()
            
            // Criteria 1: Check active status (based on gender cooldown intervals)
            val isDonorActive = isDonorActive(user.lastDonationDate, user.gender)
            
            // Criteria 2: Match compatible blood types (including universal donor O-)
            val isCompatible = isBloodTypeCompatible(donorBloodType, request.bloodTypeNeeded)
            
            if (isDonorActive && isCompatible) {
                // Criteria 3: Geospatial range check (<= 15 km) using Haversine formula
                val distance = calculateDistanceInKm(
                    user.latitude, user.longitude,
                    request.hospitalLatitude, request.hospitalLongitude
                )
                
                if (distance <= 15.0) {
                    val simulatedFcmToken = "fcm_token_usr_${user.id}_" + user.name.lowercase().replace(" ", "_")
                    
                    // Generate full-stack high-priority JSON push notification payload requested
                    val fcmPayloadJson = """
                    {
                      "to": "$simulatedFcmToken",
                      "priority": "high",
                      "notification": {
                        "title": "URGENT: Blood Needed Nearby",
                        "body": "Hi ${user.name}, ${request.hospitalName} needs ${request.unitsRequired} units of ${request.bloodTypeNeeded} blood. You are ${String.format("%.1f", distance)} km away and can donate. Safe, tested blood will be released to the patient immediately when you pledge."
                      },
                      "data": {
                        "requestId": "${request.id}",
                        "hospitalId": "${request.hospitalId}",
                        "bloodType": "${request.bloodTypeNeeded}",
                        "hospitalName": "${request.hospitalName}",
                        "patientReferenceId": "${request.patientReferenceId}",
                        "distance": "$distance",
                        "type": "URGENT_LOGISTICS_MATCH"
                      }
                    }
                    """.trimIndent()
                    
                    Log.i("LifeStreamMatching", "High-Priority Notification Dispatched to User: ${user.name}\n$fcmPayloadJson")
                    
                    matchedNotifications.add(
                        MatchingNotification(
                            recipientName = user.name,
                            recipientId = user.id,
                            distanceKm = distance,
                            deviceToken = simulatedFcmToken,
                            payload = fcmPayloadJson,
                            gender = if (user.gender.isNotBlank()) user.gender else "Male",
                            phone = if (user.phone.isNotBlank()) user.phone else "+91 98765 43210",
                            bloodType = donorBloodType,
                            email = user.email
                        )
                    )
                }
            }
        }
        return matchedNotifications.sortedBy { it.distanceKm }
    }

    /**
     * Exact geospatial sphere distance calculation using the Haversine formula.
     */
    fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
     * Determines compatibility. Standard clinical rules:
     * - O- is universal donor (compatible with all neededTypes).
     * - O+ can donate to Rh+ (O+, A+, B+, AB+).
     * - A- can donate to A-, A+, AB-, AB+.
     * - A+ can donate to A+, AB+.
     * - B- can donate to B-, B+, AB-, AB+.
     * - B+ can donate to B+, AB+.
     * - AB- can donate to AB-, AB+.
     * - AB+ can only donate to AB+.
     */
    fun isBloodTypeCompatible(donorType: String, neededType: String): Boolean {
        if (donorType == "O-") return true // Universal donor
        if (donorType == neededType) return true // Exact match
        
        val isDonorRhPositive = donorType.endsWith("+")
        val isNeededRhPositive = neededType.endsWith("+")
        
        // If receptor is Rh- but donor is Rh+, incompatible due to severe immune reaction
        if (isDonorRhPositive && !isNeededRhPositive) return false
        
        val donorLetter = donorType.dropLast(1)
        val neededLetter = neededType.dropLast(1)
        
        return when (donorLetter) {
            "O" -> true // O can donate to A, B, O, AB if Rh matches (which we verified above)
            "A" -> neededLetter == "A" || neededLetter == "AB"
            "B" -> neededLetter == "B" || neededLetter == "AB"
            "AB" -> neededLetter == "AB"
            else -> false
        }
    }

    /**
     * Prepopulates initial high-quality clinical and mock data so the app displays realistic scenarios
     */
    suspend fun seedMockDatabase() {
        // Delete old hospital list and re-seed with Gudivada hospitals and blood banks
        val existingHospitals = hospitalDao.getAllHospitals()
        val hasGudivadaOnly = existingHospitals.isNotEmpty() && existingHospitals.all {
            it.name.contains("Gudivada") || it.name.contains("Area Hospital") || it.name.contains("EVR") ||
            it.name.contains("Vivaan") || it.name.contains("Maganti") || it.name.contains("Rise") || it.name.contains("Sree Rama")
        }

        if (!hasGudivadaOnly || existingHospitals.size < 6) {
            // Delete previous non-Gudivada hospitals
            hospitalDao.clearAll()
            bloodRequestDao.clearAll()

            // Seed Gudivada Hospitals and Blood Banks (Krishna District, Andhra Pradesh - Coordinates: ~16.4326, 80.9928)
            val hosp1 = Hospital(
                name = "Area Hospital & Blood Bank (Government)",
                latitude = 16.4335,
                longitude = 80.9940,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 16, "O-": 4, "A+": 12, "A-": 3, "B+": 14, "B-": 2, "AB+": 6, "AB-": 1}"""
            )
            val hosp2 = Hospital(
                name = "Gudivada Blood Centre & Blood Bank",
                latitude = 16.4350,
                longitude = 80.9915,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 20, "O-": 6, "A+": 15, "A-": 4, "B+": 18, "B-": 3, "AB+": 8, "AB-": 2}"""
            )
            val hosp3 = Hospital(
                name = "A.P. Voluntary Blood Centre (Gudivada)",
                latitude = 16.4385,
                longitude = 80.9972,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 18, "O-": 5, "A+": 10, "A-": 2, "B+": 12, "B-": 1, "AB+": 5, "AB-": 1}"""
            )
            val hosp4 = Hospital(
                name = "EVR Super Speciality Hospital (Gudivada)",
                latitude = 16.4310,
                longitude = 80.9890,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 14, "O-": 3, "A+": 9, "A-": 2, "B+": 11, "B-": 1, "AB+": 4, "AB-": 1}"""
            )
            val hosp5 = Hospital(
                name = "Vivaan Hospitals (Emergency & Critical Care)",
                latitude = 16.4342,
                longitude = 80.9955,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 12, "O-": 2, "A+": 8, "A-": 1, "B+": 9, "B-": 2, "AB+": 3, "AB-": 1}"""
            )
            val hosp6 = Hospital(
                name = "Maganti Hospital (Gudivada)",
                latitude = 16.4360,
                longitude = 80.9930,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 10, "O-": 2, "A+": 7, "A-": 1, "B+": 8, "B-": 1, "AB+": 3, "AB-": 0}"""
            )
            val hosp7 = Hospital(
                name = "Rise Hospitals (Bone & Joint Institute)",
                latitude = 16.4295,
                longitude = 80.9910,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 8, "O-": 1, "A+": 6, "A-": 1, "B+": 7, "B-": 0, "AB+": 2, "AB-": 0}"""
            )
            val hosp8 = Hospital(
                name = "Sree Rama Nursing Home (Dr. Gangadhara Rao)",
                latitude = 16.4320,
                longitude = 80.9960,
                verifiedStatus = true,
                currentStockLevels = """{"O+": 7, "O-": 1, "A+": 5, "A-": 0, "B+": 6, "B-": 1, "AB+": 2, "AB-": 0}"""
            )

            hospitalDao.insertHospital(hosp1)
            hospitalDao.insertHospital(hosp2)
            hospitalDao.insertHospital(hosp3)
            hospitalDao.insertHospital(hosp4)
            hospitalDao.insertHospital(hosp5)
            hospitalDao.insertHospital(hosp6)
            hospitalDao.insertHospital(hosp7)
            hospitalDao.insertHospital(hosp8)

            // Seed Gudivada Blood Requests
            val req1 = BloodRequest(
                hospitalId = 1,
                hospitalName = "Area Hospital & Blood Bank (Government)",
                hospitalLatitude = 16.4335,
                hospitalLongitude = 80.9940,
                bloodTypeNeeded = "O+",
                patientReferenceId = "PAT-GDV-810",
                unitsRequired = 4,
                status = "Open"
            )
            val req2 = BloodRequest(
                hospitalId = 2,
                hospitalName = "Gudivada Blood Centre & Blood Bank",
                hospitalLatitude = 16.4350,
                hospitalLongitude = 80.9915,
                bloodTypeNeeded = "O-", // Urgent negative blood request matching universal donor
                patientReferenceId = "PAT-GDV-129",
                unitsRequired = 3,
                status = "Open"
            )
            val req3 = BloodRequest(
                hospitalId = 4,
                hospitalName = "EVR Super Speciality Hospital (Gudivada)",
                hospitalLatitude = 16.4310,
                hospitalLongitude = 80.9890,
                bloodTypeNeeded = "A+",
                patientReferenceId = "PAT-GDV-403",
                unitsRequired = 2,
                status = "Open"
            )
            val req4 = BloodRequest(
                hospitalId = 5,
                hospitalName = "Vivaan Hospitals (Emergency & Critical Care)",
                hospitalLatitude = 16.4342,
                hospitalLongitude = 80.9955,
                bloodTypeNeeded = "B+",
                patientReferenceId = "PAT-GDV-505",
                unitsRequired = 2,
                status = "Open"
            )
            bloodRequestDao.insertRequest(req1)
            bloodRequestDao.insertRequest(req2)
            bloodRequestDao.insertRequest(req3)
            bloodRequestDao.insertRequest(req4)
        }

        // Independently verify and seed the three core test users
        // Independently verify and seed the three core test users in Gudivada
        val existingUsers = userDao.getAllUsers()

        // 1. Rajesh Kumar (Donor O-)
        val hasRajesh = existingUsers.any { it.phone.replace(" ", "") == "+919830122910" }
        if (!hasRajesh) {
            val primaryUser = User(
                name = "Rajesh Kumar",
                encryptedBloodType = User.encryptBloodType("O-"),
                latitude = 16.4330,
                longitude = 80.9935,
                lastDonationDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(105),
                status = "Active",
                completedQuiz = true,
                gender = "Male",
                phone = "+91 98301 22910",
                password = "1234",
                role = "User",
                email = "donor@lifestream.com"
            )
            userDao.insertUser(primaryUser)

            // Feed mock donation records matching his autogenerated id
            val rajeshFromDb = userDao.getAllUsers().find { it.phone.replace(" ", "") == "+919830122910" }
            if (rajeshFromDb != null) {
                donationRecordDao.insertRecord(
                    DonationRecord(
                        userId = rajeshFromDb.id,
                        hospitalName = "Area Hospital & Blood Bank (Government)",
                        unitsDonated = 2,
                        bloodType = "O-",
                        donationDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(105),
                        patientReferenceId = "PAT-GDV-810",
                        replacementTokenId = "LST-GDV-810-TKN"
                    )
                )
                donationRecordDao.insertRecord(
                    DonationRecord(
                        userId = rajeshFromDb.id,
                        hospitalName = "Gudivada Blood Centre & Blood Bank",
                        unitsDonated = 1,
                        bloodType = "O-",
                        donationDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(195),
                        patientReferenceId = "PAT-GDV-129",
                        replacementTokenId = "LST-GDV-129-TKN"
                    )
                )
            }
        }

        // 2. System Administrator
        val hasAdmin = existingUsers.any { it.email?.lowercase() == "admin@lifestream.com" }
        if (!hasAdmin) {
            val adminUser = User(
                name = "System Admin",
                encryptedBloodType = User.encryptBloodType("AB+"),
                latitude = 16.4326,
                longitude = 80.9928,
                lastDonationDate = 0L,
                status = "Active",
                completedQuiz = true,
                gender = "Male",
                phone = "+91 99999 99999",
                password = "1234",
                role = "Admin",
                email = "admin@lifestream.com"
            )
            userDao.insertUser(adminUser)
        }

        // 3. Area Hospital Blood Bank User
        val hasBank = existingUsers.any { it.email?.lowercase() == "bank@lifestream.com" }
        if (!hasBank) {
            val bloodBankUser = User(
                name = "Area Hospital Blood Bank User",
                encryptedBloodType = User.encryptBloodType("O+"),
                latitude = 16.4335,
                longitude = 80.9940,
                lastDonationDate = 0L,
                status = "Active",
                completedQuiz = true,
                gender = "Male",
                phone = "+91 88888 88888",
                password = "1234",
                role = "Blood Bank",
                email = "bank@lifestream.com"
            )
            userDao.insertUser(bloodBankUser)
        }

        // Seed other mock users in Gudivada if the database user list was entirely empty
        if (existingUsers.isEmpty()) {
            val user2 = User(
                name = "Rakesh Sharma",
                encryptedBloodType = User.encryptBloodType("O+"),
                latitude = 16.4355,
                longitude = 80.9920,
                lastDonationDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(115),
                status = "Active",
                completedQuiz = true,
                gender = "Male",
                phone = "+91 88764 12390",
                password = "1234",
                role = "User"
            )
            val user3 = User(
                name = "Praneeth Reddy",
                encryptedBloodType = User.encryptBloodType("O+"),
                latitude = 16.4310,
                longitude = 80.9890,
                lastDonationDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14),
                status = "Cooldown",
                completedQuiz = true,
                gender = "Male",
                phone = "+91 99800 55431",
                password = "1234",
                role = "User"
            )
            val user4 = User(
                name = "Kiran Rao",
                encryptedBloodType = User.encryptBloodType("O-"),
                latitude = 16.4380,
                longitude = 80.9965,
                lastDonationDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(140),
                status = "Active",
                completedQuiz = true,
                gender = "Female",
                phone = "+91 94440 12345",
                password = "1234",
                role = "User"
            )
            userDao.insertUser(user2)
            userDao.insertUser(user3)
            userDao.insertUser(user4)
        }

        // Migrate any existing users that have old Bangalore coordinates (~12.9) to Gudivada
        val allCurrentUsers = userDao.getAllUsers()
        for (u in allCurrentUsers) {
            if (u.latitude in 12.0..14.0) {
                val updatedUser = u.copy(
                    latitude = 16.4326 + (Math.random() - 0.5) * 0.02,
                    longitude = 80.9928 + (Math.random() - 0.5) * 0.02
                )
                userDao.updateUser(updatedUser)
            }
        }
    }
}
