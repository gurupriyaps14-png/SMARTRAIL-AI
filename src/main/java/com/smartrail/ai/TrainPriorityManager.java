package com.smartrail.ai;

import com.smartrail.model.Track;
import com.smartrail.model.Train;
import com.smartrail.model.TrainCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent Train Priority Management Engine.
 * Evaluates competing trains using an explainable, configurable multi-criteria scoring algorithm:
 * - Train category & human passenger impact
 * - Delay duration and cascading network impact (avoids naively giving top priority to severely delayed slow freight)
 * - Scheduled arrival urgency
 * - Track geometry and speed compatibility
 */
public class TrainPriorityManager {

    /**
     * Evaluation result containing final score and explainable factor breakdown.
     */
    public static class PriorityEvaluation {
        private final Train train;
        private final double totalScore;
        private final double categoryScore;
        private final double delayScore;
        private final double scheduleUrgencyScore;
        private final double operationalConstraintScore;
        private final List<String> explanationFactors;

        public PriorityEvaluation(Train train, double totalScore, double categoryScore,
                                  double delayScore, double scheduleUrgencyScore,
                                  double operationalConstraintScore, List<String> explanationFactors) {
            this.train = train;
            this.totalScore = totalScore;
            this.categoryScore = categoryScore;
            this.delayScore = delayScore;
            this.scheduleUrgencyScore = scheduleUrgencyScore;
            this.operationalConstraintScore = operationalConstraintScore;
            this.explanationFactors = explanationFactors;
        }

        public Train getTrain() {
            return train;
        }

        public double getTotalScore() {
            return totalScore;
        }

        public double getCategoryScore() {
            return categoryScore;
        }

        public double getDelayScore() {
            return delayScore;
        }

        public double getScheduleUrgencyScore() {
            return scheduleUrgencyScore;
        }

        public double getOperationalConstraintScore() {
            return operationalConstraintScore;
        }

        public List<String> getExplanationFactors() {
            return explanationFactors;
        }
    }

    /**
     * Evaluates a train's operational priority for a given track allocation contention.
     */
    public PriorityEvaluation evaluate(Train train, Track currentTrack) {
        List<String> factors = new ArrayList<>();

        // 1. Base Score by Train Category
        double categoryScore = 0.0;
        TrainCategory cat = train.getCategory() != null ? train.getCategory() : TrainCategory.EXPRESS;
        switch (cat) {
            case EXPRESS:
                // Premium Superfast / Vande Bharat / Intercity Express
                categoryScore = 100.0;
                factors.add("High-speed Premium Express corridor service (+100.0)");
                break;
            case PASSENGER:
                // Commuter suburban and regional passengers
                categoryScore = 70.0;
                factors.add("High passenger volume commuter transit (+70.0)");
                break;
            case GOODS:
                // Time-sensitive industrial logistics (Coal, Containers, Autos)
                categoryScore = 45.0;
                factors.add("Time-sensitive commercial cargo transit (+45.0)");
                break;
            case FREIGHT:
                // Bulk raw materials / heavy mineral rakes
                categoryScore = 30.0;
                factors.add("Heavy bulk freight line service (+30.0)");
                break;
        }

        // 2. Delay Impact & Cascading Gridlock Prevention
        // Rule: A moderately delayed express gets a recovery boost.
        // But a heavily delayed freight train does NOT hijack the main line from on-time express traffic.
        double delayScore = 0.0;
        int delay = train.getDelayMinutes();
        if (delay <= 0) {
            // On-time maintenance bonus (protecting high-punctuality corridors)
            delayScore = 15.0;
            factors.add("On-schedule maintenance protection bonus (+15.0)");
        } else if (delay <= 20) {
            // Recoverable delay: incentivize clearance to return to timetable
            delayScore = 10.0 + (delay * 0.8);
            factors.add("Recoverable schedule deviation (" + delay + " min) clearance boost (+" + Math.round(delayScore) + ")");
        } else if (delay <= 45) {
            // Moderate delay
            delayScore = 20.0;
            factors.add("Moderate delay (" + delay + " min) operational priority (+20.0)");
        } else {
            // Heavy delay: Cap priority to prevent cascading network gridlock
            delayScore = 12.0;
            factors.add("Severe delay (" + delay + " min) capped to prevent cascading network gridlock (+12.0)");
        }

        // 3. Scheduled Arrival Urgency & Speed
        double urgencyScore = 0.0;
        if (train.getSpeedKmh() >= 90) {
            urgencyScore += 15.0;
            factors.add("High track speed kinetic priority [" + train.getSpeedKmh() + " km/h] (+15.0)");
        } else if (train.getSpeedKmh() >= 60) {
            urgencyScore += 8.0;
            factors.add("Moderate operating speed [" + train.getSpeedKmh() + " km/h] (+8.0)");
        } else {
            urgencyScore += 3.0;
            factors.add("Low-speed block section handling (+3.0)");
        }

        // Proximity bonus based on progress along section
        if (train.getProgressPercent() >= 60.0) {
            urgencyScore += 12.0;
            factors.add("Advanced section progress [" + Math.floor(train.getProgressPercent()) + "%] clearance (+12.0)");
        }

        // 4. Operational & Track Constraints
        double operationalScore = 0.0;
        if (currentTrack != null) {
            String trackType = currentTrack.getTrackType() != null ? currentTrack.getTrackType() : "MAIN_UP";
            if (trackType.startsWith("MAIN")) {
                if (cat == TrainCategory.EXPRESS) {
                    operationalScore += 20.0;
                    factors.add("Express alignment with Main High-Speed line (+20.0)");
                } else if (cat == TrainCategory.FREIGHT || cat == TrainCategory.GOODS) {
                    // Loop line diversion preferred for freight
                    operationalScore -= 10.0;
                    factors.add("Freight on Main line has high loop diversion preference (-10.0)");
                }
            } else if (trackType.startsWith("LOOP")) {
                if (cat == TrainCategory.PASSENGER || cat == TrainCategory.EXPRESS) {
                    operationalScore += 10.0;
                    factors.add("Platform loop passenger boarding access (+10.0)");
                }
            } else if (trackType.contains("SIDING")) {
                if (cat == TrainCategory.FREIGHT || cat == TrainCategory.GOODS) {
                    operationalScore += 15.0;
                    factors.add("Optimal siding track allocation for cargo rakes (+15.0)");
                }
            }
        }

        double totalScore = Math.max(5.0, categoryScore + delayScore + urgencyScore + operationalScore);
        totalScore = Math.round(totalScore * 10.0) / 10.0;

        return new PriorityEvaluation(train, totalScore, categoryScore, delayScore, urgencyScore, operationalScore, factors);
    }

    /**
     * Compares two contending trains and returns the winner (train that retains priority).
     */
    public PriorityEvaluation decidePriority(Train trainA, Train trainB, Track track) {
        PriorityEvaluation evalA = evaluate(trainA, track);
        PriorityEvaluation evalB = evaluate(trainB, track);

        if (evalA.getTotalScore() >= evalB.getTotalScore()) {
            return evalA;
        } else {
            return evalB;
        }
    }
}

