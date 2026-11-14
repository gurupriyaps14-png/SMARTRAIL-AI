package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.dao.TrackDAO;
import com.smartrail.model.Station;
import com.smartrail.model.Track;
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
 * REST Endpoint for Railway Tracks, line status toggling, and maintenance blocks.
 */
public class ApiTrackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RailwayService railwayService;
    private TrackDAO trackDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.railwayService = new RailwayService();
        this.trackDAO = new TrackDAO();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String stationIdParam = req.getParameter("stationId");
        int stationId = 1;

        if (stationIdParam != null && !stationIdParam.trim().isEmpty()) {
            try {
                stationId = Integer.parseInt(stationIdParam);
            } catch (NumberFormatException ignored) {}
        } else {
            String stationCode = req.getParameter("stationCode");
            if (stationCode != null && !stationCode.trim().isEmpty()) {
                Station st = railwayService.getStationByCode(stationCode);
                if (st != null) {
                    stationId = st.getId();
                }
            }
        }

        List<Track> tracks = railwayService.getTracksForStation(stationId);
        resp.getWriter().write(gson.toJson(tracks));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            int stationId = 1;
            try {
                stationId = Integer.parseInt(req.getParameter("stationId"));
            } catch (Exception ignored) {}

            int trackNumber = Integer.parseInt(req.getParameter("trackNumber"));
            String status = req.getParameter("status");
            String trainNumber = req.getParameter("trainNumber");

            boolean success = trackDAO.updateTrackStatus(stationId, trackNumber, status, trainNumber);
            out.print("{\"success\":" + success + ", \"stationId\":" + stationId + ", \"trackNumber\":" + trackNumber + ", \"status\":\"" + status + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Failed to update track: " + e.getMessage() + "\"}");
        }
    }
}
