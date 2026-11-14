package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.model.Train;
import com.smartrail.model.TrainCategory;
import com.smartrail.model.TrainStatus;
import com.smartrail.service.RailwayService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * REST Endpoint for Trains:
 * GET: retrieve trains filtered by division, station, category and time slot.
 * POST: register a new train or update train delay/details.
 */
public class ApiTrainServlet extends HttpServlet {
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

        String division = req.getParameter("division");
        String stationIdStr = req.getParameter("stationId");
        String timeSlot = req.getParameter("timeSlot");
        String category = req.getParameter("category");

        Integer stationId = null;
        if (stationIdStr != null && !stationIdStr.trim().isEmpty()) {
            try {
                stationId = Integer.parseInt(stationIdStr);
            } catch (NumberFormatException ignored) {}
        }

        List<Train> trains = railwayService.getFilteredTrains(division, stationId, timeSlot, category);
        resp.getWriter().write(gson.toJson(trains));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            String action = req.getParameter("action");

            if ("delay".equalsIgnoreCase(action)) {
                String trainNumber = req.getParameter("trainNumber");
                int delayMinutes = Integer.parseInt(req.getParameter("delayMinutes"));
                String statusStr = req.getParameter("status");
                TrainStatus status = statusStr != null ? TrainStatus.fromString(statusStr) : (delayMinutes > 0 ? TrainStatus.DELAYED : TrainStatus.ON_TIME);

                Train t = railwayService.getTrain(trainNumber);
                boolean success = false;
                if (t != null) {
                    com.smartrail.dao.TrainDAO tdao = new com.smartrail.dao.TrainDAO();
                    success = tdao.updateTrainDelay(trainNumber, delayMinutes, status.name());
                    com.smartrail.dao.TrainDelayDAO ddao = new com.smartrail.dao.TrainDelayDAO();
                    ddao.recordDelay(trainNumber, t.getCurrentStationName() != null ? t.getCurrentStationName() : "MAS", delayMinutes, "Operational line contention");
                }
                out.print("{\"success\":" + success + ", \"message\":\"Train delay updated\"}");
                return;
            }

            // Default: Add or register train details
            String trainNumber = req.getParameter("trainNumber");
            String trainName = req.getParameter("trainName");
            String categoryStr = req.getParameter("category");
            int trackNumber = 1;
            try { trackNumber = Integer.parseInt(req.getParameter("trackNumber")); } catch (Exception ignored) {}

            String direction = req.getParameter("direction");
            if (direction == null) direction = "UP";

            String statusStr = req.getParameter("status");
            String eta = req.getParameter("eta");
            if (eta == null || eta.isEmpty()) eta = "23:45";

            int speed = 80;
            try { speed = Integer.parseInt(req.getParameter("speed")); } catch (Exception ignored) {}

            int originId = 1;
            int destId = 1;
            int currentStationId = 1;
            try { originId = Integer.parseInt(req.getParameter("originId")); } catch (Exception ignored) {}
            try { destId = Integer.parseInt(req.getParameter("destId")); } catch (Exception ignored) {}
            try { currentStationId = Integer.parseInt(req.getParameter("currentStationId")); } catch (Exception ignored) {}

            Train train = new Train();
            train.setTrainNumber(trainNumber != null ? trainNumber.trim() : "T-" + (System.currentTimeMillis() % 10000));
            train.setTrainName(trainName != null ? trainName.trim() : "Special Superfast Express");
            train.setCategory(TrainCategory.fromString(categoryStr));
            train.setOriginStationId(originId);
            train.setDestinationStationId(destId);
            train.setCurrentStationId(currentStationId);
            train.setCurrentTrackNumber(trackNumber);
            train.setDirection(direction.toUpperCase());
            train.setStatus(statusStr != null ? TrainStatus.fromString(statusStr) : TrainStatus.MOVING);
            train.setEta(eta);
            train.setEtd("23:55");
            train.setDelayMinutes(0);
            train.setSpeedKmh(speed);
            train.setProgressPercent(8.0);
            train.setTimeSlot("ALL");
            train.setSimulated(true);

            boolean success = railwayService.addSimulatedTrain(train);
            out.print("{\"success\":" + success + ", \"train\":" + gson.toJson(train) + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Failed to save train: " + e.getMessage() + "\"}");
        }
    }
}
