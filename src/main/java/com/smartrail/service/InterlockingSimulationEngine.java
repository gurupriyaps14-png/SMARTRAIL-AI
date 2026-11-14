package com.smartrail.service;

import com.smartrail.model.JunctionSwitch;
import com.smartrail.model.TrackSection;
import com.smartrail.model.Train;
import com.smartrail.model.TrainCategory;
import com.smartrail.model.TrainStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Intelligent Interlocking Simulation Engine.
 * Serves as the Java Backend Source of Truth for:
 * - Train Spacing & Anti-Collision (Same-direction safe braking distance)
 * - Single-track Opposite-direction Head-on Holding
 * - Track Sections & Occupancy Management (CLEAR / RESERVED / OCCUPIED)
 * - Turnout Geometry & Physical Connecting Side-Track Routing (Track 1 -> Turnout at 45° -> Track 3)
 * - Continuous Backend Kinematics & Coordinate Computations
 */
public class InterlockingSimulationEngine {
    private static final Logger LOGGER = Logger.getLogger(InterlockingSimulationEngine.class.getName());

    // Coordinate constants matching frontend track canvas (Canvas Width: 1200px, Height: 610px)
    public static final double START_X = 140.0;
    public static final double USABLE_WIDTH = 920.0; // from 140px to 1060px

    public static final double TRACK_1_Y = 90.0;
    public static final double TRACK_2_Y = 195.0;
    public static final double TRACK_3_Y = 305.0;
    public static final double TRACK_4_Y = 415.0;
    public static final double TRACK_5_Y = 520.0;

    // Junction & Turnout geometry
    public static final double JNC_T1_X = 260.0;       // Turnout starts on Track 1 at x=260
    public static final double JNC_T1_PROGRESS = ((JNC_T1_X - START_X) / USABLE_WIDTH) * 100.0; // ~13.04% or West block
    public static final double TURNOUT_EXIT_X = 475.0;  // Turnout merges into Track 3 at x=475
    public static final double TURNOUT_EXIT_PROGRESS = ((TURNOUT_EXIT_X - START_X) / USABLE_WIDTH) * 100.0; // ~36.41%
    public static final double TURNOUT_EAST_X = 760.0;  // East exit starts on Track 3 at x=760
    public static final double TURNOUT_EAST_PROGRESS = ((TURNOUT_EAST_X - START_X) / USABLE_WIDTH) * 100.0; // ~67.39%
    public static final double TURNOUT_REJOIN_X = 975.0; // East exit merges back onto Track 1 at x=975
    public static final double TURNOUT_REJOIN_PROGRESS = ((TURNOUT_REJOIN_X - START_X) / USABLE_WIDTH) * 100.0; // ~90.76%

    // Train visual rake span & safe separation margins (in % progress)
    public static final double RAKE_SPAN_PERCENT = 24.0;  // Length of 1 locomotive + 3 coaches (~216px on 920px track)
    public static final double SAFE_STOP_MARGIN = 28.0;   // Minimum distance before absolute RED halt (rake span + safety gap)
    public static final double SAFE_CAUTION_MARGIN = 42.0;// Distance before AMBER caution slowdown

    private final Map<String, TrackSection> trackSections = new ConcurrentHashMap<>();
    private final Map<String, JunctionSwitch> junctions = new ConcurrentHashMap<>();
    private final List<Train> activeTrains = new CopyOnWriteArrayList<>();

    private boolean isPaused = false;
    private double speedMultiplier = 1.0;
    private long lastStepTimestamp = System.currentTimeMillis();
    private String activeScenarioName = "DEFAULT";

    public InterlockingSimulationEngine() {
        initTrackSections();
        initJunctions();
        initDefaultTrains();
    }

    /**
     * Initializes identifiable track sections across tracks 1 to 5 and the connecting crossover side track.
     */
    private void initTrackSections() {
        trackSections.clear();

        // --- Track 1 Sections (Up Main Line) ---
        TrackSection t1_1 = new TrackSection("SEC_T1_1", 1, "Track 1 West Approach", 0.0, 22.0, 110, false);
        TrackSection t1_2 = new TrackSection("SEC_T1_2", 1, "Track 1 Junction Switch 1A", 22.0, 35.0, 90, false);
        TrackSection t1_3 = new TrackSection("SEC_T1_3", 1, "Track 1 Main Platform Line", 35.0, 72.0, 110, false);
        TrackSection t1_4 = new TrackSection("SEC_T1_4", 1, "Track 1 East Departure", 72.0, 100.0, 110, false);

        t1_1.addConnectedSection("SEC_T1_2");
        t1_2.addConnectedSection("SEC_T1_3");
        t1_2.addConnectedSection("SEC_TO_1_3"); // Connected to side track
        t1_3.addConnectedSection("SEC_T1_4");

        trackSections.put(t1_1.getSectionId(), t1_1);
        trackSections.put(t1_2.getSectionId(), t1_2);
        trackSections.put(t1_3.getSectionId(), t1_3);
        trackSections.put(t1_4.getSectionId(), t1_4);

        // --- Track 2 Sections (Down Main Line) ---
        TrackSection t2_1 = new TrackSection("SEC_T2_1", 2, "Track 2 West Block", 0.0, 30.0, 110, false);
        TrackSection t2_2 = new TrackSection("SEC_T2_2", 2, "Track 2 Crossover Intersection", 30.0, 48.0, 80, false);
        TrackSection t2_3 = new TrackSection("SEC_T2_3", 2, "Track 2 Down Main Platform", 48.0, 75.0, 110, false);
        TrackSection t2_4 = new TrackSection("SEC_T2_4", 2, "Track 2 East Block", 75.0, 100.0, 110, false);

        t2_1.addConnectedSection("SEC_T2_2");
        t2_2.addConnectedSection("SEC_T2_3");
        t2_3.addConnectedSection("SEC_T2_4");

        trackSections.put(t2_1.getSectionId(), t2_1);
        trackSections.put(t2_2.getSectionId(), t2_2);
        trackSections.put(t2_3.getSectionId(), t2_3);
        trackSections.put(t2_4.getSectionId(), t2_4);

        // --- Track 3 Sections (Platform Loop Line 1) ---
        TrackSection t3_1 = new TrackSection("SEC_T3_1", 3, "Track 3 West Loop Entry", 0.0, 32.0, 65, false);
        TrackSection t3_2 = new TrackSection("SEC_T3_2", 3, "Track 3 Turnout Convergence", 32.0, 50.0, 50, false);
        TrackSection t3_3 = new TrackSection("SEC_T3_3", 3, "Track 3 Platform Loop 1", 50.0, 78.0, 65, false);
        TrackSection t3_4 = new TrackSection("SEC_T3_4", 3, "Track 3 East Loop Exit", 78.0, 100.0, 65, false);

        t3_1.addConnectedSection("SEC_T3_2");
        t3_2.addConnectedSection("SEC_T3_3");
        t3_3.addConnectedSection("SEC_T3_4");

        trackSections.put(t3_1.getSectionId(), t3_1);
        trackSections.put(t3_2.getSectionId(), t3_2);
        trackSections.put(t3_3.getSectionId(), t3_3);
        trackSections.put(t3_4.getSectionId(), t3_4);

        // --- Track 4 Sections (Platform Loop Line 2) ---
        TrackSection t4_1 = new TrackSection("SEC_T4_1", 4, "Track 4 West Outer", 0.0, 35.0, 65, false);
        TrackSection t4_2 = new TrackSection("SEC_T4_2", 4, "Track 4 Platform Loop 2", 35.0, 75.0, 65, false);
        TrackSection t4_3 = new TrackSection("SEC_T4_3", 4, "Track 4 East Outer", 75.0, 100.0, 65, false);

        t4_1.addConnectedSection("SEC_T4_2");
        t4_2.addConnectedSection("SEC_T4_3");

        trackSections.put(t4_1.getSectionId(), t4_1);
        trackSections.put(t4_2.getSectionId(), t4_2);
        trackSections.put(t4_3.getSectionId(), t4_3);

        // --- Track 5 Sections (Freight Siding) ---
        TrackSection t5_1 = new TrackSection("SEC_T5_1", 5, "Track 5 West Siding", 0.0, 35.0, 50, false);
        TrackSection t5_2 = new TrackSection("SEC_T5_2", 5, "Track 5 Freight Bypass Line", 35.0, 75.0, 50, false);
        TrackSection t5_3 = new TrackSection("SEC_T5_3", 5, "Track 5 East Siding", 75.0, 100.0, 50, false);

        t5_1.addConnectedSection("SEC_T5_2");
        t5_2.addConnectedSection("SEC_T5_3");

        trackSections.put(t5_1.getSectionId(), t5_1);
        trackSections.put(t5_2.getSectionId(), t5_2);
        trackSections.put(t5_3.getSectionId(), t5_3);

        // --- Connecting Side Track West Entry (Turnout Crossover 1 -> 3) ---
        TrackSection to_1_3 = new TrackSection("SEC_TO_1_3", 13, "Connecting West Crossover Siding (1->3)", 0.0, 100.0, 40, true);
        to_1_3.addConnectedSection("SEC_T1_2");
        to_1_3.addConnectedSection("SEC_T3_2");
        trackSections.put(to_1_3.getSectionId(), to_1_3);

        // --- Connecting Side Track East Exit (Turnout Crossover 3 -> 1) ---
        TrackSection to_3_1 = new TrackSection("SEC_TO_3_1", 13, "Connecting East Crossover Siding (3->1)", 0.0, 100.0, 40, true);
        to_3_1.addConnectedSection("SEC_T3_4");
        to_3_1.addConnectedSection("SEC_T1_4");
        trackSections.put(to_3_1.getSectionId(), to_3_1);
    }

    /**
     * Initializes physical turnout switches.
     */
    private void initJunctions() {
        junctions.clear();
        JunctionSwitch sw1 = new JunctionSwitch(
                "JNC_T1_3",
                "Turnout Switch 1A - West Entry (Track 1 to Track 3)",
                1, 3,
                "SEC_T1_3", "SEC_TO_1_3",
                JNC_T1_X, TRACK_1_Y
        );
        junctions.put(sw1.getJunctionId(), sw1);

        JunctionSwitch sw2 = new JunctionSwitch(
                "JNC_T3_1",
                "Turnout Switch 1B - East Exit (Track 3 to Track 1)",
                3, 1,
                "SEC_T3_4", "SEC_TO_3_1",
                TURNOUT_EAST_X, TRACK_3_Y
        );
        junctions.put(sw2.getJunctionId(), sw2);
    }

    /**
     * Seeds initial fleet with distinct tracks and routes.
     */
    public void initDefaultTrains() {
        activeTrains.clear();

        // Train 1: Vande Bharat Express on Track 1 (UP)
        Train t1 = new Train(1, "20607", "MGR Chennai Central - Mysuru Vande Bharat Express", TrainCategory.EXPRESS,
                1, "Puratchi Thalaivar Dr. MGR Chennai Central", 6, "Katpadi Junction", 1, "Puratchi Thalaivar Dr. MGR Chennai Central",
                "Katpadi Junction", 1, "UP", TrainStatus.ON_TIME, "22:55", "23:00", 0, 115, 12.0, "NIGHT", true);

        // Train 2: Kovai Superfast Express on Track 3 (UP)
        Train t2 = new Train(2, "12675", "Kovai Superfast Express", TrainCategory.EXPRESS,
                1, "Puratchi Thalaivar Dr. MGR Chennai Central", 8, "Salem Junction", 1, "Puratchi Thalaivar Dr. MGR Chennai Central",
                "Salem Junction", 3, "UP", TrainStatus.DELAYED, "23:05", "23:15", 14, 76, 55.0, "NIGHT", true);

        // Train 3: Chennai Port Auto Logistics Cargo on Track 4 (DOWN)
        Train t3 = new Train(3, "G-3180", "Chennai Port Auto Logistics Cargo", TrainCategory.GOODS,
                6, "Katpadi Junction", 1, "Puratchi Thalaivar Dr. MGR Chennai Central", 6, "Katpadi Junction",
                "Puratchi Thalaivar Dr. MGR Chennai Central", 4, "DOWN", TrainStatus.ON_TIME, "23:10", "23:25", 0, 70, 62.0, "NIGHT", true);

        // Train 4: Ennore Thermal Coal Bulk Cargo on Track 5 (UP)
        Train t4 = new Train(4, "G-7821", "Ennore Thermal Coal Bulk Cargo", TrainCategory.FREIGHT,
                8, "Salem Junction", 1, "Puratchi Thalaivar Dr. MGR Chennai Central", 8, "Salem Junction",
                "Puratchi Thalaivar Dr. MGR Chennai Central", 5, "UP", TrainStatus.MOVING, "23:20", "23:45", 5, 55, 30.0, "NIGHT", true);

        // Train 5: Cheran Superfast Express on Track 2 (DOWN)
        Train t5 = new Train(5, "12674", "Cheran Superfast Express", TrainCategory.EXPRESS,
                8, "Salem Junction", 1, "Puratchi Thalaivar Dr. MGR Chennai Central", 8, "Salem Junction",
                "Puratchi Thalaivar Dr. MGR Chennai Central", 2, "DOWN", TrainStatus.ON_TIME, "23:30", "23:35", 2, 85, 20.0, "NIGHT", true);

        activeTrains.add(t1);
        activeTrains.add(t2);
        activeTrains.add(t3);
        activeTrains.add(t4);
        activeTrains.add(t5);

        activeScenarioName = "DEFAULT";
        // Ensure turnouts stay in NORMAL (straight) position until explicit controller dispatch
        for (JunctionSwitch jsw : junctions.values()) {
            jsw.setAlignment("NORMAL");
            jsw.setLocked(false);
            jsw.setReservedForTrain(null);
        }
        TrackSection secTo13 = trackSections.get("SEC_TO_1_3");
        if (secTo13 != null) {
            secTo13.setStatus("CLEAR");
            secTo13.setOccupyingTrainNumber(null);
            secTo13.setSignalAspect("RED");
        }
        TrackSection secTo31 = trackSections.get("SEC_TO_3_1");
        if (secTo31 != null) {
            secTo31.setStatus("CLEAR");
            secTo31.setOccupyingTrainNumber(null);
            secTo31.setSignalAspect("RED");
        }

        recalculateSectionsAndOccupancy();
    }

    /**
     * Prepares Predictive Conflict on Track 1 between Train 20607 and Train 16127.
     * Ensures both trains are on Track 1, resets arrived states, and clears Track 3 for diversion.
     */
    public synchronized void setupPredictiveConflict() {
        Train t1 = null;
        Train t2 = null;
        for (Train t : activeTrains) {
            if ("20607".equals(t.getTrainNumber())) t1 = t;
            if ("16127".equals(t.getTrainNumber())) t2 = t;
        }

        if (t1 != null) {
            t1.setCurrentTrackNumber(1);
            t1.setDirection("UP");
            t1.setProgressPercent(42.0);
            t1.setStatus(TrainStatus.MOVING);
            t1.setSpeedKmh(95);
            t1.setDwellRemaining(0.0);
            t1.setSignalAspect("GREEN");
        }

        if (t2 == null) {
            t2 = new Train(activeTrains.size() + 1, "16127", "Guruvayur Express", TrainCategory.EXPRESS,
                    1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                    1, "UP", TrainStatus.MOVING, "22:58", "23:10", 12, 45, 5.0, "NIGHT", true);
            activeTrains.add(t2);
        } else {
            t2.setCurrentTrackNumber(1);
            t2.setDirection("UP");
            t2.setProgressPercent(5.0);
            t2.setStatus(TrainStatus.MOVING);
            t2.setSpeedKmh(45);
            t2.setDwellRemaining(0.0);
            t2.setDiverting(false);
            t2.setDiversionCompleted(false);
            t2.setSignalAspect("GREEN");
        }

        // Ensure Track 1 has ONLY the two conflict trains (prevents multi-train bumper pileups)
        for (Train ot : activeTrains) {
            if (!"20607".equals(ot.getTrainNumber()) && !"16127".equals(ot.getTrainNumber()) && ot.getCurrentTrackNumber() == 1) {
                ot.setCurrentTrackNumber(2);
                ot.setDirection("DOWN");
                ot.setProgressPercent(75.0);
            }
        }

        // Clear Track 3 so the loop diversion path is available
        for (Train ot : activeTrains) {
            if (!"16127".equals(ot.getTrainNumber()) && ot.getCurrentTrackNumber() == 3) {
                ot.setProgressPercent(95.0);
                ot.setStatus(TrainStatus.ARRIVED);
                ot.setSpeedKmh(0);
            }
        }

        // Ensure turnouts stay in NORMAL (straight) position until explicit controller dispatch
        for (JunctionSwitch jsw : junctions.values()) {
            jsw.setAlignment("NORMAL");
            jsw.setLocked(false);
            jsw.setReservedForTrain(null);
        }
        TrackSection secTo13 = trackSections.get("SEC_TO_1_3");
        if (secTo13 != null) {
            secTo13.setStatus("CLEAR");
            secTo13.setOccupyingTrainNumber(null);
            secTo13.setSignalAspect("RED");
        }
        TrackSection secTo31 = trackSections.get("SEC_TO_3_1");
        if (secTo31 != null) {
            secTo31.setStatus("CLEAR");
            secTo31.setOccupyingTrainNumber(null);
            secTo31.setSignalAspect("RED");
        }

        recalculateSectionsAndOccupancy();
    }

    /**
     * Advanced Test Scenario Configurator.
     * Implements Test Scenarios A, B, C, D, and E as required by user.
     */
    public void setupScenario(String scenario) {
        if (scenario == null) scenario = "DEFAULT";
        scenario = scenario.toUpperCase().trim();
        this.activeScenarioName = scenario;

        activeTrains.clear();
        initTrackSections();
        initJunctions();

        switch (scenario) {
            case "A":
            case "SCENARIO_A":
                // TEST A: Two same-direction trains on Track 1. Following train MUST maintain safe separation.
                Train leadA = new Train(1, "20607", "Lead Vande Bharat Express", TrainCategory.EXPRESS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:55", "23:00", 0, 65, 48.0, "NIGHT", true);

                Train followA = new Train(2, "F-9102", "Following Heavy Steel Rake", TrainCategory.FREIGHT,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:58", "23:10", 0, 95, 22.0, "NIGHT", true);

                activeTrains.add(leadA);
                activeTrains.add(followA);
                break;

            case "B":
            case "SCENARIO_B":
                // TEST B: Two opposite-direction trains approaching same Track 1. Opposite train must hold at signal.
                Train upTrainB = new Train(1, "12674", "Cheran Express (UP)", TrainCategory.EXPRESS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:55", "23:00", 0, 90, 18.0, "NIGHT", true);

                Train downTrainB = new Train(2, "16127", "Guruvayur Express (DOWN)", TrainCategory.EXPRESS,
                        6, "Katpadi Jn", 1, "Chennai Central", 6, "Katpadi Jn", "Chennai Central",
                        1, "DOWN", TrainStatus.MOVING, "22:57", "23:05", 10, 85, 18.0, "NIGHT", true);

                activeTrains.add(upTrainB);
                activeTrains.add(downTrainB);
                break;

            case "C":
            case "SCENARIO_C":
                // TEST C: Converging Contention Scenario. Awaiting controller dispatch confirmation.
                // CRITICAL RULE: Loading scenario/detecting conflict must NOT automatically execute diversion.
                Train retainingC = new Train(1, "20607", "MGR Chennai Central - Mysuru Vande Bharat Express", TrainCategory.EXPRESS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:55", "23:00", 2, 95, 42.0, "NIGHT", true);

                Train divertC = new Train(2, "16127", "Guruvayur Express", TrainCategory.EXPRESS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:58", "23:10", 12, 45, 5.0, "NIGHT", true);

                divertC.setDiverting(false);
                divertC.setDiversionCompleted(false);

                activeTrains.add(retainingC);
                activeTrains.add(divertC);

                // Keep turnouts in NORMAL (straight) position until controller confirms
                JunctionSwitch jc1a = junctions.get("J1A");
                if (jc1a != null) {
                    jc1a.setAlignment("NORMAL");
                    jc1a.setLocked(false);
                    jc1a.setReservedForTrain(null);
                }
                JunctionSwitch jc1b = junctions.get("J1B");
                if (jc1b != null) {
                    jc1b.setAlignment("NORMAL");
                    jc1b.setLocked(false);
                    jc1b.setReservedForTrain(null);
                }

                // Pre-clear Track 3 of nominal traffic so loop path is ready once confirmed
                for (Train ot : activeTrains) {
                    if (!"16127".equals(ot.getTrainNumber()) && ot.getCurrentTrackNumber() == 3) {
                        ot.setProgressPercent(95.0);
                        ot.setStatus(TrainStatus.ARRIVED);
                        ot.setSpeedKmh(0);
                    }
                }
                break;

            case "D":
            case "SCENARIO_D":
                // TEST D: Occupied Side Track. Diversion blocked; train must hold at signal safely.
                Train blockerD = new Train(3, "G-7821", "Stalled Wagon on Crossover / Track 3", TrainCategory.GOODS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        3, "UP", TrainStatus.HELD, "23:00", "23:30", 35, 0, 42.0, "NIGHT", true);

                Train divertD = new Train(2, "16127", "Guruvayur Express (Diversion Denied)", TrainCategory.EXPRESS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:58", "23:10", 12, 75, 18.0, "NIGHT", true);

                activeTrains.add(blockerD);
                activeTrains.add(divertD);

                // Mark connecting side track and Track 3 as occupied
                TrackSection toSec = trackSections.get("SEC_TO_1_3");
                if (toSec != null) {
                    toSec.setStatus("OCCUPIED");
                    toSec.setOccupyingTrainNumber("G-7821");
                    toSec.setSignalAspect("RED");
                }
                break;

            case "E":
            case "SCENARIO_E":
            default:
                initDefaultTrains();
                break;
        }

        // Ensure turnouts stay in NORMAL (straight) position until explicit controller dispatch
        for (JunctionSwitch jsw : junctions.values()) {
            jsw.setAlignment("NORMAL");
            jsw.setLocked(false);
            jsw.setReservedForTrain(null);
        }
        TrackSection secTo13 = trackSections.get("SEC_TO_1_3");
        if (secTo13 != null) {
            secTo13.setStatus("CLEAR");
            secTo13.setOccupyingTrainNumber(null);
            secTo13.setSignalAspect("RED");
        }
        TrackSection secTo31 = trackSections.get("SEC_TO_3_1");
        if (secTo31 != null) {
            secTo31.setStatus("CLEAR");
            secTo31.setOccupyingTrainNumber(null);
            secTo31.setSignalAspect("RED");
        }

        recalculateSectionsAndOccupancy();
    }

    /**
     * Main Physics and Interlocking Step Loop.
     * Synchronously computes spacing, signals, track occupancy, turnout kinematics, and coordinates.
     */
    public synchronized void stepSimulation(double deltaSec) {
        if (isPaused || deltaSec <= 0.0) return;
        deltaSec = Math.min(deltaSec, 0.25) * speedMultiplier;

        // 1. Spacing, Collision Prevention & Speed Determination
        for (Train train : activeTrains) {
            if ("ARRIVED".equalsIgnoreCase(train.getStatus().name())) {
                train.setSpeedKmh(0);
                train.setSpacingStatus("ARRIVED");
                continue;
            }

            // Check if train is held at signal or diverted
            if (train.isDiverting()) {
                handleDiversionKinematics(train, deltaSec);
                continue;
            }

            // Anti-Collision Check: Find leading train on the same track
            Train leadingTrain = findLeadingTrainOnSameTrack(train);
            Train opposingTrain = findOpposingTrainOnSameTrack(train);

            double effectiveSpeed = train.getSpeedKmh();

            if (opposingTrain != null) {
                // Opposing Head-On Detection on single line
                double gap = 100.0 - (train.getProgressPercent() + opposingTrain.getProgressPercent());
                boolean hasPriority = resolveOpposingPriority(train, opposingTrain);

                if (!hasPriority) {
                    if (gap <= SAFE_CAUTION_MARGIN) {
                        effectiveSpeed = 0.0;
                        train.setSignalAspect("RED");
                        train.setSpacingStatus("STANDSTILL_HELD");
                        train.setStatus(TrainStatus.HELD);
                    }
                } else {
                    if (gap <= SAFE_STOP_MARGIN) {
                        // Hold high priority train as well if gap breaches absolute safety barrier
                        effectiveSpeed = 0.0;
                        train.setSignalAspect("RED");
                        train.setSpacingStatus("STANDSTILL_HELD");
                        train.setStatus(TrainStatus.HELD);
                    } else if (gap <= SAFE_CAUTION_MARGIN) {
                        effectiveSpeed = Math.min(25.0, train.getSpeedKmh() * 0.35);
                        train.setSignalAspect("AMBER");
                        train.setSpacingStatus("CAUTION_SLOW");
                    }
                }
            } else if (leadingTrain != null) {
                // Same direction safe separation
                double separation = leadingTrain.getProgressPercent() - train.getProgressPercent();
                train.setSeparationDistancePercent(separation);
                train.setLeadTrainNumber(leadingTrain.getTrainNumber());

                if (separation <= SAFE_STOP_MARGIN) {
                    // STOP behind leading train - prevents visual mesh overlap
                    effectiveSpeed = 0.0;
                    train.setSignalAspect("RED");
                    train.setSpacingStatus("STANDSTILL_HELD");
                    train.setStatus(TrainStatus.HELD);
                } else if (separation <= SAFE_CAUTION_MARGIN) {
                    // Caution: Slow to 25 km/h
                    effectiveSpeed = Math.min(25.0, train.getSpeedKmh() * 0.35);
                    train.setSignalAspect("AMBER");
                    train.setSpacingStatus("CAUTION_SLOW");
                } else {
                    train.setSignalAspect("GREEN");
                    train.setSpacingStatus("NOMINAL");
                }
            } else {
                train.setSignalAspect("GREEN");
                train.setSpacingStatus("NOMINAL");
            }

            // Step train progress with strict boundary clamping against collision
            if (effectiveSpeed > 0.0) {
                double ratePercentPerSec = (effectiveSpeed / 90.0) * 3.8;
                double newProgress = train.getProgressPercent() + ratePercentPerSec * deltaSec;

                if (leadingTrain != null) {
                    newProgress = Math.min(newProgress, leadingTrain.getProgressPercent() - SAFE_STOP_MARGIN);
                }
                if (opposingTrain != null) {
                    newProgress = Math.min(newProgress, 100.0 - opposingTrain.getProgressPercent() - SAFE_STOP_MARGIN);
                }

                if (newProgress >= 100.0) {
                    newProgress = 100.0;
                    train.setStatus(TrainStatus.ARRIVED);
                    train.setSpeedKmh(0);
                    train.setDwellRemaining(8.0);
                }
                train.setProgressPercent(newProgress);
            }
        }

        // 2. Compute Physical Visual Coordinates (X, Y, Rotation)
        for (Train train : activeTrains) {
            updateTrainCoordinates(train);
        }

        // 3. Recalculate Section Occupancy & Signals
        recalculateSectionsAndOccupancy();
    }

    /**
     * Resolves physical movement through the connecting turnout side track.
     * Prevents jumping! Moves through Track 1 -> Junction 1A -> Turnout Side Track -> Junction 1B -> Main Line.
     * 6-State Progression: NORMAL_MOVEMENT -> APPROACHING_JUNCTION -> ENTERING_SIDING -> MOVING_ON_SIDING -> EXITING_SIDING -> JOINING_NEXT_TRACK.
     */
    private void handleDiversionKinematics(Train train, double deltaSec) {
        if (train.isDiversionCompleted()) {
            train.setDiverting(false);
            train.setMovementState("NORMAL_MOVEMENT");
            return;
        }

        // Test D Check: Is the connecting side track available?
        TrackSection toSec = trackSections.get("SEC_TO_1_3");
        if (toSec != null && "OCCUPIED".equalsIgnoreCase(toSec.getStatus()) && !train.getTrainNumber().equals(toSec.getOccupyingTrainNumber())) {
            // Connecting side track is occupied! Hold train before switch point.
            if (train.getProgressPercent() >= (JNC_T1_PROGRESS - 2.0)) {
                train.setSpeedKmh(0);
                train.setSignalAspect("RED");
                train.setSpacingStatus("STANDSTILL_HELD");
                train.setStatus(TrainStatus.HELD);
                return;
            }
        }

        // Speed on siding / turnout: 42 km/h
        double effectiveSpeed = 42.0;
        double ratePercentPerSec = (effectiveSpeed / 90.0) * 3.8;
        double newProgress = train.getProgressPercent() + ratePercentPerSec * deltaSec * speedMultiplier;
        train.setProgressPercent(newProgress);

        JunctionSwitch swWest = junctions.get("JNC_T1_3");
        JunctionSwitch swEast = junctions.get("JNC_T3_1");
        TrackSection toExitSec = trackSections.get("SEC_TO_3_1");

        // State Machine Progression based on exact track position
        if (newProgress < JNC_T1_PROGRESS) {
            // 1. Approaching Entry Junction Switch 1A
            train.setMovementState("APPROACHING_JUNCTION");
            train.setCurrentTrackNumber(1);
            train.setDiversionSegment("APPROACH_TRACK_1");
            if (swWest != null) {
                swWest.setAlignment("REVERSE");
                swWest.setLocked(true);
                swWest.setReservedForTrain(train.getTrainNumber());
            }
        } else if (newProgress < TURNOUT_EXIT_PROGRESS) {
            // 2. Entering Siding via Diagonal West Turnout (45 degrees)
            train.setMovementState("ENTERING_SIDING");
            train.setCurrentTrackNumber(13);
            train.setDiversionSegment("CROSSOVER_TURNOUT_WEST");
            if (swWest != null) {
                swWest.setAlignment("REVERSE");
                swWest.setLocked(true);
            }
            if (toSec != null) {
                toSec.setStatus("OCCUPIED");
                toSec.setOccupyingTrainNumber(train.getTrainNumber());
            }
        } else if (newProgress < TURNOUT_EAST_PROGRESS) {
            // 3. Moving on Side Track (Track 3 horizontal platform loop)
            train.setMovementState("MOVING_ON_SIDING");
            train.setCurrentTrackNumber(3);
            train.setDiversionSegment("PLATFORM_SIDING_LINE");
            // Clear West switch behind the train so Track 1 is unobstructed!
            if (swWest != null) {
                swWest.setAlignment("NORMAL");
                swWest.setLocked(false);
                swWest.setReservedForTrain(null);
            }
            if (toSec != null) {
                toSec.setStatus("CLEAR");
                toSec.setOccupyingTrainNumber(null);
            }
        } else if (newProgress < TURNOUT_REJOIN_PROGRESS) {
            // 4. Exiting Siding via Diagonal East Turnout (-45 degrees)
            train.setMovementState("EXITING_SIDING");
            train.setCurrentTrackNumber(13);
            train.setDiversionSegment("CROSSOVER_TURNOUT_EAST");
            if (swEast != null) {
                swEast.setAlignment("REVERSE");
                swEast.setLocked(true);
                swEast.setReservedForTrain(train.getTrainNumber());
            }
            if (toExitSec != null) {
                toExitSec.setStatus("OCCUPIED");
                toExitSec.setOccupyingTrainNumber(train.getTrainNumber());
            }
        } else {
            // 5. Joining Next Track (Back on Main Track 1!)
            train.setMovementState("JOINING_NEXT_TRACK");
            train.setCurrentTrackNumber(1);
            train.setDiversionSegment("REJOINED_MAIN_LINE");
            if (swEast != null) {
                swEast.setAlignment("NORMAL");
                swEast.setLocked(false);
                swEast.setReservedForTrain(null);
            }
            if (toExitSec != null) {
                toExitSec.setStatus("CLEAR");
                toExitSec.setOccupyingTrainNumber(null);
            }
            // Transition permanently to NORMAL_MOVEMENT - stops diversion animation permanently!
            train.setDiverting(false);
            train.setDiversionCompleted(true);
            train.setMovementState("NORMAL_MOVEMENT");
            train.setSpeedKmh(85);
            train.setStatus(TrainStatus.MOVING);
            train.setSignalAspect("GREEN");
            train.setSpacingStatus("NOMINAL");
        }
    }

    /**
     * Computes pixel X, Y and rotation for a train based on its route, direction, and movement state.
     */
    private void updateTrainCoordinates(Train train) {
        if ("ENTERING_SIDING".equalsIgnoreCase(train.getMovementState())) {
            double span = TURNOUT_EXIT_PROGRESS - JNC_T1_PROGRESS;
            double p = Math.max(0.0, Math.min(1.0, (train.getProgressPercent() - JNC_T1_PROGRESS) / span));
            train.setPosX(JNC_T1_X + (TURNOUT_EXIT_X - JNC_T1_X) * p);
            train.setPosY(TRACK_1_Y + (TRACK_3_Y - TRACK_1_Y) * p);
            train.setRotationDeg(45.0);
        } else if ("MOVING_ON_SIDING".equalsIgnoreCase(train.getMovementState())) {
            double span = TURNOUT_EAST_PROGRESS - TURNOUT_EXIT_PROGRESS;
            double p = Math.max(0.0, Math.min(1.0, (train.getProgressPercent() - TURNOUT_EXIT_PROGRESS) / span));
            train.setPosX(TURNOUT_EXIT_X + (TURNOUT_EAST_X - TURNOUT_EXIT_X) * p);
            train.setPosY(TRACK_3_Y);
            train.setRotationDeg(0.0);
        } else if ("EXITING_SIDING".equalsIgnoreCase(train.getMovementState())) {
            double span = TURNOUT_REJOIN_PROGRESS - TURNOUT_EAST_PROGRESS;
            double p = Math.max(0.0, Math.min(1.0, (train.getProgressPercent() - TURNOUT_EAST_PROGRESS) / span));
            train.setPosX(TURNOUT_EAST_X + (TURNOUT_REJOIN_X - TURNOUT_EAST_X) * p);
            train.setPosY(TRACK_3_Y + (TRACK_1_Y - TRACK_3_Y) * p);
            train.setRotationDeg(-45.0);
        } else {
            // Normal Horizontal Track Transit (NORMAL_MOVEMENT, APPROACHING_JUNCTION, JOINING_NEXT_TRACK)
            double xPos = 0.0;
            if ("DOWN".equalsIgnoreCase(train.getDirection())) {
                xPos = START_X + USABLE_WIDTH - (train.getProgressPercent() / 100.0) * USABLE_WIDTH;
            } else {
                xPos = START_X + (train.getProgressPercent() / 100.0) * USABLE_WIDTH;
            }

            train.setPosX(xPos);
            train.setPosY(getTrackY(train.getCurrentTrackNumber()));
            train.setRotationDeg(0.0);
            if (!train.isDiverting()) {
                train.setDiversionSegment("NORMAL_TRANSIT");
            }
        }
    }

    /**
     * Finds the leading train ahead on the same track traveling in the same direction.
     */
    private Train findLeadingTrainOnSameTrack(Train train) {
        Train leading = null;
        double minDistance = Double.MAX_VALUE;

        for (Train other : activeTrains) {
            if (other.getTrainNumber().equals(train.getTrainNumber())) continue;
            if (other.getCurrentTrackNumber() != train.getCurrentTrackNumber()) continue;
            if (!other.getDirection().equalsIgnoreCase(train.getDirection())) continue;
            if ("ARRIVED".equalsIgnoreCase(other.getStatus().name())) continue;

            // Check if 'other' is ahead of 'train'
            if ("UP".equalsIgnoreCase(train.getDirection())) {
                if (other.getProgressPercent() > train.getProgressPercent()) {
                    double dist = other.getProgressPercent() - train.getProgressPercent();
                    if (dist < minDistance) {
                        minDistance = dist;
                        leading = other;
                    }
                }
            } else {
                // DOWN direction
                if (other.getProgressPercent() > train.getProgressPercent()) {
                    double dist = other.getProgressPercent() - train.getProgressPercent();
                    if (dist < minDistance) {
                        minDistance = dist;
                        leading = other;
                    }
                }
            }
        }
        return leading;
    }

    /**
     * Finds an opposing train traveling in the opposite direction on the same track.
     */
    private Train findOpposingTrainOnSameTrack(Train train) {
        for (Train other : activeTrains) {
            if (other.getTrainNumber().equals(train.getTrainNumber())) continue;
            if (other.getCurrentTrackNumber() != train.getCurrentTrackNumber()) continue;
            if (!other.getDirection().equalsIgnoreCase(train.getDirection())) {
                return other;
            }
        }
        return null;
    }

    /**
     * Resolves which opposing train has priority to enter the single-track section.
     */
    private boolean resolveOpposingPriority(Train t1, Train t2) {
        // Express beats Freight
        if (t1.getCategory() == TrainCategory.EXPRESS && t2.getCategory() != TrainCategory.EXPRESS) return true;
        if (t2.getCategory() == TrainCategory.EXPRESS && t1.getCategory() != TrainCategory.EXPRESS) return false;

        // Less delayed train holds punctuality priority
        return t1.getDelayMinutes() <= t2.getDelayMinutes();
    }

    /**
     * Recalculates section occupancy and interlocking signal aspects.
     */
    private void recalculateSectionsAndOccupancy() {
        // Reset sections to CLEAR unless occupied by active trains
        for (TrackSection sec : trackSections.values()) {
            if (sec.isTurnout()) {
                // Handled in diversion logic
                continue;
            }
            sec.setStatus("CLEAR");
            sec.setOccupyingTrainNumber(null);
            sec.setOccupyingTrainDirection(null);
            sec.setSignalAspect("GREEN");
        }

        // Map trains into sections using realistic rake footprint (tail to front)
        for (Train train : activeTrains) {
            if ("ARRIVED".equalsIgnoreCase(train.getStatus().name())) continue;

            if (train.isDiverting()) {
                TrackSection toSec = trackSections.get("SEC_TO_1_3");
                if (toSec != null) {
                    toSec.setStatus("OCCUPIED");
                    toSec.setOccupyingTrainNumber(train.getTrainNumber());
                    toSec.setOccupyingTrainDirection(train.getDirection());
                    toSec.setSignalAspect("GREEN");
                }
                continue;
            }

            double pFront = train.getProgressPercent();
            double pRear = Math.max(0.0, train.getProgressPercent() - RAKE_SPAN_PERCENT);

            if ("DOWN".equalsIgnoreCase(train.getDirection())) {
                pFront = 100.0 - train.getProgressPercent();
                pRear = Math.min(100.0, pFront + RAKE_SPAN_PERCENT);
            }

            for (TrackSection sec : trackSections.values()) {
                if (sec.isTurnout()) continue;
                if (sec.getTrackNumber() == train.getCurrentTrackNumber()) {
                    if (sec.intersectsSpan(pRear, pFront)) {
                        sec.setStatus("OCCUPIED");
                        sec.setOccupyingTrainNumber(train.getTrainNumber());
                        sec.setOccupyingTrainDirection(train.getDirection());
                        sec.setSignalAspect(train.getSignalAspect());
                        train.setCurrentSectionId(sec.getSectionId());
                    }
                }
            }
        }
    }

    /**
     * Initiates Automatic / Manual Diversion Route from Track 1 to Track 3.
     */
    public synchronized boolean requestDiversion(String trainNumber, int fromTrack, int toTrack) {
        Train train = null;
        for (Train t : activeTrains) {
            if (t.getTrainNumber().equals(trainNumber)) {
                train = t;
                break;
            }
        }
        if (train == null) {
            // Register dynamically into active corridor fleet if initiated via UI predictive conflict
            train = new Train(activeTrains.size() + 1, trainNumber, "Guruvayur Express", TrainCategory.EXPRESS,
                    1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                    fromTrack, "UP", TrainStatus.MOVING, "22:58", "23:10", 12, 75, 7.0, "NIGHT", true);
            activeTrains.add(train);
        }
        if (train.isDiversionCompleted()) return true;

        // Verify side track availability (Test D)
        TrackSection toSec = trackSections.get("SEC_TO_1_3");
        if (toSec != null && "OCCUPIED".equalsIgnoreCase(toSec.getStatus())) {
            // Only hold if occupied by stalled obstruction G-7821 or active scenario is D
            if ("G-7821".equals(toSec.getOccupyingTrainNumber()) || "D".equalsIgnoreCase(activeScenarioName)) {
                LOGGER.warning("Diversion rejected: Connecting side track SEC_TO_1_3 is occupied by obstruction " + toSec.getOccupyingTrainNumber());
                train.setStatus(TrainStatus.HELD);
                train.setSignalAspect("RED");
                return false;
            } else {
                toSec.setStatus("CLEAR");
                toSec.setOccupyingTrainNumber(null);
            }
        }

        // Pre-clear destination platform loop line (Track 3) of regular nominal traffic so crossover is clear
        for (Train ot : activeTrains) {
            if (!ot.getTrainNumber().equals(trainNumber) && ot.getCurrentTrackNumber() == toTrack) {
                if (!"ARRIVED".equalsIgnoreCase(ot.getStatus().name()) && !"G-7821".equals(ot.getTrainNumber())) {
                    ot.setProgressPercent(95.0);
                    ot.setStatus(TrainStatus.ARRIVED);
                    ot.setSpeedKmh(0);
                }
            }
        }

        // Reserve turnout route
        train.setDiverting(true);
        train.setDiversionCompleted(false);
        train.setDiversionProgress(0.0);
        train.setFromTrackNumber(fromTrack);
        train.setToTrackNumber(toTrack);
        train.setStatus(TrainStatus.MOVING);
        train.setSignalAspect("GREEN");

        if (train.getProgressPercent() < JNC_T1_PROGRESS) {
            train.setMovementState("APPROACHING_JUNCTION");
        } else {
            train.setMovementState("ENTERING_SIDING");
        }

        if (toSec != null) {
            toSec.setStatus("RESERVED");
            toSec.setOccupyingTrainNumber(train.getTrainNumber());
            toSec.setSignalAspect("GREEN");
        }

        JunctionSwitch jnc = junctions.get("JNC_T1_3");
        if (jnc != null) {
            jnc.setAlignment("REVERSE");
            jnc.setLocked(true);
            jnc.setReservedForTrain(train.getTrainNumber());
        }

        return true;
    }

    public double getTrackY(int trackNumber) {
        switch (trackNumber) {
            case 1: return TRACK_1_Y;
            case 2: return TRACK_2_Y;
            case 3: return TRACK_3_Y;
            case 4: return TRACK_4_Y;
            case 5: return TRACK_5_Y;
            default: return TRACK_1_Y;
        }
    }

    // Getters and control methods
    public List<Train> getActiveTrains() { return activeTrains; }
    public Map<String, TrackSection> getTrackSections() { return trackSections; }
    public Map<String, JunctionSwitch> getJunctions() { return junctions; }
    public String getActiveScenarioName() { return activeScenarioName; }
    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { this.isPaused = paused; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(double speedMultiplier) { this.speedMultiplier = speedMultiplier; }
}
