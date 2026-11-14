<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // If already logged in, redirect directly to dashboard
    if (session.getAttribute("currentUser") != null) {
        response.sendRedirect("dashboard");
        return;
    }
    String error = request.getParameter("error");
    String logout = request.getParameter("logout");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartRail AI | Traffic Controller Sign In</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/variables.css">
    <style>
        :root {
            --bg-deep: #050A18;
            --bg-card: #0A1428;
            --border-glow: #1E3A8A;
            --cyan-accent: #38BDF8;
            --amber-accent: #F59E0B;
            --emerald-accent: #10B981;
            --rose-accent: #EF4444;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
            background-color: var(--bg-deep);
            color: #E2E8F0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            overflow-x: hidden;
            background-image:
                radial-gradient(circle at 15% 20%, rgba(56, 189, 248, 0.08) 0%, transparent 40%),
                radial-gradient(circle at 85% 80%, rgba(245, 158, 11, 0.06) 0%, transparent 40%),
                linear-gradient(to right, rgba(30, 58, 138, 0.1) 1px, transparent 1px),
                linear-gradient(to bottom, rgba(30, 58, 138, 0.1) 1px, transparent 1px);
            background-size: 100% 100%, 100% 100%, 40px 40px, 40px 40px;
        }

        /* Ambient railway tracks animation background */
        .track-ambient-line {
            position: absolute;
            left: 0;
            width: 100%;
            height: 2px;
            background: linear-gradient(90deg, transparent 0%, rgba(56, 189, 248, 0.3) 50%, transparent 100%);
            pointer-events: none;
        }
        .line-1 { top: 22%; animation: pulseTrack 4s ease-in-out infinite; }
        .line-2 { bottom: 18%; animation: pulseTrack 5s ease-in-out infinite 1.5s; }

        @keyframes pulseTrack {
            0%, 100% { opacity: 0.2; transform: scaleX(0.95); }
            50% { opacity: 0.7; transform: scaleX(1); }
        }

        .auth-container {
            width: 100%;
            max-width: 450px;
            padding: 20px;
            z-index: 10;
        }

        .auth-card {
            background: rgba(10, 20, 40, 0.88);
            border: 1px solid rgba(56, 189, 248, 0.25);
            border-radius: 14px;
            padding: 36px 32px;
            backdrop-filter: blur(14px);
            -webkit-backdrop-filter: blur(14px);
            box-shadow: 0 20px 45px -10px rgba(0, 0, 0, 0.7), 0 0 35px rgba(56, 189, 248, 0.08);
            position: relative;
        }

        .auth-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 15%;
            right: 15%;
            height: 2px;
            background: linear-gradient(90deg, transparent, var(--cyan-accent), transparent);
        }

        .brand-header {
            text-align: center;
            margin-bottom: 28px;
        }

        .logo-wrap {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 62px;
            height: 62px;
            border-radius: 14px;
            background: linear-gradient(135deg, rgba(30, 58, 138, 0.5), rgba(15, 23, 42, 0.8));
            border: 1px solid rgba(56, 189, 248, 0.4);
            margin-bottom: 14px;
            box-shadow: 0 0 20px rgba(56, 189, 248, 0.25);
            font-size: 28px;
        }

        .app-title {
            font-size: 1.65rem;
            font-weight: 800;
            letter-spacing: -0.5px;
            color: #FFFFFF;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .app-title span.glow {
            color: var(--cyan-accent);
            text-shadow: 0 0 16px rgba(56, 189, 248, 0.6);
        }

        .app-subtitle {
            font-size: 0.76rem;
            color: #94A3B8;
            margin-top: 6px;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-family: 'JetBrains Mono', monospace;
        }

        .zone-badge {
            display: inline-block;
            margin-top: 8px;
            padding: 3px 10px;
            background: rgba(245, 158, 11, 0.12);
            border: 1px solid rgba(245, 158, 11, 0.35);
            border-radius: 999px;
            font-size: 0.7rem;
            font-weight: 700;
            color: var(--amber-accent);
            letter-spacing: 0.5px;
        }

        /* Alerts & Status Messages */
        .alert-banner {
            padding: 11px 14px;
            border-radius: 8px;
            font-size: 0.82rem;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
            animation: fadeIn 0.3s ease;
        }

        .alert-banner.error {
            background: rgba(239, 68, 68, 0.12);
            border: 1px solid rgba(239, 68, 68, 0.4);
            color: #FCA5A5;
        }

        .alert-banner.success {
            background: rgba(16, 185, 129, 0.12);
            border: 1px solid rgba(16, 185, 129, 0.4);
            color: #6EE7B7;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-6px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-label {
            display: block;
            font-size: 0.8rem;
            font-weight: 600;
            color: #CBD5E1;
            margin-bottom: 7px;
            letter-spacing: 0.2px;
        }

        .input-wrap {
            position: relative;
            display: flex;
            align-items: center;
        }

        .input-icon {
            position: absolute;
            left: 13px;
            color: #64748B;
            font-size: 0.95rem;
            pointer-events: none;
            display: flex;
            align-items: center;
        }

        .form-input {
            width: 100%;
            padding: 12px 42px 12px 38px;
            background: rgba(6, 11, 22, 0.75);
            border: 1px solid rgba(56, 189, 248, 0.2);
            border-radius: 8px;
            color: #F8FAFC;
            font-size: 0.88rem;
            font-family: inherit;
            transition: all 0.2s ease;
            outline: none;
        }

        .form-input:focus {
            border-color: var(--cyan-accent);
            box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.15);
            background: rgba(6, 11, 22, 0.95);
        }

        .toggle-password {
            position: absolute;
            right: 12px;
            background: none;
            border: none;
            color: #64748B;
            cursor: pointer;
            padding: 4px;
            font-size: 0.95rem;
            display: flex;
            align-items: center;
            transition: color 0.2s ease;
        }

        .toggle-password:hover {
            color: var(--cyan-accent);
        }

        .btn-submit {
            width: 100%;
            padding: 13px;
            background: linear-gradient(135deg, #0284C7, #0369A1);
            border: 1px solid rgba(56, 189, 248, 0.5);
            border-radius: 8px;
            color: #FFFFFF;
            font-size: 0.92rem;
            font-weight: 700;
            letter-spacing: 0.3px;
            cursor: pointer;
            transition: all 0.2s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-top: 24px;
            box-shadow: 0 4px 14px rgba(2, 132, 199, 0.3);
        }

        .btn-submit:hover:not(:disabled) {
            background: linear-gradient(135deg, #0EA5E9, #0284C7);
            box-shadow: 0 6px 20px rgba(14, 165, 233, 0.45);
            transform: translateY(-1px);
        }

        .btn-submit:active {
            transform: translateY(0);
        }

        .btn-submit:disabled {
            opacity: 0.65;
            cursor: not-allowed;
        }

        .auth-footer {
            margin-top: 24px;
            text-align: center;
            font-size: 0.82rem;
            color: #94A3B8;
        }

        .auth-link {
            color: var(--cyan-accent);
            text-decoration: none;
            font-weight: 600;
            transition: color 0.2s ease;
        }

        .auth-link:hover {
            color: #7DD3FC;
            text-decoration: underline;
        }

        /* Demo credentials pill */
        .demo-pill-box {
            margin-top: 20px;
            padding: 10px 14px;
            background: rgba(15, 23, 42, 0.65);
            border: 1px dashed rgba(56, 189, 248, 0.3);
            border-radius: 8px;
            font-size: 0.74rem;
            color: #94A3B8;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .demo-pill-box button {
            background: rgba(56, 189, 248, 0.15);
            border: 1px solid rgba(56, 189, 248, 0.4);
            color: var(--cyan-accent);
            padding: 4px 8px;
            border-radius: 5px;
            font-size: 0.7rem;
            font-weight: 700;
            cursor: pointer;
            transition: background 0.2s;
        }

        .demo-pill-box button:hover {
            background: rgba(56, 189, 248, 0.3);
        }

        .spinner {
            display: inline-block;
            width: 16px;
            height: 16px;
            border: 2px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: #FFF;
            animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="track-ambient-line line-1"></div>
    <div class="track-ambient-line line-2"></div>

    <main class="auth-container">
        <div class="auth-card">
            <!-- Branding -->
            <div class="brand-header">
                <div class="logo-wrap" title="SmartRail AI Central Dispatch">
                    🚆
                </div>
                <h1 class="app-title">SmartRail <span class="glow">AI</span></h1>
                <div class="app-subtitle">Intelligent Railway Traffic Controller</div>
                <div class="zone-badge">Southern Railway • Tamil Nadu Operations</div>
            </div>

            <!-- Server Flash Alerts -->
            <% if ("unauthorized".equals(error)) { %>
                <div class="alert-banner error" role="alert">
                    <span>🔒</span>
                    <span>Session required. Please sign in to access the control room.</span>
                </div>
            <% } else if ("invalid_credentials".equals(error) || (error != null && !error.isEmpty())) { %>
                <div class="alert-banner error" role="alert">
                    <span>⚠️</span>
                    <span><%= error.equals("invalid_credentials") ? "Invalid email address or password." : error %></span>
                </div>
            <% } else if ("true".equals(logout)) { %>
                <div class="alert-banner success" role="status">
                    <span>✅</span>
                    <span>Controller signed out successfully. Safety locks engaged.</span>
                </div>
            <% } %>

            <!-- Dynamic Client Alert Message -->
            <div id="dynamicAlert" class="alert-banner" style="display:none;" role="alert"></div>

            <!-- Login Form -->
            <form id="loginForm" action="auth" method="POST" autocomplete="on">
                <input type="hidden" name="action" value="login">

                <!-- Email -->
                <div class="form-group">
                    <label class="form-label" for="loginEmail">Controller Email Address</label>
                    <div class="input-wrap">
                        <span class="input-icon">✉️</span>
                        <input
                            type="email"
                            id="loginEmail"
                            name="email"
                            class="form-input"
                            placeholder="controller@smartrail.gov.in"
                            required
                            autocomplete="username"
                        >
                    </div>
                </div>

                <!-- Password -->
                <div class="form-group">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:7px;">
                        <label class="form-label" for="loginPassword" style="margin-bottom:0;">Access Password</label>
                    </div>
                    <div class="input-wrap">
                        <span class="input-icon">🔑</span>
                        <input
                            type="password"
                            id="loginPassword"
                            name="password"
                            class="form-input"
                            placeholder="Enter controller password"
                            required
                            autocomplete="current-password"
                        >
                        <button type="button" class="toggle-password" id="togglePasswordBtn" title="Toggle password visibility">
                            👁️
                        </button>
                    </div>
                </div>

                <!-- Submit Button -->
                <button type="submit" id="submitBtn" class="btn-submit">
                    <span id="btnText">Sign In to Control Room</span>
                    <span id="btnSpinner" class="spinner" style="display:none;"></span>
                </button>
            </form>

            <!-- Demo Controller Pill -->
            <div class="demo-pill-box">
                <div>
                    <strong style="color:#CBD5E1;">Quick Test Account:</strong><br>
                    <span style="font-family:'JetBrains Mono', monospace; color:var(--cyan-accent);">controller@smartrail.gov.in</span>
                </div>
                <button type="button" onclick="fillDemoCredentials()">Quick Fill</button>
            </div>

            <!-- Footer Link to Sign Up -->
            <div class="auth-footer">
                Don't have a controller account?
                <a href="signup.jsp" class="auth-link">Sign Up / Register</a>
            </div>
        </div>
    </main>

    <script>
        // Password Visibility Toggle
        const toggleBtn = document.getElementById('togglePasswordBtn');
        const passwordInput = document.getElementById('loginPassword');

        toggleBtn.addEventListener('click', function() {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            toggleBtn.textContent = isPassword ? '🙈' : '👁️';
        });

        // Quick Fill Demo Credentials
        function fillDemoCredentials() {
            document.getElementById('loginEmail').value = 'controller@smartrail.gov.in';
            document.getElementById('loginPassword').value = 'SmartRail@2026';
        }

        // AJAX Form Submission for Smooth Transition
        const loginForm = document.getElementById('loginForm');
        const submitBtn = document.getElementById('submitBtn');
        const btnText = document.getElementById('btnText');
        const btnSpinner = document.getElementById('btnSpinner');
        const dynamicAlert = document.getElementById('dynamicAlert');

        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const email = document.getElementById('loginEmail').value.trim();
            const password = document.getElementById('loginPassword').value;

            if (!email || !password) {
                showAlert('Please provide both email and password.', 'error');
                return;
            }

            // Show loading state
            submitBtn.disabled = true;
            btnText.textContent = 'Verifying Credentials...';
            btnSpinner.style.display = 'inline-block';
            dynamicAlert.style.display = 'none';

            try {
                const response = await fetch('auth', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'Accept': 'application/json'
                    },
                    body: 'action=login&email=' + encodeURIComponent(email) + '&password=' + encodeURIComponent(password)
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    showAlert('Authentication successful! Initializing railway telemetry...', 'success');
                    setTimeout(() => {
                        window.location.href = data.redirectUrl || 'dashboard';
                    }, 500);
                } else {
                    showAlert(data.error || 'Invalid credentials. Access denied.', 'error');
                    submitBtn.disabled = false;
                    btnText.textContent = 'Sign In to Control Room';
                    btnSpinner.style.display = 'none';
                }
            } catch (err) {
                // Fallback to standard form post
                loginForm.submit();
            }
        });

        function showAlert(msg, type) {
            dynamicAlert.textContent = msg;
            dynamicAlert.className = `alert-banner ${type}`;
            dynamicAlert.style.display = 'flex';
        }
    </script>
</body>
</html>
