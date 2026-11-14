/**
 * SmartRail AI - Analytics Dashboard Engine (Chart.js 4.x)
 * Visualizes Southern Railway (Tamil Nadu Zone) Telemetry:
 * Chart A: Train Delay Analysis (Compare delays across trains)
 * Chart B: Train Traffic Analysis (Operating volume by time window)
 * Chart C: Train Punctuality (On-time vs Delayed proportions)
 * Chart D: Track Utilization (Occupancy rate across tracks 1 to 5)
 * Chart E: Train Category Analysis (Passenger, Express, Goods, Freight)
 * Chart F: Diversion Analysis (Conflicts detected vs Diversions vs Holds)
 */

(function () {
    'use strict';

    let chartA = null;
    let chartB = null;
    let chartC = null;
    let chartD = null;
    let chartE = null;
    let chartF = null;

    // Control Room Chart Default Theme
    const THEME = {
        fontFamily: "'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif",
        textColor: '#94A3B8',
        textMuted: '#64748B',
        gridColor: 'rgba(255, 255, 255, 0.06)',
        amber: '#F59E0B',
        amberGlow: 'rgba(245, 158, 11, 0.4)',
        teal: '#06B6D4',
        tealGlow: 'rgba(6, 182, 212, 0.4)',
        emerald: '#10B981',
        emeraldGlow: 'rgba(16, 185, 129, 0.4)',
        rose: '#EF4444',
        roseGlow: 'rgba(239, 68, 68, 0.4)',
        sky: '#38BDF8',
        purple: '#A855F7',
        navyCard: '#0B1325'
    };

    function safeVal(val, fallback) {
        if (val === null || val === undefined || isNaN(val)) {
            return fallback !== undefined ? fallback : 0;
        }
        return val;
    }

    async function fetchAnalyticsData() {
        try {
            const resp = await fetch('api/analytics');
            if (!resp.ok) throw new Error('HTTP ' + resp.status);
            const data = await resp.json();
            return data;
        } catch (err) {
            console.warn('[SmartRail Analytics] Using initial bootstrap telemetry:', err);
            return null;
        }
    }

    async function initAnalyticsCharts() {
        if (typeof Chart === 'undefined') {
            console.warn('[SmartRail Analytics] Chart.js not yet loaded.');
            return;
        }

        // Configure Chart.js global defaults
        Chart.defaults.color = THEME.textColor;
        Chart.defaults.font.family = THEME.fontFamily;
        Chart.defaults.plugins.tooltip.backgroundColor = 'rgba(11, 19, 37, 0.95)';
        Chart.defaults.plugins.tooltip.borderColor = 'rgba(6, 182, 212, 0.3)';
        Chart.defaults.plugins.tooltip.borderWidth = 1;
        Chart.defaults.plugins.tooltip.titleColor = '#F8FAFC';
        Chart.defaults.plugins.tooltip.bodyColor = '#CBD5E1';
        Chart.defaults.plugins.tooltip.padding = 10;
        Chart.defaults.plugins.tooltip.cornerRadius = 6;

        const data = await fetchAnalyticsData();
        renderAllCharts(data);
    }

    function renderAllCharts(data) {
        renderChartA_Delays(data ? data.delayAnalysis : null);
        renderChartB_Traffic(data ? data.trafficAnalysis : null);
        renderChartC_Punctuality(data ? data.punctualityAnalysis : null);
        renderChartD_TrackUtilization(data ? data.trackUtilization : null);
        renderChartE_Categories(data ? data.categoryAnalysis : null);
        renderChartF_Diversions(data ? data.diversionAnalysis : null);
    }

    /**
     * CHART A: Train Delay Analysis
     * Horizontal or vertical bar chart comparing delays (minutes) across trains.
     */
    function renderChartA_Delays(delayData) {
        const ctx = document.getElementById('chartDelayAnalysis');
        if (!ctx) return;

        let labels = [];
        let values = [];
        let bgColors = [];
        let borderColors = [];

        if (delayData && delayData.length > 0) {
            delayData.forEach(d => {
                const trnNo = d.trainNumber || 'Unknown';
                const delay = safeVal(d.delayMinutes, 0);
                labels.push(`${trnNo}`);
                values.push(delay);
                if (delay > 15) {
                    bgColors.push('rgba(239, 68, 68, 0.6)');
                    borderColors.push('#EF4444');
                } else if (delay > 0) {
                    bgColors.push('rgba(245, 158, 11, 0.6)');
                    borderColors.push('#F59E0B');
                } else {
                    bgColors.push('rgba(16, 185, 129, 0.6)');
                    borderColors.push('#10B981');
                }
            });
        } else {
            labels = ['12675 Kovai', 'G-5540 Cargo', 'F-6210 Agri', '56001 Katpadi', '20635 Ananth.', '20607 VB', '12637 Pandian'];
            values = [14, 22, 18, 12, 8, 0, 0];
            bgColors = ['rgba(245, 158, 11, 0.6)', 'rgba(239, 68, 68, 0.6)', 'rgba(239, 68, 68, 0.6)', 'rgba(245, 158, 11, 0.6)', 'rgba(245, 158, 11, 0.6)', 'rgba(16, 185, 129, 0.6)', 'rgba(16, 185, 129, 0.6)'];
            borderColors = ['#F59E0B', '#EF4444', '#EF4444', '#F59E0B', '#F59E0B', '#10B981', '#10B981'];
        }

        if (chartA) chartA.destroy();

        chartA = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Delay Duration (Minutes)',
                    data: values,
                    backgroundColor: bgColors,
                    borderColor: borderColors,
                    borderWidth: 1.5,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: THEME.textColor, font: { size: 10 } }
                    },
                    y: {
                        beginAtZero: true,
                        grid: { color: THEME.gridColor },
                        ticks: {
                            color: THEME.textColor,
                            callback: val => val + ' m'
                        }
                    }
                }
            }
        });
    }

    /**
     * CHART B: Train Traffic Analysis
     * Line / Area chart showing number of trains operating at different times.
     */
    function renderChartB_Traffic(trafficData) {
        const ctx = document.getElementById('chartTrafficAnalysis');
        if (!ctx) return;

        let labels = ['00:00 - 04:00', '04:00 - 08:00', '08:00 - 12:00', '12:00 - 16:00', '16:00 - 20:00', '20:00 - 24:00'];
        let values = [4, 9, 14, 8, 15, 18];

        if (trafficData && trafficData.labels && trafficData.trainCounts) {
            labels = trafficData.labels.map(l => l.split(' ')[0] + ' ' + (l.split(' ')[2] || ''));
            values = trafficData.trainCounts.map(v => safeVal(v, 0));
        }

        if (chartB) chartB.destroy();

        chartB = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Operating Trains (Section Density)',
                    data: values,
                    borderColor: THEME.teal,
                    backgroundColor: 'rgba(6, 182, 212, 0.15)',
                    pointBackgroundColor: THEME.amber,
                    pointBorderColor: '#FFF',
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    fill: true,
                    tension: 0.35,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { color: THEME.gridColor },
                        ticks: { color: THEME.textColor, font: { size: 10 } }
                    },
                    y: {
                        beginAtZero: true,
                        grid: { color: THEME.gridColor },
                        ticks: {
                            color: THEME.textColor,
                            stepSize: 3,
                            callback: val => val + ' trains'
                        }
                    }
                }
            }
        });
    }

    /**
     * CHART C: Train Punctuality
     * Doughnut chart comparing on-time and delayed trains.
     */
    function renderChartC_Punctuality(puncData) {
        const ctx = document.getElementById('chartPunctuality');
        if (!ctx) return;

        let onTime = 13;
        let delayed = 5;

        if (puncData) {
            onTime = safeVal(puncData.onTimeCount, 13);
            delayed = safeVal(puncData.delayedCount, 5);
        }

        const total = onTime + delayed;
        const rate = total > 0 ? ((onTime / total) * 100).toFixed(1) : '94.2';

        const centerTextElem = document.getElementById('puncRateCenterText');
        if (centerTextElem) {
            centerTextElem.textContent = rate + '%';
        }

        if (chartC) chartC.destroy();

        chartC = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['On Time / Nominal', 'Delayed (>0 min)'],
                datasets: [{
                    data: [onTime, delayed],
                    backgroundColor: [
                        'rgba(16, 185, 129, 0.8)',
                        'rgba(245, 158, 11, 0.8)'
                    ],
                    borderColor: [THEME.emerald, THEME.amber],
                    borderWidth: 2,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            boxWidth: 12,
                            padding: 10,
                            font: { size: 11 },
                            color: THEME.textColor
                        }
                    }
                }
            }
        });
    }

    /**
     * CHART D: Track Utilization
     * Displays how frequently railway tracks (1 to 5) are occupied.
     */
    function renderChartD_TrackUtilization(trackData) {
        const ctx = document.getElementById('chartTrackUtilization');
        if (!ctx) return;

        let labels = ['Track 1 (Up)', 'Track 2 (Dn)', 'Track 3 (Loop 1)', 'Track 4 (Loop 2)', 'Track 5 (Siding)'];
        let percentages = [88.5, 76.0, 48.0, 32.5, 62.0];

        if (trackData && trackData.labels && trackData.utilizationPercentages) {
            labels = trackData.labels.map(l => l.replace('Track ', 'T'));
            percentages = trackData.utilizationPercentages.map(p => safeVal(p, 0));
        }

        if (chartD) chartD.destroy();

        chartD = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Utilization Frequency (%)',
                    data: percentages,
                    backgroundColor: [
                        'rgba(6, 182, 212, 0.65)',
                        'rgba(56, 189, 248, 0.65)',
                        'rgba(16, 185, 129, 0.65)',
                        'rgba(168, 85, 247, 0.65)',
                        'rgba(245, 158, 11, 0.65)'
                    ],
                    borderColor: [THEME.teal, THEME.sky, THEME.emerald, THEME.purple, THEME.amber],
                    borderWidth: 1.5,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: THEME.textColor, font: { size: 10 } }
                    },
                    y: {
                        beginAtZero: true,
                        max: 100,
                        grid: { color: THEME.gridColor },
                        ticks: {
                            color: THEME.textColor,
                            callback: val => val + '%'
                        }
                    }
                }
            }
        });
    }

    /**
     * CHART E: Train Category Analysis
     * Compares passenger, express, goods and freight trains.
     */
    function renderChartE_Categories(catData) {
        const ctx = document.getElementById('chartCategoryAnalysis');
        if (!ctx) return;

        let labels = ['Express', 'Passenger', 'Goods', 'Freight'];
        let counts = [7, 5, 3, 3];

        if (catData && catData.categories && catData.counts) {
            labels = catData.categories.map(c => c.charAt(0) + c.slice(1).toLowerCase());
            counts = catData.counts.map(cnt => safeVal(cnt, 0));
        }

        if (chartE) chartE.destroy();

        chartE = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        'rgba(245, 158, 11, 0.8)',
                        'rgba(6, 182, 212, 0.8)',
                        'rgba(168, 85, 247, 0.8)',
                        'rgba(56, 189, 248, 0.8)'
                    ],
                    borderColor: '#060B16',
                    borderWidth: 2,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: {
                            boxWidth: 12,
                            padding: 8,
                            font: { size: 11 },
                            color: THEME.textColor
                        }
                    }
                }
            }
        });
    }

    /**
     * CHART F: Diversion Analysis
     * Shows number of detected conflicts and recommended diversions.
     */
    function renderChartF_Diversions(divData) {
        const ctx = document.getElementById('chartDiversionAnalysis');
        if (!ctx) return;

        let detected = 4;
        let automatic = 3;
        let controllerApproved = 1;
        let signalHolds = 1;

        if (divData) {
            detected = safeVal(divData.detectedConflicts, 4);
            automatic = safeVal(divData.automaticDiversions, 3);
            controllerApproved = safeVal(divData.controllerApprovedDiversions, 1);
            signalHolds = safeVal(divData.signalHolds, 1);
        }

        if (chartF) chartF.destroy();

        chartF = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Detected Conflicts', 'Auto Diversions', 'Controller Approved', 'Signal Holds'],
                datasets: [{
                    label: 'Audit Events',
                    data: [detected, automatic, controllerApproved, signalHolds],
                    backgroundColor: [
                        'rgba(239, 68, 68, 0.7)',
                        'rgba(6, 182, 212, 0.7)',
                        'rgba(16, 185, 129, 0.7)',
                        'rgba(245, 158, 11, 0.7)'
                    ],
                    borderColor: [THEME.rose, THEME.teal, THEME.emerald, THEME.amber],
                    borderWidth: 1.5,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: THEME.textColor, font: { size: 9 } }
                    },
                    y: {
                        beginAtZero: true,
                        grid: { color: THEME.gridColor },
                        ticks: {
                            color: THEME.textColor,
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }

    // Expose for dashboard refresh
    window.refreshAnalyticsDashboard = async function () {
        const data = await fetchAnalyticsData();
        renderAllCharts(data);
    };

    // Auto-init on DOMContentLoaded or immediate if document ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAnalyticsCharts);
    } else {
        initAnalyticsCharts();
    }

})();

