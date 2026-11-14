package com.smartrail.ai;

import com.smartrail.dao.TrackDAO;
import com.smartrail.dao.TrainDAO;
import com.smartrail.model.Conflict;
import com.smartrail.model.DispatchMode;
import com.smartrail.model.DiversionRecommendation;
import com.smartrail.model.Track;
import com.smartrail.model.Train;
import com.smartrail.model.TrainCategory;
import com.smartrail.model.TrainStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Intelligent Train Dispatcher.
 * Automatically resolves or recommends track diversion solutions for detected conflicts
 * according to safety interlocking principles, train priority, and track geometry.
 */
public class TrainDispatcher {
    private static final Logger LOGGER = Logger.getLogger(TrainDispatcher.class.getName());

    private final TrainPriorityManager priorityManager;
    private final TrainDAO trainDAO;
    private final TrackDAO trackDAO;

    public TrainDispatcher() {
        this.priorityManager = new TrainPriorityManager();
        this.trainDAO = new TrainDAO();
        this.trackDAO = new TrackDAO();
    }

    public TrainDispatcher(TrainDAO trainDAO, TrackDAO trackDAO) {
        this.priorityManager = new TrainPriorityManager();
        this.trainDAO = trainDAO;
        this.trackDAO = trackDAO;
    }

    /**
     * Resolves a conflict by computing priority scores, finding an optimal alternative line,
     * and building an actionable diversion recommendation.
     */
    public DiversionRecommendation planDiversion(Conflict conflict, List<Track> tracks, List<Train> allTrains, DispatchMode mode) {
        Train t1 = conflict.getTrain1();
        Train t2 = conflict.getTrain2();
        Track currentTrack = findTrack(tracks, conflict.getTrackNumber());

        // 1. Evaluate priority
        TrainPriorityManager.PriorityEvaluation eval1 = priorityManager.evaluate(t1, currentTrack);
        TrainPriorityManager.PriorityEvaluation eval2 = priorityManager.evaluate(t2, currentTrack);

        Train retainingTrain;
        Train divertedTrain;
        TrainPriorityManager.PriorityEvaluation winnerEval;
        TrainPriorityManager.PriorityEvaluation loserEval;

        if (eval1.getTotalScore() >= eval2.getTotalScore()) {
            retainingTrain = t1;
            divertedTrain = t2;
            winnerEval = eval1;
            loserEval = eval2;
        } else {
            retainingTrain = t2;
            divertedTrain = t1;
            winnerEval = eval2;
            loserEval = eval1;
        }

        // 2. Identify occupied tracks at station
        Set<Integer> occupiedTrackNumbers = new HashSet<>();
        if (allTrains != null) {
            for (Train t : allTrains) {
                if (!"ARRIVED".equalsIgnoreCase(t.getStatus().name())) {
                    occupiedTrackNumbers.add(t.getCurrentTrackNumber());
                }
            }
        }

        // 3. Select safest alternative track for diverted train
        Track alternativeTrack = findSafeAlternativeTrack(divertedTrain, tracks, occupiedTrackNumbers, conflict.getTrackNumber());

        DiversionRecommendation rec = new DiversionRecommendation();
        rec.setConflictId(conflict.getConflictId());
        rec.setRetainingTrainNumber(retainingTrain.getTrainNumber());
        rec.setRetainingTrainName(retainingTrain.getTrainName());
        rec.setDivertedTrainNumber(divertedTrain.getTrainNumber());
        rec.setDivertedTrainName(divertedTrain.getTrainName());
        rec.setFromTrackNumber(conflict.getTrackNumber());
        rec.setPriorityScoreRetaining(winnerEval.getTotalScore());
        rec.setPriorityScoreDiverted(loserEval.getTotalScore());
        rec.setDispatchMode(mode);

        List<String> factors = new ArrayList<>();
        factors.addAll(winnerEval.getExplanationFactors());
        rec.setOperationalFactors(factors);

        if (alternativeTrack != null) {
            rec.setAlternativeAvailable(true);
            rec.setToTrackNumber(alternativeTrack.getTrackNumber());
            rec.setToTrackName(alternativeTrack.getTrackName());
            rec.setRecommendedAction("DIVERT_TO_ALTERNATIVE_TRACK");

            rec.setDecisionSummary("Train #" + retainingTrain.getTrainNumber() + " retains " + conflict.getTrackName() +
                    ". Train #" + divertedTrain.getTrainNumber() + " is recommended for diversion to " + alternativeTrack.getTrackName() + ".");

            rec.setReason("Priority score of Train #" + retainingTrain.getTrainNumber() + " (" + winnerEval.getTotalScore() +
                    ") exceeds Train #" + divertedTrain.getTrainNumber() + " (" + loserEval.getTotalScore() + "). " +
                    "Evaluated factors: " + String.join("; ", winnerEval.getExplanationFactors()));
        } else {
            // No safe alternative track available!
            rec.setAlternativeAvailable(false);
            rec.setToTrackNumber(-1);
            rec.setToTrackName("None (All Lines Occupied)");
            rec.setRecommendedAction("HOLD_AT_OUTER_SIGNAL");

            rec.setDecisionSummary("NO ALTERNATIVE TRACK AVAILABLE: All platform loops and siding tracks are currently occupied!");
            rec.setReason("CRITICAL WARNING: Train #" + divertedTrain.getTrainNumber() +
                    " must be held at Outer Approach Signal. Automatic diversion aborted for safety protocol.");
        }

        // If Automatic Dispatch mode, apply immediately if alternative is available
        if (mode == DispatchMode.AUTOMATIC && rec.isAlternativeAvailable()) {
            applyDiversion(rec, conflict.getStationId());
            rec.setApproved(true);
        }

        return rec;
    }

    /**
     * Executes the diversion recommendation: updates the train's assigned track and resets conflict status.
     */
    public boolean applyDiversion(DiversionRecommendation rec, int stationId) {
        if (!rec.isAlternativeAvailable() || rec.getToTrackNumber() <= 0) {
            LOGGER.warning("Cannot apply diversion: Alternative track unavailable.");
            return false;
        }

        LOGGER.info("Executing AI Track Diversion: Train #" + rec.getDivertedTrainNumber() +
                " from Track " + rec.getFromTrackNumber() + " -> Track " + rec.getToTrackNumber());

        // Update train track in DAO / memory
        boolean trainUpdated = trainDAO.updateTrainProgress(rec.getDivertedTrainNumber(), 15.0, TrainStatus.MOVING.name(), rec.getToTrackNumber());

        // Update track statuses
        trackDAO.updateTrackStatus(stationId, rec.getToTrackNumber(), "OCCUPIED", rec.getDivertedTrainNumber());
        trackDAO.updateTrackStatus(stationId, rec.getFromTrackNumber(), "OCCUPIED", rec.getRetainingTrainNumber());

        rec.setApproved(true);
        return trainUpdated;
    }

    /**
     * Finds an available, clear track compatible with the train's classification.
     */
    private Track findSafeAlternativeTrack(Train train, List<Track> tracks, Set<Integer> occupiedTracks, int currentTrackNumber) {
        if (tracks == null) return null;

        TrainCategory cat = train.getCategory() != null ? train.getCategory() : TrainCategory.EXPRESS;

        // Categorized track preferences:
        // Passenger / Express -> Preference: Platform Loop 1 (3), Platform Loop 2 (4), Down Main (2)
        // Goods / Freight -> Preference: Freight Siding (5), Platform Loop 2 (4), Platform Loop 1 (3)
        int[] preferredTracks;
        if (cat == TrainCategory.FREIGHT || cat == TrainCategory.GOODS) {
            preferredTracks = new int[]{5, 4, 3};
        } else {
            preferredTracks = new int[]{3, 4, 2, 1};
        }

        for (int trackNum : preferredTracks) {
            if (trackNum == currentTrackNumber) {
                continue;
            }
            if (occupiedTracks.contains(trackNum)) {
                continue;
            }

            Track track = findTrack(tracks, trackNum);
            if (track != null) {
                // Interlocking safety verification: track status must not be OCCUPIED or MAINTENANCE
                String status = track.getStatus() != null ? track.getStatus().toUpperCase() : "CLEAR";
                if (!"OCCUPIED".equals(status) && !"MAINTENANCE".equals(status)) {
                    return track;
                }
            }
        }

        return null;
    }

    private Track findTrack(List<Track> tracks, int trackNumber) {
        if (tracks == null) return null;
        for (Track t : tracks) {
            if (t.getTrackNumber() == trackNumber) {
                return t;
            }
        }
        return null;
    }
}

