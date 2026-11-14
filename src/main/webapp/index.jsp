<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Intelligently route based on authentication state
    if (session.getAttribute("currentUser") != null) {
        response.sendRedirect("dashboard");
    } else {
        response.sendRedirect("login.jsp");
    }
%>
