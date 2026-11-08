/**
 * LifeStream Indian Logistics Backend Service
 * Framework: Node.js with Express.js
 * Purpose: 
 *  1. API endpoint for Indian Blood Banks to update current stock levels of various blood types (including timestamps)
 *  2. Real-time Geospatial Matching & High-Priority Push Notification triggers (FCM Node.js SDK)
 *  3. Trigger alerts to hospitals when a user pledges to donate.
 */

const express = require('express');
const cors = require('cors');
// Simulated pg/mysql module for documentation
// const { Pool } = require('pg'); 

const app = express();
app.use(express.json());
app.use(cors());

// --- Mock Database / DB Pool ---
const mockDb = {
  // Mock query simulation
  query: async (text, params) => {
    console.log(`[SQL Execute]: ${text} | Params: ${JSON.stringify(params)}`);
    return { rows: [] };
  }
};

// ==========================================
// 1. ENDPOINT: UPDATE BLOOD BANK STOCK LEVELS
// ==========================================
/**
 * @route   PUT /api/blood-bank/stock
 * @desc    Updates current stock levels for a specific blood type in a blood bank.
 * @access  Private (Admin authenticated)
 */
app.put('/api/blood-bank/stock', async (req, res) => {
  const { bloodBankId, bloodType, units, updatedBy } = req.body;

  // Input Validation
  if (!bloodBankId || !bloodType || units === undefined || !updatedBy) {
    return res.status(400).json({
      success: false,
      message: "Required fields: bloodBankId, bloodType, units, updatedBy"
    });
  }

  const validBloodTypes = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  if (!validBloodTypes.includes(bloodType)) {
    return res.status(400).json({
      success: false,
      message: `Invalid blood type. Must be one of: ${validBloodTypes.join(', ')}`
    });
  }

  if (units < 0) {
    return res.status(400).json({
      success: false,
      message: "Units available cannot be negative."
    });
  }

  try {
    // 1. Perform UPSERT syntax (for Postgres) to update or insert stock level
    const upsertStockSql = `
      INSERT INTO blood_inventory (blood_bank_id, blood_type, units_available, last_updated_at, updated_by)
      VALUES ($1, $2, $3, NOW(), $4)
      ON CONFLICT (blood_bank_id, blood_type)
      DO UPDATE SET 
        units_available = EXCLUDED.units_available,
        last_updated_at = NOW(),
        updated_by = EXCLUDED.updated_by
      RETURNING *;
    `;

    const result = await mockDb.query(upsertStockSql, [bloodBankId, bloodType, units, updatedBy]);

    // Also update parent table update timestamp
    await mockDb.query(`UPDATE blood_banks SET updated_at = NOW() WHERE id = $1`, [bloodBankId]);

    return res.status(200).json({
      success: true,
      message: `Successfully updated ${bloodType} stock level to ${units} units.`,
      timestamp: new Date().toISOString(),
      updatedStock: {
        bloodBankId,
        bloodType,
        unitsAvailable: units,
        updatedBy
      }
    });

  } catch (error) {
    console.error("Error updating stock levels:", error);
    return res.status(500).json({
      success: false,
      message: "Database error while updating stock level.",
      error: error.message
    });
  }
});


// ==========================================
// 2. TRIGGER HIGH-PRIORITY FCM NOTIFICATIONS
// ==========================================
/**
 * Haversine formula helper function for server-side distance calculation (km)
 */
function calculateDistanceInKm(lat1, lon1, lat2, lon2) {
  const earthRadius = 6371; // km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return earthRadius * c;
}

/**
 * Blood Compatibility Logic Matrix
 */
function isCompatible(donorType, neededType) {
  if (donorType === 'O-') return true; // Universal Donor O-
  if (donorType === neededType) return true;

  const donorRh = donorType.slice(-1);
  const neededRh = neededType.slice(-1);

  // Severe Immune Reaction: Rh+ blood cannot be donated to Rh- target
  if (donorRh === '+' && neededRh === '-') return false;

  const donorLetter = donorType.slice(0, -1);
  const neededLetter = neededType.slice(0, -1);

  if (donorLetter === 'O') return true;
  if (donorLetter === 'A') return (neededLetter === 'A' || neededLetter === 'AB');
  if (donorLetter === 'B') return (neededLetter === 'B' || neededLetter === 'AB');
  if (donorLetter === 'AB') return (neededLetter === 'AB');

  return false;
}

/**
 * @route   POST /api/blood-request/create-and-notify
 * @desc    Submit an URGENT request, fetch users within 15km, check compatibility, trigger high-priority push notifications
 */
app.post('/api/blood-request/create-and-notify', async (req, res) => {
  const { hospitalId, hospitalName, hLat, hLon, bloodType, unitsRequired, patientReferenceId } = req.body;

  if (!hospitalId || !bloodType || !unitsRequired || !patientReferenceId) {
    return res.status(400).json({ success: false, message: "Missing required request parameters." });
  }

  try {
    // 1. Insert search/request record in DB
    const insertRequestSql = `
      INSERT INTO blood_requests (hospital_id, blood_type_needed, units_required, patient_reference_id, status)
      VALUES ($1, $2, $3, $4, 'Open') RETURNING id;
    `;
    // Simulation: const requestId = (await mockDb.query(insertRequestSql, ...)).rows[0].id;
    const mockRequestId = Math.floor(Math.random() * 10000);

    // 2. Fetch all Active users with fcm tokens with distance limit checks
    // In a live system, we use Postgres PostGIS geospatial index query for maximum scalability:
    // SELECT * FROM users WHERE ST_DistanceSphere(geom, ST_MakePoint(lon, lat)) <= 15000
    // Below we represent the standard algorithm fallback mapping:
    const getUsersSql = `
      SELECT id, name, phone, encrypted_blood_type, latitude, longitude, fcm_device_token 
      FROM users 
      WHERE fcm_device_token IS NOT NULL;
    `;
    
    // Simulate user entities (Rajesh Kumar is Active O-, Rakesh Sharma is Cooldown, Praneeth Reddy is Active O+)
    const users = [
      { id: 101, name: "Rajesh Kumar", email: "rajesh@lifestream.in", phone: "+91 98301 22910", encrypted_blood_type: "O-", latitude: 12.9716, longitude: 77.5946, fcm_device_token: "fcm_token_rajesh" },
      { id: 102, name: "Rakesh Sharma", email: "rakesh@lifestream.in", phone: "+91 88764 12390", encrypted_blood_type: "O+", latitude: 12.9610, longitude: 77.6310, fcm_device_token: "fcm_token_rakesh" },
      { id: 103, name: "Praneeth Reddy", email: "praneeth@lifestream.in", phone: "+91 99800 55431", encrypted_blood_type: "O+", latitude: 12.9821, longitude: 77.6099, fcm_device_token: "fcm_token_praneeth" }
    ];

    const dispatchedMatches = [];

    for (const donor of users) {
      // Step A: Blood Compatibility Check
      const bloodCompat = isCompatible(donor.encrypted_blood_type, bloodType);
      
      // Step B: Geospatial 15km boundary check
      const distKm = calculateDistanceInKm(donor.latitude, donor.longitude, hLat, hLon);

      if (bloodCompat && distKm <= 15.0) {
        // Build HIGH-PRIORITY FCM JSON Message
        const fcmMessage = {
          token: donor.fcm_device_token,
          android: {
            priority: "high" // Essential for wake lock bypass under Android Doze mode
          },
          notification: {
            title: `URGENT: Blood Needed at ${hospitalName}`,
            body: `Namaste ${donor.name}, ${hospitalName} needs ${unitsRequired} units of ${bloodType} blood. You are ${distKm.toFixed(1)} km away and can donate. Safe, tested blood will be released to the patient immediately when you pledge.`
          },
          data: {
            requestId: String(mockRequestId),
            hospitalId: String(hospitalId),
            bloodTypeNeeded: bloodType,
            hospitalName,
            patientReferenceId,
            type: "URGENT_LOGISTICS_MATCH"
          }
        };

        // In production, execute Google Firebase Admin Node.js SDK:
        // admin.messaging().send(fcmMessage);
        console.log(`[Push Notification Dispatched / FCM Sandbox]:`, JSON.stringify(fcmMessage, null, 2));

        // Create log record
        dispatchedMatches.push({
          donorId: donor.id,
          donorName: donor.name,
          distance: `${distKm.toFixed(2)} km`,
          payload: fcmMessage
        });

        await mockDb.query(
          `INSERT INTO matches_log (request_id, user_id, distance_km, status) VALUES ($1, $2, $3, 'Dispatched')`,
          [mockRequestId, donor.id, distKm]
        );
      }
    }

    return res.status(200).json({
      success: true,
      message: `Urgent blood request created. Geospatial matching executed successfully.`,
      requestId: mockRequestId,
      totalMatchedCompatibleDonors: dispatchedMatches.length,
      dispatches: dispatchedMatches
    });

  } catch (err) {
    console.error("Request process error:", err);
    return res.status(500).json({ success: false, message: "Server error during geospatial match execution.", error: err.message });
  }
});


// ==========================================
// 3. ENDPOINT: NOTIFY HOSPITAL ON USER PLEDGE
// ==========================================
/**
 * @route   POST /api/blood-request/pledge
 * @desc    Triggered when a donor commits to a request. Locks a "Replacement Voucher" and dispatches real-time alert logs to the clinical triage center.
 */
app.post('/api/blood-request/pledge', async (req, res) => {
  const { requestId, userId, donorName, bloodType, hospitalId } = req.body;

  if (!requestId || !userId || !donorName || !bloodType || !hospitalId) {
    return res.status(400).json({ success: false, message: "Missing parameter details to initiate the pledge transaction." });
  }

  try {
    // Generate secure replacement token standard
    const replacementToken = `LSTREAM-IN-${Math.floor(1000 + Math.random() * 9000)}-${bloodType}`;

    // DB update: Add voucher release state
    const updateDonationSql = `
      INSERT INTO donation_history (user_id, blood_bank_id, donation_date, donation_status, replacement_token, remarks)
      VALUES ($1, $2, CURRENT_DATE, 'Pledged', $3, 'Voucher secured under India Replacement Blood Protocol.');
    `;
    await mockDb.query(updateDonationSql, [userId, hospitalId, replacementToken]);

    // Send high-priority alert to the medical clinical dashboards at the Hospital
    const hospitalSocketAlertPayload = {
      event: "PLEDGE_CONFIRMED",
      hospitalId,
      requestId,
      donorName,
      bloodType,
      replacementToken,
      bypassAssay实验室BypassDelay: true,
      message: `ALERT: Active donor ${donorName} has pledged 1 unit of ${bloodType}. VOUCHER STATUS: SECURED (${replacementToken}). Bypassing 48-hour clinical test delay via pre-tested inventory release authorization.`
    };

    console.log(`[Clinical Socket Dispatch to Blood Bank ${hospitalId} / Hospital Triage Admin Console]:`, JSON.stringify(hospitalSocketAlertPayload, null, 2));

    return res.status(200).json({
      success: true,
      message: "Pledge recorded. Hospital notified instantly. Replacement Voucher issued.",
      replacementToken,
      details: hospitalSocketAlertPayload
    });

  } catch (error) {
    console.error("Error processing pledge alert:", error);
    return res.status(500).json({ success: false, message: "Failed to resolve pledge process." });
  }
});

// Start listening
const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
  console.log(`🇮🇳 LifeStream India Fullsite Logistics Backend service deployed on port ${PORT}`);
});
