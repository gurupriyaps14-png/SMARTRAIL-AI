/**
 * SmartRail AI - Dashboard Master Controller
 * Coordinates location filters, live train monitoring, right-side telemetry panel,
 * AI conflict detection, priority management, automatic track diversion, and alert system.
 */

// Global state - Top-Level Definitions
let currentDivision = 'MAS';
let currentStationId = 1;
let currentStationData = null;
let activeTrains = [];
let trackSimulation = null;
let trainMovementEngine = null;
let currentDispatchMode = 'MANUAL';
let isConflictSimulated = false;
let isAudioEnabled = true;
let conflictPollInterval = null;

// Audio Context & Warning Siren State
let audioCtx = null;
let isAlarmPlaying = false;
let isAlarmSilenced = false;
let alarmIntervalId = null;

// AI Predictive Conflict & Dispatch State
let activeConflictData = null;
let conflictLifecycleState = 'MONITORING'; // 'MONITORING', 'CONFLICT_PREDICTED', 'CONTROLLER_ACKNOWLEDGED', 'MANUAL_APPROVED', 'DIVERSION_IN_PROGRESS', 'CONFLICT_RESOLVED', 'HELD_AT_SIGNAL'
let activeDispatchOption = 'AUTOMATIC'; // 'AUTOMATIC' or 'MANUAL'

// Audio Unlock Helper (resumes on user interaction)
function unlockAudio() {
    try {
        if (!audioCtx) {
            audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        if (audioCtx && audioCtx.state === 'suspended') {
            audioCtx.resume();
        }
    } catch (e) {}
}

if (typeof document !== 'undefined') {
    ['click', 'pointerdown', 'touchstart', 'keydown'].forEach(evt => {
        document.addEventListener(evt, unlockAudio, { once: false, passive: true });
    });
}

/**
 * Toast Notification System (Hoisted Top-Level)
 */
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast-item ${type}`;

    let icon = '🔔';
    if (type === 'success') icon = '🚆';
    else if (type === 'alert') icon = '⚠️';

    toast.innerHTML = `
        <span style="font-size:1.2rem;">${icon}</span>
        <div style="flex:1;">
            <div style="font-weight:700; font-size:0.78rem; text-transform:uppercase; color:var(--amber-light);">SmartRail AI Dispatch Alert</div>
            <div>${message}</div>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => {
            if (toast.remove) toast.remove();
            else if (toast.parentNode) toast.parentNode.removeChild(toast);
        }, 300);
    }, 4500);
}
window.showToast = showToast;

/**
 * Conflict Polling & Initialization
 */
function initConflictSystem() {
    if (conflictPollInterval) clearInterval(conflictPollInterval);
    conflictPollInterval = setInterval(pollConflictsAndAlerts, 12000);
}

async function pollConflictsAndAlerts() {
    try {
        const resp = await fetch('api/conflict');
        if (!resp.ok) return;
        const data = await resp.json();
        if (data && data.conflicts && data.conflicts.length > 0 && conflictLifecycleState === 'MONITORING') {
            // Passive sync if needed
        }
    } catch (e) {
        // Polling catch
    }
}

document.addEventListener('DOMContentLoaded', () => {
    initSidebarState();
    initClock();
    initFilterSystem();
    initSimulationEngine();
    initNavigation();
    initModals();
    initConflictSystem();
});

/**
 * Left Sidebar Collapsible Arrow Controller
 * Toggles the navigation sidebar state, persisting in localStorage and triggering
 * responsive canvas / track geometry recalculations.
 */
function toggleSidebar() {
    const appWrapper = document.querySelector('.app-wrapper');
    const toggleBtn = document.getElementById('sidebarToggleBtn');
    if (!appWrapper) return;

    const isCollapsed = appWrapper.classList.toggle('sidebar-collapsed');
    try {
        localStorage.setItem('smartrail_sidebar_collapsed', isCollapsed ? 'true' : 'false');
    } catch (e) {}

    if (toggleBtn) {
        toggleBtn.setAttribute('title', isCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar');
        toggleBtn.setAttribute('aria-label', isCollapsed ? 'Expand navigation sidebar' : 'Collapse navigation sidebar');
    }

    // Smoothly notify SVG track rendering and train coordinates to resize after transition
    setTimeout(() => {
        window.dispatchEvent(new Event('resize'));
        if (typeof trainMovementEngine !== 'undefined' && trainMovementEngine && typeof trainMovementEngine.updatePositions === 'function') {
            trainMovementEngine.updatePositions();
        }
    }, 320);
}

function initSidebarState() {
    try {
        const isCollapsed = localStorage.getItem('smartrail_sidebar_collapsed') === 'true';
        if (isCollapsed) {
            const appWrapper = document.querySelector('.app-wrapper');
            const toggleBtn = document.getElementById('sidebarToggleBtn');
            if (appWrapper) {
                appWrapper.classList.add('sidebar-collapsed');
            }
            if (toggleBtn) {
                toggleBtn.setAttribute('title', 'Expand Sidebar');
                toggleBtn.setAttribute('aria-label', 'Expand navigation sidebar');
            }
        }
    } catch (e) {}
}

/**
 * 1. Digital Clock (IST UTC+05:30)
 */
function initClock() {
    const clockEl = document.getElementById('digitalClock');
    const dateEl = document.getElementById('digitalDate');

    function tick() {
        const now = new Date();
        const timeOptions = { timeZone: 'Asia/Kolkata', hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' };
        const dateOptions = { timeZone: 'Asia/Kolkata', weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' };

        if (clockEl) clockEl.textContent = now.toLocaleTimeString('en-IN', timeOptions);
        if (dateEl) dateEl.textContent = now.toLocaleDateString('en-IN', dateOptions).toUpperCase();
    }
    tick();
    setInterval(tick, 1000);
}

/**
 * 2. Filter System (Division -> Station -> Time)
 */
function initFilterSystem() {
    const divisionSelect = document.getElementById('filterDivision');
    const stationSelect = document.getElementById('filterStation');
    const timeSelect = document.getElementById('filterTime');
    const categorySelect = document.getElementById('filterCategory');

    if (divisionSelect) {
        divisionSelect.addEventListener('change', async (e) => {
            currentDivision = e.target.value;
            await loadStationsForDivision(currentDivision);
            await refreshWorkspace();
        });
    }

    if (stationSelect) {
        stationSelect.addEventListener('change', async (e) => {
            currentStationId = parseInt(e.target.value, 10);
            await refreshWorkspace();
        });
    }

    if (timeSelect) {
        timeSelect.addEventListener('change', async () => {
            await refreshWorkspace();
        });
    }

    if (categorySelect) {
        categorySelect.addEventListener('change', async () => {
            await refreshWorkspace();
        });
    }
}

/**
 * Dynamically loads stations for selected Tamil Nadu division
 */
async function loadStationsForDivision(divisionCode) {
    const stationSelect = document.getElementById('filterStation');
    if (!stationSelect) return;

    stationSelect.innerHTML = '<option value="">Loading stations...</option>';

    try {
        const resp = await fetch(`api/stations?division=${encodeURIComponent(divisionCode)}`);
        const stations = await resp.json();

        stationSelect.innerHTML = '';
        stations.forEach((st, idx) => {
            const opt = document.createElement('option');
            opt.value = st.id;
            opt.textContent = `${st.name} (${st.code})`;
            if (idx === 0) {
                opt.selected = true;
                currentStationId = st.id;
                currentStationData = st;
            }
            stationSelect.appendChild(opt);
        });
    } catch (err) {
        console.error('Failed to load stations:', err);
    }
}

/**
 * Refreshes track visualization, train data, telemetry panel, and AI conflict scan
 */
async function refreshWorkspace() {
    const division = document.getElementById('filterDivision')?.value || 'MAS';
    const stationId = document.getElementById('filterStation')?.value || currentStationId;
    const timeSlot = document.getElementById('filterTime')?.value || 'ALL';
    const category = document.getElementById('filterCategory')?.value || 'ALL';

    // 1. Fetch Station Details
    try {
        const stationsResp = await fetch(`api/stations?division=${encodeURIComponent(division)}`);
        const stations = await stationsResp.json();
        currentStationData = stations.find(s => s.id == stationId) || stations[0];
    } catch (e) {
        console.error('Station fetch error:', e);
    }

    // 2. Fetch Tracks for Station
    let tracks = [];
    try {
        const tracksResp = await fetch(`api/tracks?stationId=${encodeURIComponent(stationId)}`);
        tracks = await tracksResp.json();
    } catch (e) {
        console.error('Tracks fetch error:', e);
    }

    // 3. Fetch Filtered Trains
    try {
        const trainsResp = await fetch(`api/trains?division=${encodeURIComponent(division)}&stationId=${encodeURIComponent(stationId)}&timeSlot=${encodeURIComponent(timeSlot)}&category=${encodeURIComponent(category)}`);
        activeTrains = await trainsResp.json();
    } catch (e) {
        console.error('Trains fetch error:', e);
    }

    // 4. Update Visual Simulation
    if (trackSimulation) {
        trackSimulation.updateStation(currentStationData, division);
    }
    if (trainMovementEngine) {
        trainMovementEngine.setTrains(activeTrains);
    }

    // 5. Update Sub-Simulation Telemetry & Dedicated Right Alert Center
    updateSubSimulationTelemetry(currentStationData, tracks, activeTrains);
    renderDedicatedAlertCenter();
    updateTelemetryKPIs(division, stationId);
    populateMonitoringRadarTable(activeTrains);

    // 6. Run AI Conflict Detection & Alerts Update
    await pollConflictsAndAlerts();
}

/**
 * 3. Initialize Visual Simulation and Train Movement Engine
 */
function initSimulationEngine() {
    trackSimulation = new TrackSimulation('trackSimulationCanvas');
    trainMovementEngine = new TrainMovementEngine(trackSimulation);
    window.trainMovementEngine = trainMovementEngine;

    // Load initial data from window or backend
    if (window.INITIAL_DATA) {
        currentStationData = window.INITIAL_DATA.currentStation;
        currentDivision = window.INITIAL_DATA.selectedDivision || 'MAS';
        currentStationId = window.INITIAL_DATA.selectedStationId || 1;
        activeTrains = window.INITIAL_DATA.trains || [];

        trackSimulation.init(window.INITIAL_DATA.tracks, currentStationData, currentDivision);
        trainMovementEngine.setTrains(activeTrains);
        updateSubSimulationTelemetry(currentStationData, window.INITIAL_DATA.tracks, activeTrains);
        renderDedicatedAlertCenter();
        populateMonitoringRadarTable(activeTrains);
    } else {
        refreshWorkspace();
    }

    // Single Pause / Resume simulation toggle function
    window.togglePauseResume = function() {
        if (trainMovementEngine) {
            trainMovementEngine.toggle();
        }
    };

    // Bind legacy / modal buttons if present
    const pauseResumeBtn = document.getElementById('simPauseResumeBtn');
    if (pauseResumeBtn) pauseResumeBtn.addEventListener('click', () => window.togglePauseResume());

    const startBtn = document.getElementById('simStartBtn');
    const pauseBtn = document.getElementById('simPauseBtn');
    const resetBtn = document.getElementById('simResetBtn');

    if (startBtn) startBtn.addEventListener('click', () => trainMovementEngine.start());
    if (pauseBtn) pauseBtn.addEventListener('click', () => trainMovementEngine.pause());
    if (resetBtn) resetBtn.addEventListener('click', () => trainMovementEngine.reset());

    const speedButtons = document.querySelectorAll('.sim-speed-btn');
    speedButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const spd = parseFloat(btn.dataset.speed);
            trainMovementEngine.setSpeed(spd);
        });
    });

    // Start movement loop automatically on load
    trainMovementEngine.start();
}

/**
 * ==========================================================================
 * 8. TELEMETRY AND TRAIN INFORMATION BELOW SIMULATION
 * Updates Station Telemetry, Incoming Trains, Transit Trains, and Train Schedules
 * ==========================================================================
 */
function updateSubSimulationTelemetry(station, tracks, trains) {
    if (!station) return;

    // 1. Station Profile Metadata
    const stNameEl = document.getElementById('subPanelStationName');
    const stCodeEl = document.getElementById('subPanelStationCode');
    const stCodePill = document.getElementById('subStationCodePill');
    const stDivEl = document.getElementById('subPanelStationDivision');
    const stPlatformsEl = document.getElementById('subPanelPlatforms');
    const stTracksEl = document.getElementById('subPanelTracksCount');
    const stStatusEl = document.getElementById('subPanelStationStatus');

    if (stNameEl) stNameEl.textContent = station.name;
    if (stCodeEl) stCodeEl.textContent = station.code;
    if (stCodePill) stCodePill.textContent = station.code;
    if (stDivEl) stDivEl.textContent = (station.divisionCode || currentDivision) + ' Division (SR)';
    if (stPlatformsEl) stPlatformsEl.textContent = `${station.platformsCount || 6} Platforms`;
    if (stTracksEl) stTracksEl.textContent = `${station.tracksCount || 5} Interlocked Lines`;
    if (stStatusEl) {
        if (conflictLifecycleState === 'CONFLICT_PREDICTED' || conflictLifecycleState === 'CONTROLLER_ACKNOWLEDGED') {
            stStatusEl.textContent = 'LINE CAUTION: Contention Active';
            stStatusEl.style.color = '#F59E0B';
        } else {
            stStatusEl.textContent = 'Operational (CTC Normal)';
            stStatusEl.style.color = '#34D399';
        }
    }

    // 2. Real-time Track Occupancy
    const trackListEl = document.getElementById('subPanelTrackOccupancyList');
    if (trackListEl) {
        trackListEl.innerHTML = '';
        const defaultTrackNames = ['Track 1 (Up Main)', 'Track 2 (Down Main)', 'Track 3 (Platform Loop 1)', 'Track 4 (Platform Loop 2)', 'Track 5 (Freight Siding)'];

        for (let i = 1; i <= 5; i++) {
            const track = (tracks || []).find(t => t.trackNumber === i);
            const trainOnTrack = (trains || []).find(t => t.currentTrackNumber === i && t.status !== 'ARRIVED');

            const item = document.createElement('div');
            item.className = 'track-occ-item';

            let statusBadge = '<span class="status-badge status-green" style="font-size:0.65rem;">CLEAR</span>';
            let trainLabel = '<span style="color:var(--text-muted); font-size:0.7rem;">None</span>';

            if (trainOnTrack) {
                const isConflict = trainOnTrack.status === 'CONFLICT';
                statusBadge = isConflict ?
                    '<span class="status-badge status-red" style="font-size:0.65rem;">CONFLICT</span>' :
                    '<span class="status-badge status-amber" style="font-size:0.65rem;">OCCUPIED</span>';
                trainLabel = `<span class="track-train-tag" style="color:${isConflict ? '#F87171' : 'var(--teal-light)'}">#${trainOnTrack.trainNumber} (${trainOnTrack.trainName})</span>`;
            } else if (track && track.status === 'RESERVED') {
                statusBadge = '<span class="status-badge status-blue" style="font-size:0.65rem;">RESERVED</span>';
            }

            item.innerHTML = `
                <div class="track-occ-name">
                    <span style="color:var(--amber-light); font-weight:700;">T${i}</span>
                    <span>${defaultTrackNames[i-1]}</span>
                </div>
                <div style="display:flex; align-items:center; gap:8px;">
                    ${trainLabel}
                    ${statusBadge}
                </div>
            `;
            trackListEl.appendChild(item);
        }
    }

    // 3. Incoming Trains Table
    const incomingTbody = document.getElementById('subIncomingTrainsTableBody');
    if (incomingTbody) {
        incomingTbody.innerHTML = '';
        const incomingTrains = (trains || []).filter(t => t.status !== 'ARRIVED');

        if (incomingTrains.length === 0) {
            incomingTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-muted); padding:14px;">No approaching services in current block section.</td></tr>';
        } else {
            incomingTrains.forEach(t => {
                const tr = document.createElement('tr');
                let catClass = 'badge-express';
                if (t.category === 'PASSENGER') catClass = 'badge-passenger';
                else if (t.category === 'GOODS') catClass = 'badge-goods';
                else if (t.category === 'FREIGHT') catClass = 'badge-freight';

                const delayColor = t.delayMinutes > 0 ? '#F59E0B' : '#34D399';
                const delayText = t.delayMinutes > 0 ? `+${t.delayMinutes} min Delay` : 'On Time';

                tr.innerHTML = `
                    <td><strong style="color:#FFF;">#${t.trainNumber}</strong> <span style="font-size:0.75rem; color:var(--amber-light);">${t.trainName}</span></td>
                    <td><span class="badge-category ${catClass}">${t.category}</span></td>
                    <td><strong style="color:var(--teal-light);">${t.nextStationName || station.name}</strong></td>
                    <td style="font-family:monospace; color:#FFF;">${t.eta}</td>
                    <td style="font-weight:700; color:${delayColor};">${delayText}</td>
                    <td><strong style="color:#FFF;">Track ${t.currentTrackNumber}</strong></td>
                `;
                incomingTbody.appendChild(tr);
            });
        }
    }

    // 4. Transit Trains Table
    const transitTbody = document.getElementById('subTransitTrainsTableBody');
    if (transitTbody) {
        transitTbody.innerHTML = '';
        if (!trains || trains.length === 0) {
            transitTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-muted); padding:14px;">No trains in active transit corridor.</td></tr>';
        } else {
            trains.forEach(t => {
                const tr = document.createElement('tr');
                const pct = Math.min(100, Math.floor(t.progressPercent || 0));
                const dirLabel = t.direction === 'UP' ? 'Eastbound (UP ➔)' : 'Westbound (⬅ DOWN)';

                tr.innerHTML = `
                    <td><strong style="color:#FFF;">#${t.trainNumber}</strong> <span style="font-size:0.75rem; color:var(--amber-light);">${t.trainName}</span></td>
                    <td><span class="station-code-pill" style="font-size:0.65rem;">Track ${t.currentTrackNumber}</span> ${station.code} Block</td>
                    <td>${dirLabel}</td>
                    <td><strong style="color:#FFF;">${t.speedKmh} km/h</strong></td>
                    <td>${t.destinationStationName || 'Terminal'}</td>
                    <td>
                        <div style="display:flex; align-items:center; gap:8px;">
                            <div class="progress-track-bar" style="width:100px;">
                                <div class="progress-track-fill" style="width:${pct}%;"></div>
                            </div>
                            <span style="font-size:0.7rem; font-family:monospace; color:var(--amber-light);">${pct}%</span>
                        </div>
                    </td>
                `;
                transitTbody.appendChild(tr);
            });
        }
    }

    // 5. Train Schedule Table
    const scheduleTbody = document.getElementById('subTrainScheduleTableBody');
    if (scheduleTbody) {
        scheduleTbody.innerHTML = '';
        if (!trains || trains.length === 0) {
            scheduleTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-muted); padding:14px;">No active timetables found.</td></tr>';
        } else {
            trains.forEach(t => {
                const tr = document.createElement('tr');
                const delayColor = t.delayMinutes > 0 ? '#F59E0B' : '#34D399';
                const delayText = t.delayMinutes > 0 ? `+${t.delayMinutes} min` : 'On Time';

                tr.innerHTML = `
                    <td><strong style="color:#FFF;">#${t.trainNumber}</strong> <span style="font-size:0.75rem; color:var(--amber-light);">${t.trainName}</span></td>
                    <td style="color:#34D399; font-weight:700;">${t.eta || '23:30'}</td>
                    <td style="color:#38BDF8; font-weight:700;">${t.eta || '23:30'}</td>
                    <td style="color:#CBD5E1;">${t.etd || '23:45'}</td>
                    <td><strong style="color:var(--amber-light);">Platform ${t.currentTrackNumber}</strong></td>
                    <td style="font-weight:700; color:${delayColor};">${delayText}</td>
                `;
                scheduleTbody.appendChild(tr);
            });
        }
    }
}

/**
 * Sub-Simulation Telemetry Tab Switcher
 */
window.switchSubTab = function(tabName) {
    const tabs = ['station-telemetry', 'incoming-trains', 'transit-trains', 'train-schedules'];
    const tabBtns = {
        'station-telemetry': document.getElementById('subTabBtnStation'),
        'incoming-trains': document.getElementById('subTabBtnIncoming'),
        'transit-trains': document.getElementById('subTabBtnTransit'),
        'train-schedules': document.getElementById('subTabBtnSchedule')
    };
    const tabPanes = {
        'station-telemetry': document.getElementById('subTabStationTelemetry'),
        'incoming-trains': document.getElementById('subTabIncomingTrains'),
        'transit-trains': document.getElementById('subTabTransitTrains'),
        'train-schedules': document.getElementById('subTabTrainSchedules')
    };

    tabs.forEach(t => {
        if (tabBtns[t]) tabBtns[t].classList.toggle('active', t === tabName);
        if (tabPanes[t]) tabPanes[t].style.display = (t === tabName) ? 'block' : 'none';
    });
};

/**
 * ==========================================================================
 * 3. CONTINUOUS WARNING SOUND ALARM SYSTEM
 * High-low alternating railway warning tone synthesized using Web Audio API
 * ==========================================================================
 */
function startWarningAlarm() {
    if (isAlarmPlaying) return;
    isAlarmPlaying = true;
    isAlarmSilenced = false;

    const playTonePulse = () => {
        if (!isAlarmPlaying || isAlarmSilenced) return;
        try {
            unlockAudio();
            if (!audioCtx) return;
            const now = audioCtx.currentTime;
            const osc = audioCtx.createOscillator();
            const gain = audioCtx.createGain();
            osc.type = 'sawtooth';
            // Alternating two-tone railway warning siren (880 Hz / 660 Hz)
            osc.frequency.setValueAtTime(880, now); // A5 (880 Hz)
            osc.frequency.setValueAtTime(660, now + 0.18); // E5 (660 Hz)
            gain.gain.setValueAtTime(0.24, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.38);
            osc.connect(gain);
            gain.connect(audioCtx.destination);
            osc.start(now);
            osc.stop(now + 0.39);
        } catch (e) {
            console.log('Audio playback error:', e);
        }
    };

    playTonePulse();
    if (alarmIntervalId) clearInterval(alarmIntervalId);
    alarmIntervalId = setInterval(playTonePulse, 600);
}

function silenceAlarm() {
    isAlarmPlaying = false;
    isAlarmSilenced = true;
    if (alarmIntervalId) {
        clearInterval(alarmIntervalId);
        alarmIntervalId = null;
    }
}

window.acknowledgeAndSilenceAlarm = function() {
    silenceAlarm();
    conflictLifecycleState = 'CONTROLLER_ACKNOWLEDGED';
    renderDedicatedAlertCenter();
    showToast('Warning alarm acknowledged and silenced by controller.', 'info');
};

window.testAlarmSoundSample = function() {
    try {
        unlockAudio();
        if (!audioCtx) {
            audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        if (audioCtx.state === 'suspended') {
            audioCtx.resume();
        }
        const now = audioCtx.currentTime;
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(880, now);
        osc.frequency.setValueAtTime(660, now + 0.2);
        osc.frequency.setValueAtTime(880, now + 0.4);
        osc.frequency.setValueAtTime(660, now + 0.6);
        gain.gain.setValueAtTime(0.28, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.95);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start(now);
        osc.stop(now + 0.96);
        showToast('Sample warning alarm siren played (loud).', 'info');
    } catch (e) {
        console.log('Sample alarm error:', e);
    }
};

/**
 * ==========================================================================
 * 4. BLINKING TRACK AND TRAIN INDICATION
 * RED Blinking on Conflict Track | AMBER Blinking on Diverting Train | GREEN Blinking on Alternative Track
 * ==========================================================================
 */
function applyConflictVisualBlinking(conflictTrackNum, divertingTrainNum, altTrackNum) {
    clearConflictVisualBlinking();

    // 1. RED BLINKING: Conflicting Track
    const trackRows = document.querySelectorAll('.track-row');
    if (trackRows[conflictTrackNum - 1]) {
        trackRows[conflictTrackNum - 1].classList.add('conflict-track-blink');
    }

    // 2. AMBER BLINKING: Diverting Train
    const trainEl = document.getElementById(`train-visual-${divertingTrainNum}`);
    if (trainEl) {
        trainEl.classList.add('diverting-train-blink');
    }

    // 3. GREEN BLINKING: Recommended Alternative Track
    if (trackRows[altTrackNum - 1]) {
        trackRows[altTrackNum - 1].classList.add('alternative-track-blink');
    }

    // 4. RED SIGNAL at turnout until approved
    if (trackSimulation) {
        trackSimulation.setSignalAspect(altTrackNum, 'red');
    }
}

function clearConflictVisualBlinking() {
    document.querySelectorAll('.track-row').forEach(row => {
        row.classList.remove('conflict-track-blink');
        row.classList.remove('alternative-track-blink');
    });
    document.querySelectorAll('.animated-train-container').forEach(t => {
        t.classList.remove('diverting-train-blink');
    });
    document.querySelectorAll('.turnout-line').forEach(to => {
        to.classList.remove('turnout-active');
    });
}

/**
 * ==========================================================================
 * 2. AUTOMATIC PREDICTIVE CONFLICT GENERATOR & LIFECYCLE
 * ==========================================================================
 */
// Global Keyboard Shortcut: Ctrl + Shift + C (Simulate Conflict Scenario)
document.addEventListener('keydown', (e) => {
    const targetTag = (e.target && e.target.tagName) ? e.target.tagName.toUpperCase() : '';
    if (targetTag === 'INPUT' || targetTag === 'TEXTAREA' || targetTag === 'SELECT' || (e.target && e.target.isContentEditable)) {
        return; // Prevent accidental trigger when typing in forms
    }

    if ((e.ctrlKey || e.metaKey) && e.shiftKey && (e.key === 'C' || e.key === 'c' || e.code === 'KeyC')) {
        e.preventDefault();
        window.triggerPredictiveConflict();
        showToast('⌨️ Test Shortcut (Ctrl+Shift+C): AI conflict simulation triggered.', 'alert');
    }
});

window.triggerPredictiveConflict = function() {
    if (activeConflictData && conflictLifecycleState !== 'MONITORING' && conflictLifecycleState !== 'CONFLICT_RESOLVED') {
        return;
    }

    // Ensure trains exist in live simulation on Track 1
    let t1 = null;
    let t2 = null;

    if (trainMovementEngine && trainMovementEngine.trains.length > 0) {
        t1 = trainMovementEngine.trains.find(t => t.trainNumber === '20607' || t.trainNumber === '12674') || trainMovementEngine.trains[0];
        t2 = trainMovementEngine.trains.find(t => t.trainNumber === '16127');

        if (!t2) {
            t2 = {
                id: 299,
                trainNumber: '16127',
                trainName: 'Guruvayur Express',
                category: 'EXPRESS',
                originStationId: 1,
                originStationName: 'Chennai Egmore',
                destinationStationId: 6,
                destinationStationName: 'Guruvayur',
                currentTrackNumber: 1,
                direction: 'UP',
                status: 'MOVING',
                eta: '23:42',
                etd: '23:45',
                delayMinutes: 14,
                speedKmh: 45,
                progressPercent: 7.0,
                timeSlot: 'NIGHT',
                movementState: 'APPROACHING_JUNCTION',
                isDiverting: false,
                diversionCompleted: false
            };
            trainMovementEngine.trains.push(t2);
            trainMovementEngine.buildTrainElements();
        } else {
            t2.currentTrackNumber = 1;
            t2.direction = 'UP';
            t2.progressPercent = 7.0;
            t2.speedKmh = 45;
            t2.status = 'MOVING';
            t2.movementState = 'APPROACHING_JUNCTION';
            t2.isDiverting = false;
            t2.diversionCompleted = false;
        }

        if (t1) {
            t1.currentTrackNumber = 1;
            t1.direction = 'UP';
            t1.progressPercent = 1.0;
            t1.speedKmh = 95;
            t1.status = 'MOVING';
        }
    }

    if (!t1) {
        t1 = {
            trainNumber: '20607',
            trainName: 'Vande Bharat Express',
            category: 'EXPRESS',
            currentTrackNumber: 1,
            direction: 'UP',
            delayMinutes: 0,
            speedKmh: 110,
            eta: '23:35',
            originStationName: 'Chennai Central',
            destinationStationName: 'Katpadi Jn'
        };
    }
    if (!t2) {
        t2 = {
            trainNumber: '16127',
            trainName: 'Guruvayur Express',
            category: 'EXPRESS',
            currentTrackNumber: 1,
            direction: 'UP',
            delayMinutes: 14,
            speedKmh: 45,
            eta: '23:42',
            originStationName: 'Chennai Egmore',
            destinationStationName: 'Guruvayur'
        };
    }

    activeConflictData = {
        conflictId: 'CONF-' + Date.now().toString().slice(-6),
        severity: 'CRITICAL - CONVERGING CONTENTION',
        trackNumber: 1,
        trackName: 'Track 1 - Up Main Line',
        train1: t1,
        train2: t2,
        retainingTrain: t1,
        divertedTrain: t2,
        alternativeTrackNumber: 3,
        alternativeTrackName: 'Track 3 - Platform Loop 1',
        priorityScoreRetaining: 184,
        priorityScoreDiverted: 142,
        reason: `Train #${t1.trainNumber} (${t1.trainName}) holds highest operational priority and zero delay tolerance. Train #${t2.trainNumber} (${t2.trainName}) possesses adequate headway braking margin to execute physical turnout diversion via Switch 1A into Platform Loop Line 3, clearing Track 1 for high-speed retention.`
    };

    conflictLifecycleState = 'CONFLICT_PREDICTED';
    isAlarmSilenced = false;

    // Apply track indications
    applyConflictVisualBlinking(1, t2.trainNumber, 3);

    // Continuous warning siren
    startWarningAlarm();

    // Render alert panel
    renderDedicatedAlertCenter();

    // Show toast alert
    showToast(`🚨 AI PREDICTION: Converging contention predicted on Track 1 between #${t1.trainNumber} and #${t2.trainNumber}!`, 'alert');

    // If Automatic Dispatch protocol is active, execute dispatch after brief alert recognition (2.5s)
    if (activeDispatchOption === 'AUTOMATIC') {
        setTimeout(() => {
            if (conflictLifecycleState === 'CONFLICT_PREDICTED') {
                window.executeAutomaticDispatch();
            }
        }, 2500);
    }

    // Sync with backend
    fetch('api/conflict', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `action=simulate&division=${encodeURIComponent(currentDivision)}&stationId=${currentStationId}&trackNumber=1`
    }).catch(e => console.log('Conflict sync:', e));
};

/**
 * ==========================================================================
 * 7. DEDICATED RIGHT-SIDE AI PREDICTIVE ALERT PANEL & DECISION CENTER
 * ==========================================================================
 */
function renderDedicatedAlertCenter() {
    const container = document.getElementById('aiAlertCenterContainer');
    const badgeEl = document.getElementById('conflictLifecycleBadge');
    if (!container) return;

    if (badgeEl) {
        badgeEl.textContent = conflictLifecycleState.replace(/_/g, ' ');
        if (conflictLifecycleState === 'CONFLICT_PREDICTED') {
            badgeEl.className = 'status-badge status-red';
        } else if (conflictLifecycleState === 'CONTROLLER_ACKNOWLEDGED' || conflictLifecycleState === 'MANUAL_APPROVED') {
            badgeEl.className = 'status-badge status-amber';
        } else if (conflictLifecycleState === 'DIVERSION_IN_PROGRESS') {
            badgeEl.className = 'status-badge status-teal';
        } else if (conflictLifecycleState === 'CONFLICT_RESOLVED' || conflictLifecycleState === 'MONITORING') {
            badgeEl.className = 'status-badge status-green';
        }
    }

    // Nominal State (No manual conflict or siren buttons - fully automatic detection)
    if (!activeConflictData || conflictLifecycleState === 'MONITORING') {
        container.innerHTML = `
            <div style="background:rgba(16,185,129,0.08); border:1px solid rgba(16,185,129,0.25); border-radius:var(--radius-sm); padding:16px; font-size:0.78rem; color:#34D399; display:flex; flex-direction:column; gap:10px;">
                <div style="display:flex; align-items:center; justify-content:space-between; font-weight:800;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <span style="font-size:1.1rem;">🟢</span>
                        <span>NOMINAL TRAFFIC MONITORING</span>
                    </div>
                    <span class="status-badge status-green" style="font-size:0.65rem;">Active Interlocking</span>
                </div>
                <div style="color:var(--text-secondary); line-height:1.5;">
                    All 5 track sections clear. Automatic predictive AI monitoring active for Southern Railway corridor. System continuously monitors train positions, speeds, headway separation, and block occupancy.
                </div>
                <div style="font-size:0.7rem; color:var(--text-muted); display:flex; align-items:center; gap:6px;">
                    <span>🛡️ AI Collision Prevention &amp; Block Protection Active</span>
                </div>
            </div>
        `;
        return;
    }

    const c = activeConflictData;
    const t1 = c.train1;
    const t2 = c.train2;

    // Build Step Indicator
    const steps = [
        { id: 'CONFLICT_PREDICTED', label: '1. PREDICTED' },
        { id: 'CONTROLLER_ACKNOWLEDGED', label: '2. ACKNOWLEDGED' },
        { id: 'MANUAL_APPROVED', label: '3. DECISION' },
        { id: 'DIVERSION_IN_PROGRESS', label: '4. DIVERSION' },
        { id: 'CONFLICT_RESOLVED', label: '5. RESOLVED' }
    ];

    let stepsHtml = '<div class="lifecycle-tracker">';
    steps.forEach(s => {
        let cls = 'lifecycle-step';
        if (s.id === conflictLifecycleState) cls += ' active';
        else if (isStepCompleted(s.id, conflictLifecycleState)) cls += ' completed';
        stepsHtml += `<div class="${cls}">${s.label}</div>`;
    });
    stepsHtml += '</div>';

    // Alarm Bar HTML
    let alarmBarHtml = '';
    if (!isAlarmSilenced) {
        alarmBarHtml = `
            <div class="alarm-status-bar alarm-active">
                <span><span class="alarm-pulsing-icon">🚨</span> WARNING ALARM ACTIVE</span>
                <div style="display:flex; gap:6px; align-items:center;">
                    <button class="btn btn-warning btn-sm" onclick="window.testAlarmSoundSample()" title="Sound Siren Sample" style="padding:4px 8px; font-weight:700; font-size:0.7rem;">
                        <span>🔊 Siren</span>
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="window.acknowledgeAndSilenceAlarm()" style="padding:4px 10px; font-weight:800; font-size:0.72rem;">
                        <span>🔕 SILENCE ALARM</span>
                    </button>
                </div>
            </div>
        `;
    } else {
        alarmBarHtml = `
            <div class="alarm-status-bar alarm-silenced">
                <span>🔕 ALARM ACKNOWLEDGED &amp; MUTED</span>
                <span style="font-size:0.68rem; color:#A7F3D0;">Controller in Command</span>
            </div>
        `;
    }

    // Bottom Controller Decision Options HTML
    let decisionHtml = '';
    if (conflictLifecycleState === 'CONFLICT_PREDICTED') {
        decisionHtml = `
            <div style="background:rgba(239,68,68,0.12); border:1px dashed #EF4444; border-radius:var(--radius-sm); padding:10px; font-size:0.73rem; color:#FCA5A5; text-align:center;">
                ⚠️ <strong>Controller Action Required:</strong> Acknowledge and silence the warning alarm to unlock dispatch decisions.
            </div>
        `;
    } else if (conflictLifecycleState === 'CONTROLLER_ACKNOWLEDGED' || conflictLifecycleState === 'MANUAL_APPROVED' || conflictLifecycleState === 'HELD_AT_SIGNAL') {
        const isAuto = activeDispatchOption === 'AUTOMATIC';

        decisionHtml = `
            <div style="background:#060B16; border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:12px; display:flex; flex-direction:column; gap:10px;">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <span style="font-size:0.75rem; font-weight:700; color:var(--amber-light); text-transform:uppercase;">Select Dispatch Protocol:</span>
                </div>

                <div class="dispatch-mode-selector" style="margin-bottom:4px;">
                    <button class="mode-tab-btn ${isAuto ? 'active' : ''}" onclick="selectDispatchOption('AUTOMATIC')">
                        <span>⚡ Option A: Automatic Dispatch</span>
                    </button>
                    <button class="mode-tab-btn ${!isAuto ? 'active' : ''}" onclick="selectDispatchOption('MANUAL')">
                        <span>👨‍✈️ Option B: Manual Approval</span>
                    </button>
                </div>

                ${isAuto ? `
                    <!-- OPTION A: AUTOMATIC DISPATCH WORKFLOW -->
                    <div style="display:flex; flex-direction:column; gap:8px;">
                        <div style="font-size:0.73rem; color:var(--text-secondary); line-height:1.4;">
                            AI Recommendation: Automatically route <strong>Train #${c.divertedTrain.trainNumber}</strong> onto <strong>${c.alternativeTrackName}</strong> to clear Main Track 1.
                        </div>
                        <div style="font-size:0.7rem; color:var(--text-muted);">
                            Controller confirmation required before automated turnout lock &amp; signal clear.
                        </div>
                        <button class="btn btn-teal btn-sm" onclick="executeAutomaticDispatch()" style="padding:7px; font-weight:800;">
                            <span>⚡ Confirm &amp; Execute Automatic Dispatch</span>
                        </button>
                    </div>
                ` : `
                    <!-- OPTION B: MANUAL APPROVAL WORKFLOW -->
                    <div style="display:flex; flex-direction:column; gap:8px;">
                        <div style="font-size:0.73rem; color:var(--text-secondary); line-height:1.4;">
                            Review proposed diversion of <strong>Train #${c.divertedTrain.trainNumber}</strong> to <strong>${c.alternativeTrackName}</strong>.
                        </div>

                        ${conflictLifecycleState !== 'MANUAL_APPROVED' ? `
                            <div style="display:flex; gap:8px;">
                                <button class="btn btn-primary btn-sm" style="flex:1;" onclick="approveManualRecommendation()">
                                    <span>✅ Approve Recommendation</span>
                                </button>
                                <button class="btn btn-outline btn-sm" style="color:#F87171; border-color:rgba(239,68,68,0.4);" onclick="rejectAndHoldAtSignal()">
                                    <span>🛑 Reject &amp; Hold at Signal</span>
                                </button>
                            </div>
                        ` : `
                            <!-- Simulated Voice Announcement Console -->
                            <div class="voice-announcement-card">
                                <div style="display:flex; justify-content:space-between; align-items:center;">
                                    <span style="font-size:0.72rem; font-weight:800; color:var(--teal-light); text-transform:uppercase;">Simulated Radio Dispatch Console</span>
                                    <span class="status-badge status-teal" style="font-size:0.6rem;">Prototype TTS</span>
                                </div>
                                <div class="voice-transcript-text">
                                    "Southern Railway Central Dispatch to Pilot of Train 16127 Guruvayur Express: Priority Cheran Express retaining Main Track 1. You are authorized and diverted to Platform Loop 1 Track 3. Signal cleared to Green. Proceed with caution."
                                </div>
                                <div id="voiceBroadcastStatus" class="voice-status-pill">
                                    Ready to broadcast voice order to locomotive pilot cab...
                                </div>
                                <button class="btn btn-teal btn-sm" onclick="broadcastVoiceAnnouncement()">
                                    <span class="mic-pulse">🎙️</span> Broadcast Voice Announcement (TTS)
                                </button>
                                <button class="btn btn-primary btn-sm" style="margin-top:4px;" onclick="executeApprovedManualDiversion()">
                                    <span>🚀 Execute Manual Route Diversion</span>
                                </button>
                            </div>
                        `}
                    </div>
                `}
            </div>
        `;
    } else if (conflictLifecycleState === 'DIVERSION_IN_PROGRESS') {
        decisionHtml = `
            <div class="diversion-progress-box">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <span style="font-size:0.76rem; font-weight:800; color:#38BDF8;">⚡ DIVERSION IN PROGRESS</span>
                    <span id="diversionPctText" style="font-size:0.75rem; font-family:monospace; color:var(--amber-light); font-weight:700;">0%</span>
                </div>
                <div style="font-size:0.72rem; color:var(--text-secondary);">
                    Train #${c.divertedTrain.trainNumber} transitioning onto ${c.alternativeTrackName} via crossover turnout switch point...
                </div>
                <div class="diversion-progress-bar">
                    <div id="diversionBarFill" class="diversion-progress-fill" style="width:0%;"></div>
                </div>
            </div>
        `;
    } else if (conflictLifecycleState === 'CONFLICT_RESOLVED') {
        decisionHtml = `
            <div class="conflict-resolved-card">
                <div style="font-size:1.4rem;">🎉</div>
                <div style="font-weight:800; font-size:0.9rem; color:#34D399;">CONFLICT RESOLVED SAFELY</div>
                <div style="font-size:0.73rem; color:#E2E8F0; line-height:1.45;">
                    Train #${c.divertedTrain.trainNumber} has safely traversed <strong>${c.alternativeTrackName}</strong> and rejoined Main Track 1. Main Line Track 1 is clear for priority Train #${c.retainingTrain.trainNumber}. Full journey continuing collision-free.
                </div>
                <button class="btn btn-outline btn-sm" style="margin-top:6px;" onclick="resetConflictToMonitoring()">
                    <span>Clear Resolution &amp; Resume Normal Scanning</span>
                </button>
            </div>
        `;
    }

    container.innerHTML = `
        <!-- TOP: Alert Title, Severity, Alarm Bar -->
        <div style="display:flex; flex-direction:column; gap:8px;">
            <div style="display:flex; align-items:center; justify-content:space-between;">
                <div style="font-weight:800; color:#F87171; font-size:0.95rem; display:flex; align-items:center; gap:6px;">
                    <span>⚠️</span> TRACK CONFLICT DETECTED
                </div>
                <span class="status-badge status-red" style="font-size:0.65rem;">${c.severity}</span>
            </div>
            ${alarmBarHtml}
        </div>

        <!-- MIDDLE: Conflicting Trains & AI Decision Details -->
        <div style="background:#060B16; border:1px solid var(--border-light); border-radius:var(--radius-sm); padding:12px; display:flex; flex-direction:column; gap:8px;">
            <div style="font-size:0.7rem; color:var(--text-muted); text-transform:uppercase; font-weight:700;">CONVERGING SERVICES (TRACK 1):</div>
            
            <div style="background:rgba(15,23,42,0.6); padding:8px; border-radius:4px; font-size:0.74rem;">
                <div style="display:flex; justify-content:space-between;">
                    <strong style="color:#FFF;">#${t1.trainNumber} ${t1.trainName}</strong>
                    <span class="badge-category badge-express" style="font-size:0.6rem;">${t1.category}</span>
                </div>
                <div style="color:var(--text-secondary); font-size:0.7rem; margin-top:2px;">
                    Track 1 (Up Main) | ETA: ${t1.eta} | Delay: +${t1.delayMinutes || 4}m
                </div>
            </div>

            <div style="background:rgba(15,23,42,0.6); padding:8px; border-radius:4px; font-size:0.74rem;">
                <div style="display:flex; justify-content:space-between;">
                    <strong style="color:#FBBF24;">#${t2.trainNumber} ${t2.trainName}</strong>
                    <span class="badge-category badge-express" style="font-size:0.6rem;">${t2.category}</span>
                </div>
                <div style="color:var(--text-secondary); font-size:0.7rem; margin-top:2px;">
                    Track 1 (Up Main) | ETA: ${t2.eta} | Delay: +${t2.delayMinutes || 14}m
                </div>
            </div>

            <!-- Priority Decision -->
            <div style="border-top:1px dashed var(--border-color); padding-top:8px; margin-top:2px;">
                <div style="font-size:0.7rem; color:var(--amber-light); font-weight:700;">AI PRIORITY DECISION:</div>
                <div style="font-size:0.73rem; color:#34D399; margin-top:2px;">
                    ✓ Train #${c.retainingTrain.trainNumber} retains Track 1 (Score: ${c.priorityScoreRetaining} pts).
                </div>
                <div style="font-size:0.73rem; color:#38BDF8; margin-top:2px;">
                    ➔ Train #${c.divertedTrain.trainNumber} recommended for diversion to <strong>${c.alternativeTrackName}</strong>.
                </div>
            </div>

            <!-- Explainable Reason -->
            <div style="border-top:1px dashed var(--border-color); padding-top:8px; font-size:0.7rem; color:var(--text-secondary); line-height:1.4;">
                <span style="color:var(--text-muted); font-weight:700;">Explainable Logic:</span> ${c.reason}
            </div>
        </div>

        <!-- BOTTOM: Lifecycle & Controller Actions -->
        <div style="display:flex; flex-direction:column; gap:8px;">
            ${stepsHtml}
            ${decisionHtml}
        </div>
    `;
}

function isStepCompleted(stepId, current) {
    const order = ['CONFLICT_PREDICTED', 'CONTROLLER_ACKNOWLEDGED', 'MANUAL_APPROVED', 'DIVERSION_IN_PROGRESS', 'CONFLICT_RESOLVED'];
    return order.indexOf(stepId) < order.indexOf(current);
}

window.selectDispatchOption = function(option) {
    activeDispatchOption = option;
    renderDedicatedAlertCenter();
};

window.executeAutomaticDispatch = function() {
    if (!activeConflictData) return;
    const divTrn = activeConflictData.divertedTrain.trainNumber;
    const success = trainMovementEngine.startDiversion(divTrn, activeConflictData.trackNumber, activeConflictData.alternativeTrackNumber);
    if (!success) {
        conflictLifecycleState = 'HELD_AT_SIGNAL';
        silenceAlarm();
        renderDedicatedAlertCenter();
        showToast(`🛑 Turnout/Side track unavailable. Train #${divTrn} held at outer approach signal.`, 'alert');
        return;
    }
    conflictLifecycleState = 'DIVERSION_IN_PROGRESS';
    renderDedicatedAlertCenter();

    showToast(`⚡ Automated dispatch executed: Train #${divTrn} rerouting via physical turnout onto alternative line.`, 'success');

    // Sync with backend
    fetch('api/conflict', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `action=approve&conflictId=${encodeURIComponent(activeConflictData.conflictId)}&stationId=${currentStationId}&division=${encodeURIComponent(currentDivision)}`
    }).catch(e => console.log('Approve sync:', e));
};

window.approveManualRecommendation = function() {
    conflictLifecycleState = 'MANUAL_APPROVED';
    renderDedicatedAlertCenter();
    showToast('Diversion recommendation approved. Broadcast simulated voice order to pilot cab.', 'info');
};

window.broadcastVoiceAnnouncement = function() {
    const statusEl = document.getElementById('voiceBroadcastStatus');
    const announcement = "Southern Railway Central Dispatch to Pilot of Train 16127 Guruvayur Express: Priority Cheran Express retaining Main Track 1. You are authorized and diverted to Platform Loop 1 Track 3. Signal cleared to Green. Proceed with caution.";

    if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
        const utter = new SpeechSynthesisUtterance(announcement);
        utter.rate = 0.95;
        utter.pitch = 1.0;
        utter.volume = 1.0;

        utter.onstart = () => {
            if (statusEl) {
                statusEl.innerHTML = '<span class="mic-pulse">🎙️</span> <strong>Broadcasting simulated dispatch to cab radio...</strong>';
                statusEl.style.color = '#F59E0B';
            }
        };

        utter.onend = () => {
            if (statusEl) {
                statusEl.innerHTML = '✅ <strong>Pilot Acknowledged Voice Order (Simulated)</strong>';
                statusEl.style.color = '#34D399';
            }
        };

        utter.onerror = () => {
            if (statusEl) {
                statusEl.innerHTML = '✅ <strong>Pilot Acknowledged Voice Order (Simulated)</strong>';
                statusEl.style.color = '#34D399';
            }
        };

        window.speechSynthesis.speak(utter);
    } else {
        if (statusEl) {
            statusEl.innerHTML = '✅ <strong>Pilot Acknowledged Voice Order (Simulated)</strong>';
            statusEl.style.color = '#34D399';
        }
    }
};

window.executeApprovedManualDiversion = function() {
    if (!activeConflictData) return;
    const divTrn = activeConflictData.divertedTrain.trainNumber;
    const success = trainMovementEngine.startDiversion(divTrn, activeConflictData.trackNumber, activeConflictData.alternativeTrackNumber);
    if (!success) {
        conflictLifecycleState = 'HELD_AT_SIGNAL';
        silenceAlarm();
        renderDedicatedAlertCenter();
        showToast(`🛑 Turnout/Side track unavailable. Train #${divTrn} held at outer approach signal.`, 'alert');
        return;
    }
    conflictLifecycleState = 'DIVERSION_IN_PROGRESS';
    renderDedicatedAlertCenter();

    showToast(`🚀 Manual diversion active: Train #${divTrn} transitioning via turnout crossover.`, 'success');

    fetch('api/conflict', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `action=approve&conflictId=${encodeURIComponent(activeConflictData.conflictId)}&stationId=${currentStationId}&division=${encodeURIComponent(currentDivision)}`
    }).catch(e => console.log('Manual approve sync:', e));
};

window.runInterlockingScenario = async function(scenarioName) {
    try {
        const resp = await fetch('api/simulation', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=scenario&name=${encodeURIComponent(scenarioName)}`
        });
        const data = await resp.json();
        if (data.success && data.trains) {
            if (trainMovementEngine) {
                trainMovementEngine.setTrains(data.trains);
                trainMovementEngine.simElapsedSeconds = 0;
                trainMovementEngine.predictiveConflictTriggered = false;
                trainMovementEngine.isPaused = false;
            }
            if (scenarioName === 'C') {
                // Test C: Trigger real turnout diversion along physical side track
                setTimeout(() => {
                    if (trainMovementEngine) {
                        trainMovementEngine.startDiversion('16127', 1, 3);
                    }
                }, 1000);
            } else if (scenarioName === 'D') {
                // Test D: Attempt diversion when crossover is occupied -> should hold at signal
                setTimeout(() => {
                    if (trainMovementEngine) {
                        trainMovementEngine.startDiversion('16127', 1, 3);
                    }
                }, 1000);
            }
            const descMap = {
                'A': 'Test A: Same-Direction Safe Spacing (Train 12675 following Train 12671)',
                'B': 'Test B: Opposing Head-On Signal Hold (Single-Track Section 2 Hold)',
                'C': 'Test C: Turnout Diversion (Trk 1 -> Junction 1A -> Crossover -> Trk 3)',
                'D': 'Test D: Occupied Side Track Hold (Diversion Blocked)',
                'E': 'Test E: Multi-Train Simultaneous Corridor Transit'
            };
            const label = descMap[scenarioName] || `Scenario: ${scenarioName}`;
            showToast(`⚙️ Loaded ${label}`, 'success');
            const scBadge = document.getElementById('modalScenarioBadge');
            if (scBadge) scBadge.textContent = `Scenario: ${scenarioName}`;

            if (currentStationData) {
                updateSubSimulationTelemetry(currentStationData, null, data.trains);
            }
        }
    } catch (err) {
        console.error('Failed to load scenario:', err);
        showToast('Error switching scenario: ' + err.message, 'alert');
    }
};

window.rejectAndHoldAtSignal = function() {
    if (!activeConflictData) return;
    conflictLifecycleState = 'HELD_AT_SIGNAL';
    silenceAlarm();

    const divTrn = activeConflictData.divertedTrain.trainNumber;
    const train = trainMovementEngine.trains.find(t => t.trainNumber === divTrn);
    if (train) {
        train.status = 'HELD';
    }

    // Keep signal RED
    trackSimulation.setSignalAspect(activeConflictData.alternativeTrackNumber, 'red');

    renderDedicatedAlertCenter();
    showToast(`🛑 Train #${divTrn} ordered held at outer approach signal.`, 'alert');

    fetch('api/conflict', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `action=hold&conflictId=${encodeURIComponent(activeConflictData.conflictId)}&stationId=${currentStationId}`
    }).catch(e => console.log('Hold sync:', e));
};

window.updateDiversionProgressUI = function(pct) {
    const barEl = document.getElementById('diversionBarFill');
    const textEl = document.getElementById('diversionPctText');
    if (barEl) barEl.style.width = `${Math.floor(pct)}%`;
    if (textEl) textEl.textContent = `${Math.floor(pct)}%`;
};

window.onTrainDiversionCompleted = function(train) {
    clearConflictVisualBlinking();
    conflictLifecycleState = 'CONFLICT_RESOLVED';
    silenceAlarm();
    renderDedicatedAlertCenter();

    // Refresh sub-simulation telemetry
    if (currentStationData) {
        updateSubSimulationTelemetry(currentStationData, null, trainMovementEngine.trains);
    }

    showToast(`🎉 CONFLICT RESOLVED: Train #${train.trainNumber} traversed side track, rejoined Main Track 1, and resumed journey.`, 'success');

    // Auto-restore to nominal monitoring after 10 seconds
    setTimeout(() => {
        if (conflictLifecycleState === 'CONFLICT_RESOLVED') {
            window.resetConflictToMonitoring();
        }
    }, 10000);
};

window.resetConflictToMonitoring = function() {
    activeConflictData = null;
    conflictLifecycleState = 'MONITORING';
    silenceAlarm();
    clearConflictVisualBlinking();
    renderDedicatedAlertCenter();
    if (trainMovementEngine) {
        trainMovementEngine.predictiveConflictTriggered = false;
        trainMovementEngine.simElapsedSeconds = 0;
    }
    showToast('Simulation restored to nominal monitoring.', 'info');
};

// Toggle simulated conflict for testing modal
window.toggleSimulatedConflict = function() {
    if (conflictLifecycleState === 'MONITORING' || conflictLifecycleState === 'CONFLICT_RESOLVED') {
        window.triggerPredictiveConflict();
    } else {
        window.resetConflictToMonitoring();
    }
};

/**
 * View Switcher: Track Simulation vs Live Train Monitoring vs Analytics vs Alert History vs Schedules vs Movements
 */
window.switchView = function(viewType) {
    const containers = {
        'track': document.getElementById('dashboardViewContainer'),
        'monitoring': document.getElementById('monitoringViewContainer'),
        'analytics': document.getElementById('analyticsViewContainer'),
        'alerts': document.getElementById('alertHistoryViewContainer'),
        'scheduling': document.getElementById('schedulingViewContainer'),
        'movements': document.getElementById('movementHistoryViewContainer')
    };

    const buttons = {
        'track': document.getElementById('viewTrackBtn'),
        'monitoring': document.getElementById('viewMonitoringBtn'),
        'analytics': document.getElementById('viewAnalyticsBtn'),
        'alerts': document.getElementById('viewAlertsBtn'),
        'scheduling': document.getElementById('viewSchedulingBtn')
    };

    // Hide all containers, show the target one
    Object.keys(containers).forEach(k => {
        const c = containers[k];
        if (c) {
            c.style.display = (k === viewType) ? (k === 'track' ? 'block' : 'flex') : 'none';
        }
    });

    // Update button active styles
    Object.keys(buttons).forEach(k => {
        const b = buttons[k];
        if (b) {
            if (k === viewType) {
                b.className = 'btn btn-sm btn-teal';
                b.style.border = 'none';
            } else {
                b.className = 'btn btn-sm btn-outline';
                b.style.border = 'none';
            }
        }
    });

    // Sync sidebar active highlight
    document.querySelectorAll('.nav-item').forEach(item => {
        const p = item.dataset.page;
        if (p === viewType || (viewType === 'track' && p === 'dashboard')) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Trigger target view data loaders
    if (viewType === 'monitoring') {
        populateMonitoringRadarTable(activeTrains);
    } else if (viewType === 'analytics') {
        if (window.refreshAnalyticsDashboard) {
            window.refreshAnalyticsDashboard();
        }
    } else if (viewType === 'alerts') {
        loadAlertHistory();
    } else if (viewType === 'scheduling') {
        loadSchedulesTable();
    } else if (viewType === 'movements') {
        loadMovementsTable();
    }
};

/**
 * Populates the dedicated Live Train Monitoring Center Radar Table
 */
function populateMonitoringRadarTable(trains) {
    const tbody = document.getElementById('monitoringTableBody');
    if (!tbody) return;

    if (!trains || trains.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; color:var(--text-muted); padding:20px;">No active trains found for the selected division &amp; filters.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    trains.forEach(t => {
        const tr = document.createElement('tr');
        if (t.status === 'CONFLICT') {
            tr.className = 'conflict-warning-row';
        }

        let catClass = 'badge-express';
        if (t.category === 'PASSENGER') catClass = 'badge-passenger';
        else if (t.category === 'GOODS') catClass = 'badge-goods';
        else if (t.category === 'FREIGHT') catClass = 'badge-freight';

        let statusClass = 'status-green';
        if (t.status === 'DELAYED') statusClass = 'status-amber';
        else if (t.status === 'CONFLICT') statusClass = 'status-red';
        else if (t.status === 'MOVING') statusClass = 'status-blue';
        else if (t.status === 'ARRIVED') statusClass = 'status-teal';

        const delayDisplay = t.delayMinutes > 0 ? `+${t.delayMinutes} min` : 'On Time';
        const delayColor = t.delayMinutes > 0 ? '#F59E0B' : '#10B981';
        const pct = Math.min(100, Math.floor(t.progressPercent));

        tr.innerHTML = `
            <td>
                <strong>#${t.trainNumber}</strong>
                <div style="font-size:0.74rem; color:var(--amber-light);">${t.trainName}</div>
            </td>
            <td><span class="badge-category ${catClass}">${t.category}</span></td>
            <td><strong style="color:#FFF;">Track ${t.currentTrackNumber}</strong></td>
            <td style="font-size:0.75rem; color:var(--text-secondary);">${t.originStationName || 'Origin'} ➔ ${t.destinationStationName || 'Dest'}</td>
            <td><strong style="color:#FFF;">${t.speedKmh} km/h</strong></td>
            <td>
                <div style="display:flex; align-items:center; gap:8px;">
                    <div class="progress-track-bar">
                        <div class="progress-track-fill" style="width:${pct}%;"></div>
                    </div>
                    <span style="font-size:0.7rem; font-family:monospace;">${pct}%</span>
                </div>
            </td>
            <td>${t.eta}</td>
            <td style="font-weight:700; color:${delayColor};">${delayDisplay}</td>
            <td><span class="status-badge ${statusClass}" style="font-size:0.65rem;">${t.status.replace('_', ' ')}</span></td>
            <td>
                <button class="btn btn-outline btn-sm" style="padding:3px 8px; font-size:0.7rem;" onclick="inspectTrainOnTrack('${t.trainNumber}')">
                    <span>Inspect</span>
                </button>
            </td>
        `;

        tbody.appendChild(tr);
    });
}

window.inspectTrainOnTrack = function(trainNumber) {
    switchView('track');
    selectTrainInPanel(trainNumber);
};

/**
 * Web Audio API: Realistic Railway Dispatch Sounds (synthesized natively)
 */

window.toggleAudio = function() {
    isAudioEnabled = !isAudioEnabled;
    const icons = [document.getElementById('audioIcon'), document.getElementById('modalAudioIcon')];
    const texts = [document.getElementById('audioText'), document.getElementById('modalAudioText')];

    if (isAudioEnabled) {
        if (!audioCtx) {
            audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        icons.forEach(i => { if (i) i.textContent = '🔊'; });
        texts.forEach(t => { if (t) t.textContent = 'Audio On'; });
        showToast('Dispatch sound effects enabled.', 'info');
        playChime('chime');
    } else {
        icons.forEach(i => { if (i) i.textContent = '🔇'; });
        texts.forEach(t => { if (t) t.textContent = 'Audio Off'; });
        showToast('Dispatch sound effects muted.', 'info');
    }
};

function playChime(type) {
    if (!isAudioEnabled || !audioCtx) return;
    try {
        if (audioCtx.state === 'suspended') {
            audioCtx.resume();
        }

        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.connect(gain);
        gain.connect(audioCtx.destination);

        const now = audioCtx.currentTime;

        if (type === 'alert') {
            // Two-tone warning horn
            osc.frequency.setValueAtTime(440, now);
            osc.frequency.setValueAtTime(370, now + 0.15);
            gain.gain.setValueAtTime(0.12, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
            osc.start(now);
            osc.stop(now + 0.45);
        } else if (type === 'diversion') {
            // High chime confirmation
            osc.frequency.setValueAtTime(587.33, now); // D5
            osc.frequency.setValueAtTime(880.00, now + 0.12); // A5
            gain.gain.setValueAtTime(0.10, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.35);
            osc.start(now);
            osc.stop(now + 0.35);
        } else {
            // Nominal chime
            osc.frequency.setValueAtTime(523.25, now); // C5
            osc.frequency.setValueAtTime(659.25, now + 0.1); // E5
            gain.gain.setValueAtTime(0.08, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.3);
            osc.start(now);
            osc.stop(now + 0.3);
        }
    } catch (e) {
        console.log('Audio playback ignored:', e);
    }
}

/**
 * Select train in panel and visually highlight, updating the Controller Inspector
 */
window.selectTrainInPanel = function(trainNumber) {
    document.querySelectorAll('.train-card-item').forEach(el => el.classList.remove('selected'));
    const panelItem = document.getElementById(`panel-train-item-${trainNumber}`);
    if (panelItem) {
        panelItem.classList.add('selected');
        panelItem.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    if (trainMovementEngine) {
        trainMovementEngine.selectedTrainNumber = trainNumber;
        document.querySelectorAll('.animated-train-container').forEach(el => {
            el.classList.remove('selected-train');
        });
        const trainVisual = document.getElementById(`train-visual-${trainNumber}`);
        if (trainVisual) {
            trainVisual.classList.add('selected-train');
            trainVisual.scrollIntoView({ behavior: 'smooth', inline: 'center' });
        }
    }

    window.inspectTrainDetails(trainNumber);
};

/**
 * 3. CONTROLLER INFORMATION: Populates the Complete Train Details Inspector Panel
 */
window.inspectTrainDetails = function(trainNumber) {
    const inspectorEl = document.getElementById('trainInspectorCard');
    if (!inspectorEl) return;

    // Search both activeTrains and trainMovementEngine active states
    const train = (trainMovementEngine?.trains || []).find(t => t.trainNumber === trainNumber) ||
                  (activeTrains || []).find(t => t.trainNumber === trainNumber);

    if (!train) {
        inspectorEl.innerHTML = '<div style="color:var(--text-muted); font-size:0.75rem; padding:12px; text-align:center;">Select a train from the track display to inspect telemetry.</div>';
        return;
    }

    const isArrived = train.status === 'ARRIVED' || train.progressPercent >= 100.0;
    const isConflict = train.status === 'CONFLICT';
    const isHeld = train.status === 'HELD';

    // 1. Category styling
    let catClass = 'badge-express';
    if (train.category === 'PASSENGER') catClass = 'badge-passenger';
    else if (train.category === 'GOODS') catClass = 'badge-goods';
    else if (train.category === 'FREIGHT') catClass = 'badge-freight';

    // 2. Delay calculation
    const delayMins = train.delayMinutes || 0;
    const delayText = delayMins > 0 ? `+${delayMins} min (Delay)` : '0 min (On Time)';
    const delayColorClass = delayMins > 0 ? 'highlight-amber' : 'highlight-green';

    // 3. Direction
    const dirText = train.direction === 'UP' ? 'Eastbound (UP Line ➔)' : 'Westbound (⬅ DOWN Line)';

    // 4. Current speed
    const currentSpeedText = isArrived ? '0 km/h (Platform Dwell)' : (isHeld ? '0 km/h (Signal Hold)' : `${train.speedKmh} km/h`);

    // 5. Track section & occupancy
    const trackNames = {
        1: 'Track 1 - Up Main Line',
        2: 'Track 2 - Down Main Line',
        3: 'Track 3 - Platform Loop 1',
        4: 'Track 4 - Platform Loop 2',
        5: 'Track 5 - Freight Bypass Siding'
    };
    const trackName = trackNames[train.currentTrackNumber] || `Track ${train.currentTrackNumber}`;
    const trackOccupancyText = `Interlocked Block Active (Track ${train.currentTrackNumber})`;

    // 6. Signal Status
    let signalAspectText = '🟢 GREEN - CLEAR (Proceed)';
    let signalClass = 'highlight-green';
    if (isConflict) {
        signalAspectText = '🔴 RED - CONFLICT STOP (Hold)';
        signalClass = 'highlight-red';
    } else if (isHeld) {
        signalAspectText = '🔴 RED - CONTROLLER HOLD';
        signalClass = 'highlight-red';
    } else if (delayMins > 10) {
        signalAspectText = '🟡 AMBER - CAUTION (Aspect 2)';
        signalClass = 'highlight-amber';
    }

    // 7. STA (Scheduled Arrival Time) and ATA (Actual Arrival Time)
    const etaStr = train.eta || '23:30';
    let staStr = etaStr;
    try {
        const parts = etaStr.split(':');
        let h = parseInt(parts[0], 10);
        let m = parseInt(parts[1], 10);
        let totalM = h * 60 + m - delayMins;
        if (totalM < 0) totalM += 1440;
        const sh = String(Math.floor(totalM / 60) % 24).padStart(2, '0');
        const sm = String(totalM % 60).padStart(2, '0');
        staStr = `${sh}:${sm}`;
    } catch (e) {
        staStr = etaStr;
    }
    const ataStr = isArrived ? `${etaStr} (Arrived)` : (delayMins > 0 ? `${etaStr} (Est.)` : `${staStr} (On Schedule)`);

    const progressPct = Math.min(100, Math.floor(train.progressPercent || 0));

    inspectorEl.innerHTML = `
        <div class="inspector-header-row">
            <div>
                <div class="inspector-train-name">${train.trainName}</div>
                <div class="inspector-train-num">TRAIN #${train.trainNumber}</div>
            </div>
            <span class="badge-category ${catClass}">${train.category}</span>
        </div>

        ${isArrived ? `
            <div class="inspector-success-badge">
                ✓ SUCCESSFULLY REACHED DESTINATION (${train.destinationStationName || 'Terminal'})
            </div>
        ` : ''}

        <div class="inspector-grid">
            <div class="inspector-field">
                <span class="inspector-label">Current Section</span>
                <span class="inspector-value highlight-teal">${trackName}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Direction of Travel</span>
                <span class="inspector-value">${dirText}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Current Speed</span>
                <span class="inspector-value" id="inspSpeedVal">${currentSpeedText}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Next Station</span>
                <span class="inspector-value highlight-amber">${train.nextStationName || train.destinationStationName || 'Section Block'}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Scheduled Arrival (STA)</span>
                <span class="inspector-value">${staStr}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Estimated Arrival (ETA)</span>
                <span class="inspector-value highlight-teal">${etaStr}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Actual Arrival (ATA)</span>
                <span class="inspector-value">${ataStr}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Delay in Minutes</span>
                <span class="inspector-value ${delayColorClass}">${delayText}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Current Track Occupancy</span>
                <span class="inspector-value">${trackOccupancyText}</span>
            </div>
            <div class="inspector-field">
                <span class="inspector-label">Interlocking Signal Status</span>
                <span class="inspector-value ${signalClass}">${signalAspectText}</span>
            </div>
        </div>

        <div style="margin-top:4px;">
            <div style="display:flex; justify-content:space-between; font-size:0.68rem; color:var(--text-muted); margin-bottom:2px;">
                <span>Transit Progress:</span>
                <span id="inspProgressPct" style="font-family:monospace; color:var(--amber-light); font-weight:700;">${progressPct}%</span>
            </div>
            <div class="inspector-progress-track">
                <div id="inspProgressFill" class="inspector-progress-fill" style="width:${progressPct}%;"></div>
            </div>
        </div>
    `;
};

/**
 * High-performance sub-second DOM update for the Inspector when train is in motion
 */
window.updateLiveInspectorTelemetry = function(train) {
    const pctEl = document.getElementById('inspProgressPct');
    const fillEl = document.getElementById('inspProgressFill');
    const speedEl = document.getElementById('inspSpeedVal');

    if (pctEl && fillEl) {
        const pct = Math.min(100, Math.floor(train.progressPercent || 0));
        pctEl.textContent = `${pct}%`;
        fillEl.style.width = `${pct}%`;
    }
    if (speedEl) {
        const isArrived = train.status === 'ARRIVED' || train.progressPercent >= 100.0;
        speedEl.textContent = isArrived ? '0 km/h (Platform Dwell)' : `${train.speedKmh} km/h`;
    }
};

/**
 * Fetch and update telemetry stats
 */
async function updateTelemetryKPIs(division, stationId) {
    try {
        const resp = await fetch(`api/simulation?action=telemetry&division=${encodeURIComponent(division)}&stationId=${encodeURIComponent(stationId)}`);
        const stats = await resp.json();

        const activeTrainsEl = document.getElementById('kpiActiveTrains');
        const punctualityEl = document.getElementById('kpiPunctuality');
        const avgDelayEl = document.getElementById('kpiAvgDelay');
        const signalStatusEl = document.getElementById('kpiSignalStatus');

        if (activeTrainsEl) activeTrainsEl.textContent = stats.totalTrains;
        if (punctualityEl) punctualityEl.textContent = `${stats.punctualityPercent}%`;
        if (avgDelayEl) avgDelayEl.textContent = `${stats.avgDelayMinutes}m`;
        if (signalStatusEl) {
            if (stats.conflictCount > 0) {
                signalStatusEl.textContent = 'CAUTION';
                signalStatusEl.style.color = '#F87171';
            } else {
                signalStatusEl.textContent = 'NOMINAL';
                signalStatusEl.style.color = '#22D3EE';
            }
        }
    } catch (e) {
        console.log('KPI fetch error:', e);
    }
}

window.refreshInfoPanel = function() {
    updateRightInfoPanel(currentStationData, null, activeTrains);
};

/**
 * 7. Toast Notification System
 */
window.showToast = function(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast-item ${type}`;

    let icon = '🔔';
    if (type === 'success') icon = '🚆';
    else if (type === 'alert') icon = '⚠️';

    toast.innerHTML = `
        <span style="font-size:1.2rem;">${icon}</span>
        <div style="flex:1;">
            <div style="font-weight:700; font-size:0.78rem; text-transform:uppercase; color:var(--amber-light);">SmartRail AI Dispatch Alert</div>
            <div>${message}</div>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => {
            if (toast.remove) toast.remove();
            else if (toast.parentNode) toast.parentNode.removeChild(toast);
        }, 300);
    }, 4500);
};

/**
 * 8. Left Sidebar Navigation
 */
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            const page = item.dataset.page;
            if (!page) return;

            item.classList.toggle('expanded');

            if (page === 'dashboard') {
                switchView('track');
            } else if (page === 'monitoring') {
                switchView('monitoring');
            } else if (page === 'analytics') {
                switchView('analytics');
            } else if (page === 'alerts') {
                switchView('alerts');
            } else if (page === 'scheduling') {
                switchView('scheduling');
            } else if (page === 'train-mgmt') {
                openTrainMgmtModal();
            } else if (page === 'station-mgmt') {
                openStationMgmtModal();
            } else if (page === 'track-mgmt') {
                openTrackMgmtModal();
            } else if (page === 'ai-dispatch') {
                showModal('aiDispatchModal');
            } else {
                showModal('genericPhase2Modal', item.querySelector('.nav-item-left span')?.textContent);
            }
        });
    });
}

/**
 * 9. Modals (Add Train, Schedule Update, Fleet, Stations, Tracks)
 */
function initModals() {
    const addTrainBtn = document.getElementById('openAddTrainModalBtn');
    if (addTrainBtn) {
        addTrainBtn.addEventListener('click', () => {
            showModal('addTrainModal');
        });
    }

    const addTrainForm = document.getElementById('addTrainForm');
    if (addTrainForm) {
        addTrainForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(addTrainForm);
            const params = new URLSearchParams(formData);

            try {
                const resp = await fetch('api/trains', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params.toString()
                });
                const result = await resp.json();
                if (result.success) {
                    playChime('chime');
                    window.showToast('New simulated train registered in database.', 'success');
                    closeModal('addTrainModal');
                    addTrainForm.reset();
                    await refreshWorkspace();
                } else {
                    alert('Error adding train: ' + (result.error || result.message));
                }
            } catch (err) {
                console.error('Failed to submit train:', err);
            }
        });
    }

    // Schedule update form handler
    const editScheduleForm = document.getElementById('editScheduleForm');
    if (editScheduleForm) {
        editScheduleForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(editScheduleForm);
            const params = new URLSearchParams(formData);

            try {
                const resp = await fetch('api/schedules', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params.toString()
                });
                const res = await resp.json();
                if (res.success) {
                    playChime('chime');
                    window.showToast('Schedule updated successfully in database!', 'success');
                    closeModal('editScheduleModal');
                    loadSchedulesTable();
                } else {
                    alert('Error updating schedule: ' + res.error);
                }
            } catch (err) {
                console.error('Failed to update schedule:', err);
            }
        });
    }
}

/**
 * ==========================================================================
 * 10. ALERT HISTORY CONTROLLER
 * ==========================================================================
 */
let cachedAlertHistory = [];

window.loadAlertHistory = async function() {
    const tbody = document.getElementById('alertHistoryTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:18px; color:var(--text-muted);">Loading alert audit records from database...</td></tr>';

    try {
        const resp = await fetch('api/alert-history');
        cachedAlertHistory = await resp.json();
        renderAlertHistoryTable(cachedAlertHistory);
    } catch (err) {
        console.error('Failed to load alert history:', err);
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:18px; color:#F87171;">Failed to load alert history.</td></tr>';
    }
};

function renderAlertHistoryTable(alerts) {
    const tbody = document.getElementById('alertHistoryTableBody');
    if (!tbody) return;

    if (!alerts || alerts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--text-muted);">No alert records in database audit log.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    alerts.forEach(a => {
        const tr = document.createElement('tr');

        let statusClass = 'badge-status-auto';
        if (a.dispatchStatus === 'CONTROLLER_APPROVED') statusClass = 'badge-status-controller';
        else if (a.dispatchStatus === 'HELD_AT_SIGNAL') statusClass = 'badge-status-held';
        else if (a.dispatchStatus === 'PENDING_APPROVAL') statusClass = 'badge-status-pending';

        let sevColor = '#38BDF8';
        if (a.severity === 'CRITICAL' || a.severity === 'RED') sevColor = '#F87171';
        else if (a.severity === 'HIGH' || a.severity === 'AMBER') sevColor = '#FBBF24';
        else if (a.severity === 'TEAL' || a.severity === 'GREEN') sevColor = '#34D399';

        tr.innerHTML = `
            <td><strong style="color:var(--amber-light);">${a.alertCode || 'ALT-000'}</strong></td>
            <td><strong style="color:#FFF;">#${a.trainNumber || 'N/A'}</strong></td>
            <td><span style="color:${sevColor}; font-weight:600;">${a.conflictType || 'Operational'}</span></td>
            <td style="font-size:0.75rem; color:var(--text-secondary);">${a.timeDetected || 'Just now'}</td>
            <td style="font-size:0.78rem; color:#FFF;">${a.recommendedAction || '-'}</td>
            <td><span class="${statusClass}">${(a.dispatchStatus || 'LOGGED').replace(/_/g, ' ')}</span></td>
            <td><span class="station-code-pill" style="font-size:0.65rem;">${a.stationCode || 'MAS'} T${a.trackNumber || 1}</span></td>
        `;
        tbody.appendChild(tr);
    });
}

window.filterAlertHistoryTable = function() {
    const q = (document.getElementById('alertHistorySearch')?.value || '').toLowerCase();
    const filtered = cachedAlertHistory.filter(a => {
        const trn = (a.trainNumber || '').toLowerCase();
        const code = (a.alertCode || '').toLowerCase();
        const type = (a.conflictType || '').toLowerCase();
        const act = (a.recommendedAction || '').toLowerCase();
        return trn.includes(q) || code.includes(q) || type.includes(q) || act.includes(q);
    });
    renderAlertHistoryTable(filtered);
};

/**
 * ==========================================================================
 * 11. TRAIN SCHEDULING CONTROLLER
 * ==========================================================================
 */
let cachedSchedules = [];

window.loadSchedulesTable = async function() {
    const tbody = document.getElementById('schedulingTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:18px; color:var(--text-muted);">Loading timetable schedules from database...</td></tr>';

    try {
        const resp = await fetch('api/schedules');
        cachedSchedules = await resp.json();
        renderSchedulesTable(cachedSchedules);
    } catch (err) {
        console.error('Failed to load schedules:', err);
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:18px; color:#F87171;">Failed to load schedules.</td></tr>';
    }
};

function renderSchedulesTable(schedules) {
    const tbody = document.getElementById('schedulingTableBody');
    if (!tbody) return;

    if (!schedules || schedules.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:20px; color:var(--text-muted);">No schedules configured in database.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    schedules.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <strong style="color:#FFF;">#${s.trainNumber}</strong>
                <div style="font-size:0.72rem; color:var(--amber-light);">${s.trainName}</div>
            </td>
            <td>
                <strong>${s.stationName}</strong>
                <span class="station-code-pill" style="font-size:0.62rem; margin-left:4px;">${s.stationCode}</span>
            </td>
            <td style="color:#34D399; font-weight:700;">${s.scheduledArrival}</td>
            <td style="color:#38BDF8; font-weight:700;">${s.scheduledDeparture}</td>
            <td><strong style="color:var(--amber-light);">Platform ${s.platformNumber}</strong></td>
            <td>Stop #${s.stopSequence}</td>
            <td>${s.haltDurationMinutes} min</td>
            <td>
                <button class="btn btn-outline btn-sm" style="padding:2px 8px; font-size:0.7rem;" onclick="openEditScheduleModal(${s.id}, '${s.trainNumber}', '${s.trainName.replace(/'/g, "\\'")}', '${s.stationCode}', '${s.stationName.replace(/'/g, "\\'")}', '${s.scheduledArrival}', '${s.scheduledDeparture}', ${s.platformNumber})">
                    <span>✏️ Edit</span>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

window.filterScheduleTable = function() {
    const q = (document.getElementById('scheduleSearchInput')?.value || '').toLowerCase();
    const filtered = cachedSchedules.filter(s => {
        return (s.trainNumber || '').toLowerCase().includes(q) ||
               (s.trainName || '').toLowerCase().includes(q) ||
               (s.stationName || '').toLowerCase().includes(q) ||
               (s.stationCode || '').toLowerCase().includes(q);
    });
    renderSchedulesTable(filtered);
};

window.openEditScheduleModal = function(id, trainNo, trainName, stationCode, stationName, arr, dep, plat) {
    document.getElementById('editScheduleId').value = id;
    document.getElementById('editScheduleTrainLabel').textContent = `#${trainNo} - ${trainName}`;
    document.getElementById('editScheduleStationLabel').textContent = `${stationName} (${stationCode})`;
    document.getElementById('editScheduleArrival').value = arr;
    document.getElementById('editScheduleDeparture').value = dep;
    document.getElementById('editSchedulePlatform').value = plat;
    showModal('editScheduleModal');
};

/**
 * ==========================================================================
 * 12. TRAIN MOVEMENT TELEMETRY HISTORY CONTROLLER
 * ==========================================================================
 */
window.loadMovementsTable = async function() {
    const tbody = document.getElementById('movementHistoryTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:18px; color:var(--text-muted);">Streaming section telemetry from database...</td></tr>';

    try {
        const resp = await fetch('api/movements');
        const movements = await resp.json();

        if (!movements || movements.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:20px; color:var(--text-muted);">No telemetry logs found.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        movements.forEach(m => {
            const tr = document.createElement('tr');
            let statusBadge = '<span class="status-badge status-blue" style="font-size:0.65rem;">IN TRANSIT</span>';
            if (m.movementStatus === 'DIVERTED') statusBadge = '<span class="status-badge status-teal" style="font-size:0.65rem;">DIVERTED</span>';
            else if (m.movementStatus === 'HELD') statusBadge = '<span class="status-badge status-amber" style="font-size:0.65rem;">HELD</span>';
            else if (m.movementStatus === 'ARRIVED') statusBadge = '<span class="status-badge status-green" style="font-size:0.65rem;">ARRIVED</span>';

            tr.innerHTML = `
                <td style="font-size:0.75rem; color:var(--text-secondary);">${m.timestamp}</td>
                <td><strong style="color:var(--amber-light);">#${m.trainNumber}</strong></td>
                <td><span class="station-code-pill" style="font-size:0.65rem;">${m.stationCode}</span></td>
                <td><strong style="color:#FFF;">Track ${m.trackNumber}</strong></td>
                <td>${m.speedKmh} km/h</td>
                <td>${Math.round(m.progressPercent)}%</td>
                <td>${statusBadge}</td>
                <td style="font-size:0.75rem; color:var(--text-secondary);">${m.notes || '-'}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error('Failed to load movements:', err);
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:18px; color:#F87171;">Failed to load movement telemetry.</td></tr>';
    }
};

/**
 * ==========================================================================
 * 13. TRAIN FLEET MANAGEMENT MODAL
 * ==========================================================================
 */
let cachedFleetTrains = [];

window.openTrainMgmtModal = async function() {
    showModal('trainMgmtModal');
    const tbody = document.getElementById('trainMgmtTableBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:18px; color:var(--text-muted);">Loading Tamil Nadu fleet from database...</td></tr>';

    try {
        const resp = await fetch('api/trains?division=ALL');
        cachedFleetTrains = await resp.json();
        renderTrainMgmtTable(cachedFleetTrains);
    } catch (err) {
        console.error('Failed to load fleet trains:', err);
    }
};

function renderTrainMgmtTable(trains) {
    const tbody = document.getElementById('trainMgmtTableBody');
    if (!tbody) return;

    if (!trains || trains.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:20px; color:var(--text-muted);">No trains registered.</td></tr>';
        return;
    }

    tbody.innerHTML = '';
    trains.forEach(t => {
        const tr = document.createElement('tr');
        let catClass = 'badge-express';
        if (t.category === 'PASSENGER') catClass = 'badge-passenger';
        else if (t.category === 'GOODS') catClass = 'badge-goods';
        else if (t.category === 'FREIGHT') catClass = 'badge-freight';

        tr.innerHTML = `
            <td>
                <strong style="color:#FFF;">#${t.trainNumber}</strong>
                <div style="font-size:0.72rem; color:var(--amber-light);">${t.trainName}</div>
            </td>
            <td><span class="badge-category ${catClass}">${t.category}</span></td>
            <td style="font-size:0.75rem;">${t.originStationName || 'Origin'} ➔ ${t.destinationStationName || 'Dest'}</td>
            <td>${t.currentStationName || 'Section'}</td>
            <td><strong style="color:#FFF;">Track ${t.currentTrackNumber}</strong></td>
            <td>${t.speedKmh} km/h</td>
            <td>${t.eta}</td>
            <td>
                <button class="btn btn-outline btn-sm" style="padding:2px 8px; font-size:0.7rem; color:var(--amber-light);" onclick="quickDelayTrain('${t.trainNumber}', 15)">
                    <span>+15m Delay</span>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

window.filterTrainMgmtTable = function() {
    const q = (document.getElementById('trainMgmtSearch')?.value || '').toLowerCase();
    const cat = document.getElementById('trainMgmtCategoryFilter')?.value || 'ALL';

    const filtered = cachedFleetTrains.filter(t => {
        const matchesQ = (t.trainNumber || '').toLowerCase().includes(q) || (t.trainName || '').toLowerCase().includes(q);
        const matchesCat = (cat === 'ALL' || t.category === cat);
        return matchesQ && matchesCat;
    });
    renderTrainMgmtTable(filtered);
};

window.quickDelayTrain = async function(trainNumber, minutes) {
    try {
        const resp = await fetch('api/trains', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=delay&trainNumber=${encodeURIComponent(trainNumber)}&delayMinutes=${minutes}&status=DELAYED`
        });
        const res = await resp.json();
        if (res.success) {
            playChime('alert');
            window.showToast(`Train #${trainNumber} delay updated to +${minutes}m. Status set to DELAYED.`, 'alert');
            await refreshWorkspace();
            openTrainMgmtModal();
        }
    } catch (err) {
        console.error('Failed to update delay:', err);
    }
};

/**
 * ==========================================================================
 * 14. STATION INFRASTRUCTURE INSPECTOR MODAL
 * ==========================================================================
 */
window.openStationMgmtModal = async function() {
    showModal('stationMgmtModal');
    const grid = document.getElementById('stationMgmtGrid');
    if (!grid) return;
    grid.innerHTML = '<div style="color:var(--text-muted); padding:20px; text-align:center;">Loading station infrastructure...</div>';

    try {
        const resp = await fetch(`api/stations?division=${encodeURIComponent(currentDivision)}`);
        const stations = await resp.json();

        grid.innerHTML = '';
        stations.forEach(s => {
            const card = document.createElement('div');
            card.style.background = '#060B16';
            card.style.border = '1px solid var(--border-color)';
            card.style.borderRadius = 'var(--radius-sm)';
            card.style.padding = '12px';
            card.style.display = 'flex';
            card.style.flexDirection = 'column';
            card.style.gap = '6px';

            card.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <span style="font-weight:700; color:#FFF; font-size:0.85rem;">${s.name}</span>
                    <span class="station-code-pill">${s.code}</span>
                </div>
                <div style="font-size:0.72rem; color:var(--text-secondary); margin-top:2px;">
                    Type: <strong style="color:var(--teal-light);">${s.junctionType || 'SSI Interlocking'}</strong>
                </div>
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:6px; font-size:0.7rem; color:var(--text-muted); margin-top:4px;">
                    <div>Platforms: <strong style="color:#FFF;">${s.platformsCount || 6}</strong></div>
                    <div>Tracks: <strong style="color:#FFF;">${s.tracksCount || 5} Lines</strong></div>
                </div>
                <button class="btn btn-primary btn-sm" style="margin-top:8px; width:100%; font-size:0.72rem; padding:4px;" onclick="closeModal('stationMgmtModal'); selectStationFromInspector(${s.id});">
                    <span>Inspect On Central Display</span>
                </button>
            `;
            grid.appendChild(card);
        });
    } catch (err) {
        console.error('Failed to load stations:', err);
    }
};

window.selectStationFromInspector = async function(stationId) {
    const sel = document.getElementById('filterStation');
    if (sel) sel.value = stationId;
    currentStationId = stationId;
    switchView('track');
    await refreshWorkspace();
};

/**
 * ==========================================================================
 * 15. TRACK INTERLOCKING & MAINTENANCE CONTROLLER MODAL
 * ==========================================================================
 */
window.openTrackMgmtModal = async function() {
    showModal('trackMgmtModal');
    const list = document.getElementById('trackMgmtList');
    if (!list) return;
    list.innerHTML = '<div style="color:var(--text-muted); padding:16px; text-align:center;">Loading line blocks...</div>';

    try {
        const resp = await fetch(`api/tracks?stationId=${encodeURIComponent(currentStationId)}`);
        const tracks = await resp.json();

        list.innerHTML = '';
        tracks.forEach(trk => {
            const row = document.createElement('div');
            row.style.background = '#060B16';
            row.style.border = '1px solid var(--border-color)';
            row.style.borderRadius = 'var(--radius-sm)';
            row.style.padding = '10px 14px';
            row.style.display = 'flex';
            row.style.justifyContent = 'space-between';
            row.style.alignItems = 'center';

            let statusBadge = `<span class="status-badge status-green">${trk.status}</span>`;
            if (trk.status === 'OCCUPIED') statusBadge = `<span class="status-badge status-amber">OCCUPIED</span>`;
            else if (trk.status === 'MAINTENANCE') statusBadge = `<span class="status-badge status-red">MAINTENANCE BLOCK</span>`;

            row.innerHTML = `
                <div>
                    <div style="font-weight:700; color:#FFF; font-size:0.85rem;">Track ${trk.trackNumber}: ${trk.trackName}</div>
                    <div style="font-size:0.72rem; color:var(--text-secondary);">Max Speed: ${trk.maxSpeedKmh} km/h | Length: ${trk.lengthMeters}m</div>
                </div>
                <div style="display:flex; align-items:center; gap:10px;">
                    ${statusBadge}
                    <button class="btn btn-outline btn-sm" style="font-size:0.7rem; padding:3px 8px; color:var(--amber-light);" onclick="toggleTrackStatus(${trk.trackNumber}, '${trk.status === 'MAINTENANCE' ? 'CLEAR' : 'MAINTENANCE'}')">
                        <span>${trk.status === 'MAINTENANCE' ? 'Clear Block' : 'Flag Maintenance'}</span>
                    </button>
                </div>
            `;
            list.appendChild(row);
        });
    } catch (err) {
        console.error('Failed to load tracks:', err);
    }
};

window.toggleTrackStatus = async function(trackNumber, newStatus) {
    try {
        const resp = await fetch('api/tracks', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=updateStatus&stationId=${currentStationId}&trackNumber=${trackNumber}&status=${newStatus}`
        });
        const res = await resp.json();
        if (res.success) {
            playChime('chime');
            window.showToast(`Track ${trackNumber} status set to ${newStatus}.`, 'info');
            openTrackMgmtModal();
            await refreshWorkspace();
        }
    } catch (err) {
        console.error('Failed to toggle track status:', err);
    }
};

function showModal(modalId, featureName) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    if (featureName && modalId === 'genericPhase2Modal') {
        const titleEl = document.getElementById('phase2ModalTitle');
        if (titleEl) titleEl.textContent = featureName;
    }
    modal.style.display = 'flex';
}

window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.style.display = 'none';
};
