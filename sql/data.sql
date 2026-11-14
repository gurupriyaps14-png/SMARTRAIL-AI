-- SmartRail AI: Tamil Nadu Railway Initial Dataset for All 10 Tables
USE smartrail_db;

-- 1. Railway Divisions
INSERT INTO divisions (id, code, name, headquarters, zone, active_trains_count, description) VALUES
(1, 'MAS', 'Chennai Division', 'Chennai', 'Southern Railway (SR)', 6, 'Handles Chennai Suburban, Golden Quadrilateral corridor to Katpadi & Southern trunk line.'),
(2, 'SA', 'Salem Division', 'Salem', 'Southern Railway (SR)', 5, 'Strategic industrial transit linking Coimbatore textile hub, Erode electric loco shed, and Salem steel.'),
(3, 'TPJ', 'Tiruchirappalli Division', 'Tiruchirappalli', 'Southern Railway (SR)', 4, 'Heart of Cauvery delta linking Golden Rock Workshop, Thanjavur granary, and Villupuram interchange.'),
(4, 'MDU', 'Madurai Division', 'Madurai', 'Southern Railway (SR)', 4, 'Southern heritage & maritime corridor connecting Temple City, Rameswaram Pamban route, and Tuticorin Port.');

-- 2. Stations (18 Key Stations in Tamil Nadu)
INSERT INTO stations (id, code, name, division_id, platforms_count, tracks_count, junction_type, latitude, longitude, is_major_junction) VALUES
(1, 'MAS', 'Puratchi Thalaivar Dr. MGR Chennai Central', 1, 12, 5, 'Terminal Super-Junction', 13.0827, 80.2707, TRUE),
(2, 'MS', 'Chennai Egmore', 1, 11, 5, 'Major Southern Junction', 13.0797, 80.2600, TRUE),
(3, 'TBM', 'Tambaram', 1, 8, 5, 'Suburban Transit Junction', 12.9249, 80.1000, TRUE),
(4, 'CGL', 'Chengalpattu Junction', 1, 7, 5, 'Southern Quad-Junction', 12.6841, 79.9836, TRUE),
(5, 'AJJ', 'Arakkonam Junction', 1, 6, 5, 'Hexagonal Tri-Junction', 13.0784, 79.6677, TRUE),
(6, 'KPD', 'Katpadi Junction', 1, 5, 5, 'Dual Electrified Junction', 12.9698, 79.1378, TRUE),
(7, 'CBE', 'Coimbatore Junction', 2, 6, 5, 'Major Western Terminal', 11.0016, 76.9628, TRUE),
(8, 'SA', 'Salem Junction', 2, 5, 5, 'Pentagonal Quad-Junction', 11.6643, 78.1460, TRUE),
(9, 'ED', 'Erode Junction', 2, 4, 5, 'Loco Depot Interlocking Junction', 11.3410, 77.7172, TRUE),
(10, 'TUP', 'Tiruppur', 2, 4, 5, 'Industrial Interlocking Station', 11.1085, 77.3411, FALSE),
(11, 'TPJ', 'Tiruchirappalli Junction', 3, 7, 5, 'Central Delta Super-Junction', 10.7905, 78.6908, TRUE),
(12, 'TJ', 'Thanjavur Junction', 3, 5, 5, 'Heritage Branch Junction', 10.7870, 79.1378, TRUE),
(13, 'VM', 'Villupuram Junction', 3, 6, 5, 'Chord-Main Interlocking Junction', 11.9401, 79.4861, TRUE),
(14, 'KMU', 'Kumbakonam', 3, 4, 5, 'Delta Cross-Over Station', 10.9602, 79.3845, FALSE),
(15, 'MDU', 'Madurai Junction', 4, 8, 5, 'Southern Historic Super-Junction', 9.9252, 78.1198, TRUE),
(16, 'DG', 'Dindigul Junction', 4, 5, 5, 'Chord-Line Quad Junction', 10.3673, 77.9803, TRUE),
(17, 'TEN', 'Tirunelveli Junction', 4, 5, 5, 'Deep South Trans-Terminal', 8.7139, 77.7567, TRUE),
(18, 'RMM', 'Rameswaram', 4, 4, 5, 'Island Maritime Terminal', 9.2885, 79.3129, TRUE);

-- 3. Tracks for Stations
INSERT INTO tracks (station_id, track_number, track_name, track_type, max_speed_kmh, status, length_meters, current_train_number) VALUES
-- MAS
(1, 1, 'Track 1 - Up Main Line', 'MAIN_UP', 110, 'OCCUPIED', 750, '20607'),
(1, 2, 'Track 2 - Down Main Line', 'MAIN_DOWN', 110, 'CLEAR', 750, NULL),
(1, 3, 'Track 3 - Platform Loop 1', 'LOOP_1', 65, 'CLEAR', 700, NULL),
(1, 4, 'Track 4 - Platform Loop 2', 'LOOP_2', 65, 'CLEAR', 700, NULL),
(1, 5, 'Track 5 - Freight Bypass Siding', 'FREIGHT_SIDING', 50, 'OCCUPIED', 850, 'G-7821'),
-- MS
(2, 1, 'Track 1 - Up Main Line', 'MAIN_UP', 110, 'OCCUPIED', 750, '12637'),
(2, 2, 'Track 2 - Down Main Line', 'MAIN_DOWN', 110, 'CLEAR', 750, NULL),
(2, 3, 'Track 3 - Platform Loop 1', 'LOOP_1', 65, 'CLEAR', 700, NULL),
(2, 4, 'Track 4 - Suburban Loop', 'LOOP_2', 65, 'OCCUPIED', 650, '66017'),
(2, 5, 'Track 5 - Goods Bypass Siding', 'FREIGHT_SIDING', 50, 'CLEAR', 800, NULL),
-- SA
(8, 1, 'Track 1 - Up Main Line', 'MAIN_UP', 110, 'OCCUPIED', 750, 'F-9102'),
(8, 2, 'Track 2 - Down Main Line', 'MAIN_DOWN', 110, 'CLEAR', 750, NULL),
(8, 3, 'Track 3 - Platform Loop 1', 'LOOP_1', 65, 'OCCUPIED', 700, '12675'),
(8, 4, 'Track 4 - Passenger Loop', 'LOOP_2', 65, 'CLEAR', 700, NULL),
(8, 5, 'Track 5 - Steel Plant Siding', 'FREIGHT_SIDING', 50, 'CLEAR', 900, NULL),
-- TPJ
(11, 1, 'Track 1 - Chord Up Main', 'MAIN_UP', 110, 'OCCUPIED', 750, '12605'),
(11, 2, 'Track 2 - Chord Down Main', 'MAIN_DOWN', 110, 'CLEAR', 750, NULL),
(11, 3, 'Track 3 - Thanjavur Branch Loop', 'LOOP_1', 65, 'OCCUPIED', 700, '56841'),
(11, 4, 'Track 4 - Platform Loop 2', 'LOOP_2', 65, 'CLEAR', 700, NULL),
(11, 5, 'Track 5 - GOC Workshop Siding', 'FREIGHT_SIDING', 50, 'CLEAR', 850, NULL),
-- MDU
(15, 1, 'Track 1 - Up Main Line', 'MAIN_UP', 110, 'CLEAR', 750, NULL),
(15, 2, 'Track 2 - Down Main Line', 'MAIN_DOWN', 110, 'OCCUPIED', 750, '22671'),
(15, 3, 'Track 3 - Dindigul Passenger Loop', 'LOOP_1', 65, 'OCCUPIED', 700, '56711'),
(15, 4, 'Track 4 - Rameswaram Branch Loop', 'LOOP_2', 65, 'CLEAR', 700, NULL),
(15, 5, 'Track 5 - Goods Transit Siding', 'FREIGHT_SIDING', 50, 'OCCUPIED', 850, 'F-6210');

-- 4. Trains
INSERT INTO trains (id, train_number, train_name, category, origin_station_id, destination_station_id, current_station_id, current_track_number, direction, status, eta, etd, delay_minutes, speed_kmh, progress_percent, time_slot, is_simulated) VALUES
(1, '12637', 'Pandian Superfast Express', 'EXPRESS', 2, 15, 2, 2, 'DOWN', 'MOVING', '22:45', '22:50', 0, 88, 70.0, 'NIGHT', TRUE),
(2, '20607', 'MGR Chennai Central - Mysuru Vande Bharat Express', 'EXPRESS', 1, 6, 1, 1, 'UP', 'ON_TIME', '22:55', '23:00', 0, 115, 38.0, 'NIGHT', TRUE),
(3, '12675', 'Kovai Superfast Express', 'EXPRESS', 1, 7, 8, 3, 'UP', 'DELAYED', '23:10', '23:15', 14, 76, 92.0, 'NIGHT', TRUE),
(4, '12605', 'Pallavan Superfast Express', 'EXPRESS', 2, 11, 11, 2, 'DOWN', 'ON_TIME', '22:40', '22:45', 0, 92, 45.0, 'NIGHT', TRUE),
(5, '22671', 'Chennai Egmore - Madurai Tejas Express', 'EXPRESS', 2, 15, 15, 2, 'DOWN', 'MOVING', '23:05', '23:10', 0, 110, 78.0, 'NIGHT', TRUE),
(6, '20635', 'Ananthapuri Superfast Express', 'EXPRESS', 2, 17, 3, 2, 'DOWN', 'DELAYED', '23:25', '23:30', 8, 80, 15.0, 'NIGHT', TRUE),
(7, '16127', 'Chennai Egmore - Guruvayur Express', 'EXPRESS', 2, 17, 4, 1, 'UP', 'ON_TIME', '23:35', '23:40', 0, 45, 6.0, 'NIGHT', TRUE),
(8, '56841', 'Tiruchirappalli - Thanjavur Fast Passenger', 'PASSENGER', 11, 12, 11, 3, 'DOWN', 'ON_TIME', '22:30', '22:35', 0, 55, 35.0, 'NIGHT', TRUE),
(9, '56711', 'Madurai - Dindigul Passenger', 'PASSENGER', 15, 16, 15, 3, 'UP', 'MOVING', '22:42', '22:48', 0, 50, 18.0, 'NIGHT', TRUE),
(10, '56001', 'Arakkonam - Katpadi Passenger', 'PASSENGER', 5, 6, 5, 3, 'UP', 'DELAYED', '23:00', '23:05', 12, 45, 40.0, 'NIGHT', TRUE),
(11, '66017', 'Chennai Beach - Chengalpattu Suburban Passenger', 'PASSENGER', 2, 4, 2, 4, 'DOWN', 'MOVING', '22:48', '22:52', 0, 60, 25.0, 'NIGHT', TRUE),
(12, '56829', 'Villupuram - Mayiladuthurai Passenger', 'PASSENGER', 13, 14, 13, 3, 'DOWN', 'ON_TIME', '22:50', '22:55', 0, 48, 10.0, 'NIGHT', TRUE),
(13, 'G-7821', 'Ennore Thermal Coal Bulk Cargo', 'GOODS', 1, 8, 1, 5, 'DOWN', 'MOVING', '23:15', '23:25', 0, 65, 32.0, 'NIGHT', TRUE),
(14, 'G-5540', 'Tuticorin Port Container Cargo', 'GOODS', 17, 7, 17, 5, 'UP', 'DELAYED', '23:30', '23:45', 22, 60, 50.0, 'NIGHT', TRUE),
(15, 'G-3180', 'Chennai Port Auto Logistics Cargo', 'GOODS', 1, 5, 1, 4, 'UP', 'ON_TIME', '22:38', '22:45', 0, 70, 65.0, 'NIGHT', TRUE),
(16, 'F-9102', 'Salem Steel Plant Heavy BRC Freight', 'FREIGHT', 8, 1, 8, 5, 'UP', 'MOVING', '22:58', '23:10', 0, 58, 70.0, 'NIGHT', TRUE),
(17, 'F-4418', 'Neyveli Lignite Heavy Rake', 'FREIGHT', 13, 11, 11, 5, 'DOWN', 'ON_TIME', '23:05', '23:15', 0, 55, 60.0, 'NIGHT', TRUE),
(18, 'F-6210', 'Madurai Agri-Produce Express Freight', 'FREIGHT', 15, 16, 15, 5, 'UP', 'DELAYED', '23:20', '23:35', 18, 52, 28.0, 'NIGHT', TRUE);

-- 5. Train Schedules
INSERT INTO train_schedules (train_id, station_id, scheduled_arrival, scheduled_departure, platform_number, stop_sequence, halt_duration_minutes) VALUES
(1, 2, '21:40', '21:45', 1, 1, 5),
(1, 3, '22:10', '22:12', 3, 2, 2),
(1, 4, '22:38', '22:40', 2, 3, 2),
(1, 13, '00:05', '00:10', 1, 4, 5),
(1, 15, '05:30', '05:40', 2, 5, 10),
(2, 1, '05:45', '05:50', 2, 1, 5),
(2, 5, '06:33', '06:35', 1, 2, 2),
(2, 6, '07:18', '07:20', 1, 3, 2),
(3, 1, '06:10', '06:15', 5, 1, 5),
(3, 5, '07:08', '07:10', 2, 2, 2),
(3, 8, '10:50', '10:55', 3, 3, 5),
(3, 7, '14:05', '14:15', 1, 4, 10),
(4, 2, '15:45', '15:50', 4, 1, 5),
(4, 13, '18:05', '18:10', 2, 2, 5),
(4, 11, '21:20', '21:30', 1, 3, 10),
(5, 2, '06:00', '06:05', 3, 1, 5),
(5, 11, '10:05', '10:10', 2, 2, 5),
(5, 15, '12:15', '12:25', 1, 3, 10);

-- 6. Train Movements (Telemetry & Audit Logs)
INSERT INTO train_movements (train_number, station_code, track_number, speed_kmh, progress_percent, movement_status, notes) VALUES
('12637', 'MS', 1, 88, 24.0, 'IN_TRANSIT', 'Cleared Egmore home signal, advancing towards Tambaram section.'),
('20607', 'MAS', 1, 115, 12.0, 'DEPARTED', 'On-time green corridor dispatch towards Arakkonam Junction.'),
('12675', 'SA', 3, 76, 58.0, 'DIVERTED', 'Diverted to Loop Line 3 to facilitate non-stop high-priority overtakes.'),
('12605', 'TPJ', 1, 92, 45.0, 'APPROACHING', 'Outer home signal cleared for Platform 1 arrival.'),
('22671', 'MDU', 2, 110, 78.0, 'IN_TRANSIT', 'Approaching Dindigul-Madurai double line chord section.'),
('G-7821', 'MAS', 5, 65, 32.0, 'IN_TRANSIT', 'Bulk coal rake utilizing freight bypass avoiding suburban platforms.'),
('F-9102', 'SA', 1, 58, 42.0, 'HELD', 'Signal hold at outer home pending clearance of Down express rake.'),
('56841', 'TPJ', 3, 55, 35.0, 'IN_TRANSIT', 'Cauvery Delta passenger traversing Thanjavur branch line.');

-- 7. Train Delays (Root-Cause Records)
INSERT INTO train_delays (train_number, station_code, delay_minutes, delay_reason) VALUES
('12675', 'SA', 14, 'Interlocking line contention at Erode junction throat.'),
('20635', 'TBM', 8, 'Suburban EMU clearance delay ahead on Chord line.'),
('56001', 'AJJ', 12, 'Temporary engineering speed restriction (30 km/h) on Track 3.'),
('G-5540', 'TEN', 22, 'Heavy container loading hold at Tuticorin port marshalling yard.'),
('F-6210', 'MDU', 18, 'Agricultural bulk freight sorting delay at Dindigul junction yard.');

-- 8. Track Conflicts
INSERT INTO track_conflicts (conflict_code, station_code, track_number, train_primary, train_secondary, conflict_type, severity, priority_winner, decision_reason, status) VALUES
('CONF-2026-081', 'MAS', 1, '20607', 'G-7821', 'OVERTAKE', 'HIGH', '20607', 'Vande Bharat Express granted mainline speed priority; freight steered to loop.', 'RESOLVED'),
('CONF-2026-082', 'SA', 1, '12675', 'F-9102', 'CROSSING', 'CRITICAL', '12675', 'Kovai Superfast given Mainline; Salem Steel freight routed to Loop Line 3.', 'RESOLVED'),
('CONF-2026-083', 'MS', 1, '12637', '66017', 'OVERTAKE', 'HIGH', '12637', 'Pandian Superfast retains Track 1; suburban local diverted to Loop Line 4.', 'RESOLVED'),
('CONF-2026-084', 'TPJ', 1, '12605', '56841', 'HEAD_ON', 'HIGH', '12605', 'Pallavan Express retains Chord Up line; Passenger held on Track 3 loop.', 'ACTIVE');

-- 9. Diversion History
INSERT INTO diversion_history (conflict_code, train_number, diverted_from_track, diverted_to_track, station_code, action_type, controller_approved, reason) VALUES
('CONF-2026-081', 'G-7821', 1, 5, 'MAS', 'AUTOMATIC_DIVERSION', TRUE, 'AI priority scoring: 20607 (Vande Bharat = 98 pts) vs G-7821 (Goods = 38 pts). Diverted freight to siding 5.'),
('CONF-2026-082', 'F-9102', 1, 3, 'SA', 'CONTROLLER_DIVERSION', TRUE, 'Controller approved diversion of slow freight to Loop 3 to preserve Kovai Superfast momentum.'),
('CONF-2026-083', '66017', 1, 4, 'MS', 'AUTOMATIC_DIVERSION', TRUE, 'Suburban passenger moved to Track 4 dedicated EMU loop to prevent cascading delays.'),
('CONF-2026-080', 'G-3180', 1, 4, 'AJJ', 'AUTOMATIC_DIVERSION', TRUE, 'Auto logistics rake routed to loop line to clear track for approaching Mail service.');

-- 10. Alert Notifications (History)
INSERT INTO alert_notifications (alert_code, train_number, conflict_type, recommended_action, dispatch_status, severity, station_code, track_number, details) VALUES
('ALT-9041', 'G-7821', 'Track Contention', 'Divert G-7821 to Freight Siding 5', 'AUTOMATICALLY_DIVERTED', 'HIGH', 'MAS', 1, 'Train 20607 and G-7821 approached same Track 1 within 5-min window. Vande Bharat maintained main track.'),
('ALT-9042', 'F-9102', 'Line Clearance Overlap', 'Divert F-9102 to Platform Loop 3', 'CONTROLLER_APPROVED', 'CRITICAL', 'SA', 1, 'Controller verified and approved track switch avoiding head-on conflict with incoming Express 12675.'),
('ALT-9043', '12675', 'Signal Hold Warning', 'Hold at Outer Home Signal', 'HELD_AT_SIGNAL', 'MODERATE', 'ED', 2, 'Temporary caution clearance issued for Erode yard cross-over.'),
('ALT-9044', '66017', 'Suburban Headway Merge', 'Reroute to Track 4 EMU Loop', 'AUTOMATICALLY_DIVERTED', 'HIGH', 'MS', 1, 'Suburban EMU shifted from Track 1 to Track 4 to maintain mainline throughput.'),
('ALT-9045', 'G-5540', 'Yard Schedule Contention', 'Hold on Siding 5 until 23:45', 'PENDING_APPROVAL', 'LOW', 'TEN', 5, 'Container rake awaiting line token clearance from Madurai controller.');
