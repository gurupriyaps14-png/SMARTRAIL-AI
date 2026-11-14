-- SmartRail AI: MySQL Database Schema
-- Focus: Tamil Nadu Railway Divisions (Southern Railway)

CREATE DATABASE IF NOT EXISTS smartrail_db;
USE smartrail_db;

-- 0. User Accounts Table (Authentication & Controller Authorization)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role VARCHAR(50) DEFAULT 'TRAIN_CONTROLLER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 1. Railway Divisions Table
CREATE TABLE IF NOT EXISTS divisions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    headquarters VARCHAR(100) NOT NULL,
    zone VARCHAR(50) DEFAULT 'Southern Railway (SR)',
    active_trains_count INT DEFAULT 0,
    description TEXT
);

-- 2. Stations Table
CREATE TABLE IF NOT EXISTS stations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    division_id INT NOT NULL,
    platforms_count INT DEFAULT 6,
    tracks_count INT DEFAULT 5,
    junction_type VARCHAR(60) DEFAULT 'Electronic Interlocking Junction',
    latitude DOUBLE,
    longitude DOUBLE,
    is_major_junction BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (division_id) REFERENCES divisions(id) ON DELETE CASCADE
);

-- 3. Tracks Table
CREATE TABLE IF NOT EXISTS tracks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    station_id INT NOT NULL,
    track_number INT NOT NULL,
    track_name VARCHAR(100) NOT NULL,
    track_type VARCHAR(50) NOT NULL, -- MAIN_UP, MAIN_DOWN, LOOP_1, LOOP_2, FREIGHT_SIDING
    max_speed_kmh INT DEFAULT 110,
    status VARCHAR(30) DEFAULT 'CLEAR', -- CLEAR, OCCUPIED, RESERVED, MAINTENANCE
    length_meters INT DEFAULT 750,
    current_train_number VARCHAR(20) DEFAULT NULL,
    FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE
);

-- 4. Trains Table
CREATE TABLE IF NOT EXISTS trains (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(20) NOT NULL UNIQUE,
    train_name VARCHAR(120) NOT NULL,
    category VARCHAR(30) NOT NULL, -- PASSENGER, EXPRESS, GOODS, FREIGHT
    origin_station_id INT NOT NULL,
    destination_station_id INT NOT NULL,
    current_station_id INT NOT NULL,
    current_track_number INT DEFAULT 1,
    direction VARCHAR(10) NOT NULL, -- UP, DOWN
    status VARCHAR(30) NOT NULL, -- ON_TIME, DELAYED, CONFLICT, MOVING, ARRIVED
    eta VARCHAR(20) NOT NULL,
    etd VARCHAR(20) NOT NULL,
    delay_minutes INT DEFAULT 0,
    speed_kmh INT DEFAULT 85,
    progress_percent DOUBLE DEFAULT 10.0,
    time_slot VARCHAR(20) DEFAULT 'ALL',
    is_simulated BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (origin_station_id) REFERENCES stations(id),
    FOREIGN KEY (destination_station_id) REFERENCES stations(id),
    FOREIGN KEY (current_station_id) REFERENCES stations(id)
);

-- 5. Train Schedules Table
CREATE TABLE IF NOT EXISTS train_schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_id INT NOT NULL,
    station_id INT NOT NULL,
    scheduled_arrival VARCHAR(20) NOT NULL,
    scheduled_departure VARCHAR(20) NOT NULL,
    platform_number INT DEFAULT 1,
    stop_sequence INT DEFAULT 1,
    halt_duration_minutes INT DEFAULT 5,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE
);

-- 6. Train Movements Table (Live & Historical Block Telemetry)
CREATE TABLE IF NOT EXISTS train_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(20) NOT NULL,
    station_code VARCHAR(10) NOT NULL,
    track_number INT NOT NULL,
    speed_kmh INT DEFAULT 0,
    progress_percent DOUBLE DEFAULT 0,
    movement_status VARCHAR(30) NOT NULL, -- DEPARTED, IN_TRANSIT, APPROACHING, ARRIVED, DIVERTED, HELD
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- 7. Train Delays Table (Historical & Live Delay Analytics)
CREATE TABLE IF NOT EXISTS train_delays (
    id INT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(20) NOT NULL,
    station_code VARCHAR(10) NOT NULL,
    delay_minutes INT NOT NULL,
    delay_reason VARCHAR(255) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Track Conflicts Table (Detected Interlocking & Line Contention)
CREATE TABLE IF NOT EXISTS track_conflicts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conflict_code VARCHAR(30) NOT NULL UNIQUE,
    station_code VARCHAR(10) NOT NULL,
    track_number INT NOT NULL,
    train_primary VARCHAR(20) NOT NULL,
    train_secondary VARCHAR(20) NOT NULL,
    conflict_type VARCHAR(50) NOT NULL, -- HEAD_ON, OVERTAKE, CROSSING, OCCUPIED_BLOCK
    severity VARCHAR(20) NOT NULL, -- HIGH, CRITICAL, MODERATE
    priority_winner VARCHAR(20),
    decision_reason TEXT,
    detected_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) DEFAULT 'ACTIVE' -- ACTIVE, RESOLVED, DISMISSED
);

-- 9. Diversion History Table (Audit Log of Track Swaps & Signal Holds)
CREATE TABLE IF NOT EXISTS diversion_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conflict_code VARCHAR(30) NOT NULL,
    train_number VARCHAR(20) NOT NULL,
    diverted_from_track INT NOT NULL,
    diverted_to_track INT NOT NULL,
    station_code VARCHAR(10) NOT NULL,
    action_type VARCHAR(50) NOT NULL, -- AUTOMATIC_DIVERSION, CONTROLLER_DIVERSION, SIGNAL_HOLD
    controller_approved BOOLEAN DEFAULT TRUE,
    reason TEXT,
    diversion_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Alert Notifications Table (Alert History Log)
CREATE TABLE IF NOT EXISTS alert_notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alert_code VARCHAR(30) NOT NULL UNIQUE,
    train_number VARCHAR(20) NOT NULL,
    conflict_type VARCHAR(50) NOT NULL,
    time_detected TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recommended_action VARCHAR(255) NOT NULL,
    dispatch_status VARCHAR(50) NOT NULL, -- AUTOMATICALLY_DIVERTED, CONTROLLER_APPROVED, HELD_AT_SIGNAL, RESOLVED, PENDING_APPROVAL
    severity VARCHAR(20) DEFAULT 'HIGH',
    station_code VARCHAR(10) NOT NULL,
    track_number INT NOT NULL,
    details TEXT
);
