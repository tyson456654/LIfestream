-- LifeStream India Logistics Deployment - Database Schema (PostgreSQL / MySQL)
-- Designed for blood banks, user profiles, past donation history, and push notification tokens.

-- ============== 1. BLOOD BANKS / HOSPITALS INVENTORY ==============
CREATE TABLE IF NOT EXISTS blood_banks (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL, -- e.g. "Indian Red Cross Blood Bank (Bangalore)"
    address TEXT,
    city VARCHAR(100) DEFAULT 'Bangalore',
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    contact_number VARCHAR(20),
    is_verified BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS blood_inventory (
    id SERIAL PRIMARY KEY,
    blood_bank_id INT REFERENCES blood_banks(id) ON DELETE CASCADE,
    blood_type VARCHAR(5) NOT NULL, -- 'A+', 'O-', 'B+', etc.
    units_available INT DEFAULT 0 CHECK (units_available >= 0),
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) -- Name or email of the admin who made the update
);

-- Index for speedy lookups on blood bank stock
CREATE UNIQUE INDEX idx_blood_bank_inventory ON blood_inventory(blood_bank_id, blood_type);


-- ============== 2. USER PROFILE & EMERGENCY CONTACTS ==============
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL, -- e.g. "Amit Sharma"
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL, -- Indian phone format e.g. "+91 98765 43210"
    encrypted_blood_type VARCHAR(255) NOT NULL, -- O-, B+, etc. (stored securely)
    latitude DOUBLE PRECISION NOT NULL, -- For geospatial matching
    longitude DOUBLE PRECISION NOT NULL,
    fcm_device_token VARCHAR(255), -- For high-priority push notifications
    state_region VARCHAR(100) DEFAULT 'Karnataka',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Emergency Contacts (nested or separate table to allow multiple contacts)
CREATE TABLE IF NOT EXISTS emergency_contacts (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    contact_name VARCHAR(150) NOT NULL,
    relationship VARCHAR(50) NOT NULL, -- e.g. "Spouse", "Father", "Friend"
    phone VARCHAR(20) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- ============== 3. BLOOD DONATION HISTORY ==============
CREATE TABLE IF NOT EXISTS donation_history (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    blood_bank_id INT REFERENCES blood_banks(id),
    donation_date DATE NOT NULL,
    units_donated INT DEFAULT 1,
    donation_status VARCHAR(50) DEFAULT 'Completed', -- 'Completed', 'Pledged', 'Deferred'
    replacement_token VARCHAR(100), -- Token reference if a replacement voucher was issued
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- ============== 4. URGENT BLOOD REQUESTS ==============
CREATE TABLE IF NOT EXISTS blood_requests (
    id SERIAL PRIMARY KEY,
    hospital_id INT REFERENCES blood_banks(id) ON DELETE CASCADE,
    blood_type_needed VARCHAR(5) NOT NULL,
    units_required INT NOT NULL DEFAULT 1,
    patient_reference_id VARCHAR(50) UNIQUE NOT NULL, -- e.g. "PAT-993-810"
    status VARCHAR(20) DEFAULT 'Open', -- 'Open', 'Fulfilled', 'Cancelled'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- TABLE to log sent high-priority notifications (Auditable matches)
CREATE TABLE IF NOT EXISTS matches_log (
    id SERIAL PRIMARY KEY,
    request_id INT REFERENCES blood_requests(id) ON DELETE CASCADE,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    distance_km DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) DEFAULT 'Dispatched', -- 'Dispatched', 'Responded', 'Pledged'
    notified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
