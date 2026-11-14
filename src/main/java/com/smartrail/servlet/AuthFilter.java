package com.smartrail.servlet;

import com.smartrail.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authentication and Security Filter for SmartRail AI.
 * Protects control-room dashboard and railway dispatch APIs from unauthorized access.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = path.substring(contextPath.length());

        // Check for public static assets
        if (isPublicResource(relativePath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        boolean isLoggedIn = (user != null);

        // If user is already logged in and navigates to login or signup page, redirect to dashboard
        if (isLoggedIn && (relativePath.equals("/login.jsp") || relativePath.equals("/signup.jsp"))) {
            resp.sendRedirect(contextPath + "/dashboard");
            return;
        }

        // Welcome index.jsp handler
        if (relativePath.equals("/") || relativePath.equals("/index.jsp")) {
            if (isLoggedIn) {
                resp.sendRedirect(contextPath + "/dashboard");
            } else {
                resp.sendRedirect(contextPath + "/login.jsp");
            }
            return;
        }

        // If not logged in and requesting protected resource
        if (!isLoggedIn) {
            if (relativePath.startsWith("/api/")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"error\": \"Unauthorized. Please login to SmartRail AI.\", \"authenticated\": false}");
                return;
            } else if (!relativePath.equals("/login.jsp") && !relativePath.equals("/signup.jsp")) {
                resp.sendRedirect(contextPath + "/login.jsp?error=unauthorized");
                return;
            }
        }

        // Continue chain for authenticated or authorized requests
        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.startsWith("/css/")
                || lower.startsWith("/js/")
                || lower.startsWith("/images/")
                || lower.startsWith("/fonts/")
                || lower.equals("/favicon.ico")
                || lower.equals("/auth")
                || lower.startsWith("/api/auth")
                || lower.equals("/login.jsp")
                || lower.equals("/signup.jsp");
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}

