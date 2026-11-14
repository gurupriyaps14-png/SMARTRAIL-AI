package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.dao.AlertNotificationDAO;
import com.smartrail.model.AlertNotification;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Controller API to query and record alert history logs:
 * - Alert ID
 * - Train Number
 * - Conflict Type
 * - Time Detected
 * - Recommended Action
 * - Dispatch Status
 */
public class ApiAlertHistoryServlet extends HttpServlet {
    private final AlertNotificationDAO alertDAO = new AlertNotificationDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = response.getWriter()) {
            List<AlertNotification> alerts = alertDAO.getAllAlerts();
            out.print(gson.toJson(alerts));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Failed to load alert history: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String alertCode = request.getParameter("alertCode");
            if (alertCode == null || alertCode.trim().isEmpty()) {
                alertCode = "ALT-" + (int)(System.currentTimeMillis() % 100000);
            }
            String trainNumber = request.getParameter("trainNumber");
            String conflictType = request.getParameter("conflictType");
            String action = request.getParameter("recommendedAction");
            String dispatchStatus = request.getParameter("dispatchStatus");
            String severity = request.getParameter("severity");
            String stationCode = request.getParameter("stationCode");
            int trackNumber = 1;
            try {
                trackNumber = Integer.parseInt(request.getParameter("trackNumber"));
            } catch (Exception ignored) {}
            String details = request.getParameter("details");

            boolean success = alertDAO.insertAlert(alertCode, trainNumber, conflictType, action, dispatchStatus,
                    severity != null ? severity : "HIGH", stationCode != null ? stationCode : "MAS", trackNumber, details);

            out.print("{\"success\":" + success + ", \"alertCode\":\"" + alertCode + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Failed to log alert: " + e.getMessage() + "\"}");
        }
    }
}
