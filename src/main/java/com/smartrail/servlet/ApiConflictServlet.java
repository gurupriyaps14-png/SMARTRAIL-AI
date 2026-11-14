package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.model.AlertItem;
import com.smartrail.model.Conflict;
import com.smartrail.model.DispatchMode;
import com.smartrail.service.RailwayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Endpoint for AI-based Conflict Detection, Priority Management,
 * Track Diversions, and Live Alerts.
 */
public class ApiConflictServlet extends HttpServlet {
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
        String division = req.getParameter("division");
        if (division == null || division.trim().isEmpty()) {
            division = "MAS";
        }

        int stationId = 1;
        String stationIdStr = req.getParameter("stationId");
        if (stationIdStr != null && !stationIdStr.trim().isEmpty()) {
            try {
                stationId = Integer.parseInt(stationIdStr);
            } catch (NumberFormatException ignored) {}
        }

        if ("alerts".equalsIgnoreCase(action)) {
            List<AlertItem> alerts = railwayService.getAlertHistory();
            resp.getWriter().write(gson.toJson(alerts));
            return;
        }

        if ("active".equalsIgnoreCase(action)) {
            List<Conflict> conflicts = railwayService.getActiveConflicts();
            resp.getWriter().write(gson.toJson(conflicts));
            return;
        }

        // Default: Scan and plan conflicts
        List<Conflict> conflicts = railwayService.scanAndPlanConflicts(division, stationId);
        Map<String, Object> result = new HashMap<>();
        result.put("conflictCount", conflicts.size());
        result.put("conflicts", conflicts);
        result.put("dispatchMode", railwayService.getDispatchMode().name());
        result.put("dispatchModeLabel", railwayService.getDispatchMode().getLabel());
        result.put("alerts", railwayService.getAlertHistory());

        resp.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String action = req.getParameter("action");
        String division = req.getParameter("division");
        if (division == null) division = "MAS";

        int stationId = 1;
        String stationIdStr = req.getParameter("stationId");
        if (stationIdStr != null) {
            try { stationId = Integer.parseInt(stationIdStr); } catch (NumberFormatException ignored) {}
        }

        Map<String, Object> responseData = new HashMap<>();

        if ("simulate".equalsIgnoreCase(action)) {
            int trackNumber = 1;
            String trackStr = req.getParameter("trackNumber");
            if (trackStr != null) {
                try { trackNumber = Integer.parseInt(trackStr); } catch (NumberFormatException ignored) {}
            }
            Conflict conflict = railwayService.simulateConflict(division, stationId, trackNumber);
            responseData.put("success", conflict != null);
            responseData.put("conflict", conflict);
            responseData.put("message", conflict != null ? "Track conflict successfully simulated on Track " + trackNumber : "Could not simulate conflict");
        } else if ("clear".equalsIgnoreCase(action)) {
            boolean cleared = railwayService.clearSimulatedConflicts(division, stationId);
            responseData.put("success", cleared);
            responseData.put("message", "Simulated track conflict cleared. Nominal transit restored.");
        } else if ("approve".equalsIgnoreCase(action)) {
            String conflictId = req.getParameter("conflictId");
            boolean approved = railwayService.approveDiversion(conflictId, stationId);
            responseData.put("success", approved);
            responseData.put("message", approved ? "Track diversion approved by controller and applied successfully." : "Failed to apply diversion.");
        } else if ("hold".equalsIgnoreCase(action)) {
            String conflictId = req.getParameter("conflictId");
            boolean held = railwayService.holdAtSignal(conflictId);
            responseData.put("success", held);
            responseData.put("message", "Train ordered to hold at outer approach signal.");
        } else if ("setMode".equalsIgnoreCase(action)) {
            String modeStr = req.getParameter("mode");
            DispatchMode mode = DispatchMode.fromString(modeStr);
            railwayService.setDispatchMode(mode);
            responseData.put("success", true);
            responseData.put("dispatchMode", mode.name());
            responseData.put("dispatchModeLabel", mode.getLabel());
            responseData.put("message", "Dispatch mode changed to " + mode.getLabel());
        } else {
            responseData.put("success", false);
            responseData.put("message", "Unknown action: " + action);
        }

        resp.getWriter().write(gson.toJson(responseData));
    }
}

