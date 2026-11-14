package com.smartrail.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Actionable track diversion recommendation produced by the explainable AI decision engine.
 */
public class DiversionRecommendation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String conflictId;
    private String retainingTrainNumber;
    private String retainingTrainName;
    private String divertedTrainNumber;
    private String divertedTrainName;
    private int fromTrackNumber;
    private int toTrackNumber;
    private String toTrackName;
    private boolean alternativeAvailable;
    private String recommendedAction; // DIVERT_TO_ALTERNATIVE_TRACK, HOLD_AT_OUTER_SIGNAL, SPEED_REGULATION
    private double priorityScoreRetaining;
    private double priorityScoreDiverted;
    private String decisionSummary;
    private String reason;
    private List<String> operationalFactors;
    private boolean approved;
    private DispatchMode dispatchMode;

    public DiversionRecommendation() {
        this.operationalFactors = new ArrayList<>();
        this.dispatchMode = DispatchMode.MANUAL;
        this.approved = false;
    }

    public String getConflictId() {
        return conflictId;
    }

    public void setConflictId(String conflictId) {
        this.conflictId = conflictId;
    }

    public String getRetainingTrainNumber() {
        return retainingTrainNumber;
    }

    public void setRetainingTrainNumber(String retainingTrainNumber) {
        this.retainingTrainNumber = retainingTrainNumber;
    }

    public String getRetainingTrainName() {
        return retainingTrainName;
    }

    public void setRetainingTrainName(String retainingTrainName) {
        this.retainingTrainName = retainingTrainName;
    }

    public String getDivertedTrainNumber() {
        return divertedTrainNumber;
    }

    public void setDivertedTrainNumber(String divertedTrainNumber) {
        this.divertedTrainNumber = divertedTrainNumber;
    }

    public String getDivertedTrainName() {
        return divertedTrainName;
    }

    public void setDivertedTrainName(String divertedTrainName) {
        this.divertedTrainName = divertedTrainName;
    }

    public int getFromTrackNumber() {
        return fromTrackNumber;
    }

    public void setFromTrackNumber(int fromTrackNumber) {
        this.fromTrackNumber = fromTrackNumber;
    }

    public int getToTrackNumber() {
        return toTrackNumber;
    }

    public void setToTrackNumber(int toTrackNumber) {
        this.toTrackNumber = toTrackNumber;
    }

    public String getToTrackName() {
        return toTrackName;
    }

    public void setToTrackName(String toTrackName) {
        this.toTrackName = toTrackName;
    }

    public boolean isAlternativeAvailable() {
        return alternativeAvailable;
    }

    public void setAlternativeAvailable(boolean alternativeAvailable) {
        this.alternativeAvailable = alternativeAvailable;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public double getPriorityScoreRetaining() {
        return priorityScoreRetaining;
    }

    public void setPriorityScoreRetaining(double priorityScoreRetaining) {
        this.priorityScoreRetaining = priorityScoreRetaining;
    }

    public double getPriorityScoreDiverted() {
        return priorityScoreDiverted;
    }

    public void setPriorityScoreDiverted(double priorityScoreDiverted) {
        this.priorityScoreDiverted = priorityScoreDiverted;
    }

    public String getDecisionSummary() {
        return decisionSummary;
    }

    public void setDecisionSummary(String decisionSummary) {
        this.decisionSummary = decisionSummary;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getOperationalFactors() {
        return operationalFactors;
    }

    public void setOperationalFactors(List<String> operationalFactors) {
        this.operationalFactors = operationalFactors;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public DispatchMode getDispatchMode() {
        return dispatchMode;
    }

    public void setDispatchMode(DispatchMode dispatchMode) {
        this.dispatchMode = dispatchMode;
    }
}

