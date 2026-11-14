package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.model.Train;
import com.smartrail.model.TrainCategory;
import com.smartrail.model.TrainStatus;
import com.smartrail.service.RailwayService;
import com.smartrail.service.InterlockingSimulationEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller endpoint for Simulation Controls (Start, Pause, Reset, Telemetry, Progress Updates).
 */
public class ApiSimulationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RailwayService railwayService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.railwayService = new RailwayService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String action = req.getParameter("action");
        if ("telemetry".equalsIgnoreCase(action)) {
            String division = req.getParameter("division");
            String stationIdStr = req.getParameter("stationId");
            Integer stationId = null;
            if (stationIdStr != null && !stationIdStr.trim().isEmpty()) {
                try {
                    stationId = Integer.parseInt(stationIdStr);
                } catch (NumberFormatException ignored) {}
            }
            Map<String, Object> telemetry = railwayService.getTelemetryOverview(division, stationId);
            resp.getWriter().write(gson.toJson(telemetry));
            return;
        }

        // Default or action=state: Return full interlocking simulation snapshot
        InterlockingSimulationEngine engine = railwayService.getInterlockingEngine();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ready");
        result.put("system", "SmartRail AI Simulation Engine");
        result.put("mode", "SIMULATION (Tamil Nadu Operational Corridor)");
        result.put("scenario", engine.getActiveScenarioName());
        result.put("isPaused", engine.isPaused());
        result.put("speedMultiplier", engine.getSpeedMultiplier());
        result.put("trains", engine.getActiveTrains());
        result.put("trackSections", engine.getTrackSections());
        result.put("junctions", engine.getJunctions());

        resp.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String action = req.getParameter("action");
        Map<String, Object> result = new HashMap<>();
        InterlockingSimulationEngine engine = railwayService.getInterlockingEngine();

        if ("step".equalsIgnoreCase(action)) {
            String deltaStr = req.getParameter("deltaSec");
            double deltaSec = 0.05;
            if (deltaStr != null) {
                try {
                    deltaSec = Double.parseDouble(deltaStr);
                } catch (NumberFormatException ignored) {}
            }
            engine.stepSimulation(deltaSec);

            result.put("success", true);
            result.put("scenario", engine.getActiveScenarioName());
            result.put("trains", engine.getActiveTrains());
            result.put("trackSections", engine.getTrackSections());
            result.put("junctions", engine.getJunctions());
        } else if ("scenario".equalsIgnoreCase(action)) {
            String scenario = req.getParameter("name");
            engine.setupScenario(scenario);

            result.put("success", true);
            result.put("scenario", engine.getActiveScenarioName());
            result.put("message", "Interlocking scenario switched to Scenario " + engine.getActiveScenarioName());
            result.put("trains", engine.getActiveTrains());
            result.put("trackSections", engine.getTrackSections());
            result.put("junctions", engine.getJunctions());
        } else if ("dispatchDiversion".equalsIgnoreCase(action)) {
            String trainNumber = req.getParameter("trainNumber");
            int fromTrack = 1;
            int toTrack = 3;
            try {
                if (req.getParameter("fromTrack") != null) fromTrack = Integer.parseInt(req.getParameter("fromTrack"));
                if (req.getParameter("toTrack") != null) toTrack = Integer.parseInt(req.getParameter("toTrack"));
            } catch (NumberFormatException ignored) {}

            boolean ok = engine.requestDiversion(trainNumber, fromTrack, toTrack);
            result.put("success", ok);
            result.put("trainNumber", trainNumber);
            result.put("fromTrack", fromTrack);
            result.put("toTrack", toTrack);
            result.put("message", ok ? "Diversion route reserved across connecting turnout side track." : "Diversion held: Side track occupied.");
            result.put("trains", engine.getActiveTrains());
            result.put("trackSections", engine.getTrackSections());
            result.put("junctions", engine.getJunctions());
        } else if ("pause".equalsIgnoreCase(action)) {
            engine.setPaused(true);
            result.put("success", true);
            result.put("isPaused", true);
        } else if ("resume".equalsIgnoreCase(action)) {
            engine.setPaused(false);
            result.put("success", true);
            result.put("isPaused", false);
        } else if ("updateProgress".equalsIgnoreCase(action)) {
            String trainNumber = req.getParameter("trainNumber");
            String progressStr = req.getParameter("progress");
            String status = req.getParameter("status");
            String trackStr = req.getParameter("trackNumber");

            double progress = 0.0;
            int trackNumber = 1;

            if (progressStr != null) {
                try {
                    progress = Double.parseDouble(progressStr);
                } catch (NumberFormatException ignored) {}
            }
            if (trackStr != null) {
                try {
                    trackNumber = Integer.parseInt(trackStr);
                } catch (NumberFormatException ignored) {}
            }

            boolean updated = railwayService.updateTrainProgress(trainNumber, progress, status, trackNumber);
            result.put("success", updated);
            result.put("trainNumber", trainNumber);
            result.put("progress", progress);
            result.put("isArrived", progress >= 100.0);
            if (progress >= 100.0) {
                result.put("message", "Successfully Reached Destination");
            }
        } else if ("addTrain".equalsIgnoreCase(action)) {
            try {
                Train train = new Train();
                train.setTrainNumber(req.getParameter("trainNumber"));
                train.setTrainName(req.getParameter("trainName"));
                train.setCategory(TrainCategory.fromString(req.getParameter("category")));
                train.setOriginStationId(Integer.parseInt(req.getParameter("originId")));
                train.setDestinationStationId(Integer.parseInt(req.getParameter("destId")));
                train.setCurrentStationId(Integer.parseInt(req.getParameter("currentStationId")));
                train.setCurrentTrackNumber(Integer.parseInt(req.getParameter("trackNumber")));
                train.setDirection(req.getParameter("direction"));
                train.setStatus(TrainStatus.fromString(req.getParameter("status")));
                train.setEta(req.getParameter("eta"));
                train.setEtd(req.getParameter("etd"));
                train.setDelayMinutes(Integer.parseInt(req.getParameter("delay")));
                train.setSpeedKmh(Integer.parseInt(req.getParameter("speed")));
                train.setProgressPercent(0.0);
                train.setSimulated(true);

                boolean added = railwayService.addSimulatedTrain(train);
                result.put("success", added);
                result.put("message", added ? "Simulated train added successfully." : "Failed to add train.");
            } catch (Exception e) {
                result.put("success", false);
                result.put("error", e.getMessage());
            }
        } else if ("reset".equalsIgnoreCase(action)) {
            // Reset trains progress & backend interlocking state
            engine.initDefaultTrains();
            List<Train> trains = railwayService.getAllTrains();
            for (Train t : trains) {
                railwayService.updateTrainProgress(t.getTrainNumber(), 10.0, TrainStatus.ON_TIME.name(), t.getCurrentTrackNumber());
            }
            result.put("success", true);
            result.put("message", "Simulation telemetry reset to initial checkpoint.");
            result.put("trains", engine.getActiveTrains());
            result.put("trackSections", engine.getTrackSections());
        } else {
            result.put("success", false);
            result.put("message", "Unknown action: " + action);
        }

        resp.getWriter().write(gson.toJson(result));
    }
}
