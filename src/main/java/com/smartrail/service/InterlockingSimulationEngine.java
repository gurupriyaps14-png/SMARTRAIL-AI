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
    public static final double TURNOUT_EXIT_PROGRESS = ((TURNOUT_EXIT_X - START_X) / USABLE_WIDTH) * 100.0; // ~36.4%

    // Safe Separation Margins (in % progress)
    public static final double SAFE_STOP_MARGIN = 16.0;   // Minimum distance before absolute RED halt
    public static final double SAFE_CAUTION_MARGIN = 28.0;// Distance before AMBER caution slowdown

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

        // --- Connecting Side Track (Turnout Crossover 1 -> 3) ---
        TrackSection to_1_3 = new TrackSection("SEC_TO_1_3", 13, "Connecting Crossover Side Track (1->3)", 0.0, 100.0, 40, true);
        to_1_3.addConnectedSection("SEC_T1_2");
        to_1_3.addConnectedSection("SEC_T3_2");
        trackSections.put(to_1_3.getSectionId(), to_1_3);
    }

    /**
     * Initializes physical turnout switches.
     */
    private void initJunctions() {
        junctions.clear();
        JunctionSwitch sw1 = new JunctionSwitch(
                "JNC_T1_3",
                "Turnout Switch 1A (Track 1 to Track 3)",
                1, 3,
                "SEC_T1_3", "SEC_TO_1_3",
                JNC_T1_X, TRACK_1_Y
        );
        junctions.put(sw1.getJunctionId(), sw1);
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
                        1, "UP", TrainStatus.MOVING, "22:55", "23:00", 0, 90, 28.0, "NIGHT", true);

                Train downTrainB = new Train(2, "16127", "Guruvayur Express (DOWN)", TrainCategory.EXPRESS,
                        6, "Katpadi Jn", 1, "Chennai Central", 6, "Katpadi Jn", "Chennai Central",
                        1, "DOWN", TrainStatus.MOVING, "22:57", "23:05", 10, 85, 78.0, "NIGHT", true);

                activeTrains.add(upTrainB);
                activeTrains.add(downTrainB);
                break;

            case "C":
            case "SCENARIO_C":
                // TEST C: Automatic Diversion. Train on Track 1 diverts to Track 3 via connecting side track.
                Train retainingC = new Train(1, "12674", "Cheran Superfast Express", TrainCategory.EXPRESS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:55", "23:00", 2, 85, 10.0, "NIGHT", true);

                Train divertC = new Train(2, "16127", "Guruvayur Express (Approaching Switch)", TrainCategory.EXPRESS,
                        1, "Chennai Central", 6, "Katpadi Jn", 1, "Chennai Central", "Katpadi Jn",
                        1, "UP", TrainStatus.MOVING, "22:58", "23:10", 12, 75, 18.0, "NIGHT", true);

                activeTrains.add(retainingC);
                activeTrains.add(divertC);

                // Pre-clear Track 3 and initiate diversion route
                requestDiversion("16127", 1, 3);
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
                // Test B: Opposing trains on single line!
                // High priority train retains; lower priority train held at approach
                boolean hasPriority = resolveOpposingPriority(train, opposingTrain);
                if (!hasPriority) {
                    // Hold lower priority train at approach signal
                    if (train.getProgressPercent() <= 20.0 || train.getProgressPercent() >= 80.0) {
                        effectiveSpeed = 0.0;
                        train.setSignalAspect("RED");
                        train.setSpacingStatus("STANDSTILL_HELD");
                        train.setStatus(TrainStatus.HELD);
                    }
                }
            } else if (leadingTrain != null) {
                // Test A: Following train on same track!
                double separation = Math.abs(leadingTrain.getProgressPercent() - train.getProgressPercent());
                train.setSeparationDistancePercent(separation);
                train.setLeadTrainNumber(leadingTrain.getTrainNumber());

                if (separation < SAFE_STOP_MARGIN) {
                    // Absolute Danger: STOP behind leading train
                    effectiveSpeed = 0.0;
                    train.setSignalAspect("RED");
                    train.setSpacingStatus("STANDSTILL_HELD");
                } else if (separation < SAFE_CAUTION_MARGIN) {
                    // Caution: Slow to 25 km/h
                    effectiveSpeed = Math.min(25.0, train.getSpeedKmh() * 0.35);
                    train.setSignalAspect("AMBER");
                    train.setSpacingStatus("CAUTION_SLOW");
                } else {
                    // Clear ahead
                    train.setSignalAspect("GREEN");
                    train.setSpacingStatus("NOMINAL");
                }
            } else {
                train.setSignalAspect("GREEN");
                train.setSpacingStatus("NOMINAL");
            }

            // Step train progress
            if (effectiveSpeed > 0.0) {
                double ratePercentPerSec = (effectiveSpeed / 90.0) * 3.8;
                double newProgress = train.getProgressPercent() + ratePercentPerSec * deltaSec;

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
     * Prevents jumping! Moves through Track 1 -> Junction 1A -> Turnout Side Track -> Track 3.
     */
    private void handleDiversionKinematics(Train train, double deltaSec) {
        // Test D Check: Is the connecting side track available?
        TrackSection toSec = trackSections.get("SEC_TO_1_3");
        if (toSec != null && "OCCUPIED".equalsIgnoreCase(toSec.getStatus()) && !train.getTrainNumber().equals(toSec.getOccupyingTrainNumber())) {
            // Test D: Connecting side track is occupied! Hold train before switch point.
            if (train.getProgressPercent() >= (JNC_T1_PROGRESS - 2.0)) {
                train.setSpeedKmh(0);
                train.setSignalAspect("RED");
                train.setSpacingStatus("STANDSTILL_HELD");
                train.setStatus(TrainStatus.HELD);
                return;
            }
        }

        // Advance diversion progress: 100% takes ~12 seconds
        double diversionRate = (100.0 / 12.0) * deltaSec * speedMultiplier;
        double curDivProg = train.getDiversionProgress() + diversionRate;
        train.setDiversionProgress(Math.min(100.0, curDivProg));

        JunctionSwitch jnc = junctions.get("JNC_T1_3");
        if (jnc != null) {
            jnc.setAlignment("REVERSE");
            jnc.setLocked(true);
            jnc.setReservedForTrain(train.getTrainNumber());
        }

        if (toSec != null) {
            toSec.setStatus("OCCUPIED");
            toSec.setOccupyingTrainNumber(train.getTrainNumber());
            toSec.setSignalAspect("GREEN");
        }

        if (train.getDiversionProgress() >= 100.0) {
            // Diversion Completed: Train has fully transitioned onto Track 3
            train.setDiverting(false);
            train.setCurrentTrackNumber(3);
            train.setProgressPercent(TURNOUT_EXIT_PROGRESS + 4.0);
            train.setRotationDeg(0.0);
            train.setStatus(TrainStatus.MOVING);
            train.setSignalAspect("GREEN");

            if (toSec != null) {
                toSec.setStatus("CLEAR");
                toSec.setOccupyingTrainNumber(null);
            }
            if (jnc != null) {
                jnc.setAlignment("NORMAL");
                jnc.setLocked(false);
                jnc.setReservedForTrain(null);
            }
        }
    }

    /**
     * Computes pixel X, Y and rotation for a train based on its route, direction, and diversion status.
     */
    private void updateTrainCoordinates(Train train) {
        double trackY = getTrackY(train.getCurrentTrackNumber());

        if (train.isDiverting()) {
            // Physical Crossover Geometry:
            // Point A (Track 1 Switch): x = 260px, y = 90px
            // Point B (Track 3 Convergence): x = 475px, y = 305px
            // Angle: 45 degrees
            double pct = train.getDiversionProgress() / 100.0;

            double startX = JNC_T1_X;
            double startY = TRACK_1_Y;
            double endX = TURNOUT_EXIT_X;
            double endY = TRACK_3_Y;

            train.setPosX(startX + (endX - startX) * pct);
            train.setPosY(startY + (endY - startY) * pct);

            if (pct > 0.05 && pct < 0.95) {
                train.setRotationDeg(45.0); // Tilted along the diagonal turnout track!
                train.setDiversionSegment("CROSSOVER_TURNOUT");
            } else if (pct <= 0.05) {
                train.setRotationDeg(0.0);
                train.setDiversionSegment("APPROACH_TRACK_1");
            } else {
                train.setRotationDeg(0.0);
                train.setDiversionSegment("MERGE_TRACK_3");
            }
        } else {
            // Normal Horizontal Track Transit
            double xPos = 0.0;
            if ("DOWN".equalsIgnoreCase(train.getDirection())) {
                xPos = START_X + USABLE_WIDTH - (train.getProgressPercent() / 100.0) * USABLE_WIDTH;
            } else {
                xPos = START_X + (train.getProgressPercent() / 100.0) * USABLE_WIDTH;
            }

            train.setPosX(xPos);
            train.setPosY(trackY);
            train.setRotationDeg(0.0);
            train.setDiversionSegment("NORMAL_TRANSIT");
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

        // Map trains into sections
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

            for (TrackSection sec : trackSections.values()) {
                if (sec.isTurnout()) continue;
                if (sec.getTrackNumber() == train.getCurrentTrackNumber()) {
                    if (sec.containsProgress(train.getProgressPercent())) {
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
        if (train == null) return false;

        // Verify side track availability (Test D)
        TrackSection toSec = trackSections.get("SEC_TO_1_3");
        if (toSec != null && "OCCUPIED".equalsIgnoreCase(toSec.getStatus())) {
            LOGGER.warning("Diversion rejected: Connecting side track SEC_TO_1_3 is occupied!");
            train.setStatus(TrainStatus.HELD);
            train.setSignalAspect("RED");
            return false;
        }

        // Reserve turnout route
        train.setDiverting(true);
        train.setDiversionProgress(0.0);
        train.setFromTrackNumber(fromTrack);
        train.setToTrackNumber(toTrack);
        train.setStatus(TrainStatus.MOVING);
        train.setSignalAspect("GREEN");

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
