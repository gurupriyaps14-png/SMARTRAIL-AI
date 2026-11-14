package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.dao.AnalyticsDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * REST API providing telemetry metrics for the 6 Chart.js graphs:
 * A. Train Delay Analysis
 * B. Train Traffic Analysis
 * C. Train Punctuality
 * D. Track Utilization
 * E. Train Category Analysis
 * F. Diversion Analysis
 */
public class ApiAnalyticsServlet extends HttpServlet {
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = response.getWriter()) {
            Map<String, Object> data = analyticsDAO.getFullAnalytics();
            out.print(gson.toJson(data));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Failed to retrieve analytics: " + e.getMessage() + "\"}");
        }
    }
}
