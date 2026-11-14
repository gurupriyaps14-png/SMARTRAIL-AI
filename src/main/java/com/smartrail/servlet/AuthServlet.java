package com.smartrail.servlet;

import com.google.gson.Gson;
import com.smartrail.dao.UserDAO;
import com.smartrail.model.User;
import com.smartrail.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Authentication and Controller Account Management Servlet.
 * Handles Login, Registration (Sign Up), Logout, and Auth Status checks.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth", "/api/auth"})
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "status";
        }

        switch (action.toLowerCase()) {
            case "logout":
                handleLogout(req, resp);
                break;
            case "status":
            default:
                handleStatus(req, resp);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "login";
        }

        switch (action.toLowerCase()) {
            case "signup":
            case "register":
                handleSignup(req, resp);
                break;
            case "login":
            default:
                handleLogin(req, resp);
                break;
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        boolean isAjax = isAjaxRequest(req);

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            sendError(req, resp, "Email and password are required.", 400, isAjax, "/login.jsp");
            return;
        }

        User user = userDAO.authenticate(email.trim(), password);
        if (user == null) {
            sendError(req, resp, "Invalid email address or password.", 401, isAjax, "/login.jsp");
            return;
        }

        // Establish secure session
        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);
        session.setMaxInactiveInterval(60 * 60); // 60 minutes

        if (isAjax) {
            resp.setContentType("application/json;charset=UTF-8");
            Map<String, Object> out = new HashMap<>();
            out.put("success", true);
            out.put("message", "Login successful. Welcome back, " + user.getFullName() + ".");
            out.put("redirectUrl", req.getContextPath() + "/dashboard");
            out.put("user", Map.of(
                    "id", user.getId(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));
            resp.getWriter().write(gson.toJson(out));
        } else {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    private void handleSignup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        boolean isAjax = isAjaxRequest(req);

        // 1. Validate full name
        if (fullName == null || fullName.trim().length() < 2) {
            sendError(req, resp, "Full name must be at least 2 characters.", 400, isAjax, "/signup.jsp");
            return;
        }

        // 2. Validate email format
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            sendError(req, resp, "Please enter a valid email address.", 400, isAjax, "/signup.jsp");
            return;
        }

        // 3. Validate password strength
        if (password == null || password.length() < 6) {
            sendError(req, resp, "Password must be at least 6 characters.", 400, isAjax, "/signup.jsp");
            return;
        }

        // 4. Validate password confirmation
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            sendError(req, resp, "Passwords do not match.", 400, isAjax, "/signup.jsp");
            return;
        }

        // 5. Prevent duplicate email registration
        if (userDAO.emailExists(email.trim())) {
            sendError(req, resp, "An account with this email address already exists.", 409, isAjax, "/signup.jsp");
            return;
        }

        // 6. Secure password hashing with unique salt
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        User newUser = new User(fullName.trim(), email.trim(), passwordHash, salt, "TRAIN_CONTROLLER");
        boolean created = userDAO.createUser(newUser);

        if (!created) {
            sendError(req, resp, "Could not register account. Please try again.", 500, isAjax, "/signup.jsp");
            return;
        }

        // Automatically log in the newly registered user
        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", newUser);
        session.setMaxInactiveInterval(60 * 60);

        if (isAjax) {
            resp.setContentType("application/json;charset=UTF-8");
            Map<String, Object> out = new HashMap<>();
            out.put("success", true);
            out.put("message", "Account successfully registered. Welcome to SmartRail AI, " + newUser.getFullName() + "!");
            out.put("redirectUrl", req.getContextPath() + "/dashboard");
            out.put("user", Map.of(
                    "id", newUser.getId(),
                    "fullName", newUser.getFullName(),
                    "email", newUser.getEmail(),
                    "role", newUser.getRole()
            ));
            resp.getWriter().write(gson.toJson(out));
        } else {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.removeAttribute("currentUser");
            session.invalidate();
        }
        if (isAjaxRequest(req)) {
            resp.setContentType("application/json;charset=UTF-8");
            Map<String, Object> out = new HashMap<>();
            out.put("success", true);
            out.put("message", "Logged out successfully");
            out.put("redirectUrl", req.getContextPath() + "/login.jsp");
            resp.getWriter().write(gson.toJson(out));
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?logout=true");
        }
    }

    private void handleStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        Map<String, Object> out = new HashMap<>();
        if (user != null) {
            out.put("authenticated", true);
            out.put("user", Map.of(
                    "id", user.getId(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));
        } else {
            out.put("authenticated", false);
        }
        resp.getWriter().write(gson.toJson(out));
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        String xRequestedWith = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith) ||
                (accept != null && accept.contains("application/json"));
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp, String message, int statusCode, boolean isAjax, String redirectTarget) throws IOException {
        if (isAjax) {
            resp.setStatus(statusCode);
            resp.setContentType("application/json;charset=UTF-8");
            Map<String, Object> out = new HashMap<>();
            out.put("success", false);
            out.put("error", message);
            resp.getWriter().write(gson.toJson(out));
        } else {
            req.getSession(true).setAttribute("authError", message);
            resp.sendRedirect(req.getContextPath() + redirectTarget + "?error=" + java.net.URLEncoder.encode(message, "UTF-8"));
        }
    }
}

