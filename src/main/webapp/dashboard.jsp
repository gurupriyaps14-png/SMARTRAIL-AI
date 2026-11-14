<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.smartrail.model.Division" %>
<%@ page import="com.smartrail.model.Station" %>
<%@ page import="com.smartrail.model.Track" %>
<%@ page import="com.smartrail.model.Train" %>
<%@ page import="com.smartrail.model.User" %>
<%@ page import="com.google.gson.Gson" %>
<%
    User authUser = (User) session.getAttribute("currentUser");
    List<Division> divisions = (List<Division>) request.getAttribute("divisions");
    List<Station> stations = (List<Station>) request.getAttribute("stations");
    String selectedDivision = (String) request.getAttribute("selectedDivision");
    Integer selectedStationId = (Integer) request.getAttribute("selectedStationId");
    Station currentStation = (Station) request.getAttribute("currentStation");
    List<Track> tracks = (List<Track>) request.getAttribute("tracks");
    List<Train> trains = (List<Train>) request.getAttribute("trains");
    Map<String, Object> telemetry = (Map<String, Object>) request.getAttribute("telemetry");
    String dbType = (String) request.getAttribute("dbType");

    Gson gson = new Gson();
    String initialJson = "{}";
    try {
        java.util.Map<String, Object> initialMap = new java.util.HashMap<>();
        initialMap.put("divisions", divisions);
        initialMap.put("stations", stations);
        initialMap.put("selectedDivision", selectedDivision);
        initialMap.put("selectedStationId", selectedStationId);
        initialMap.put("currentStation", currentStation);
        initialMap.put("tracks", tracks);
        initialMap.put("trains", trains);
        initialMap.put("telemetry", telemetry);
        initialJson = gson.toJson(initialMap);
    } catch (Exception e) {
        initialJson = "{}";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartRail AI | Railway Traffic Monitoring &amp; Control Room</title>
    <!-- Stylesheets -->
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/loading.css">
    <link rel="stylesheet" href="css/dashboard.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="css/track-display.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>

    <!-- ==========================================================================
         1. LOADING / OPENING SCREEN (Indian Railway-Themed Animation)
         ========================================================================== -->
    <div id="loadingScreen" class="loading-screen">
        <div class="loading-bg-grid"></div>
        <div class="loading-scan-line"></div>

        <!-- Branding -->
        <div class="loading-brand">
            <div class="loading-logo-box">
                <div class="loading-logo-ring"></div>
                <div class="loading-logo-ring-outer"></div>
                <!-- SmartRail AI SVG Logo -->
                <svg width="44" height="44" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 12C8 9.79 9.79 8 12 8H36C38.21 8 40 9.79 40 12V32C40 34.21 38.21 36 36 36H12C9.79 36 8 34.21 8 32V12Z" stroke="#F59E0B" stroke-width="2.5"/>
                    <path d="M12 14H36V22H12V14Z" fill="#06B6D4" fill-opacity="0.3" stroke="#06B6D4" stroke-width="1.5"/>
                    <circle cx="16" cy="30" r="2.5" fill="#F59E0B"/>
                    <circle cx="32" cy="30" r="2.5" fill="#F59E0B"/>
                    <path d="M6 42L14 36M42 42L34 36" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round"/>
                    <path d="M20 40H28" stroke="#38BDF8" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="24" cy="18" r="1.5" fill="#FFFFFF"/>
                </svg>
            </div>
            <h1 class="loading-title">SMARTRAIL AI</h1>
            <div class="loading-subtitle">Intelligent Railway Traffic Monitoring &amp; Control</div>
            <div class="loading-zone-tag">SOUTHERN RAILWAY | TAMIL NADU DIVISION DISPATCH</div>
        </div>

        <!-- Indian Railway Train Moving Stage Animation -->
        <div class="railway-stage">
            <div class="catenary-wire"></div>
            <div class="catenary-poles">
                <div class="catenary-pole"></div>
                <div class="catenary-pole"></div>
                <div class="catenary-pole"></div>
                <div class="catenary-pole"></div>
                <div class="catenary-pole"></div>
                <div class="catenary-pole"></div>
                <div class="catenary-pole"></div>
                <div class="catenary-pole"></div>
            </div>

            <!-- Moving Indian Railway Train (WAP-7 / Vande Bharat Style) -->
            <div class="loading-train">
                <!-- Headlight Cone -->
                <div class="train-headlight-beam"></div>

                <!-- Locomotive Engine -->
                <div class="loco-engine">
                    <div class="loco-pantograph"></div>
                    <div class="loco-cab"></div>
                    <div class="loco-tricolor"></div>
                    <div class="train-wheelset">
                        <div class="train-wheel"></div>
                        <div class="train-wheel"></div>
                        <div class="train-wheel"></div>
                    </div>
                </div>

                <div class="train-coupler"></div>

                <!-- Passenger Coaches -->
                <div class="train-coach">
                    <div class="coach-windows">
                        <div class="coach-window"></div>
                        <div class="coach-window"></div>
                        <div class="coach-window"></div>
                        <div class="coach-window"></div>
                    </div>
                    <div class="coach-stripe"></div>
                    <div class="train-wheelset">
                        <div class="train-wheel"></div>
                        <div class="train-wheel"></div>
                    </div>
                </div>

                <div class="train-coupler"></div>

                <div class="train-coach">
                    <div class="coach-windows">
                        <div class="coach-window"></div>
                        <div class="coach-window"></div>
                        <div class="coach-window"></div>
                        <div class="coach-window"></div>
                    </div>
                    <div class="coach-stripe"></div>
                    <div class="train-wheelset">
                        <div class="train-wheel"></div>
                        <div class="train-wheel"></div>
                    </div>
                </div>
            </div>

            <!-- Railway Track Base -->
            <div class="loading-track">
                <div class="track-rail-top"></div>
                <div class="track-sleepers"></div>
                <div class="track-rail-bottom"></div>
            </div>
        </div>

        <!-- Progress Indicator -->
        <div class="loading-progress-box">
            <div class="loading-progress-bar-wrap">
                <div id="loadingProgressBar" class="loading-progress-bar"></div>
            </div>
            <div class="loading-status-row">
                <div class="loading-status-text">
                    <span class="loading-status-dot"></span>
                    <span id="loadingStatusText">Connecting to Tamil Nadu Division Dispatch...</span>
                </div>
                <div id="loadingPercent" class="loading-percent">0%</div>
            </div>
        </div>

        <button id="skipLoadingBtn" class="loading-skip-btn">Skip Intro ➔</button>
    </div>

    <!-- ==========================================================================
         2. MAIN DASHBOARD LAYOUT (Railway Control-Room Interface)
         ========================================================================== -->
    <div class="app-wrapper">

        <!-- LEFT SIDEBAR -->
        <aside class="sidebar" id="mainSidebar">
            <!-- Sidebar Collapse / Expand Toggle Arrow Button -->
            <button id="sidebarToggleBtn" class="sidebar-toggle-btn" onclick="toggleSidebar()" aria-label="Toggle navigation sidebar" title="Collapse Sidebar">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="15 18 9 12 15 6"></polyline>
                </svg>
            </button>

            <div class="sidebar-header">
                <div class="brand-row">
                    <div class="brand-emblem-box">
                        <!-- SmartRail AI Logo -->
                        <svg class="brand-logo-svg" viewBox="0 0 48 48" fill="none">
                            <rect x="6" y="8" width="36" height="28" rx="6" stroke="#F59E0B" stroke-width="2.5" fill="#0B1325"/>
                            <rect x="10" y="12" width="28" height="10" rx="3" fill="#06B6D4" fill-opacity="0.3" stroke="#06B6D4" stroke-width="1.5"/>
                            <circle cx="16" cy="28" r="3" fill="#F59E0B"/>
                            <circle cx="32" cy="28" r="3" fill="#F59E0B"/>
                            <path d="M4 42L12 36M44 42L36 36" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                        <!-- Indian Railways Inspired Emblem -->
                        <svg class="ir-logo-svg" viewBox="0 0 36 36" fill="none">
                            <circle cx="18" cy="18" r="16" stroke="#06B6D4" stroke-width="1.8"/>
                            <circle cx="18" cy="18" r="9" stroke="#F59E0B" stroke-width="1.5" stroke-dasharray="2 2"/>
                            <path d="M18 2V34M2 18H34M7 7L29 29M7 29L29 7" stroke="rgba(245,158,11,0.5)" stroke-width="1"/>
                            <circle cx="18" cy="18" r="4" fill="#F59E0B"/>
                        </svg>
                    </div>
                    <div class="brand-text">
                        <div class="brand-title">SmartRail <span>AI</span></div>
                        <div class="brand-subtitle">Control &amp; Dispatch</div>
                    </div>
                </div>

                <div class="division-badge-tag">
                    <span>Active Territory:</span>
                    <strong>TAMIL NADU (SR)</strong>
                </div>
            </div>

            <!-- Navigation Sections with Expandable Arrows -->
            <nav class="sidebar-nav">
                <div class="nav-section-title">Operations (Functional)</div>

                <div class="nav-item active" data-page="dashboard">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                        </svg>
                        <span>Dashboard</span>
                    </div>
                    <svg class="nav-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </div>

                <div class="nav-item" data-page="monitoring">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                        </svg>
                        <span>Live Train Monitoring</span>
                    </div>
                    <span class="nav-tag-badge tag-active">Live</span>
                </div>

                <div class="nav-section-title">Asset &amp; Movement Control</div>

                <div class="nav-item" data-page="train-mgmt">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                        </svg>
                        <span>Train Management</span>
                    </div>
                    <svg class="nav-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </div>

                <div class="nav-item" data-page="station-mgmt">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                        </svg>
                        <span>Station Management</span>
                    </div>
                    <svg class="nav-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </div>

                <div class="nav-item" data-page="track-mgmt">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
                        </svg>
                        <span>Track Management</span>
                    </div>
                    <svg class="nav-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </div>

                <div class="nav-item" data-page="scheduling">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <span>Train Scheduling</span>
                    </div>
                    <svg class="nav-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </div>

                <div class="nav-section-title">Intelligent Systems</div>

                <div class="nav-item" data-page="ai-dispatch">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                        </svg>
                        <span>AI Dispatch</span>
                    </div>
                    <span class="nav-tag-badge tag-phase2">Phase 2</span>
                </div>

                <div class="nav-item" data-page="alerts">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                        </svg>
                        <span>Alerts &amp; Notifications</span>
                    </div>
                    <svg class="nav-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </div>

                <div class="nav-item" data-page="analytics">
                    <div class="nav-item-left">
                        <svg class="nav-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M16 8v8m-4-5v5m-4-2v2m-2 4h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <span>Analytics</span>
                    </div>
                    <svg class="nav-arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </div>
            </nav>

            <div class="sidebar-footer">
                <div style="background:rgba(15,23,42,0.7); border:1px solid rgba(56,189,248,0.25); border-radius:8px; padding:10px; margin-bottom:10px; text-align:left;">
                    <div style="font-size:0.65rem; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.5px;">Active Operator</div>
                    <div style="font-size:0.78rem; font-weight:700; color:#38BDF8; margin-top:2px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                        <%= authUser != null ? authUser.getFullName() : "Chief Dispatcher" %>
                    </div>
                    <div style="font-size:0.66rem; color:var(--amber-light); margin-top:1px;">
                        <%= authUser != null ? authUser.getRole() : "TRAIN_CONTROLLER" %>
                    </div>
                    <div style="margin-top:6px; border-top:1px solid rgba(255,255,255,0.08); padding-top:6px;">
                        <a href="auth?action=logout" style="color:#F87171; font-size:0.72rem; text-decoration:none; font-weight:700; display:inline-flex; align-items:center; gap:4px;" title="Sign out of console">
                            <span>🚪 Sign Out</span>
                        </a>
                    </div>
                </div>
                <div class="sys-health">INTERLOCKING NOMINAL</div>
                <div>Engine: <%= dbType %></div>
                <div style="color:var(--text-muted); font-size:0.68rem;">SmartRail AI &copy; 2026 SR Tamil Nadu</div>
            </div>
        </aside>

        <!-- MAIN VIEWPORT -->
        <main class="main-content">

            <!-- Top Header -->
            <header class="top-header">
                <div class="header-left">
                    <h2 class="page-title">
                        <span style="color:var(--amber-primary)">Tamil Nadu Central Dispatch</span>
                        <span style="color:var(--border-light)">|</span>
                        <span style="font-size:0.95rem; color:var(--text-secondary);" id="currentStationBadge">
                            <%= currentStation != null ? currentStation.getName() : "Chennai Central" %> (<%= currentStation != null ? currentStation.getCode() : "MAS" %>)
                        </span>
                    </h2>

                    <!-- Required: Clearly distinguish simulated data from actual railway live data -->
                    <div class="simulation-pill-banner">
                        <span class="sim-pulse-dot"></span>
                        <span>[SIMULATION MODE] Tamil Nadu Zone - Simulated Telemetry</span>
                    </div>
                </div>

                <div class="header-right">
                    <!-- View Switcher (Direct Console Access) -->
                    <div style="display:flex; background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:2px; gap:4px; flex-wrap:wrap;">
                        <button id="viewTrackBtn" class="btn btn-sm btn-teal" onclick="switchView('track')" title="Visual Track Simulation">
                            <span>🚆 Track Display</span>
                        </button>
                        <button id="viewMonitoringBtn" class="btn btn-sm btn-outline" style="border:none;" onclick="switchView('monitoring')" title="Live Telemetry Radar">
                            <span>📡 Live Radar</span>
                        </button>
                        <button id="viewAnalyticsBtn" class="btn btn-sm btn-outline" style="border:none;" onclick="switchView('analytics')" title="Chart.js Telemetry Analytics">
                            <span>📊 Analytics</span>
                        </button>
                        <button id="viewAlertsBtn" class="btn btn-sm btn-outline" style="border:none;" onclick="switchView('alerts')" title="Alert & Incident History">
                            <span>🚨 Alert History</span>
                        </button>
                        <button id="viewSchedulingBtn" class="btn btn-sm btn-outline" style="border:none;" onclick="switchView('scheduling')" title="Timetables & Schedules">
                            <span>🕒 Schedules</span>
                        </button>
                    </div>

                    <!-- Live Digital Clock (IST UTC+05:30) -->
                    <div class="digital-clock-box">
                        <div id="digitalClock" class="clock-time">00:00:00</div>
                        <div id="digitalDate" class="clock-zone">IST UTC+05:30</div>
                    </div>

                    <!-- Controller Profile Badge & Quick Logout -->
                    <div style="display:flex; align-items:center; gap:8px; background:rgba(6,11,22,0.85); border:1px solid rgba(56,189,248,0.25); border-radius:var(--radius-sm); padding:4px 10px;">
                        <span style="font-size:0.9rem;">👤</span>
                        <div style="display:flex; flex-direction:column; line-height:1.2;">
                            <span style="font-size:0.75rem; font-weight:700; color:#F8FAFC;"><%= authUser != null ? authUser.getFullName() : "Chief Dispatcher" %></span>
                            <span style="font-size:0.65rem; color:var(--cyan-light);"><%= authUser != null ? authUser.getRole() : "CONTROLLER" %></span>
                        </div>
                        <a href="auth?action=logout" class="btn btn-sm btn-outline" style="border-color:rgba(239,68,68,0.4); color:#F87171; text-decoration:none; padding:4px 8px; font-size:0.72rem; margin-left:4px;" title="Logout and lock dispatch console">
                            <span>🚪 Logout</span>
                        </a>
                    </div>
                </div>
            </header>

            <!-- 4. FILTER SYSTEM (Tamil Nadu Division, Station, Time) -->
            <section class="filters-bar">
                <div class="filters-group">
                    <!-- Filter 1: City / Railway Division -->
                    <div class="filter-item">
                        <label class="filter-label" for="filterDivision">
                            <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                            </svg>
                            Division:
                        </label>
                        <select id="filterDivision" class="control-select">
                            <% if (divisions != null) {
                                for (Division d : divisions) { %>
                                    <option value="<%= d.getCode() %>" <%= d.getCode().equalsIgnoreCase(selectedDivision) ? "selected" : "" %>>
                                        <%= d.getName() %> (<%= d.getCode() %>)
                                    </option>
                            <%  }
                               } %>
                        </select>
                    </div>

                    <!-- Filter 2: Station (Dynamically updates based on division) -->
                    <div class="filter-item">
                        <label class="filter-label" for="filterStation">
                            <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16" />
                            </svg>
                            Station:
                        </label>
                        <select id="filterStation" class="control-select">
                            <% if (stations != null) {
                                for (Station s : stations) { %>
                                    <option value="<%= s.getId() %>" <%= (selectedStationId != null && s.getId() == selectedStationId) ? "selected" : "" %>>
                                        <%= s.getName() %> (<%= s.getCode() %>)
                                    </option>
                            <%  }
                               } %>
                        </select>
                    </div>

                    <!-- Filter 3: Time Slot / Window -->
                    <div class="filter-item">
                        <label class="filter-label" for="filterTime">
                            <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            Time:
                        </label>
                        <select id="filterTime" class="control-select">
                            <option value="ALL">All Operational Hours</option>
                            <option value="NIGHT" selected>Current (Night 20:00 - 00:00)</option>
                            <option value="MORNING">Morning Peak (06:00 - 10:00)</option>
                            <option value="MIDDAY">Midday Express (10:00 - 14:00)</option>
                            <option value="EVENING">Evening Peak (16:00 - 20:00)</option>
                        </select>
                    </div>

                    <!-- Train Category Filter -->
                    <div class="filter-item">
                        <label class="filter-label" for="filterCategory">Category:</label>
                        <select id="filterCategory" class="control-select">
                            <option value="ALL">All Categories</option>
                            <option value="EXPRESS">Express</option>
                            <option value="PASSENGER">Passenger</option>
                            <option value="GOODS">Goods</option>
                            <option value="FREIGHT">Freight</option>
                        </select>
                    </div>
                </div>

                <!-- Clean Controller Controls: Pause/Resume + Simulation Settings & Testing -->
                <div class="simulation-controls-group">
                    <button id="simPauseResumeBtn" class="btn btn-outline btn-sm" onclick="window.togglePauseResume()" title="Pause or Resume live train simulation">
                        <span>⏸ Pause Simulation</span>
                    </button>
                    <button id="simSettingsBtn" class="btn btn-outline btn-sm" onclick="openModal('simulationSettingsModal')" title="Open Simulation Testing, Conflict &amp; Reset Settings">
                        <span>⚙️ Simulation Settings</span>
                    </button>
                </div>
            </section>

            <!-- Dashboard Workspace (Central Track Display + Right Info Panel) -->
            <div class="dashboard-workspace">

                <!-- Central Column: Telemetry KPI Cards + Visual Railway Track Simulation -->
                <div class="track-viewport-container">

                    <!-- VIEW 1: Central Dashboard & Visual Track Display -->
                    <div id="dashboardViewContainer">
                        <!-- Telemetry KPI Cards -->
                        <div class="kpi-row">
                            <div class="kpi-card">
                                <div class="kpi-left">
                                    <span class="kpi-title">Active Section Trains</span>
                                    <span id="kpiActiveTrains" class="kpi-value"><%= telemetry != null ? telemetry.get("totalTrains") : 6 %></span>
                                    <span class="kpi-sub">Tamil Nadu Division</span>
                                </div>
                                <div class="kpi-icon-box kpi-blue">🚆</div>
                            </div>

                            <div class="kpi-card">
                                <div class="kpi-left">
                                    <span class="kpi-title">Section Punctuality</span>
                                    <span id="kpiPunctuality" class="kpi-value"><%= telemetry != null ? telemetry.get("punctualityPercent") : "94.2" %>%</span>
                                    <span class="kpi-sub" style="color:#34D399;">Nominal Dispatch</span>
                                </div>
                                <div class="kpi-icon-box kpi-green">⏱️</div>
                            </div>

                            <div class="kpi-card">
                                <div class="kpi-left">
                                    <span class="kpi-title">Average Transit Delay</span>
                                    <span id="kpiAvgDelay" class="kpi-value"><%= telemetry != null ? telemetry.get("avgDelayMinutes") : "4.8" %>m</span>
                                    <span class="kpi-sub" style="color:var(--amber-light);">Within Tolerance</span>
                                </div>
                                <div class="kpi-icon-box kpi-amber">⏳</div>
                            </div>

                            <div class="kpi-card">
                                <div class="kpi-left">
                                    <span class="kpi-title">Signal Interlocking</span>
                                    <span id="kpiSignalStatus" class="kpi-value" style="font-size:1.15rem; color:#22D3EE;">NOMINAL</span>
                                    <span class="kpi-sub" style="color:var(--teal-primary);">All Line Relays Clear</span>
                                </div>
                                <div class="kpi-icon-box kpi-teal">🟢</div>
                            </div>
                        </div>

                        <!-- 3. MAIN RAILWAY TRACK DISPLAY (Visual Simulation) -->
                        <div class="track-simulation-wrapper" style="margin-top:18px;">
                            <!-- Simulation Header -->
                            <div class="track-sim-header">
                                <div class="sim-header-left">
                                    <div class="sim-mode-badge">
                                        <span class="sim-active-dot"></span>
                                        <span>CTC CONTROL ROOM DISPLAY</span>
                                    </div>
                                    <div class="sim-station-title">
                                        <span id="simCanvasStationName"><%= currentStation != null ? currentStation.getName() : "Puratchi Thalaivar Dr. MGR Chennai Central" %></span>
                                        <span id="simCanvasDivision" class="sim-station-division">[<%= selectedDivision %> DIVISION]</span>
                                    </div>
                                </div>

                                <div class="sim-header-right">
                                    <!-- Legend -->
                                    <div class="sim-legend">
                                        <div class="legend-item"><span class="legend-swatch swatch-main"></span> Up Main</div>
                                        <div class="legend-item"><span class="legend-swatch swatch-main" style="background:#0284C7"></span> Down Main</div>
                                        <div class="legend-item"><span class="legend-swatch swatch-loop"></span> Platform Loops</div>
                                        <div class="legend-item"><span class="legend-swatch swatch-siding"></span> Freight Siding</div>
                                    </div>
                                </div>
                            </div>

                            <!-- Track Simulation Stage (5 Parallel Lines, Signals, Switch Points, Animated Trains) -->
                            <div id="trackSimulationCanvas" class="track-canvas-container">
                                <!-- Populated dynamically by trackSimulation.js and trainMovement.js -->
                                <div id="trainsLayer"></div>
                            </div>

                            <!-- Simulation Footer -->
                            <div class="track-sim-footer">
                                <div class="sim-footer-controls">
                                    <span style="font-size:0.75rem; color:var(--text-muted);">Active Speed:</span>
                                    <span style="font-size:0.8rem; font-weight:700; color:var(--amber-light);">Continuous Real-time Interpolation</span>
                                </div>
                                <div class="sim-footer-stats">
                                    <div class="sim-stat-item">Zone: <strong>Southern Railway</strong></div>
                                    <div class="sim-stat-item">Signaling: <strong>Absolute Block / Solid State Interlocking</strong></div>
                                    <div class="sim-stat-item">Hover over trains for <strong>Telemetric Profile</strong></div>
                                </div>
                            </div>
                        </div>

                        <!-- ==========================================================================
                             8. TELEMETRY AND TRAIN INFORMATION BELOW SIMULATION
                             ========================================================================== -->
                        <div class="sub-sim-telemetry-container" id="subSimTelemetryContainer" style="margin-top:20px;">
                            <!-- Tabs / Section Header Bar -->
                            <div class="sub-telemetry-nav-bar">
                                <div class="sub-telemetry-tabs">
                                    <button class="sub-tab-btn active" id="subTabBtnStation" onclick="switchSubTab('station-telemetry')">
                                        <span>🏢 Station Telemetry</span>
                                    </button>
                                    <button class="sub-tab-btn" id="subTabBtnIncoming" onclick="switchSubTab('incoming-trains')">
                                        <span>🚆 Incoming Trains</span>
                                    </button>
                                    <button class="sub-tab-btn" id="subTabBtnTransit" onclick="switchSubTab('transit-trains')">
                                        <span>📡 Transit Trains</span>
                                    </button>
                                    <button class="sub-tab-btn" id="subTabBtnSchedule" onclick="switchSubTab('train-schedules')">
                                        <span>🕒 Train Schedule</span>
                                    </button>
                                </div>
                                <div style="display:flex; align-items:center; gap:8px;">
                                    <span style="font-size:0.7rem; color:var(--text-muted);">Active Terminal:</span>
                                    <span class="station-code-pill" id="subStationCodePill"><%= currentStation != null ? currentStation.getCode() : "MAS" %></span>
                                </div>
                            </div>

                            <!-- SUB-TAB 1: STATION TELEMETRY -->
                            <div id="subTabStationTelemetry" class="sub-telemetry-pane">
                                <div class="sub-telemetry-grid">
                                    <!-- Station Status Profile -->
                                    <div class="station-card">
                                        <div class="station-title-row">
                                            <span id="subPanelStationName" class="station-name-text">
                                                <%= currentStation != null ? currentStation.getName() : "Chennai Central" %>
                                            </span>
                                            <span id="subPanelStationCode" class="station-code-pill">
                                                <%= currentStation != null ? currentStation.getCode() : "MAS" %>
                                            </span>
                                        </div>
                                        <div class="station-meta-grid" style="margin-top:8px;">
                                            <div class="meta-box">
                                                <span class="meta-label">Division</span>
                                                <span id="subPanelStationDivision" class="meta-val"><%= selectedDivision %> Division (SR)</span>
                                            </div>
                                            <div class="meta-box">
                                                <span class="meta-label">Platforms</span>
                                                <span id="subPanelPlatforms" class="meta-val"><%= currentStation != null ? currentStation.getPlatformsCount() : 12 %> Platforms</span>
                                            </div>
                                            <div class="meta-box">
                                                <span class="meta-label">Interlocked Tracks</span>
                                                <span id="subPanelTracksCount" class="meta-val"><%= currentStation != null ? currentStation.getTracksCount() : 5 %> Lines</span>
                                            </div>
                                            <div class="meta-box">
                                                <span class="meta-label">Station Status</span>
                                                <span id="subPanelStationStatus" class="meta-val" style="color:#34D399;">Operational (CTC Normal)</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Real-time Track Occupancy -->
                                    <div class="station-card">
                                        <div style="font-size:0.76rem; font-weight:700; color:var(--amber-light); margin-bottom:8px; display:flex; justify-content:space-between;">
                                            <span>TRACK OCCUPANCY &amp; RELAYS</span>
                                            <span style="color:var(--teal-primary); font-size:0.68rem;">Solid State Interlocking Blocks</span>
                                        </div>
                                        <div id="subPanelTrackOccupancyList" class="track-occupancy-list" style="display:grid; grid-template-columns:1fr; gap:6px;">
                                            <!-- Dynamically rendered by dashboard.js -->
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- SUB-TAB 2: INCOMING TRAINS -->
                            <div id="subTabIncomingTrains" class="sub-telemetry-pane" style="display:none;">
                                <div class="table-card-wrap">
                                    <table class="telemetry-table">
                                        <thead>
                                            <tr>
                                                <th>Train No &amp; Name</th>
                                                <th>Category</th>
                                                <th>Next Station Block</th>
                                                <th>Estimated Arrival (ETA)</th>
                                                <th>Delay Status</th>
                                                <th>Expected Assigned Track</th>
                                            </tr>
                                        </thead>
                                        <tbody id="subIncomingTrainsTableBody">
                                            <!-- Dynamically populated by dashboard.js -->
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <!-- SUB-TAB 3: TRANSIT TRAINS -->
                            <div id="subTabTransitTrains" class="sub-telemetry-pane" style="display:none;">
                                <div class="table-card-wrap">
                                    <table class="telemetry-table">
                                        <thead>
                                            <tr>
                                                <th>Train No &amp; Name</th>
                                                <th>Current Location / Block</th>
                                                <th>Direction</th>
                                                <th>Speed</th>
                                                <th>Destination</th>
                                                <th>Transit Progress</th>
                                            </tr>
                                        </thead>
                                        <tbody id="subTransitTrainsTableBody">
                                            <!-- Dynamically populated by dashboard.js -->
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <!-- SUB-TAB 4: TRAIN SCHEDULE -->
                            <div id="subTabTrainSchedules" class="sub-telemetry-pane" style="display:none;">
                                <div class="table-card-wrap">
                                    <table class="telemetry-table">
                                        <thead>
                                            <tr>
                                                <th>Train No &amp; Name</th>
                                                <th>Scheduled Arrival (STA)</th>
                                                <th>Actual Arrival (ATA)</th>
                                                <th>Scheduled Departure (ETD)</th>
                                                <th>Allocated Platform</th>
                                                <th>Delay Status</th>
                                            </tr>
                                        </thead>
                                        <tbody id="subTrainScheduleTableBody">
                                            <!-- Dynamically populated by dashboard.js -->
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- VIEW 2: Dedicated Live Train Monitoring Center -->
                    <div id="monitoringViewContainer" class="monitoring-view-panel" style="display:none;">
                        <div class="monitoring-top-bar">
                            <div class="monitoring-title-box">
                                <div class="kpi-icon-box kpi-teal" style="width:36px; height:36px; font-size:1rem;">📡</div>
                                <div>
                                    <h3 style="font-size:1.05rem; font-weight:800; color:#FFF;">Live Train Monitoring &amp; Telemetry Center</h3>
                                    <div style="font-size:0.72rem; color:var(--text-secondary);">Real-time Block Section Radar &amp; Transit Status</div>
                                </div>
                            </div>
                            <div style="display:flex; align-items:center; gap:8px;">
                                <button class="btn btn-primary btn-sm" onclick="switchView('track')">
                                    <span>➔ View on Track Display</span>
                                </button>
                            </div>
                        </div>

                        <!-- Telemetry Radar Table -->
                        <div class="monitoring-table-container">
                            <table class="telemetry-table">
                                <thead>
                                    <tr>
                                        <th>Train No &amp; Name</th>
                                        <th>Category</th>
                                        <th>Assigned Line</th>
                                        <th>Route Transit</th>
                                        <th>Speed</th>
                                        <th>Section Progress</th>
                                        <th>ETA</th>
                                        <th>Delay</th>
                                        <th>Status Indicator</th>
                                        <th>Controller Action</th>
                                    </tr>
                                </thead>
                                <tbody id="monitoringTableBody">
                                    <!-- Populated dynamically by dashboard.js -->
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- VIEW 3: Interactive Analytics Dashboard (6 Chart.js Graphs) -->
                    <div id="analyticsViewContainer" class="monitoring-view-panel" style="display:none;">
                        <div class="monitoring-top-bar">
                            <div class="monitoring-title-box">
                                <div class="kpi-icon-box kpi-amber" style="width:36px; height:36px; font-size:1rem;">📊</div>
                                <div>
                                    <h3 style="font-size:1.05rem; font-weight:800; color:#FFF;">Southern Railway AI Analytics &amp; Reporting Engine</h3>
                                    <div style="font-size:0.72rem; color:var(--text-secondary);">Real-time Telemetry Visualizations &amp; Historical Performance</div>
                                </div>
                            </div>
                            <div style="display:flex; align-items:center; gap:8px;">
                                <button class="btn btn-outline btn-sm" onclick="window.refreshAnalyticsDashboard()">
                                    <span>🔄 Refresh Charts</span>
                                </button>
                                <button class="btn btn-primary btn-sm" onclick="switchView('track')">
                                    <span>➔ View on Track Display</span>
                                </button>
                            </div>
                        </div>

                        <!-- 6 Interactive Chart Cards Grid -->
                        <div class="analytics-grid">
                            <!-- A. Train Delay Analysis -->
                            <div class="chart-card">
                                <div class="chart-card-header">
                                    <div class="chart-card-title">
                                        <span>⏳ A. Train Delay Analysis</span>
                                    </div>
                                    <span class="chart-card-badge">Minutes / Train</span>
                                </div>
                                <div class="chart-canvas-wrapper">
                                    <canvas id="chartDelayAnalysis"></canvas>
                                </div>
                            </div>

                            <!-- B. Train Traffic Analysis -->
                            <div class="chart-card">
                                <div class="chart-card-header">
                                    <div class="chart-card-title">
                                        <span>🚆 B. Train Traffic Volume</span>
                                    </div>
                                    <span class="chart-card-badge">Hourly Intervals</span>
                                </div>
                                <div class="chart-canvas-wrapper">
                                    <canvas id="chartTrafficAnalysis"></canvas>
                                </div>
                            </div>

                            <!-- C. Train Punctuality -->
                            <div class="chart-card">
                                <div class="chart-card-header">
                                    <div class="chart-card-title">
                                        <span>⏱️ C. Train Punctuality Proportion</span>
                                    </div>
                                    <span class="chart-card-badge">Nominal vs Delayed</span>
                                </div>
                                <div class="chart-canvas-wrapper">
                                    <canvas id="chartPunctuality"></canvas>
                                    <div class="chart-center-overlay">
                                        <div id="puncRateCenterText" class="chart-center-val">94.2%</div>
                                        <div class="chart-center-lbl">On-Time</div>
                                    </div>
                                </div>
                            </div>

                            <!-- D. Track Utilization -->
                            <div class="chart-card">
                                <div class="chart-card-header">
                                    <div class="chart-card-title">
                                        <span>🛤️ D. Track Utilization Frequency</span>
                                    </div>
                                    <span class="chart-card-badge">Tracks 1 to 5</span>
                                </div>
                                <div class="chart-canvas-wrapper">
                                    <canvas id="chartTrackUtilization"></canvas>
                                </div>
                            </div>

                            <!-- E. Train Category Analysis -->
                            <div class="chart-card">
                                <div class="chart-card-header">
                                    <div class="chart-card-title">
                                        <span>📋 E. Train Category Breakdown</span>
                                    </div>
                                    <span class="chart-card-badge">Fleet Distribution</span>
                                </div>
                                <div class="chart-canvas-wrapper">
                                    <canvas id="chartCategoryAnalysis"></canvas>
                                </div>
                            </div>

                            <!-- F. Diversion Analysis -->
                            <div class="chart-card">
                                <div class="chart-card-header">
                                    <div class="chart-card-title">
                                        <span>🔀 F. Conflict &amp; Diversion Resolution</span>
                                    </div>
                                    <span class="chart-card-badge">AI Safety Metrics</span>
                                </div>
                                <div class="chart-canvas-wrapper">
                                    <canvas id="chartDiversionAnalysis"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- VIEW 4: Alert History Page -->
                    <div id="alertHistoryViewContainer" class="monitoring-view-panel" style="display:none;">
                        <div class="monitoring-top-bar">
                            <div class="monitoring-title-box">
                                <div class="kpi-icon-box kpi-red" style="width:36px; height:36px; font-size:1rem;">🚨</div>
                                <div>
                                    <h3 style="font-size:1.05rem; font-weight:800; color:#FFF;">Southern Railway Alert &amp; Incident History Log</h3>
                                    <div style="font-size:0.72rem; color:var(--text-secondary);">Comprehensive Audit of Track Conflicts, Diversions &amp; Signal Holds</div>
                                </div>
                            </div>
                            <div style="display:flex; align-items:center; gap:8px;">
                                <input type="text" id="alertHistorySearch" placeholder="Search by Alert ID or Train..." class="control-select" style="min-width:200px; padding:4px 10px;" oninput="filterAlertHistoryTable()">
                                <button class="btn btn-outline btn-sm" onclick="loadAlertHistory()">
                                    <span>🔄 Refresh Log</span>
                                </button>
                                <button class="btn btn-primary btn-sm" onclick="switchView('track')">
                                    <span>➔ View Track Display</span>
                                </button>
                            </div>
                        </div>

                        <!-- Alert History Table -->
                        <div class="monitoring-table-container">
                            <table class="telemetry-table">
                                <thead>
                                    <tr>
                                        <th>Alert ID</th>
                                        <th>Train Number</th>
                                        <th>Conflict Type</th>
                                        <th>Time Detected</th>
                                        <th>Recommended Action</th>
                                        <th>Dispatch Status</th>
                                        <th>Location Block</th>
                                    </tr>
                                </thead>
                                <tbody id="alertHistoryTableBody">
                                    <!-- Populated dynamically by dashboard.js from /api/alert-history -->
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- VIEW 5: Train Scheduling Management Page -->
                    <div id="schedulingViewContainer" class="monitoring-view-panel" style="display:none;">
                        <div class="monitoring-top-bar">
                            <div class="monitoring-title-box">
                                <div class="kpi-icon-box kpi-teal" style="width:36px; height:36px; font-size:1rem;">🕒</div>
                                <div>
                                    <h3 style="font-size:1.05rem; font-weight:800; color:#FFF;">Tamil Nadu Train Timetable &amp; Schedule Management</h3>
                                    <div style="font-size:0.72rem; color:var(--text-secondary);">Real-time Schedule Adjustments, Platform Assignments &amp; Stops</div>
                                </div>
                            </div>
                            <div style="display:flex; align-items:center; gap:8px;">
                                <input type="text" id="scheduleSearchInput" placeholder="Filter schedules..." class="control-select" style="min-width:180px; padding:4px 10px;" oninput="filterScheduleTable()">
                                <button class="btn btn-outline btn-sm" onclick="loadSchedulesTable()">
                                    <span>🔄 Refresh Timetable</span>
                                </button>
                                <button class="btn btn-primary btn-sm" onclick="switchView('track')">
                                    <span>➔ View Track Display</span>
                                </button>
                            </div>
                        </div>

                        <!-- Train Schedules Table -->
                        <div class="monitoring-table-container">
                            <table class="telemetry-table">
                                <thead>
                                    <tr>
                                        <th>Train No &amp; Name</th>
                                        <th>Station Stop</th>
                                        <th>Scheduled Arrival</th>
                                        <th>Scheduled Departure</th>
                                        <th>Platform</th>
                                        <th>Stop Sequence</th>
                                        <th>Halt (min)</th>
                                        <th>Controller Action</th>
                                    </tr>
                                </thead>
                                <tbody id="schedulingTableBody">
                                    <!-- Populated dynamically by dashboard.js from /api/schedules -->
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- VIEW 6: Train Movement Telemetry History -->
                    <div id="movementHistoryViewContainer" class="monitoring-view-panel" style="display:none;">
                        <div class="monitoring-top-bar">
                            <div class="monitoring-title-box">
                                <div class="kpi-icon-box kpi-blue" style="width:36px; height:36px; font-size:1rem;">📡</div>
                                <div>
                                    <h3 style="font-size:1.05rem; font-weight:800; color:#FFF;">Train Movement History &amp; Section Telemetry Log</h3>
                                    <div style="font-size:0.72rem; color:var(--text-secondary);">Real-time Telemetry Stream &amp; Block Signal Verification</div>
                                </div>
                            </div>
                            <div style="display:flex; align-items:center; gap:8px;">
                                <button class="btn btn-outline btn-sm" onclick="loadMovementsTable()">
                                    <span>🔄 Refresh Telemetry</span>
                                </button>
                                <button class="btn btn-primary btn-sm" onclick="switchView('track')">
                                    <span>➔ View Track Display</span>
                                </button>
                            </div>
                        </div>

                        <!-- Movement History Table -->
                        <div class="monitoring-table-container">
                            <table class="telemetry-table">
                                <thead>
                                    <tr>
                                        <th>Timestamp</th>
                                        <th>Train Number</th>
                                        <th>Station Block</th>
                                        <th>Line</th>
                                        <th>Speed</th>
                                        <th>Section Progress</th>
                                        <th>Status</th>
                                        <th>Telemetry Event &amp; Controller Notes</th>
                                    </tr>
                                </thead>
                                <tbody id="movementHistoryTableBody">
                                    <!-- Populated dynamically by dashboard.js from /api/movements -->
                                </tbody>
                            </table>
                        </div>
                    </div>

                </div>

                <!-- 6. RIGHT-SIDE INFORMATION PANEL -->
                <!-- 7. RIGHT-SIDE ALERT PANEL (Dedicated to AI Alerts & Controller Decisions) -->
                <aside class="info-panel" id="aiAlertPanel">
                    <div class="panel-header">
                        <div class="panel-title">
                            <span style="font-size:1.1rem;">🚨</span>
                            <span>AI Predictive Conflict Center</span>
                        </div>
                        <span id="conflictLifecycleBadge" class="status-badge status-green" style="font-size:0.68rem;">
                            MONITORING
                        </span>
                    </div>

                    <!-- Complete Dedicated AI Alert & Controller Decision Center -->
                    <div id="aiAlertCenterContainer" style="padding:16px; display:flex; flex-direction:column; gap:14px; flex:1; overflow-y:auto;">
                        <!-- Dynamically managed by dashboard.js for TOP, MIDDLE, BOTTOM lifecycle -->
                    </div>

                    <!-- Live AI Alert Activity Feed -->
                    <div class="panel-section" style="border-top:1px solid var(--border-color); border-bottom:none; background:#070C18;">
                        <div class="section-label">
                            <span>Live AI Incident Feed</span>
                            <span id="alertsCountBadge" class="status-badge status-teal" style="font-size:0.65rem;">Active Buffer</span>
                        </div>
                        <div id="liveAlertsFeedList" style="max-height:170px; overflow-y:auto; display:flex; flex-direction:column; gap:6px;">
                            <!-- Dynamically populated by dashboard.js -->
                        </div>
                    </div>
                </aside>

            </div>
        </main>
    </div>

    <!-- ==========================================================================
         MODALS & OVERLAYS
         ========================================================================== -->

    <!-- Simulation Settings & Testing Controls Modal -->
    <div id="simulationSettingsModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:580px; border-radius:var(--radius-md); padding:24px; position:relative;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
                <h3 style="font-size:1.15rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span style="color:var(--amber-primary)">⚙️</span> Simulation Settings &amp; Testing Controls
                </h3>
                <button onclick="closeModal('simulationSettingsModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>

            <!-- Keyboard Shortcut Demo Documentation -->
            <div style="background:rgba(59,130,246,0.08); border:1px solid rgba(59,130,246,0.3); border-radius:var(--radius-sm); padding:10px 14px; margin-bottom:14px; display:flex; align-items:center; gap:12px;">
                <span style="font-size:1.3rem;">⌨️</span>
                <div style="font-size:0.75rem; color:#94A3B8; line-height:1.4;">
                    <strong style="color:#60A5FA;">Developer / Testing Keyboard Shortcut:</strong><br/>
                    Press <kbd style="background:#0F172A; border:1px solid #334155; padding:2px 6px; border-radius:4px; font-family:monospace; color:#38BDF8; font-weight:700;">Ctrl + Shift + C</kbd> anywhere on the dashboard to immediately test the simulated AI conflict detection, priority assessment, and automated turnout diversion workflow.
                </div>
            </div>

            <div style="display:flex; flex-direction:column; gap:16px;">
                <!-- 1. Reset Simulation -->
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:12px; display:flex; align-items:center; justify-content:space-between;">
                    <div>
                        <div style="font-weight:700; color:#FFF; font-size:0.85rem;">Reset Section Simulation</div>
                        <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">Resets in-transit train positions and line block occupancies to initial state.</div>
                    </div>
                    <button id="modalResetBtn" class="btn btn-outline btn-sm" onclick="window.trainMovementEngine.reset(); closeModal('simulationSettingsModal');">
                        <span>🔄 Reset Trains</span>
                    </button>
                </div>

                <!-- 2. Test Track Conflict -->
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:12px; display:flex; align-items:center; justify-content:space-between;">
                    <div>
                        <div style="font-weight:700; color:#FFF; font-size:0.85rem;">Simulate Track Conflict</div>
                        <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">Forces Track 1 contention between two services to test AI priority decision.</div>
                    </div>
                    <button id="modalConflictBtn" class="btn btn-outline btn-sm" style="color:#F87171; border-color:rgba(239,68,68,0.4);" onclick="toggleSimulatedConflict()">
                        <span>⚠️ Trigger Conflict</span>
                    </button>
                </div>

                <!-- 3. Add Simulated Train -->
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:12px; display:flex; align-items:center; justify-content:space-between;">
                    <div>
                        <div style="font-weight:700; color:#FFF; font-size:0.85rem;">Deploy Custom Train</div>
                        <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">Register and inject a new passenger or freight train into Tamil Nadu lines.</div>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="closeModal('simulationSettingsModal'); openModal('addTrainModal');">
                        <span>+ Add Train</span>
                    </button>
                </div>

                <!-- 4. Simulation Speed Multipliers (Moved to Testing Settings) -->
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:12px; display:flex; align-items:center; justify-content:space-between;">
                    <div>
                        <div style="font-weight:700; color:#FFF; font-size:0.85rem;">Simulation Clock Speed</div>
                        <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">Accelerate simulated movement rate for demonstration.</div>
                    </div>
                    <div style="display:flex; gap:6px;">
                        <button class="sim-speed-btn active" data-speed="1" onclick="window.trainMovementEngine.setSpeed(1)">1x</button>
                        <button class="sim-speed-btn" data-speed="2" onclick="window.trainMovementEngine.setSpeed(2)">2x</button>
                        <button class="sim-speed-btn" data-speed="5" onclick="window.trainMovementEngine.setSpeed(5)">5x</button>
                    </div>
                </div>

                <!-- 5. Dispatch Chimes & Locomotive Audio -->
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:12px; display:flex; align-items:center; justify-content:space-between;">
                    <div>
                        <div style="font-weight:700; color:#FFF; font-size:0.85rem;">Dispatch Audio Signals</div>
                        <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">Station chimes, interlocking alarms and conflict buzzer synthesis.</div>
                    </div>
                    <button id="modalAudioToggleBtn" class="btn btn-outline btn-sm" onclick="toggleAudio()">
                        <span id="modalAudioIcon">🔇</span> <span id="modalAudioText">Audio Off</span>
                    </button>
                </div>

                <!-- 6. Test Warning Alarm Tone -->
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:12px; display:flex; align-items:center; justify-content:space-between;">
                    <div>
                        <div style="font-weight:700; color:#FFF; font-size:0.85rem;">Test Warning Siren (Alarm)</div>
                        <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">Plays a test sample of the continuous two-tone conflict warning siren.</div>
                    </div>
                    <button id="modalTestAlarmBtn" class="btn btn-outline btn-sm" style="color:#F87171; border-color:rgba(239,68,68,0.4);" onclick="testAlarmSoundSample()">
                        <span>🔔 Test Alarm Siren</span>
                    </button>
                </div>

                <!-- 7. Comprehensive Railway Safety Scenario Verification Suite (Scenarios A - E) -->
                <div style="background:#091122; border:1px solid #1E3A8A; border-radius:var(--radius-sm); padding:14px; display:flex; flex-direction:column; gap:8px;">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:0.8rem; font-weight:800; color:#38BDF8; text-transform:uppercase;">🧪 Railway Safety Scenario Verification Suite:</span>
                        <span class="status-badge status-teal" id="modalScenarioBadge">Scenario: DEFAULT</span>
                    </div>
                    <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-top:4px;">
                        <button class="btn btn-outline btn-sm" onclick="runInterlockingScenario('A')" style="text-align:left; padding:7px 10px; font-size:0.72rem; border-color:rgba(56,189,248,0.4);">
                            <strong>Test A:</strong> Same-Direction Safe Spacing
                        </button>
                        <button class="btn btn-outline btn-sm" onclick="runInterlockingScenario('B')" style="text-align:left; padding:7px 10px; font-size:0.72rem; border-color:rgba(245,158,11,0.4);">
                            <strong>Test B:</strong> Opposing Head-On Signal Hold
                        </button>
                        <button class="btn btn-outline btn-sm" onclick="runInterlockingScenario('C')" style="text-align:left; padding:7px 10px; font-size:0.72rem; border-color:rgba(16,185,129,0.4);">
                            <strong>Test C:</strong> Turnout Diversion (Trk 1➔3)
                        </button>
                        <button class="btn btn-outline btn-sm" onclick="runInterlockingScenario('D')" style="text-align:left; padding:7px 10px; font-size:0.72rem; border-color:rgba(239,68,68,0.4);">
                            <strong>Test D:</strong> Occupied Side Track Hold
                        </button>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="runInterlockingScenario('E')" style="margin-top:2px; padding:7px 10px; font-size:0.75rem; font-weight:700;">
                        <strong>Test E:</strong> Multi-Train Simultaneous Corridor Transit
                    </button>
                </div>
            </div>

            <div style="display:flex; justify-content:flex-end; margin-top:18px;">
                <button onclick="closeModal('simulationSettingsModal')" class="btn btn-outline btn-sm">Close Settings</button>
            </div>
        </div>
    </div>

    <!-- Add Simulated Train Modal -->
    <div id="addTrainModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:540px; border-radius:var(--radius-md); padding:24px; position:relative;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:18px;">
                <h3 style="font-size:1.15rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span style="color:var(--amber-primary)">+</span> Register Simulated Train
                </h3>
                <button onclick="closeModal('addTrainModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>

            <form id="addTrainForm" style="display:flex; flex-direction:column; gap:12px;">
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Train Number</label>
                        <input type="text" name="trainNumber" required placeholder="e.g. 12638" class="control-select" style="width:100%;">
                    </div>
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Train Name</label>
                        <input type="text" name="trainName" required placeholder="e.g. Nellai Superfast Express" class="control-select" style="width:100%;">
                    </div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Category</label>
                        <select name="category" class="control-select" style="width:100%;">
                            <option value="EXPRESS">Express</option>
                            <option value="PASSENGER">Passenger</option>
                            <option value="GOODS">Goods</option>
                            <option value="FREIGHT">Freight</option>
                        </select>
                    </div>
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Assign Track</label>
                        <select name="trackNumber" class="control-select" style="width:100%;">
                            <option value="1">Track 1 - Up Main Line</option>
                            <option value="2">Track 2 - Down Main Line</option>
                            <option value="3">Track 3 - Platform Loop 1</option>
                            <option value="4">Track 4 - Platform Loop 2</option>
                            <option value="5">Track 5 - Freight Siding</option>
                        </select>
                    </div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Direction</label>
                        <select name="direction" class="control-select" style="width:100%;">
                            <option value="UP">Eastbound (UP ➔)</option>
                            <option value="DOWN">Westbound (⬅ DOWN)</option>
                        </select>
                    </div>
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Initial Status</label>
                        <select name="status" class="control-select" style="width:100%;">
                            <option value="MOVING">Moving (Blue)</option>
                            <option value="ON_TIME">On Time (Green)</option>
                            <option value="DELAYED">Delayed (Amber)</option>
                        </select>
                    </div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Estimated Arrival (ETA)</label>
                        <input type="text" name="eta" value="23:45" class="control-select" style="width:100%;">
                    </div>
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Cruise Speed (km/h)</label>
                        <input type="number" name="speed" value="90" min="20" max="130" class="control-select" style="width:100%;">
                    </div>
                </div>

                <!-- Hidden inputs for station linking -->
                <input type="hidden" name="originId" value="1">
                <input type="hidden" name="destId" value="<%= currentStation != null ? currentStation.getId() : 1 %>">
                <input type="hidden" name="currentStationId" value="<%= currentStation != null ? currentStation.getId() : 1 %>">
                <input type="hidden" name="etd" value="23:55">
                <input type="hidden" name="delay" value="0">

                <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:12px;">
                    <button type="button" onclick="closeModal('addTrainModal')" class="btn btn-outline">Cancel</button>
                    <button type="submit" class="btn btn-primary">Deploy to Track</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Edit Train Schedule Modal -->
    <div id="editScheduleModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:480px; border-radius:var(--radius-md); padding:24px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                <h3 style="font-size:1.15rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span style="color:var(--teal-primary)">🕒</span> Update Railway Train Schedule
                </h3>
                <button onclick="closeModal('editScheduleModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>
            <form id="editScheduleForm" style="display:flex; flex-direction:column; gap:12px;">
                <input type="hidden" id="editScheduleId" name="scheduleId">
                <input type="hidden" name="action" value="update">

                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:10px;">
                    <div style="font-size:0.75rem; color:var(--text-muted);">Train Service:</div>
                    <div id="editScheduleTrainLabel" style="font-weight:700; color:var(--amber-light); font-size:0.9rem;">-</div>
                    <div style="font-size:0.75rem; color:var(--text-muted); margin-top:4px;">Station Stop:</div>
                    <div id="editScheduleStationLabel" style="font-weight:600; color:#FFF; font-size:0.85rem;">-</div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px;">
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Scheduled Arrival (HH:MM)</label>
                        <input type="text" id="editScheduleArrival" name="arrival" required class="control-select" style="width:100%;">
                    </div>
                    <div>
                        <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Scheduled Departure (HH:MM)</label>
                        <input type="text" id="editScheduleDeparture" name="departure" required class="control-select" style="width:100%;">
                    </div>
                </div>

                <div>
                    <label style="font-size:0.75rem; color:var(--text-secondary); display:block; margin-bottom:4px;">Allocated Platform Number</label>
                    <select id="editSchedulePlatform" name="platform" class="control-select" style="width:100%;">
                        <option value="1">Platform 1 (Main Up)</option>
                        <option value="2">Platform 2 (Main Down)</option>
                        <option value="3">Platform 3 (Loop 1)</option>
                        <option value="4">Platform 4 (Loop 2)</option>
                        <option value="5">Platform 5 (Island Platform)</option>
                        <option value="6">Platform 6</option>
                    </select>
                </div>

                <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:10px;">
                    <button type="button" onclick="closeModal('editScheduleModal')" class="btn btn-outline btn-sm">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Save &amp; Update Database</button>
                </div>
            </form>
        </div>
    </div>

    <!-- AI Dispatch Information Modal -->
    <div id="aiDispatchModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:620px; border-radius:var(--radius-md); padding:28px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                <h3 style="font-size:1.25rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span>🧠</span> SmartRail AI - Decision Engine Architecture
                </h3>
                <button onclick="closeModal('aiDispatchModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>
            <p style="font-size:0.86rem; color:var(--text-secondary); line-height:1.6; margin-bottom:14px;">
                SmartRail AI utilizes an <strong>explainable, multi-factor rule-based decision engine</strong> designed for railway dispatchers. It evaluates real-time telemetry, track geometries, and arrival windows without black-box opacity.
            </p>
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:16px;">
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:10px;">
                    <div style="font-weight:700; font-size:0.78rem; color:var(--teal-light); margin-bottom:4px;">1. Conflict Detection</div>
                    <div style="font-size:0.72rem; color:var(--text-muted);">Spatial overlap, opposing direction head-on risk, and overlapping schedule arrival windows (≤ 20 min).</div>
                </div>
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:10px;">
                    <div style="font-weight:700; font-size:0.78rem; color:var(--amber-light); margin-bottom:4px;">2. Priority Scoring</div>
                    <div style="font-size:0.72rem; color:var(--text-muted);">Train categories (Express +100, Passenger +70, Goods +45, Freight +30), delay impact, kinetic speed, and gridlock capping.</div>
                </div>
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:10px;">
                    <div style="font-weight:700; font-size:0.78rem; color:#34D399; margin-bottom:4px;">3. Automatic Track Diversion</div>
                    <div style="font-size:0.72rem; color:var(--text-muted);">Automated loop/siding track routing with interlocking verification and safety signal holds.</div>
                </div>
                <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:10px;">
                    <div style="font-weight:700; font-size:0.78rem; color:#F87171; margin-bottom:4px;">4. Controller Governance</div>
                    <div style="font-size:0.72rem; color:var(--text-muted);">Supports both Automatic Dispatch and Human-in-the-Loop Manual Controller Approval.</div>
                </div>
            </div>
            <div style="display:flex; justify-content:flex-end;">
                <button onclick="closeModal('aiDispatchModal')" class="btn btn-primary btn-sm">Close</button>
            </div>
        </div>
    </div>

    <!-- Train Management Modal -->
    <div id="trainMgmtModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:92%; max-width:960px; max-height:85vh; border-radius:var(--radius-md); padding:24px; display:flex; flex-direction:column; overflow:hidden;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                <h3 style="font-size:1.25rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span>📋</span> Tamil Nadu Train Fleet Management
                </h3>
                <button onclick="closeModal('trainMgmtModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>
            <div style="display:flex; gap:12px; margin-bottom:14px; flex-wrap:wrap;">
                <input type="text" id="trainMgmtSearch" placeholder="Search by train number or name..." class="control-select" style="flex:1; min-width:220px;" oninput="filterTrainMgmtTable()">
                <select id="trainMgmtCategoryFilter" class="control-select" onchange="filterTrainMgmtTable()">
                    <option value="ALL">All Categories</option>
                    <option value="EXPRESS">Express</option>
                    <option value="PASSENGER">Passenger</option>
                    <option value="GOODS">Goods</option>
                    <option value="FREIGHT">Freight</option>
                </select>
                <button class="btn btn-primary btn-sm" onclick="closeModal('trainMgmtModal'); showModal('addTrainModal');">
                    <span>+ Add Train</span>
                </button>
            </div>
            <div style="flex:1; overflow-y:auto; border:1px solid var(--border-color); border-radius:var(--radius-sm);">
                <table class="telemetry-table" style="width:100%;">
                    <thead>
                        <tr>
                            <th>Train No &amp; Name</th>
                            <th>Category</th>
                            <th>Route (Origin ➔ Dest)</th>
                            <th>Current Station</th>
                            <th>Assigned Track</th>
                            <th>Speed</th>
                            <th>ETA</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody id="trainMgmtTableBody">
                        <!-- Populated dynamically -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Station Management Modal -->
    <div id="stationMgmtModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:800px; max-height:85vh; border-radius:var(--radius-md); padding:24px; display:flex; flex-direction:column; overflow:hidden;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                <h3 style="font-size:1.2rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span>🚉</span> Tamil Nadu Station Infrastructure Inspector
                </h3>
                <button onclick="closeModal('stationMgmtModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>
            <div style="flex:1; overflow-y:auto;">
                <div id="stationMgmtGrid" style="display:grid; grid-template-columns:repeat(auto-fit, minmax(230px, 1fr)); gap:12px;">
                    <!-- Populated dynamically -->
                </div>
            </div>
        </div>
    </div>

    <!-- Track Management Modal -->
    <div id="trackMgmtModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:700px; border-radius:var(--radius-md); padding:24px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                <h3 style="font-size:1.2rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span>🛤️</span> Interlocking Track &amp; Maintenance Controller
                </h3>
                <button onclick="closeModal('trackMgmtModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>
            <p style="font-size:0.8rem; color:var(--text-secondary); margin-bottom:14px;">
                Toggle line maintenance blocks. When a track is flagged for <strong>Maintenance</strong>, the AI Dispatcher automatically steers all competing traffic around it to platform loop lines!
            </p>
            <div id="trackMgmtList" style="display:flex; flex-direction:column; gap:10px; margin-bottom:16px;">
                <!-- Populated dynamically -->
            </div>
            <div style="display:flex; justify-content:flex-end;">
                <button onclick="closeModal('trackMgmtModal')" class="btn btn-outline btn-sm">Close</button>
            </div>
        </div>
    </div>

    <!-- Analytics Modal -->
    <div id="analyticsModal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:760px; border-radius:var(--radius-md); padding:24px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                <h3 style="font-size:1.25rem; color:#FFF; display:flex; align-items:center; gap:8px;">
                    <span>📊</span> Southern Railway Punctuality &amp; Telemetry Analytics
                </h3>
                <button onclick="closeModal('analyticsModal')" style="background:none; border:none; color:var(--text-muted); font-size:1.4rem; cursor:pointer;">&times;</button>
            </div>
            <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(160px, 1fr)); gap:12px; margin-bottom:18px;">
                <div class="kpi-card" style="padding:12px;">
                    <div class="kpi-title">Chennai Division (MAS)</div>
                    <div class="kpi-value" style="font-size:1.3rem; color:#34D399;">96.4%</div>
                    <div class="kpi-sub">Punctuality Rate</div>
                </div>
                <div class="kpi-card" style="padding:12px;">
                    <div class="kpi-title">Salem Division (SA)</div>
                    <div class="kpi-value" style="font-size:1.3rem; color:#38BDF8;">93.8%</div>
                    <div class="kpi-sub">Punctuality Rate</div>
                </div>
                <div class="kpi-card" style="padding:12px;">
                    <div class="kpi-title">Tiruchirappalli (TPJ)</div>
                    <div class="kpi-value" style="font-size:1.3rem; color:#FBBF24;">91.2%</div>
                    <div class="kpi-sub">Punctuality Rate</div>
                </div>
                <div class="kpi-card" style="padding:12px;">
                    <div class="kpi-title">Madurai Division (MDU)</div>
                    <div class="kpi-value" style="font-size:1.3rem; color:#34D399;">95.1%</div>
                    <div class="kpi-sub">Punctuality Rate</div>
                </div>
            </div>
            <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:14px;">
                <div style="font-size:0.8rem; font-weight:700; color:#FFF; margin-bottom:8px;">AI Automated Dispatch Efficiency Impact:</div>
                <div style="font-size:0.75rem; color:var(--text-secondary); line-height:1.6;">
                    • <strong>Average Delay Reduction</strong>: -4.6 minutes saved per line contention using automated loop diversions.<br>
                    • <strong>Cascading Prevention Score</strong>: 100% of high-speed passenger services preserved on main lines against slow mineral freight.<br>
                    • <strong>Safety Interlocking Assurance</strong>: 0 conflicting overlaps permitted into occupied platform blocks.
                </div>
            </div>
            <div style="display:flex; justify-content:flex-end; margin-top:16px;">
                <button onclick="closeModal('analyticsModal')" class="btn btn-primary btn-sm">Close</button>
            </div>
        </div>
    </div>

    <!-- Generic Phase 2 Modal -->
    <div id="genericPhase2Modal" class="modal-backdrop" style="display:none; position:fixed; inset:0; background:rgba(2,6,23,0.85); backdrop-filter:blur(8px); z-index:10000; align-items:center; justify-content:center;">
        <div class="glass-panel" style="width:90%; max-width:460px; border-radius:var(--radius-md); padding:24px; text-align:center;">
            <h3 id="phase2ModalTitle" style="font-size:1.2rem; color:#FFF; margin-bottom:10px;">Module Prepared</h3>
            <p style="font-size:0.85rem; color:var(--text-secondary); margin-bottom:18px;">
                This module interface is prepared for future Southern Railway divisional expansion. Current focus is active on <strong>Dashboard &amp; Live Train Monitoring</strong>.
            </p>
            <button onclick="closeModal('genericPhase2Modal')" class="btn btn-outline" style="min-width:120px;">Close</button>
        </div>
    </div>

    <!-- Toast Notifications Container -->
    <div id="toastContainer"></div>

    <!-- Server Data Bootstrap -->
    <script>
        window.INITIAL_DATA = <%= initialJson %>;
    </script>

    <!-- JavaScript Controllers -->
    <script src="js/loading.js?v=<%= System.currentTimeMillis() %>"></script>
    <script src="js/chart.min.js"></script>
    <script src="js/analytics.js?v=<%= System.currentTimeMillis() %>"></script>
    <script src="js/trackSimulation.js?v=<%= System.currentTimeMillis() %>"></script>
    <script src="js/trainMovement.js?v=<%= System.currentTimeMillis() %>"></script>
    <script src="js/dashboard.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>

