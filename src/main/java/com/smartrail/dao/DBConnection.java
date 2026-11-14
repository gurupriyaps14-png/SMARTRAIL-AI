package com.smartrail.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robust Database Connection Manager using JDBC.
 * Primary target: MySQL (localhost:3306/smartrail_db).
 * Resilience fallback: Embedded in-memory H2 database with MySQL compatibility mode.
 * Automatically initializes schema and Tamil Nadu railway datasets across all 10 tables.
 */
public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static final String MYSQL_URL = System.getenv("SMARTRAIL_DB_URL") != null ?
            System.getenv("SMARTRAIL_DB_URL") : "jdbc:mysql://localhost:3306/smartrail_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String MYSQL_USER = System.getenv("SMARTRAIL_DB_USER") != null ?
            System.getenv("SMARTRAIL_DB_USER") : "root";
    private static final String MYSQL_PASS = System.getenv("SMARTRAIL_DB_PASS") != null ?
            System.getenv("SMARTRAIL_DB_PASS") : "root";

    private static String getH2JdbcUrl() {
        String customPath = System.getenv("SMARTRAIL_H2_PATH");
        if (customPath != null && !customPath.trim().isEmpty()) {
            return "jdbc:h2:file:" + customPath.trim() + ";DB_CLOSE_DELAY=-1;MODE=MySQL;AUTO_SERVER=TRUE";
        }
        try {
            java.io.File dbDir = new java.io.File("./smartrail_data");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            return "jdbc:h2:file:" + dbDir.getAbsolutePath() + "/smartrail_db;DB_CLOSE_DELAY=-1;MODE=MySQL;AUTO_SERVER=TRUE";
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Falling back to in-memory H2 database: " + ex.getMessage());
            return "jdbc:h2:mem:smartrail_db;DB_CLOSE_DELAY=-1;MODE=MySQL";
        }
    }

    private static final String H2_USER = "sa";
    private static final String H2_PASS = "";

    private static boolean useH2Fallback = false;
    private static boolean initialized = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "MySQL driver not in boot classpath, will verify H2 fallback: " + e.getMessage());
        }
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "H2 driver not available: " + e.getMessage());
        }
    }

    public static synchronized Connection getConnection() {
        Connection conn = null;

        if (!useH2Fallback) {
            try {
                conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
                if (!initialized) {
                    verifyAndSeed(conn, false);
                    initialized = true;
                }
                return conn;
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, "MySQL not reachable (" + ex.getMessage() +
                        "). Switching to persistent embedded H2 JDBC engine with full Tamil Nadu railway dataset.");
                useH2Fallback = true;
            }
        }

        // Fallback to H2 embedded database
        try {
            conn = DriverManager.getConnection(getH2JdbcUrl(), H2_USER, H2_PASS);
            if (!initialized) {
                verifyAndSeed(conn, true);
                initialized = true;
            }
            return conn;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal: Unable to initialize fallback JDBC connection: " + e.getMessage(), e);
            throw new RuntimeException("Database connection failure", e);
        }
    }

    public static boolean isUsingFallback() {
        return useH2Fallback;
    }

    public static String getDatabaseType() {
        return useH2Fallback ? "Persistent Embedded H2 (MySQL Compatible Mode)" : "MySQL 8.x Live Database";
    }

    private static void verifyAndSeed(Connection conn, boolean isFallback) {
        try (Statement stmt = conn.createStatement()) {
            boolean tablesExist = false;
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM trains")) {
                if (rs.next()) {
                    tablesExist = rs.getInt(1) > 0;
                }
            } catch (Exception ignored) {
                tablesExist = false;
            }

            if (!tablesExist) {
                LOGGER.info("Seeding SmartRail AI Tamil Nadu railway schema and 10-table datasets...");
                loadBuiltinSeeds(conn);
                LOGGER.info("SmartRail AI Database initialized successfully.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Auto-seeding encountered issue, loading embedded statements: " + e.getMessage());
            loadBuiltinSeeds(conn);
        }
    }

    private static void loadBuiltinSeeds(Connection conn) {
        try (Statement s = conn.createStatement()) {
            // 0. Users (Authentication & Authorization)
            s.execute("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, full_name VARCHAR(120) NOT NULL, email VARCHAR(120) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, salt VARCHAR(64) NOT NULL, role VARCHAR(50) DEFAULT 'TRAIN_CONTROLLER', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            if (!hasRows(s, "users")) {
                String salt = com.smartrail.util.PasswordUtil.generateSalt();
                String hash = com.smartrail.util.PasswordUtil.hashPassword("SmartRail@2026", salt);
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (full_name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, "Chief Dispatcher - Tamil Nadu Zone");
                    ps.setString(2, "controller@smartrail.gov.in");
                    ps.setString(3, hash);
                    ps.setString(4, salt);
                    ps.setString(5, "CHIEF_CONTROLLER");
                    ps.executeUpdate();
                    LOGGER.info("Default controller account seeded: controller@smartrail.gov.in");
                }
            }

            // 1. Divisions
            s.execute("CREATE TABLE IF NOT EXISTS divisions (id INT PRIMARY KEY, code VARCHAR(10) UNIQUE, name VARCHAR(100), headquarters VARCHAR(100), zone VARCHAR(50), active_trains_count INT, description TEXT)");

            // 2. Stations
            s.execute("CREATE TABLE IF NOT EXISTS stations (id INT PRIMARY KEY, code VARCHAR(10) UNIQUE, name VARCHAR(100), division_id INT, platforms_count INT, tracks_count INT, junction_type VARCHAR(60), latitude DOUBLE, longitude DOUBLE, is_major_junction BOOLEAN)");

            // 3. Tracks
            s.execute("CREATE TABLE IF NOT EXISTS tracks (id INT AUTO_INCREMENT PRIMARY KEY, station_id INT, track_number INT, track_name VARCHAR(100), track_type VARCHAR(50), max_speed_kmh INT, status VARCHAR(30), length_meters INT, current_train_number VARCHAR(20))");

            // 4. Trains
            s.execute("CREATE TABLE IF NOT EXISTS trains (id INT PRIMARY KEY, train_number VARCHAR(20) UNIQUE, train_name VARCHAR(120), category VARCHAR(30), origin_station_id INT, destination_station_id INT, current_station_id INT, current_track_number INT, direction VARCHAR(10), status VARCHAR(30), eta VARCHAR(20), etd VARCHAR(20), delay_minutes INT, speed_kmh INT, progress_percent DOUBLE, time_slot VARCHAR(20), is_simulated BOOLEAN)");

            // 5. Train Schedules
            s.execute("CREATE TABLE IF NOT EXISTS train_schedules (id INT AUTO_INCREMENT PRIMARY KEY, train_id INT, station_id INT, scheduled_arrival VARCHAR(20), scheduled_departure VARCHAR(20), platform_number INT, stop_sequence INT, halt_duration_minutes INT)");

            // 6. Train Movements
            s.execute("CREATE TABLE IF NOT EXISTS train_movements (id INT AUTO_INCREMENT PRIMARY KEY, train_number VARCHAR(20), station_code VARCHAR(10), track_number INT, speed_kmh INT, progress_percent DOUBLE, movement_status VARCHAR(30), timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, notes TEXT)");

            // 7. Train Delays
            s.execute("CREATE TABLE IF NOT EXISTS train_delays (id INT AUTO_INCREMENT PRIMARY KEY, train_number VARCHAR(20), station_code VARCHAR(10), delay_minutes INT, delay_reason VARCHAR(255), recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 8. Track Conflicts
            s.execute("CREATE TABLE IF NOT EXISTS track_conflicts (id INT AUTO_INCREMENT PRIMARY KEY, conflict_code VARCHAR(30) UNIQUE, station_code VARCHAR(10), track_number INT, train_primary VARCHAR(20), train_secondary VARCHAR(20), conflict_type VARCHAR(50), severity VARCHAR(20), priority_winner VARCHAR(20), decision_reason TEXT, detected_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, status VARCHAR(30))");

            // 9. Diversion History
            s.execute("CREATE TABLE IF NOT EXISTS diversion_history (id INT AUTO_INCREMENT PRIMARY KEY, conflict_code VARCHAR(30), train_number VARCHAR(20), diverted_from_track INT, diverted_to_track INT, station_code VARCHAR(10), action_type VARCHAR(50), controller_approved BOOLEAN, reason TEXT, diversion_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 10. Alert Notifications
            s.execute("CREATE TABLE IF NOT EXISTS alert_notifications (id INT AUTO_INCREMENT PRIMARY KEY, alert_code VARCHAR(30) UNIQUE, train_number VARCHAR(20), conflict_type VARCHAR(50), time_detected TIMESTAMP DEFAULT CURRENT_TIMESTAMP, recommended_action VARCHAR(255), dispatch_status VARCHAR(50), severity VARCHAR(20), station_code VARCHAR(10), track_number INT, details TEXT)");

            // Check if trains already populated
            try (ResultSet rs = s.executeQuery("SELECT count(*) FROM trains")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            } catch (Exception ignored) {}

            // Populate Divisions
            if (!hasRows(s, "divisions")) {
                s.execute("INSERT INTO divisions VALUES (1, 'MAS', 'Chennai Division', 'Chennai', 'Southern Railway (SR)', 6, 'Capital division handling trunk corridors and Chennai suburban.')");
                s.execute("INSERT INTO divisions VALUES (2, 'SA', 'Salem Division', 'Salem', 'Southern Railway (SR)', 5, 'Western corridor serving textile and steel freight corridor.')");
                s.execute("INSERT INTO divisions VALUES (3, 'TPJ', 'Tiruchirappalli Division', 'Tiruchirappalli', 'Southern Railway (SR)', 4, 'Delta junction and workshop hub.')");
                s.execute("INSERT INTO divisions VALUES (4, 'MDU', 'Madurai Division', 'Madurai', 'Southern Railway (SR)', 4, 'Deep south heritage and coastal corridor.')");
            }

            // Populate Stations
            if (!hasRows(s, "stations")) {
                s.execute("INSERT INTO stations VALUES (1, 'MAS', 'Puratchi Thalaivar Dr. MGR Chennai Central', 1, 12, 5, 'Terminal Super-Junction', 13.0827, 80.2707, TRUE)");
                s.execute("INSERT INTO stations VALUES (2, 'MS', 'Chennai Egmore', 1, 11, 5, 'Major Southern Junction', 13.0797, 80.2600, TRUE)");
                s.execute("INSERT INTO stations VALUES (3, 'TBM', 'Tambaram', 1, 8, 5, 'Suburban Transit Junction', 12.9249, 80.1000, TRUE)");
                s.execute("INSERT INTO stations VALUES (4, 'CGL', 'Chengalpattu Junction', 1, 7, 5, 'Southern Quad-Junction', 12.6841, 79.9836, TRUE)");
                s.execute("INSERT INTO stations VALUES (5, 'AJJ', 'Arakkonam Junction', 1, 6, 5, 'Hexagonal Tri-Junction', 13.0784, 79.6677, TRUE)");
                s.execute("INSERT INTO stations VALUES (6, 'KPD', 'Katpadi Junction', 1, 5, 5, 'Dual Electrified Junction', 12.9698, 79.1378, TRUE)");

                s.execute("INSERT INTO stations VALUES (7, 'CBE', 'Coimbatore Junction', 2, 6, 5, 'Major Western Terminal', 11.0016, 76.9628, TRUE)");
                s.execute("INSERT INTO stations VALUES (8, 'SA', 'Salem Junction', 2, 5, 5, 'Pentagonal Quad-Junction', 11.6643, 78.1460, TRUE)");
                s.execute("INSERT INTO stations VALUES (9, 'ED', 'Erode Junction', 2, 4, 5, 'Loco Depot Interlocking Junction', 11.3410, 77.7172, TRUE)");
                s.execute("INSERT INTO stations VALUES (10, 'TUP', 'Tiruppur', 2, 4, 5, 'Industrial Interlocking Station', 11.1085, 77.3411, FALSE)");

                s.execute("INSERT INTO stations VALUES (11, 'TPJ', 'Tiruchirappalli Junction', 3, 7, 5, 'Central Delta Super-Junction', 10.7905, 78.6908, TRUE)");
                s.execute("INSERT INTO stations VALUES (12, 'TJ', 'Thanjavur Junction', 3, 5, 5, 'Heritage Branch Junction', 10.7870, 79.1378, TRUE)");
                s.execute("INSERT INTO stations VALUES (13, 'VM', 'Villupuram Junction', 3, 6, 5, 'Chord-Main Interlocking Junction', 11.9401, 79.4861, TRUE)");
                s.execute("INSERT INTO stations VALUES (14, 'KMU', 'Kumbakonam', 3, 4, 5, 'Delta Cross-Over Station', 10.9602, 79.3845, FALSE)");

                s.execute("INSERT INTO stations VALUES (15, 'MDU', 'Madurai Junction', 4, 8, 5, 'Southern Historic Super-Junction', 9.9252, 78.1198, TRUE)");
                s.execute("INSERT INTO stations VALUES (16, 'DG', 'Dindigul Junction', 4, 5, 5, 'Chord-Line Quad Junction', 10.3673, 77.9803, TRUE)");
                s.execute("INSERT INTO stations VALUES (17, 'TEN', 'Tirunelveli Junction', 4, 5, 5, 'Deep South Trans-Terminal', 8.7139, 77.7567, TRUE)");
                s.execute("INSERT INTO stations VALUES (18, 'RMM', 'Rameswaram', 4, 4, 5, 'Island Maritime Terminal', 9.2885, 79.3129, TRUE)");
            }

            // Tracks
            if (!hasRows(s, "tracks")) {
                for (int stId = 1; stId <= 18; stId++) {
                    s.execute("INSERT INTO tracks (station_id, track_number, track_name, track_type, max_speed_kmh, status, length_meters, current_train_number) VALUES " +
                            "(" + stId + ", 1, 'Track 1 - Up Main Line', 'MAIN_UP', 110, 'CLEAR', 750, NULL)");
                    s.execute("INSERT INTO tracks (station_id, track_number, track_name, track_type, max_speed_kmh, status, length_meters, current_train_number) VALUES " +
                            "(" + stId + ", 2, 'Track 2 - Down Main Line', 'MAIN_DOWN', 110, 'CLEAR', 750, NULL)");
                    s.execute("INSERT INTO tracks (station_id, track_number, track_name, track_type, max_speed_kmh, status, length_meters, current_train_number) VALUES " +
                            "(" + stId + ", 3, 'Track 3 - Platform Loop 1', 'LOOP_1', 65, 'CLEAR', 700, NULL)");
                    s.execute("INSERT INTO tracks (station_id, track_number, track_name, track_type, max_speed_kmh, status, length_meters, current_train_number) VALUES " +
                            "(" + stId + ", 4, 'Track 4 - Platform Loop 2', 'LOOP_2', 65, 'CLEAR', 700, NULL)");
                    s.execute("INSERT INTO tracks (station_id, track_number, track_name, track_type, max_speed_kmh, status, length_meters, current_train_number) VALUES " +
                            "(" + stId + ", 5, 'Track 5 - Freight Bypass Siding', 'FREIGHT_SIDING', 50, 'CLEAR', 850, NULL)");
                }
            }

            // Trains
            if (!hasRows(s, "trains")) {
                s.execute("INSERT INTO trains VALUES (1, '12637', 'Pandian Superfast Express', 'EXPRESS', 2, 15, 2, 1, 'DOWN', 'MOVING', '22:45', '22:50', 0, 88, 24.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (2, '20607', 'MGR Chennai Central - Mysuru Vande Bharat Express', 'EXPRESS', 1, 6, 1, 1, 'UP', 'ON_TIME', '22:55', '23:00', 0, 115, 12.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (3, '12675', 'Kovai Superfast Express', 'EXPRESS', 1, 7, 8, 3, 'DOWN', 'DELAYED', '23:10', '23:15', 14, 76, 58.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (4, '12605', 'Pallavan Superfast Express', 'EXPRESS', 2, 11, 11, 1, 'DOWN', 'ON_TIME', '22:40', '22:45', 0, 92, 45.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (5, '22671', 'Chennai Egmore - Madurai Tejas Express', 'EXPRESS', 2, 15, 15, 2, 'DOWN', 'MOVING', '23:05', '23:10', 0, 110, 78.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (6, '20635', 'Ananthapuri Superfast Express', 'EXPRESS', 2, 17, 3, 2, 'DOWN', 'DELAYED', '23:25', '23:30', 8, 80, 15.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (7, '16127', 'Chennai Egmore - Guruvayur Express', 'EXPRESS', 2, 17, 4, 2, 'DOWN', 'ON_TIME', '23:35', '23:40', 0, 85, 30.0, 'NIGHT', TRUE)");

                s.execute("INSERT INTO trains VALUES (8, '56841', 'Tiruchirappalli - Thanjavur Fast Passenger', 'PASSENGER', 11, 12, 11, 3, 'DOWN', 'ON_TIME', '22:30', '22:35', 0, 55, 35.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (9, '56711', 'Madurai - Dindigul Passenger', 'PASSENGER', 15, 16, 15, 3, 'UP', 'MOVING', '22:42', '22:48', 0, 50, 18.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (10, '56001', 'Arakkonam - Katpadi Passenger', 'PASSENGER', 5, 6, 5, 3, 'UP', 'DELAYED', '23:00', '23:05', 12, 45, 40.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (11, '66017', 'Chennai Beach - Chengalpattu Suburban Passenger', 'PASSENGER', 2, 4, 2, 4, 'DOWN', 'MOVING', '22:48', '22:52', 0, 60, 25.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (12, '56829', 'Villupuram - Mayiladuthurai Passenger', 'PASSENGER', 13, 14, 13, 3, 'DOWN', 'ON_TIME', '22:50', '22:55', 0, 48, 10.0, 'NIGHT', TRUE)");

                s.execute("INSERT INTO trains VALUES (13, 'G-7821', 'Ennore Thermal Coal Bulk Cargo', 'GOODS', 1, 8, 1, 5, 'DOWN', 'MOVING', '23:15', '23:25', 0, 65, 32.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (14, 'G-5540', 'Tuticorin Port Container Cargo', 'GOODS', 17, 7, 17, 5, 'UP', 'DELAYED', '23:30', '23:45', 22, 60, 50.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (15, 'G-3180', 'Chennai Port Auto Logistics Cargo', 'GOODS', 1, 5, 1, 4, 'UP', 'ON_TIME', '22:38', '22:45', 0, 70, 65.0, 'NIGHT', TRUE)");

                s.execute("INSERT INTO trains VALUES (16, 'F-9102', 'Salem Steel Plant Heavy BRC Freight', 'FREIGHT', 8, 1, 8, 1, 'UP', 'MOVING', '22:58', '23:10', 0, 58, 42.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (17, 'F-4418', 'Neyveli Lignite Heavy Rake', 'FREIGHT', 13, 11, 11, 5, 'DOWN', 'ON_TIME', '23:05', '23:15', 0, 55, 60.0, 'NIGHT', TRUE)");
                s.execute("INSERT INTO trains VALUES (18, 'F-6210', 'Madurai Agri-Produce Express Freight', 'FREIGHT', 15, 16, 15, 5, 'UP', 'DELAYED', '23:20', '23:35', 18, 52, 28.0, 'NIGHT', TRUE)");
            }

            // Update track occupancy
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '20607' WHERE station_id = 1 AND track_number = 1");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = 'G-7821' WHERE station_id = 1 AND track_number = 5");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '12637' WHERE station_id = 2 AND track_number = 1");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '66017' WHERE station_id = 2 AND track_number = 4");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = 'F-9102' WHERE station_id = 8 AND track_number = 1");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '12675' WHERE station_id = 8 AND track_number = 3");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '12605' WHERE station_id = 11 AND track_number = 1");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '56841' WHERE station_id = 11 AND track_number = 3");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '22671' WHERE station_id = 15 AND track_number = 2");
            s.execute("UPDATE tracks SET status = 'OCCUPIED', current_train_number = '56711' WHERE station_id = 15 AND track_number = 3");

            // 5. Populate Train Schedules
            if (!hasRows(s, "train_schedules")) {
                s.execute("INSERT INTO train_schedules (train_id, station_id, scheduled_arrival, scheduled_departure, platform_number, stop_sequence, halt_duration_minutes) VALUES " +
                        "(1, 2, '21:40', '21:45', 1, 1, 5), (1, 3, '22:10', '22:12', 3, 2, 2), (1, 4, '22:38', '22:40', 2, 3, 2), (1, 13, '00:05', '00:10', 1, 4, 5), (1, 15, '05:30', '05:40', 2, 5, 10), " +
                        "(2, 1, '05:45', '05:50', 2, 1, 5), (2, 5, '06:33', '06:35', 1, 2, 2), (2, 6, '07:18', '07:20', 1, 3, 2), " +
                        "(3, 1, '06:10', '06:15', 5, 1, 5), (3, 5, '07:08', '07:10', 2, 2, 2), (3, 8, '10:50', '10:55', 3, 3, 5), (3, 7, '14:05', '14:15', 1, 4, 10), " +
                        "(4, 2, '15:45', '15:50', 4, 1, 5), (4, 13, '18:05', '18:10', 2, 2, 5), (4, 11, '21:20', '21:30', 1, 3, 10), " +
                        "(5, 2, '06:00', '06:05', 3, 1, 5), (5, 11, '10:05', '10:10', 2, 2, 5), (5, 15, '12:15', '12:25', 1, 3, 10)");
            }

            // 6. Populate Train Movements
            if (!hasRows(s, "train_movements")) {
                s.execute("INSERT INTO train_movements (train_number, station_code, track_number, speed_kmh, progress_percent, movement_status, notes) VALUES " +
                        "('12637', 'MS', 1, 88, 24.0, 'IN_TRANSIT', 'Cleared Egmore home signal, advancing towards Tambaram section.'), " +
                        "('20607', 'MAS', 1, 115, 12.0, 'DEPARTED', 'On-time green corridor dispatch towards Arakkonam Junction.'), " +
                        "('12675', 'SA', 3, 76, 58.0, 'DIVERTED', 'Diverted to Loop Line 3 to facilitate non-stop high-priority overtakes.'), " +
                        "('12605', 'TPJ', 1, 92, 45.0, 'APPROACHING', 'Outer home signal cleared for Platform 1 arrival.'), " +
                        "('22671', 'MDU', 2, 110, 78.0, 'IN_TRANSIT', 'Approaching Dindigul-Madurai double line chord section.'), " +
                        "('G-7821', 'MAS', 5, 65, 32.0, 'IN_TRANSIT', 'Bulk coal rake utilizing freight bypass avoiding suburban platforms.'), " +
                        "('F-9102', 'SA', 1, 58, 42.0, 'HELD', 'Signal hold at outer home pending clearance of Down express rake.'), " +
                        "('56841', 'TPJ', 3, 55, 35.0, 'IN_TRANSIT', 'Cauvery Delta passenger traversing Thanjavur branch line.')");
            }

            // 7. Populate Train Delays
            if (!hasRows(s, "train_delays")) {
                s.execute("INSERT INTO train_delays (train_number, station_code, delay_minutes, delay_reason) VALUES " +
                        "('12675', 'SA', 14, 'Interlocking line contention at Erode junction throat.'), " +
                        "('20635', 'TBM', 8, 'Suburban EMU clearance delay ahead on Chord line.'), " +
                        "('56001', 'AJJ', 12, 'Temporary engineering speed restriction (30 km/h) on Track 3.'), " +
                        "('G-5540', 'TEN', 22, 'Heavy container loading hold at Tuticorin port marshalling yard.'), " +
                        "('F-6210', 'MDU', 18, 'Agricultural bulk freight sorting delay at Dindigul junction yard.')");
            }

            // 8. Populate Track Conflicts
            if (!hasRows(s, "track_conflicts")) {
                s.execute("INSERT INTO track_conflicts (conflict_code, station_code, track_number, train_primary, train_secondary, conflict_type, severity, priority_winner, decision_reason, status) VALUES " +
                        "('CONF-2026-081', 'MAS', 1, '20607', 'G-7821', 'OVERTAKE', 'HIGH', '20607', 'Vande Bharat Express granted mainline speed priority; freight steered to loop.', 'RESOLVED'), " +
                        "('CONF-2026-082', 'SA', 1, '12675', 'F-9102', 'CROSSING', 'CRITICAL', '12675', 'Kovai Superfast given Mainline; Salem Steel freight routed to Loop Line 3.', 'RESOLVED'), " +
                        "('CONF-2026-083', 'MS', 1, '12637', '66017', 'OVERTAKE', 'HIGH', '12637', 'Pandian Superfast retains Track 1; suburban local diverted to Loop Line 4.', 'RESOLVED'), " +
                        "('CONF-2026-084', 'TPJ', 1, '12605', '56841', 'HEAD_ON', 'HIGH', '12605', 'Pallavan Express retains Chord Up line; Passenger held on Track 3 loop.', 'ACTIVE')");
            }

            // 9. Populate Diversion History
            if (!hasRows(s, "diversion_history")) {
                s.execute("INSERT INTO diversion_history (conflict_code, train_number, diverted_from_track, diverted_to_track, station_code, action_type, controller_approved, reason) VALUES " +
                        "('CONF-2026-081', 'G-7821', 1, 5, 'MAS', 'AUTOMATIC_DIVERSION', TRUE, 'AI priority scoring: 20607 (Vande Bharat = 98 pts) vs G-7821 (Goods = 38 pts). Diverted freight to siding 5.'), " +
                        "('CONF-2026-082', 'F-9102', 1, 3, 'SA', 'CONTROLLER_DIVERSION', TRUE, 'Controller approved diversion of slow freight to Loop 3 to preserve Kovai Superfast momentum.'), " +
                        "('CONF-2026-083', '66017', 1, 4, 'MS', 'AUTOMATIC_DIVERSION', TRUE, 'Suburban passenger moved to Track 4 dedicated EMU loop to prevent cascading delays.'), " +
                        "('CONF-2026-080', 'G-3180', 1, 4, 'AJJ', 'AUTOMATIC_DIVERSION', TRUE, 'Auto logistics rake routed to loop line to clear track for approaching Mail service.')");
            }

            // 10. Populate Alert Notifications
            if (!hasRows(s, "alert_notifications")) {
                s.execute("INSERT INTO alert_notifications (alert_code, train_number, conflict_type, recommended_action, dispatch_status, severity, station_code, track_number, details) VALUES " +
                        "('ALT-9041', 'G-7821', 'Track Contention', 'Divert G-7821 to Freight Siding 5', 'AUTOMATICALLY_DIVERTED', 'HIGH', 'MAS', 1, 'Train 20607 and G-7821 approached same Track 1 within 5-min window. Vande Bharat maintained main track.'), " +
                        "('ALT-9042', 'F-9102', 'Line Clearance Overlap', 'Divert F-9102 to Platform Loop 3', 'CONTROLLER_APPROVED', 'CRITICAL', 'SA', 1, 'Controller verified and approved track switch avoiding head-on conflict with incoming Express 12675.'), " +
                        "('ALT-9043', '12675', 'Signal Hold Warning', 'Hold at Outer Home Signal', 'HELD_AT_SIGNAL', 'MODERATE', 'ED', 2, 'Temporary caution clearance issued for Erode yard cross-over.'), " +
                        "('ALT-9044', '66017', 'Suburban Headway Merge', 'Reroute to Track 4 EMU Loop', 'AUTOMATICALLY_DIVERTED', 'HIGH', 'MS', 1, 'Suburban EMU shifted from Track 1 to Track 4 to maintain mainline throughput.'), " +
                        "('ALT-9045', 'G-5540', 'Yard Schedule Contention', 'Hold on Siding 5 until 23:45', 'PENDING_APPROVAL', 'LOW', 'TEN', 5, 'Container rake awaiting line token clearance from Madurai controller.')");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load built-in seeds: " + e.getMessage(), e);
        }
    }

    private static boolean hasRows(Statement s, String tableName) {
        try (ResultSet rs = s.executeQuery("SELECT count(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
