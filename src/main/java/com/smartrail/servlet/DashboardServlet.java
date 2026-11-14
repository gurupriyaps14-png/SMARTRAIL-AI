package com.smartrail.servlet;

import com.smartrail.dao.DBConnection;
import com.smartrail.model.Division;
import com.smartrail.model.Station;
import com.smartrail.model.Track;
import com.smartrail.model.Train;
import com.smartrail.service.RailwayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller Servlet for the Main SmartRail AI Dashboard.
 */
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RailwayService railwayService;

    @Override
    public void init() throws ServletException {
        super.init();
        // Warm up database connection and seed tables
        DBConnection.getConnection();
        this.railwayService = new RailwayService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String divisionCode = req.getParameter("division");
        if (divisionCode == null || divisionCode.trim().isEmpty()) {
            divisionCode = "MAS"; // Default to Chennai Division
        }

        String stationIdStr = req.getParameter("stationId");
        Integer stationId = null;
        if (stationIdStr != null && !stationIdStr.trim().isEmpty()) {
            try {
                stationId = Integer.parseInt(stationIdStr);
            } catch (NumberFormatException ignored) {}
        }

        List<Division> divisions = railwayService.getDivisions();
        List<Station> stations = railwayService.getStationsForDivision(divisionCode);

        // If no stationId provided, select first station in division
        if (stationId == null && !stations.isEmpty()) {
            stationId = stations.get(0).getId();
        }

        Station currentStation = null;
        if (stationId != null) {
            currentStation = railwayService.getStation(stationId);
        } else if (!stations.isEmpty()) {
            currentStation = stations.get(0);
        }

        List<Track> tracks = currentStation != null ?
                railwayService.getTracksForStation(currentStation.getId()) :
                railwayService.getTracksForStation(1);

        String timeSlot = req.getParameter("timeSlot");
        String category = req.getParameter("category");

        List<Train> trains = railwayService.getFilteredTrains(divisionCode, stationId, timeSlot, category);
        Map<String, Object> telemetry = railwayService.getTelemetryOverview(divisionCode, stationId);

        req.setAttribute("divisions", divisions);
        req.setAttribute("stations", stations);
        req.setAttribute("selectedDivision", divisionCode);
        req.setAttribute("selectedStationId", stationId);
        req.setAttribute("currentStation", currentStation);
        req.setAttribute("tracks", tracks);
        req.setAttribute("trains", trains);
        req.setAttribute("telemetry", telemetry);
        req.setAttribute("dbType", DBConnection.getDatabaseType());

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}

