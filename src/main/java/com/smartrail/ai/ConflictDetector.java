package com.smartrail.ai;

import com.smartrail.model.Conflict;
import com.smartrail.model.Track;
import com.smartrail.model.Train;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Intelligent Conflict Detection Mechanism.
 * Continuously evaluates spatial positions, track assignments, travel directions,
 * and overlapping arrival windows across the Tamil Nadu railway network.
 */
public class ConflictDetector {

    /**
     * Scans a list of trains and station tracks to identify any track occupancy contention.
     */
    public List<Conflict> detectConflicts(List<Train> trains, List<Track> tracks, int stationId, String stationCode) {
        List<Conflict> conflicts = new ArrayList<>();
        if (trains == null || trains.size() < 2) {
            return conflicts;
        }

        // Compare every pair of trains in the section
        for (int i = 0; i < trains.size(); i++) {
            Train t1 = trains.get(i);
            // Skip trains that have already terminated/arrived
            if ("ARRIVED".equalsIgnoreCase(t1.getStatus().name())) {
                continue;
            }

            for (int j = i + 1; j < trains.size(); j++) {
                Train t2 = trains.get(j);
                if ("ARRIVED".equalsIgnoreCase(t2.getStatus().name())) {
                    continue;
                }

                // Check 1: Same Track Assignment
                if (t1.getCurrentTrackNumber() == t2.getCurrentTrackNumber()) {
                    Conflict conflict = evaluateTrackContention(t1, t2, tracks, stationId, stationCode);
                    if (conflict != null) {
                        conflicts.add(conflict);
                    }
                }
            }
        }

        return conflicts;
    }

    /**
     * Evaluates spatial and temporal overlap between two trains on the same track.
     */
    private Conflict evaluateTrackContention(Train t1, Train t2, List<Track> tracks, int stationId, String stationCode) {
        int trackNum = t1.getCurrentTrackNumber();
        Track track = findTrack(tracks, trackNum);
        String trackName = track != null ? track.getTrackName() : "Track " + trackNum;

        // Check direction and progress
        boolean opposing = !t1.getDirection().equalsIgnoreCase(t2.getDirection());
        double distProgress = Math.abs(t1.getProgressPercent() - t2.getProgressPercent());

        // Temporal ETA overlap check (convert "HH:mm" to minutes from midnight)
        int eta1 = parseTimeToMinutes(t1.getEta());
        int eta2 = parseTimeToMinutes(t2.getEta());
        int timeDiffMinutes = Math.abs(eta1 - eta2);

        // Conflict condition:
        // Case A: Opposing transit on same track (High risk Head-On)
        // Case B: Same direction within 25% section distance of each other (Overtake / Rear-End proximity)
        // Case C: Overlapping ETA within 20 minutes on the same track
        boolean hasConflict = false;
        String conflictType = "SAME_TRACK_WINDOW";
        String severity = "HIGH";

        if (opposing) {
            hasConflict = true;
            conflictType = "OPPOSING_HEAD_ON_RISK";
            severity = "CRITICAL";
        } else if (distProgress <= 35.0) {
            hasConflict = true;
            conflictType = "REAR_END_OVERTAKE_CONVERGENCE";
            severity = distProgress <= 15.0 ? "CRITICAL" : "HIGH";
        } else if (timeDiffMinutes <= 20) {
            hasConflict = true;
            conflictType = "OVERLAPPING_SCHEDULE_WINDOW";
            severity = "MODERATE";
        }

        if (hasConflict) {
            Conflict conflict = new Conflict();
            conflict.setConflictId("CONF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            conflict.setStationId(stationId);
            conflict.setStationCode(stationCode);
            conflict.setTrackNumber(trackNum);
            conflict.setTrackName(trackName);
            conflict.setTrain1(t1);
            conflict.setTrain2(t2);
            conflict.setConflictType(conflictType);
            conflict.setSeverity(severity);
            conflict.setTimeWindowOverlapMinutes(timeDiffMinutes);
            conflict.setStatus("DETECTED");
            conflict.setTimestamp(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            return conflict;
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

    private int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) {
            return 0;
        }
        try {
            String[] parts = timeStr.trim().split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}

