<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // If already logged in, redirect directly to dashboard
    if (session.getAttribute("currentUser") != null) {
        response.sendRedirect("dashboard");
        return;
    }
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartRail AI | Register Controller Account</title>
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

        .auth-container {
            width: 100%;
            max-width: 470px;
            padding: 20px;
            z-index: 10;
        }

        .auth-card {
            background: rgba(10, 20, 40, 0.88);
            border: 1px solid rgba(56, 189, 248, 0.25);
            border-radius: 14px;
            padding: 32px 30px;
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
            margin-bottom: 24px;
        }

        .logo-wrap {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 56px;
            height: 56px;
            border-radius: 14px;
            background: linear-gradient(135deg, rgba(30, 58, 138, 0.5), rgba(15, 23, 42, 0.8));
            border: 1px solid rgba(56, 189, 248, 0.4);
            margin-bottom: 12px;
            box-shadow: 0 0 20px rgba(56, 189, 248, 0.25);
            font-size: 26px;
        }

        .app-title {
            font-size: 1.55rem;
            font-weight: 800;
            letter-spacing: -0.5px;
            color: #FFFFFF;
        }

        .app-title span.glow {
            color: var(--cyan-accent);
            text-shadow: 0 0 16px rgba(56, 189, 248, 0.6);
        }

        .app-subtitle {
            font-size: 0.75rem;
            color: #94A3B8;
            margin-top: 4px;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-family: 'JetBrains Mono', monospace;
        }

        .zone-badge {
            display: inline-block;
            margin-top: 6px;
            padding: 2px 9px;
            background: rgba(16, 185, 129, 0.12);
            border: 1px solid rgba(16, 185, 129, 0.35);
            border-radius: 999px;
            font-size: 0.68rem;
            font-weight: 700;
            color: var(--emerald-accent);
        }

        .alert-banner {
            padding: 10px 14px;
            border-radius: 8px;
            font-size: 0.82rem;
            margin-bottom: 18px;
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
            margin-bottom: 16px;
        }

        .form-label {
            display: block;
            font-size: 0.78rem;
            font-weight: 600;
            color: #CBD5E1;
            margin-bottom: 6px;
        }

        .input-wrap {
            position: relative;
            display: flex;
            align-items: center;
        }

        .input-icon {
            position: absolute;
            left: 12px;
            color: #64748B;
            font-size: 0.9rem;
            pointer-events: none;
        }

        .form-input {
            width: 100%;
            padding: 11px 40px 11px 36px;
            background: rgba(6, 11, 22, 0.75);
            border: 1px solid rgba(56, 189, 248, 0.2);
            border-radius: 8px;
            color: #F8FAFC;
            font-size: 0.86rem;
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

        .input-hint {
            font-size: 0.7rem;
            color: #64748B;
            margin-top: 4px;
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .btn-submit {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #10B981, #059669);
            border: 1px solid rgba(16, 185, 129, 0.5);
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
            margin-top: 20px;
            box-shadow: 0 4px 14px rgba(16, 185, 129, 0.3);
        }

        .btn-submit:hover:not(:disabled) {
            background: linear-gradient(135deg, #34D399, #10B981);
            box-shadow: 0 6px 20px rgba(16, 185, 129, 0.45);
            transform: translateY(-1px);
        }

        .btn-submit:disabled {
            opacity: 0.65;
            cursor: not-allowed;
        }

        .auth-footer {
            margin-top: 20px;
            text-align: center;
            font-size: 0.82rem;
            color: #94A3B8;
        }

        .auth-link {
            color: var(--cyan-accent);
            text-decoration: none;
            font-weight: 600;
        }

        .auth-link:hover {
            color: #7DD3FC;
            text-decoration: underline;
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
    <main class="auth-container">
        <div class="auth-card">
            <!-- Branding -->
            <div class="brand-header">
                <div class="logo-wrap" title="SmartRail AI Registration">
                    🛡️
                </div>
                <h1 class="app-title">SmartRail <span class="glow">AI</span></h1>
                <div class="app-subtitle">Register Railway Traffic Controller</div>
                <div class="zone-badge">Secure Interlocking Authorization</div>
            </div>

            <!-- Server Flash Alerts -->
            <% if (error != null && !error.isEmpty()) { %>
                <div class="alert-banner error" role="alert">
                    <span>⚠️</span>
                    <span><%= error %></span>
                </div>
            <% } %>

            <!-- Dynamic Client Alert Message -->
            <div id="dynamicAlert" class="alert-banner" style="display:none;" role="alert"></div>

            <!-- Sign Up Form -->
            <form id="signupForm" action="auth" method="POST" autocomplete="on">
                <input type="hidden" name="action" value="signup">

                <!-- Full Name -->
                <div class="form-group">
                    <label class="form-label" for="fullName">Full Name</label>
                    <div class="input-wrap">
                        <span class="input-icon">👤</span>
                        <input
                            type="text"
                            id="fullName"
                            name="fullName"
                            class="form-input"
                            placeholder="e.g. S. Ramachandran"
                            required
                            minlength="2"
                            maxlength="100"
                            autocomplete="name"
                        >
                    </div>
                </div>

                <!-- Email -->
                <div class="form-group">
                    <label class="form-label" for="signupEmail">Official Email Address</label>
                    <div class="input-wrap">
                        <span class="input-icon">✉️</span>
                        <input
                            type="email"
                            id="signupEmail"
                            name="email"
                            class="form-input"
                            placeholder="dispatcher@smartrail.gov.in"
                            required
                            autocomplete="username"
                        >
                    </div>
                </div>

                <!-- Password -->
                <div class="form-group">
                    <label class="form-label" for="signupPassword">Password</label>
                    <div class="input-wrap">
                        <span class="input-icon">🔑</span>
                        <input
                            type="password"
                            id="signupPassword"
                            name="password"
                            class="form-input"
                            placeholder="Minimum 6 characters"
                            required
                            minlength="6"
                            autocomplete="new-password"
                        >
                        <button type="button" class="toggle-password" id="togglePasswordBtn1" title="Toggle password visibility">
                            👁️
                        </button>
                    </div>
                    <div class="input-hint">🔒 Passwords are salt-hashed with PBKDF2 HMAC-SHA256.</div>
                </div>

                <!-- Confirm Password -->
                <div class="form-group">
                    <label class="form-label" for="confirmPassword">Confirm Password</label>
                    <div class="input-wrap">
                        <span class="input-icon">🔒</span>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            class="form-input"
                            placeholder="Re-enter password"
                            required
                            minlength="6"
                            autocomplete="new-password"
                        >
                        <button type="button" class="toggle-password" id="togglePasswordBtn2" title="Toggle password visibility">
                            👁️
                        </button>
                    </div>
                    <div id="matchHint" class="input-hint" style="display:none;"></div>
                </div>

                <!-- Submit Button -->
                <button type="submit" id="submitBtn" class="btn-submit">
                    <span id="btnText">Create Controller Account</span>
                    <span id="btnSpinner" class="spinner" style="display:none;"></span>
                </button>
            </form>

            <!-- Footer Link to Login -->
            <div class="auth-footer">
                Already registered as a controller?
                <a href="login.jsp" class="auth-link">Login Here</a>
            </div>
        </div>
    </main>

    <script>
        // Password Visibility Toggles
        function setupToggle(btnId, inputId) {
            const btn = document.getElementById(btnId);
            const input = document.getElementById(inputId);
            btn.addEventListener('click', function() {
                const isPassword = input.getAttribute('type') === 'password';
                input.setAttribute('type', isPassword ? 'text' : 'password');
                btn.textContent = isPassword ? '🙈' : '👁️';
            });
        }
        setupToggle('togglePasswordBtn1', 'signupPassword');
        setupToggle('togglePasswordBtn2', 'confirmPassword');

        // Real-time password match check
        const pwdInput = document.getElementById('signupPassword');
        const confirmInput = document.getElementById('confirmPassword');
        const matchHint = document.getElementById('matchHint');

        function checkMatch() {
            if (!confirmInput.value) {
                matchHint.style.display = 'none';
                return;
            }
            matchHint.style.display = 'flex';
            if (pwdInput.value === confirmInput.value) {
                matchHint.textContent = '✅ Passwords match';
                matchHint.style.color = '#34D399';
            } else {
                matchHint.textContent = '❌ Passwords do not match';
                matchHint.style.color = '#F87171';
            }
        }
        pwdInput.addEventListener('input', checkMatch);
        confirmInput.addEventListener('input', checkMatch);

        // Form Submit Handler
        const signupForm = document.getElementById('signupForm');
        const submitBtn = document.getElementById('submitBtn');
        const btnText = document.getElementById('btnText');
        const btnSpinner = document.getElementById('btnSpinner');
        const dynamicAlert = document.getElementById('dynamicAlert');

        signupForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const fullName = document.getElementById('fullName').value.trim();
            const email = document.getElementById('signupEmail').value.trim();
            const password = pwdInput.value;
            const confirmPassword = confirmInput.value;

            if (fullName.length < 2) {
                showAlert('Full name must be at least 2 characters.', 'error');
                return;
            }

            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                showAlert('Please enter a valid email address.', 'error');
                return;
            }

            if (password.length < 6) {
                showAlert('Password must be at least 6 characters long.', 'error');
                return;
            }

            if (password !== confirmPassword) {
                showAlert('Passwords do not match.', 'error');
                return;
            }

            submitBtn.disabled = true;
            btnText.textContent = 'Registering Account...';
            btnSpinner.style.display = 'inline-block';
            dynamicAlert.style.display = 'none';

            try {
                const response = await fetch('auth', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'Accept': 'application/json'
                    },
                    body: 'action=signup&fullName=' + encodeURIComponent(fullName) +
                          '&email=' + encodeURIComponent(email) +
                          '&password=' + encodeURIComponent(password) +
                          '&confirmPassword=' + encodeURIComponent(confirmPassword)
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    showAlert(data.message || 'Account registered! Opening control room...', 'success');
                    setTimeout(() => {
                        window.location.href = data.redirectUrl || 'dashboard';
                    }, 500);
                } else {
                    showAlert(data.error || 'Failed to create account. Please check your information.', 'error');
                    submitBtn.disabled = false;
                    btnText.textContent = 'Create Controller Account';
                    btnSpinner.style.display = 'none';
                }
            } catch (err) {
                signupForm.submit();
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
