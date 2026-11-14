package com.smartrail.service;

import com.smartrail.ai.ConflictDetector;
import com.smartrail.ai.TrainDispatcher;
import com.smartrail.ai.TrainPriorityManager;
import com.smartrail.dao.DivisionDAO;
import com.smartrail.dao.StationDAO;
import com.smartrail.dao.TrackDAO;
import com.smartrail.dao.TrainDAO;
import com.smartrail.model.AlertItem;
import com.smartrail.model.Conflict;
import com.smartrail.model.DispatchMode;
import com.smartrail.model.DiversionRecommendation;
import com.smartrail.model.Division;
import com.smartrail.model.Station;
import com.smartrail.model.Track;
import com.smartrail.model.Train;
import com.smartrail.model.TrainStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * High-level Railway Traffic Management Service.
 * Coordinates train movement, track occupancy, telemetry stats, conflict detection,
 * priority management, and automatic/manual track diversions.
 */
public class RailwayService {
    private final DivisionDAO divisionDAO;
    private final StationDAO stationDAO;
    private final TrackDAO trackDAO;
    private final TrainDAO trainDAO;
    private final com.smartrail.dao.TrackConflictDAO trackConflictDAO;
    private final com.smartrail.dao.DiversionHistoryDAO diversionHistoryDAO;
    private final com.smartrail.dao.AlertNotificationDAO alertNotificationDAO;
    private final com.smartrail.dao.TrainMovementDAO trainMovementDAO;
    private final com.smartrail.dao.TrainDelayDAO trainDelayDAO;

    private final ConflictDetector conflictDetector;
    private final TrainPriorityManager priorityManager;
    private final TrainDispatcher trainDispatcher;

    private static final InterlockingSimulationEngine INTERLOCKING_ENGINE = new InterlockingSimulationEngine();

    public InterlockingSimulationEngine getInterlockingEngine() {
        return INTERLOCKING_ENGINE;
    }

    private DispatchMode dispatchMode = DispatchMode.MANUAL;
    private final Map<String, Conflict> activeConflicts = new ConcurrentHashMap<>();
    private final List<AlertItem> alertHistory = new CopyOnWriteArrayList<>();

    public RailwayService() {
        this.divisionDAO = new DivisionDAO();
        this.stationDAO = new StationDAO();
        this.trackDAO = new TrackDAO();
        this.trainDAO = new TrainDAO();
        this.trackConflictDAO = new com.smartrail.dao.TrackConflictDAO();
        this.diversionHistoryDAO = new com.smartrail.dao.DiversionHistoryDAO();
        this.alertNotificationDAO = new com.smartrail.dao.AlertNotificationDAO();
        this.trainMovementDAO = new com.smartrail.dao.TrainMovementDAO();
        this.trainDelayDAO = new com.smartrail.dao.TrainDelayDAO();

        this.conflictDetector = new ConflictDetector();
        this.priorityManager = new TrainPriorityManager();
        this.trainDispatcher = new TrainDispatcher(trainDAO, trackDAO);

        seedInitialAlerts();
    }

    private void seedInitialAlerts() {
        String now = new SimpleDateFormat("HH:mm:ss").format(new Date());
        alertHistory.add(new AlertItem("ALT-101", "TRAIN_APPROACHING", "Train Approaching Station",
                "Train #20607 (Vande Bharat Express) approaching Puratchi Thalaivar Dr. MGR Chennai Central (MAS). Track 1 signals clear.",
                "BLUE", now, "20607", 1, "MAS"));
        alertHistory.add(new AlertItem("ALT-102", "TRAIN_DELAYED", "Schedule Deviation Alert",
                "Train #12675 (Kovai Superfast Express) delayed by +14 min due to western line block. Priority score recalibrated.",
                "AMBER", now, "12675", 3, "SA"));
    }

    public List<Division> getDivisions() {
        return divisionDAO.getAllDivisions();
    }

    public List<Station> getStationsForDivision(String divisionCode) {
        if (divisionCode == null || divisionCode.trim().isEmpty() || "ALL".equalsIgnoreCase(divisionCode)) {
            return stationDAO.getAllStations();
        }
        return stationDAO.getStationsByDivisionCode(divisionCode);
    }

    public Station getStation(int id) {
        return stationDAO.getStationById(id);
    }

    public Station getStationByCode(String code) {
        return stationDAO.getStationByCode(code);
    }

    public List<Track> getTracksForStation(int stationId) {
        return trackDAO.getTracksByStationId(stationId);
    }

    public List<Train> getFilteredTrains(String divisionCode, Integer stationId, String timeSlot, String category) {
        return trainDAO.getTrainsByFilter(divisionCode, stationId, timeSlot, category);
    }

    public List<Train> getAllTrains() {
        return trainDAO.getAllTrains();
    }

    public Train getTrain(String trainNumber) {
        return trainDAO.getTrainByNumber(trainNumber);
    }

    public boolean updateTrainProgress(String trainNumber, double progress, String status, int trackNumber) {
        TrainStatus trainStatus = TrainStatus.fromString(status);
        if (progress >= 100.0) {
            trainStatus = TrainStatus.ARRIVED;
            progress = 100.0;
        }
        return trainDAO.updateTrainProgress(trainNumber, progress, trainStatus.name(), trackNumber);
    }

    public boolean addSimulatedTrain(Train train) {
        return trainDAO.addSimulatedTrain(train);
    }

    public DispatchMode getDispatchMode() {
        return dispatchMode;
    }

    public void setDispatchMode(DispatchMode mode) {
        this.dispatchMode = mode;
        String now = new SimpleDateFormat("HH:mm:ss").format(new Date());
        addAlert(new AlertItem("ALT-" + System.currentTimeMillis() % 10000, "MANUAL_OVERRIDE",
                "Dispatch Mode Switch",
                "Railway Controller switched dispatch operating mode to " + mode.getLabel() + ".",
                mode == DispatchMode.AUTOMATIC ? "TEAL" : "AMBER", now, null, 0, null));
    }

    public List<AlertItem> getAlertHistory() {
        return new ArrayList<>(alertHistory);
    }

    public void addAlert(AlertItem alert) {
        alertHistory.add(0, alert);
        // Keep alert feed clean and fast (max 40 alerts)
        if (alertHistory.size() > 40) {
            alertHistory.remove(alertHistory.size() - 1);
        }
    }

    /**
     * AI Conflict Detection & Auto/Manual Resolution Planning.
     */
    public List<Conflict> scanAndPlanConflicts(String divisionCode, int stationId) {
        Station station = stationDAO.getStationById(stationId);
        String stCode = station != null ? station.getCode() : "MAS";
        List<Track> tracks = trackDAO.getTracksByStationId(stationId);
        List<Train> trains = trainDAO.getTrainsByFilter(divisionCode, stationId, null, null);

        List<Conflict> detected = conflictDetector.detectConflicts(trains, tracks, stationId, stCode);

        for (Conflict c : detected) {
            DiversionRecommendation rec = trainDispatcher.planDiversion(c, tracks, trains, dispatchMode);
            c.setRecommendation(rec);

            // Cache conflict
            activeConflicts.put(c.getConflictId(), c);

            // Log dynamic alert if not already logged
            String now = new SimpleDateFormat("HH:mm:ss").format(new Date());
            if (!hasRecentAlertForConflict(c.getConflictId())) {
                String severity = "RED";
                String alertType = rec.isAlternativeAvailable() ? "CONFLICT_DETECTED" : "ALTERNATIVE_UNAVAILABLE";
                String title = rec.isAlternativeAvailable() ? "TRACK CONFLICT DETECTED!" : "CRITICAL: NO ALTERNATIVE TRACK AVAILABLE!";
                String alertCode = "ALT-CONF-" + c.getConflictId();

                addAlert(new AlertItem(
                        alertCode,
                        alertType,
                        title,
                        "Train #" + c.getTrain1().getTrainNumber() + " and Train #" + c.getTrain2().getTrainNumber() +
                                " approaching Track " + c.getTrackNumber() + ". " + rec.getDecisionSummary(),
                        severity,
                        now,
                        c.getTrain2().getTrainNumber(),
                        c.getTrackNumber(),
                        stCode
                ));

                // Persist to track_conflicts table and alert_notifications table
                trackConflictDAO.recordConflict(
                        "CONF-" + c.getConflictId(),
                        stCode,
                        c.getTrackNumber(),
                        c.getTrain1().getTrainNumber(),
                        c.getTrain2().getTrainNumber(),
                        c.getConflictType(),
                        severity,
                        rec.getRetainingTrainNumber(),
                        rec.getReason()
                );

                alertNotificationDAO.insertAlert(
                        alertCode,
                        c.getTrain2().getTrainNumber(),
                        c.getConflictType(),
                        rec.isAlternativeAvailable() ? "Divert to " + rec.getToTrackName() : "Hold at outer signal",
                        dispatchMode == DispatchMode.AUTOMATIC ? "AUTOMATICALLY_DIVERTED" : "PENDING_APPROVAL",
                        severity,
                        stCode,
                        c.getTrackNumber(),
                        rec.getReason()
                );

                // CRITICAL RULE: Conflict detection does NOT automatically execute diversion.
                // Generating an AI recommendation must NOT change the train's route or activate turnouts.
                // The system creates a pending alert and awaits explicit controller confirmation.
                String pendingAlertCode = "ALT-PRED-" + System.currentTimeMillis() % 10000;
                addAlert(new AlertItem(
                        pendingAlertCode,
                        "CONFLICT_PREDICTED",
                        "Potential Track Contention Detected",
                        "AI detected converging contention on Track " + c.getTrackNumber() +
                                ". Recommended diversion of Train #" + rec.getDivertedTrainNumber() +
                                " to Track " + rec.getToTrackNumber() + " (" + rec.getToTrackName() + "). Awaiting controller confirmation.",
                        "AMBER",
                        now,
                        rec.getDivertedTrainNumber(),
                        c.getTrackNumber(),
                        stCode
                ));

                alertNotificationDAO.insertAlert(
                        pendingAlertCode,
                        rec.getDivertedTrainNumber(),
                        c.getConflictType(),
                        "Pending Diversion to Track " + rec.getToTrackNumber(),
                        "AWAITING_CONTROLLER_CONFIRMATION",
                        "AMBER",
                        stCode,
                        c.getTrackNumber(),
                        rec.getReason()
                );
            }
        }

        return detected;
    }

    private boolean hasRecentAlertForConflict(String conflictId) {
        for (AlertItem a : alertHistory) {
            if (a.getId() != null && a.getId().contains(conflictId)) {
                return true;
            }
        }
        return false;
    }

    public List<Conflict> getActiveConflicts() {
        return new ArrayList<>(activeConflicts.values());
    }

    /**
     * Controller Approval for Recommended Diversion.
     */
    public boolean approveDiversion(String conflictId, int stationId) {
        Conflict conflict = activeConflicts.get(conflictId);
        if (conflict == null || conflict.getRecommendation() == null) {
            return false;
        }

        DiversionRecommendation rec = conflict.getRecommendation();
        boolean success = trainDispatcher.applyDiversion(rec, stationId);
        if (success) {
            INTERLOCKING_ENGINE.requestDiversion(rec.getDivertedTrainNumber(), rec.getFromTrackNumber(), rec.getToTrackNumber());
        }

        if (success) {
            conflict.setStatus("RESOLVED");
            String now = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String alertCode = "ALT-APP-" + System.currentTimeMillis() % 10000;
            addAlert(new AlertItem(
                    alertCode,
                    "TRAIN_DIVERTED",
                    "Controller Approved Diversion",
                    "Railway Controller approved diversion: Train #" + rec.getDivertedTrainNumber() +
                            " diverted to " + rec.getToTrackName() + ". Line contention resolved.",
                    "TEAL",
                    now,
                    rec.getDivertedTrainNumber(),
                    rec.getToTrackNumber(),
                    conflict.getStationCode()
            ));

            diversionHistoryDAO.recordDiversion(
                    "CONF-" + conflictId,
                    rec.getDivertedTrainNumber(),
                    rec.getFromTrackNumber(),
                    rec.getToTrackNumber(),
                    conflict.getStationCode(),
                    "CONTROLLER_DIVERSION",
                    true,
                    rec.getReason()
            );

            alertNotificationDAO.insertAlert(
                    alertCode,
                    rec.getDivertedTrainNumber(),
                    conflict.getConflictType(),
                    "Controller Approved Diversion to Track " + rec.getToTrackNumber(),
                    "CONTROLLER_APPROVED",
                    "TEAL",
                    conflict.getStationCode(),
                    rec.getToTrackNumber(),
                    rec.getReason()
            );

            activeConflicts.remove(conflictId);
        }

        return success;
    }

    /**
     * Controller Rejection / Hold train at signal.
     */
    public boolean holdAtSignal(String conflictId) {
        Conflict conflict = activeConflicts.get(conflictId);
        if (conflict == null) return false;

        conflict.setStatus("HELD");
        if (conflict.getRecommendation() != null) {
            String diverted = conflict.getRecommendation().getDivertedTrainNumber();
            trainDAO.updateTrainProgress(diverted, 10.0, TrainStatus.CONFLICT.name(), conflict.getTrackNumber());
        }

        String now = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String alertCode = "ALT-HLD-" + System.currentTimeMillis() % 10000;
        String divertedTrn = (conflict.getRecommendation() != null ? conflict.getRecommendation().getDivertedTrainNumber() : "");
        addAlert(new AlertItem(
                alertCode,
                "ALTERNATIVE_UNAVAILABLE",
                "Train Held at Outer Signal",
                "Railway Controller ordered Train #" + divertedTrn +
                        " held at Outer Home Signal. Track entry inhibited.",
                "AMBER",
                now,
                divertedTrn,
                conflict.getTrackNumber(),
                conflict.getStationCode()
        ));

        diversionHistoryDAO.recordDiversion(
                "CONF-" + conflictId,
                divertedTrn,
                conflict.getTrackNumber(),
                0, // 0 = held at outer home
                conflict.getStationCode(),
                "SIGNAL_HOLD",
                true,
                "Controller held train at outer signal; track entry inhibited."
        );

        alertNotificationDAO.insertAlert(
                alertCode,
                divertedTrn,
                conflict.getConflictType(),
                "Hold at outer home signal",
                "HELD_AT_SIGNAL",
                "AMBER",
                conflict.getStationCode(),
                conflict.getTrackNumber(),
                "Held at signal to prevent platform track gridlock"
        );

        return true;
    }

    /**
     * Simulates an interactive track conflict between two trains on a specified line.
     */
    public Conflict simulateConflict(String divisionCode, int stationId, int trackNumber) {
        List<Train> trains = trainDAO.getTrainsByFilter(divisionCode, stationId, null, null);
        if (trains.size() < 2) {
            trains = trainDAO.getAllTrains();
        }
        if (trains.size() < 2) {
            return null;
        }

        Train t1 = trains.get(0);
        Train t2 = trains.get(1);

        // Put both trains on the same track with converging progress
        t1.setCurrentTrackNumber(trackNumber);
        t1.setProgressPercent(28.0);
        t1.setStatus(TrainStatus.CONFLICT);
        t1.setDirection("UP");

        t2.setCurrentTrackNumber(trackNumber);
        t2.setProgressPercent(36.0);
        t2.setStatus(TrainStatus.CONFLICT);
        t2.setDirection("DOWN"); // Opposing head-on risk!

        trainDAO.updateTrainProgress(t1.getTrainNumber(), 28.0, TrainStatus.CONFLICT.name(), trackNumber);
        trainDAO.updateTrainProgress(t2.getTrainNumber(), 36.0, TrainStatus.CONFLICT.name(), trackNumber);

        // Scan and plan immediately
        List<Conflict> conflicts = scanAndPlanConflicts(divisionCode, stationId);
        return conflicts.isEmpty() ? null : conflicts.get(0);
    }

    /**
     * Clears simulated conflicts and restores nominal schedule.
     */
    public boolean clearSimulatedConflicts(String divisionCode, int stationId) {
        activeConflicts.clear();
        List<Train> trains = trainDAO.getTrainsByFilter(divisionCode, stationId, null, null);
        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            int safeTrack = (i % 4) + 1;
            trainDAO.updateTrainProgress(t.getTrainNumber(), 12.0 + (i * 8.0), TrainStatus.MOVING.name(), safeTrack);
        }

        String now = new SimpleDateFormat("HH:mm:ss").format(new Date());
        addAlert(new AlertItem(
                "ALT-CLR-" + System.currentTimeMillis() % 10000,
                "TRAIN_DIVERTED",
                "Track Contention Cleared",
                "All lines restored to nominal dispatch. Interlocking relays clear.",
                "GREEN",
                now,
                null,
                1,
                null
        ));
        return true;
    }

    public Map<String, Object> getTelemetryOverview(String divisionCode, Integer stationId) {
        List<Train> trains = getFilteredTrains(divisionCode, stationId, null, null);
        int total = trains.size();
        int onTime = 0;
        int delayed = 0;
        int conflicts = activeConflicts.size();
        int moving = 0;
        int arrived = 0;
        int totalDelayMinutes = 0;

        for (Train t : trains) {
            if (t.getStatus() == TrainStatus.ON_TIME) onTime++;
            else if (t.getStatus() == TrainStatus.DELAYED) delayed++;
            else if (t.getStatus() == TrainStatus.CONFLICT) conflicts++;
            else if (t.getStatus() == TrainStatus.MOVING) moving++;
            else if (t.getStatus() == TrainStatus.ARRIVED) arrived++;

            totalDelayMinutes += t.getDelayMinutes();
        }

        double avgDelay = total > 0 ? (double) totalDelayMinutes / total : 0.0;
        double punctualityRate = total > 0 ? ((double) (total - delayed - conflicts) / total) * 100.0 : 100.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrains", total);
        stats.put("onTimeCount", onTime);
        stats.put("delayedCount", delayed);
        stats.put("conflictCount", conflicts);
        stats.put("movingCount", moving);
        stats.put("arrivedCount", arrived);
        stats.put("avgDelayMinutes", Math.round(avgDelay * 10.0) / 10.0);
        stats.put("punctualityPercent", Math.round(punctualityRate * 10.0) / 10.0);
        stats.put("activeLineBlock", conflicts > 0 ? "CAUTION - TRACK CONTENTION ACTIVE" : "CLEAR - ALL SIGNALS NORMAL");
        stats.put("interlockingMode", "Solid State Interlocking (SSI) - Active");
        stats.put("dispatchMode", dispatchMode.name());
        stats.put("dispatchModeLabel", dispatchMode.getLabel());
        return stats;
    }
}
