package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.dao.TrainScheduleDAO;
import com.smartrail.model.TrainSchedule;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Controller API to view and update railway train schedules.
 */
public class ApiScheduleServlet extends HttpServlet {
    private final TrainScheduleDAO scheduleDAO = new TrainScheduleDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = response.getWriter()) {
            List<TrainSchedule> list = scheduleDAO.getAllSchedules();
            out.print(gson.toJson(list));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Failed to load schedules: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String action = request.getParameter("action");
            if ("update".equalsIgnoreCase(action)) {
                int scheduleId = Integer.parseInt(request.getParameter("scheduleId"));
                String arrival = request.getParameter("arrival");
                String departure = request.getParameter("departure");
                int platform = Integer.parseInt(request.getParameter("platform"));

                boolean success = scheduleDAO.updateSchedule(scheduleId, arrival, departure, platform);
                out.print("{\"success\":" + success + ", \"message\":\"Schedule updated successfully\"}");
            } else if ("insert".equalsIgnoreCase(action)) {
                int trainId = Integer.parseInt(request.getParameter("trainId"));
                int stationId = Integer.parseInt(request.getParameter("stationId"));
                String arrival = request.getParameter("arrival");
                String departure = request.getParameter("departure");
                int platform = Integer.parseInt(request.getParameter("platform"));
                int seq = Integer.parseInt(request.getParameter("stopSequence"));

                boolean success = scheduleDAO.insertSchedule(trainId, stationId, arrival, departure, platform, seq);
                out.print("{\"success\":" + success + ", \"message\":\"New schedule stop added\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Unsupported action\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Failed to update schedule: " + e.getMessage() + "\"}");
        }
    }
}
