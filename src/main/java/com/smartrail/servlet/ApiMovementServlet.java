package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.dao.TrainMovementDAO;
import com.smartrail.model.TrainMovement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Controller API to view train movement telemetry history.
 */
public class ApiMovementServlet extends HttpServlet {
    private final TrainMovementDAO movementDAO = new TrainMovementDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = response.getWriter()) {
            int limit = 50;
            String limitParam = request.getParameter("limit");
            if (limitParam != null) {
                try { limit = Integer.parseInt(limitParam); } catch (Exception ignored) {}
            }
            List<TrainMovement> list = movementDAO.getRecentMovements(limit);
            out.print(gson.toJson(list));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Failed to load movements: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String trainNumber = request.getParameter("trainNumber");
            String stationCode = request.getParameter("stationCode");
            int trackNumber = Integer.parseInt(request.getParameter("trackNumber"));
            int speedKmh = Integer.parseInt(request.getParameter("speedKmh"));
            double progress = Double.parseDouble(request.getParameter("progress"));
            String status = request.getParameter("status");
            String notes = request.getParameter("notes");

            boolean success = movementDAO.recordMovement(trainNumber, stationCode, trackNumber, speedKmh, progress, status, notes);
            out.print("{\"success\":" + success + "}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Failed to record movement: " + e.getMessage() + "\"}");
        }
    }
}
