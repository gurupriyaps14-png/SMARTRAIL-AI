# SmartRail AI - Intelligent Railway Traffic Monitoring and Control System
### Southern Railway Zone — Tamil Nadu Operational Corridor

**SmartRail AI** is an enterprise-grade Java web application designed for railway controllers to monitor live train movements, track section occupancy, detect potential track conflicts in real time, evaluate train priority with explainable multi-factor scoring, automatically divert trains to clear loop/siding tracks, manage timetables & schedules, and inspect comprehensive telemetry analytics via interactive Chart.js visualizations.

---

## 🚆 System Highlights & Capabilities

### 1. Opening & Loading Screen (Indian Railway Theme)
- Animated Indian Railway electric locomotive (WAP-7 / Vande Bharat styling with national tricolor stripe, pantograph, rotating wheelsets, headlight cone glow) speeding along railway tracks beneath catenary electric wires.
- Multi-stage telemetry initialization progress bar (`0%` to `100%`) with smooth transition into the control-room dashboard. Includes "Skip Intro" and top-header "Replay Intro" controls.

### 2. Main Dashboard & Control-Room Interface
- **Design System**: Dark navy (`#080E1C`, `#0B1325`) and black (`#060913`) interface, crisp white text, **amber highlights** (`#F59E0B`), and **teal status indicators** (`#06B6D4`, `#10B981`).
- **Left Sidebar**: Expandable navigation tree (*Dashboard*, *Live Train Monitoring*, *Train Management*, *Station Management*, *Track Management*, *Train Scheduling*, *AI Dispatch*, *Alerts & Notifications*, *Analytics*).
- **Territory & Telemetry**: Tamil Nadu divisions under Southern Railway (SR), real-time digital clock in **IST (UTC+05:30)**, and clear simulated telemetry badge indicator.

### 3. Tamil Nadu Location & Time Filter System
- **City / Railway Division**: Chennai Division (`MAS`), Salem Division (`SA`), Tiruchirappalli Division (`TPJ`), Madurai Division (`MDU`).
- **Station**: Dynamically updates based on the selected division (*Chennai Central*, *Chennai Egmore*, *Tambaram*, *Chengalpattu*, *Katpadi*, *Salem Jn*, *Coimbatore Jn*, *Erode Jn*, *Tiruchirappalli Jn*, *Madurai Jn*, *Tirunelveli Jn*, *Rameswaram*, etc.).
- **Time Slot & Category**: Night, Morning Peak, Midday, Evening Peak, or All Operational Hours; Express, Passenger, Goods, Freight.

### 4. Central Visual Railway Track Simulation (SCADA / CTC Panel)
- 5 parallel railway tracks:
  - `Track 1`: Up Main Line (Eastbound ➔, 110 km/h)
  - `Track 2`: Down Main Line (⬅ Westbound, 110 km/h)
  - `Track 3`: Platform Loop 1 (65 km/h)
  - `Track 4`: Platform Loop 2 (65 km/h)
  - `Track 5`: Freight Bypass Siding (50 km/h)
- Visual track sleepers, turnout switches (crossover lines), station platform structures, and multi-aspect signals (Green, Amber, Red).

### 5. Live Train Movement & Destination Arrival Engine
- Continuous position interpolation using `requestAnimationFrame`.
- Realistic Tamil Nadu trains (*12637 Pandian Superfast*, *20607 Vande Bharat Express*, *12675 Kovai Express*, *G-7821 Ennore Coal Cargo*, *F-9102 Salem Steel Freight*).
- Interactive hover tooltip with comprehensive live telemetry.
- **Destination Arrival Logic**: Stops train at platform, displays celebratory banner: `"Successfully Reached Destination"`, fires toast alert, and updates status to teal `ARRIVED`.
- Simulation controls: **Start (▶)**, **Pause (⏸)**, **Reset (🔄)**, and Speed Multipliers (**1x**, **2x**, **5x**).

---

## 🗄️ MySQL Database Integration (10 Tables)

SmartRail AI uses JDBC for robust database connectivity with primary support for **MySQL** (`localhost:3306/smartrail_db`) and zero-config embedded JDBC fallback mode.

The database schema (`sql/schema.sql`) implements all 10 required relational tables:

1. **`divisions`**: Railway Divisions (MAS, SA, TPJ, MDU), headquarters, active train count, zone descriptions.
2. **`stations`**: 18 key junction stations across Tamil Nadu with platform count, track lines, coordinates, and junction classification.
3. **`tracks`**: 5 interlocked lines per station (Up Main, Down Main, Platform Loop 1, Platform Loop 2, Freight Siding), length, speed rating, and occupancy status.
4. **`trains`**: Live train registry with train number, name, category, origin, destination, current station, track, direction, ETA/ETD, delay, speed, and progress.
5. **`train_schedules`**: Timetable stops linking trains to stations, arrival/departure times, platform numbers, and sequence.
6. **`train_movements`**: Real-time and historical block section transit telemetry, speed, progress, and movement status.
7. **`train_delays`**: Log of delays across trains, recorded duration in minutes, station codes, and operational delay reasons.
8. **`track_conflicts`**: Conflict audit records, conflict code, contending train numbers, conflict type, severity, priority winner, and decision reason.
9. **`diversion_history`**: Audit trail of executed diversions, from-track, to-track, station, action type (Automatic, Controller Approved, Signal Hold), and approval status.
10. **`alert_notifications`**: Historical notification records with Alert ID, Train number, Conflict type, Time detected, Recommended action, and Dispatch status.

---

## 📊 Analytics Dashboard (Chart.js 4.x)

The Analytics view provides 6 interactive charts with real-time database telemetry:

- **A. Train Delay Analysis**: Bar chart comparing delay duration in minutes across trains in the fleet.
- **B. Train Traffic Analysis**: Line/area chart showing active train volume operating across hourly time blocks.
- **C. Train Punctuality**: Donut chart displaying the proportion of on-time services (0 min delay) vs delayed trains with centered KPI percentage.
- **D. Track Utilization**: Bar chart tracking occupancy frequency and capacity utilization across Track 1 to Track 5.
- **E. Train Category Analysis**: Pie chart breaking down Passenger, Express, Goods, and Freight rakes.
- **F. Diversion Analysis**: Grouped bar chart comparing detected conflicts vs automatic diversions vs controller-approved diversions vs signal holds.

---

## 🚨 Alert History Page

The Alert History page displays all recorded safety events:
- **Alert ID**: Unique tracking identifier (e.g., `ALT-9041`, `ALT-CONF-07A719B0`).
- **Train Number**: Contending train number.
- **Conflict Type**: Same-track contention, opposing head-on risk, overtake, or yard block.
- **Time Detected**: Precise timestamp of detection.
- **Recommended Action**: Recommended track diversion or outer signal hold.
- **Dispatch Status**: `AUTOMATICALLY_DIVERTED`, `CONTROLLER_APPROVED`, `HELD_AT_SIGNAL`, `PENDING_APPROVAL`.
- Includes live search and severity filtering.

---

## 👨‍✈️ Railway Data Management

Authorized controllers can:
1. **Add Train Details**: Register simulated trains with custom number, name, category, speed, track assignment, and direction via modal and database persistence.
2. **Update Train Schedules**: View and adjust scheduled arrival, departure, and platform numbers via the Schedule Management center.
3. **Inspect Station Infrastructure**: Review platform count, interlocking types, and track lines across Tamil Nadu stations.
4. **Monitor Track Availability**: View line block occupancy and toggle maintenance flags on any track.
5. **View Train Movement History**: Audit chronological block transits, speeds, and signal indications.

---

## 🧠 AI Conflict Detection & Priority Engine

1. **`ConflictDetector.java`**: Continuous spatial and temporal scanning detecting same-track contentions, opposing head-on approaches, and overlapping arrival windows.
2. **`TrainPriorityManager.java`**: Multi-factor explainable scoring:
   - Category weights: Express (+100), Passenger (+70), Goods (+45), Freight (+30).
   - On-time bonus (+15) and moderate delay recovery incentives.
   - Severe delay capping (>45 min) to prevent slow freight from causing gridlock.
   - Kinetic speed and mainline geometry alignment.
3. **`TrainDispatcher.java`**: Evaluates alternative platform loops and freight siding lines; assigns safe diversions or issues signal holds when all lines are occupied.

---

## 🧪 Verified Scenarios

The system has been tested and verified across all 7 operational scenarios:

| Scenario | Description | Verified Result |
|---|---|---|
| **Scenario 1** | Train moves normally and reaches destination | Progress reaches 100%, status changes to `ARRIVED`, animation stops, and `"Successfully Reached Destination"` banner is displayed. |
| **Scenario 2** | Two trains approach the same track | Conflict is detected (`OPPOSING_HEAD_ON_RISK`), track status turns red, and warning card triggers. |
| **Scenario 3** | Priority engine evaluates both trains | Multi-factor scores calculated (e.g. 150.0 vs 149.2), explainable decision reasons logged. |
| **Scenario 4** | Alternative track available, diversion recommended | Lower-priority train recommended for diversion to Platform Loop 1 (Track 3); controller approves; track reroutes safely. |
| **Scenario 5** | No alternative track available | Warning displayed: `"CRITICAL WARNING: Train must be held at Outer Approach Signal"`; Signal Hold executed. |
| **Scenario 6** | Train delayed and status updates | Delay updated to +45 min via API/controller UI; status updates to `DELAYED` across DB and telemetry. |
| **Scenario 7** | Analytics dashboard updates | Chart.js graphs immediately reflect updated delay (Train #12675 top delayed), punctuality recalculated, and audit events logged. |

---

## 📂 Project Structure

```
SmartRail Ai Web/
├── deploy.ps1                     # Automated build, compilation & deployment script
├── run.bat                        # One-click Windows runner
├── run.ps1                        # PowerShell runner
├── stop.bat                       # Stop Tomcat script
├── pom.xml                        # Maven configuration
├── sql/
│   ├── schema.sql                 # 10 MySQL tables schema
│   └── data.sql                   # Initial Tamil Nadu railway dataset
├── src/main/java/com/smartrail/
│   ├── ai/
│   │   ├── ConflictDetector.java     # Conflict detection engine
│   │   ├── TrainPriorityManager.java # Priority scoring algorithm
│   │   └── TrainDispatcher.java      # Track diversion & interlocking resolver
│   ├── model/
│   │   ├── Train.java                # Train entity
│   │   ├── Station.java              # Station entity
│   │   ├── Track.java                # Track entity
│   │   ├── Division.java             # Division entity
│   │   ├── TrainSchedule.java        # Timetable schedule model
│   │   ├── TrainMovement.java        # Telemetry movement model
│   │   ├── TrainDelay.java           # Train delay model
│   │   ├── TrackConflictRecord.java  # Conflict audit record model
│   │   ├── DiversionRecord.java      # Diversion history model
│   │   ├── AlertNotification.java    # Alert notification model
│   │   ├── Conflict.java             # Active contention model
│   │   ├── DiversionRecommendation.java # Explainable diversion model
│   │   ├── AlertItem.java            # Live alert item
│   │   ├── DispatchMode.java         # AUTOMATIC vs MANUAL enum
│   │   ├── TrainCategory.java        # Category enum
│   │   └── TrainStatus.java          # Operational status enum
│   ├── dao/
│   │   ├── DBConnection.java         # JDBC Dual MySQL + Embedded fallback manager
│   │   ├── DivisionDAO.java
│   │   ├── StationDAO.java
│   │   ├── TrackDAO.java
│   │   ├── TrainDAO.java
│   │   ├── TrainScheduleDAO.java
│   │   ├── TrainMovementDAO.java
│   │   ├── TrainDelayDAO.java
│   │   ├── TrackConflictDAO.java
│   │   ├── DiversionHistoryDAO.java
│   │   ├── AlertNotificationDAO.java
│   │   └── AnalyticsDAO.java
│   ├── service/
│   │   └── RailwayService.java       # High-level coordinator
│   └── servlet/
│       ├── DashboardServlet.java     # / and /dashboard
│       ├── ApiConflictServlet.java   # /api/conflict
│       ├── ApiDivisionServlet.java   # /api/divisions
│       ├── ApiStationServlet.java    # /api/stations
│       ├── ApiTrackServlet.java      # /api/tracks
│       ├── ApiTrainServlet.java      # /api/trains
│       ├── ApiSimulationServlet.java # /api/simulation
│       ├── ApiAnalyticsServlet.java  # /api/analytics
│       ├── ApiScheduleServlet.java   # /api/schedules
│       ├── ApiAlertHistoryServlet.java # /api/alert-history
│       └── ApiMovementServlet.java   # /api/movements
├── src/main/webapp/
│   ├── css/                          # style.css, loading.css, dashboard.css, track-display.css
│   ├── js/                           # loading.js, trackSimulation.js, trainMovement.js, dashboard.js, analytics.js, chart.min.js
│   ├── index.jsp                     # Welcome redirect
│   └── dashboard.jsp                 # Control room dashboard JSP
└── tools/apache-tomcat-9.0.95/       # Embedded servlet container
```

---

## 🚀 How to Access & Run

The application is currently running live:
👉 **`http://localhost:8080/`** (or **`http://localhost:8080/dashboard`**)

To restart or run in terminal anytime:
```cmd
run.bat
```
or via PowerShell:
```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

To recompile and redeploy:
```powershell
powershell -ExecutionPolicy Bypass -File .\deploy.ps1
```

> **Prototype Note**: SmartRail AI is an interactive simulation and decision assistance prototype. All scheduling, kinetic speeds, and track occupancy telemetry are simulated using transparent, explainable rule-based algorithms.
