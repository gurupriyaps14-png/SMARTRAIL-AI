package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.model.Station;
import com.smartrail.service.RailwayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * REST Endpoint for Railway Stations by Division in Tamil Nadu.
 */
public class ApiStationServlet extends HttpServlet {
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
        List<Station> stations = railwayService.getStationsForDivision(division);
        resp.getWriter().write(gson.toJson(stations));
    }
}

