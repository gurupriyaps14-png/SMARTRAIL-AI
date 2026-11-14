/**
 * SmartRail AI - Live Train Movement & Interlocking Animation Engine
 * Java Backend Synchronized Source of Truth for:
 * - Anti-collision safe spacing between same-direction trains (Test A)
 * - Single-track opposite-direction interlocking holds (Test B)
 * - True physical turnout crossover side-track kinematics from Track 1 to Track 3 (Test C)
 * - Occupied side track hold prevention (Test D)
 * - Multi-train continuous collision-free transit (Test E)
 */

class TrainMovementEngine {
    constructor(trackSim) {
        this.trackSim = trackSim;
        this.trains = [];
        this.isRunning = false;
        this.speedMultiplier = 1.0;
        this.animationFrameId = null;
        this.lastTimestamp = null;
        this.trainElements = new Map();
        this.notifiedArrivals = new Set();
        this.selectedTrainNumber = null;
        this.lastSyncTime = 0;
        this.simElapsedSeconds = 0;
        this.predictiveConflictTriggered = false;
        this.lastScenario = 'DEFAULT';
    }

    setTrains(trainsData) {
        const existingMap = new Map();
        this.trains.forEach(t => {
            existingMap.set(t.trainNumber, {
                progressPercent: t.progressPercent,
                direction: t.direction,
                status: t.status,
                dwellRemaining: t.dwellRemaining,
                isDiverting: t.isDiverting,
                diversionProgress: t.diversionProgress,
                fromTrack: t.fromTrack,
                toTrack: t.toTrack,
                signalAspect: t.signalAspect,
                spacingStatus: t.spacingStatus
            });
        });

        this.trains = JSON.parse(JSON.stringify(trainsData || []));

        this.trains.forEach(t => {
            if (existingMap.has(t.trainNumber)) {
                const prev = existingMap.get(t.trainNumber);
                t.progressPercent = prev.progressPercent;
                t.direction = prev.direction;
                if (prev.status === 'ARRIVED' || prev.status === 'HELD' || prev.status === 'DIVERSION_IN_PROGRESS') {
                    t.status = prev.status;
                }
                t.dwellRemaining = prev.dwellRemaining;
                t.isDiverting = prev.isDiverting;
                t.diversionProgress = prev.diversionProgress;
                t.fromTrack = prev.fromTrack;
                t.toTrack = prev.toTrack;
                t.signalAspect = prev.signalAspect || 'GREEN';
                t.spacingStatus = prev.spacingStatus || 'NOMINAL';
            } else if (t.progressPercent == null || t.progressPercent === 0) {
                const hash = (t.trainNumber || '').split('').reduce((acc, c) => acc + c.charCodeAt(0), 0);
                t.progressPercent = (hash % 60) + 5.0;
            }
        });

        if (!this.selectedTrainNumber && this.trains.length > 0) {
            this.selectedTrainNumber = this.trains[0].trainNumber;
        }

        this.buildTrainElements();

        if (window.inspectTrainDetails && this.selectedTrainNumber) {
            window.inspectTrainDetails(this.selectedTrainNumber);
        }
    }

    buildTrainElements() {
        const layer = document.getElementById('trainsLayer');
        if (!layer) return;
        layer.innerHTML = '';
        this.trainElements.clear();

        this.trains.forEach(train => {
            const trainEl = this.createTrainDOM(train);
            layer.appendChild(trainEl);
            this.trainElements.set(train.trainNumber, {
                element: trainEl,
                data: train
            });
        });

        this.updatePositions();
    }

    rebuildSingleTrain(train) {
        const layer = document.getElementById('trainsLayer');
        if (!layer) return;
        const record = this.trainElements.get(train.trainNumber);
        if (record && record.element) {
            record.element.remove();
        }
        const trainEl = this.createTrainDOM(train);
        layer.appendChild(trainEl);
        this.trainElements.set(train.trainNumber, {
            element: trainEl,
            data: train
        });
        this.updatePositions();
    }

    createTrainDOM(train) {
        const container = document.createElement('div');
        container.className = 'animated-train-container';
        container.id = `train-visual-${train.trainNumber}`;
        container.style.pointerEvents = 'auto';

        if (train.trainNumber === this.selectedTrainNumber) {
            container.classList.add('selected-train');
        }

        const isDown = train.direction === 'DOWN';
        const isArrived = train.status === 'ARRIVED' || train.progressPercent >= 100.0;
        const isConflict = train.status === 'CONFLICT';

        if (isArrived) container.classList.add('arrived');
        if (isConflict) container.classList.add('conflict');

        let coachModelClass = '';
        if (train.category === 'GOODS') coachModelClass = 'coach-model-goods';
        else if (train.category === 'FREIGHT') coachModelClass = 'coach-model-freight';

        // 1. Overhead identification badge with interlocking state
        const overheadTag = document.createElement('div');
        overheadTag.className = 'train-overhead-tag';
        overheadTag.id = `tag-${train.trainNumber}`;
        this.updateOverheadTag(overheadTag, train);
        container.appendChild(overheadTag);

        // 2. Train Mesh (Locomotive + Rake Carriages)
        const mesh = document.createElement('div');
        mesh.className = 'train-mesh';

        const glow = document.createElement('div');
        glow.className = `train-beam-glow ${isDown ? 'train-beam-west' : 'train-beam-east'}`;
        mesh.appendChild(glow);

        if (isDown) {
            const loco = document.createElement('div');
            loco.className = 'loco-model reversed';
            mesh.appendChild(loco);

            for (let i = 0; i < 3; i++) {
                const coach = document.createElement('div');
                coach.className = `coach-model ${coachModelClass}`;
                coach.innerHTML = '<div class="coach-window-pip"></div><div class="coach-window-pip"></div>';
                mesh.appendChild(coach);
            }
        } else {
            for (let i = 0; i < 3; i++) {
                const coach = document.createElement('div');
                coach.className = `coach-model ${coachModelClass}`;
                coach.innerHTML = '<div class="coach-window-pip"></div><div class="coach-window-pip"></div>';
                mesh.appendChild(coach);
            }
            const loco = document.createElement('div');
            loco.className = 'loco-model';
            mesh.appendChild(loco);
        }

        container.appendChild(mesh);

        // 3. Rich Hover Tooltip
        const tooltip = document.createElement('div');
        tooltip.className = 'train-hover-tooltip';
        tooltip.id = `tooltip-${train.trainNumber}`;
        this.updateTooltipContent(tooltip, train);
        container.appendChild(tooltip);

        container.addEventListener('click', (e) => {
            e.stopPropagation();
            this.selectTrain(train.trainNumber);
        });

        return container;
    }

    updateOverheadTag(tag, train) {
        if (!tag) return;
        const isArrived = train.status === 'ARRIVED' || train.progressPercent >= 100.0;
        const isDown = train.direction === 'DOWN';

        let catClass = 'badge-express';
        if (train.category === 'PASSENGER') catClass = 'badge-passenger';
        else if (train.category === 'GOODS') catClass = 'badge-goods';
        else if (train.category === 'FREIGHT') catClass = 'badge-freight';

        if (isArrived) {
            tag.innerHTML = `<span>#${train.trainNumber}</span><span style="color:#34D399; font-weight:700;">✓ Reached</span>`;
        } else if (train.isDiverting) {
            tag.innerHTML = `<span>#${train.trainNumber}</span><span style="background:#0D9488; color:#FFF; font-size:0.6rem; padding:1px 4px; border-radius:2px;">⚡ Turnout (1➔3)</span>`;
        } else if (train.spacingStatus === 'STANDSTILL_HELD') {
            tag.innerHTML = `<span>#${train.trainNumber}</span><span style="background:#DC2626; color:#FFF; font-size:0.6rem; padding:1px 4px; border-radius:2px;">🛑 Signal Hold: Safe Spacing</span>`;
        } else if (train.status === 'HELD') {
            tag.innerHTML = `<span>#${train.trainNumber}</span><span style="background:#B91C1C; color:#FFF; font-size:0.6rem; padding:1px 4px; border-radius:2px;">🛑 Opposing Train Hold</span>`;
        } else if (train.spacingStatus === 'CAUTION_SLOW') {
            tag.innerHTML = `<span>#${train.trainNumber}</span><span style="background:#D97706; color:#FFF; font-size:0.6rem; padding:1px 4px; border-radius:2px;">⚠️ Caution (25 km/h)</span>`;
        } else {
            tag.innerHTML = `
                <span>#${train.trainNumber}</span>
                <span class="train-tag-category ${catClass}">${train.category}</span>
                <span>${isDown ? '⬅' : '➔'}</span>
            `;
        }
    }

    selectTrain(trainNumber) {
        this.selectedTrainNumber = trainNumber;
        document.querySelectorAll('.animated-train-container').forEach(el => {
            el.classList.remove('selected-train');
        });
        const trainEl = document.getElementById(`train-visual-${trainNumber}`);
        if (trainEl) trainEl.classList.add('selected-train');

        if (window.selectTrainInPanel) window.selectTrainInPanel(trainNumber);
        if (window.inspectTrainDetails) window.inspectTrainDetails(trainNumber);
    }

    updateTooltipContent(tooltip, train) {
        const isArrived = train.status === 'ARRIVED' || train.progressPercent >= 100.0;
        tooltip.innerHTML = `
            <div class="tooltip-title-row">
                <div class="tooltip-train-num">TRAIN #${train.trainNumber}</div>
                <div class="status-badge status-${train.status.toLowerCase().replace('_', '-')}" style="font-size:0.65rem;">
                    ${train.status.replace('_', ' ')}
                </div>
            </div>
            <div class="tooltip-train-name">${train.trainName}</div>
            <div class="tooltip-grid">
                <div>Category: <span class="tooltip-val" style="color:var(--teal-primary)">${train.category}</span></div>
                <div>Current Track: <span class="tooltip-val">Track ${train.currentTrackNumber}</span></div>
                <div>Signal Aspect: <span class="tooltip-val" style="color:${train.signalAspect === 'RED' ? '#EF4444' : (train.signalAspect === 'AMBER' ? '#F59E0B' : '#10B981')}">${train.signalAspect || 'GREEN'}</span></div>
                <div>Spacing Mode: <span class="tooltip-val">${train.spacingStatus || 'NOMINAL'}</span></div>
                <div>Direction: <span class="tooltip-val">${train.direction === 'UP' ? 'Eastbound (UP)' : 'Westbound (DOWN)'}</span></div>
                <div>Speed: <span class="tooltip-val">${isArrived ? '0 km/h (Halt)' : (train.speedKmh + ' km/h')}</span></div>
                <div>ETA: <span class="tooltip-val">${train.eta}</span></div>
                <div>Progress: <span class="tooltip-val">${Math.min(100, Math.floor(train.progressPercent))}%</span></div>
            </div>
            ${isArrived ? '<div class="tooltip-dest-reached">✓ Successfully Reached Destination</div>' : ''}
        `;
    }

    start() {
        if (this.isRunning && this.animationFrameId) return;
        this.isRunning = true;
        this.lastTimestamp = null;
        this.animationFrameId = requestAnimationFrame(ts => this.loop(ts));
        this.updateSimControlUI();
    }

    pause() {
        this.isRunning = false;
        if (this.animationFrameId) {
            cancelAnimationFrame(this.animationFrameId);
            this.animationFrameId = null;
        }
        this.updateSimControlUI();
    }

    toggle() {
        if (this.isRunning) {
            this.pause();
            fetch('api/simulation', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: 'action=pause' }).catch(() => {});
            if (window.showToast) window.showToast('Simulation paused.', 'info');
        } else {
            this.start();
            fetch('api/simulation', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: 'action=resume' }).catch(() => {});
            if (window.showToast) window.showToast('Simulation resumed. Trains in motion.', 'success');
        }
    }

    reset() {
        fetch('api/simulation', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=reset'
        })
        .then(res => res.json())
        .then(data => {
            if (data && data.trains) {
                this.setTrains(data.trains);
            }
        })
        .catch(() => {});

        this.notifiedArrivals.clear();
        this.simElapsedSeconds = 0;
        this.predictiveConflictTriggered = false;
        this.start();

        if (window.showToast) {
            window.showToast('Simulation and interlocking reset to initial checkpoint.', 'alert');
        }
    }

    setSpeed(speed) {
        this.speedMultiplier = speed;
        const speedButtons = document.querySelectorAll('.sim-speed-btn');
        speedButtons.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.speed == speed);
        });
        if (window.showToast) {
            window.showToast(`Simulation Speed: ${speed}x`, 'success');
        }
    }

    loop(timestamp) {
        if (!this.isRunning) return;

        if (!this.lastTimestamp) this.lastTimestamp = timestamp;
        const deltaSec = Math.min((timestamp - this.lastTimestamp) / 1000, 0.1);
        this.lastTimestamp = timestamp;

        // Auto Predictive Conflict Trigger in default nominal scenario (~35 seconds)
        this.simElapsedSeconds += deltaSec;
        if (!this.predictiveConflictTriggered && this.simElapsedSeconds >= 35.0 && this.lastScenario === 'DEFAULT') {
            this.predictiveConflictTriggered = true;
            if (window.triggerPredictiveConflict) {
                window.triggerPredictiveConflict();
            }
        }

        this.updatePhysics(deltaSec);
        this.updatePositions();

        // Sync with Java Backend Interlocking Engine every 350ms
        if (timestamp - this.lastSyncTime > 350) {
            this.lastSyncTime = timestamp;
            this.syncBackendInterlocking(deltaSec);
        }

        this.animationFrameId = requestAnimationFrame(ts => this.loop(ts));
    }

    /**
     * Interlocking & Collision Prevention Physics
     */
    updatePhysics(deltaSec) {
        this.trains.forEach(train => {
            const isArrived = train.status === 'ARRIVED' || train.progressPercent >= 100.0;

            if (isArrived) {
                if (train.dwellRemaining == null) {
                    train.dwellRemaining = 8.0;
                    train.status = 'ARRIVED';
                    train.progressPercent = 100.0;
                    this.handleDestinationArrival(train);
                } else {
                    train.dwellRemaining -= deltaSec;
                    if (train.dwellRemaining <= 0) {
                        train.dwellRemaining = null;
                        train.status = 'MOVING';
                        train.progressPercent = 0.0;
                        train.direction = (train.direction === 'UP') ? 'DOWN' : 'UP';

                        const oldOrigId = train.originStationId;
                        const oldOrigName = train.originStationName;
                        train.originStationId = train.destinationStationId;
                        train.originStationName = train.destinationStationName;
                        train.destinationStationId = oldOrigId;
                        train.destinationStationName = oldOrigName;

                        this.notifiedArrivals.delete(train.trainNumber);
                        this.rebuildSingleTrain(train);
                    }
                }
                return;
            }

            // DIVERSION KINEMATICS (Test C & Test D)
            if (train.isDiverting) {
                // Advance along diagonal turnout crossover
                const diversionRate = (100.0 / 12.0) * deltaSec * this.speedMultiplier;
                train.diversionProgress = Math.min(100.0, (train.diversionProgress || 0) + diversionRate);

                if (window.updateDiversionProgressUI) {
                    window.updateDiversionProgressUI(train.diversionProgress);
                }

                if (train.diversionProgress >= 100.0) {
                    train.isDiverting = false;
                    train.currentTrackNumber = train.toTrack || 3;
                    train.progressPercent = 40.0; // Merges onto Track 3 at 40%
                    train.status = 'MOVING';
                    train.signalAspect = 'GREEN';

                    document.querySelectorAll('.turnout-line').forEach(el => el.classList.remove('turnout-active'));
                    const tag = document.getElementById('tag-turnout-1-3');
                    if (tag) tag.classList.remove('active');

                    if (window.onTrainDiversionCompleted) {
                        window.onTrainDiversionCompleted(train);
                    }
                }
                return;
            }

            // Anti-Collision 1: Following Train Safe Separation (Test A)
            let leadTrain = null;
            let minDistance = 999.0;
            this.trains.forEach(other => {
                if (other.trainNumber === train.trainNumber) return;
                if (other.currentTrackNumber !== train.currentTrackNumber) return;
                if (other.direction !== train.direction) return;
                if (other.status === 'ARRIVED') return;

                if (other.progressPercent > train.progressPercent) {
                    const dist = other.progressPercent - train.progressPercent;
                    if (dist < minDistance) {
                        minDistance = dist;
                        leadTrain = other;
                    }
                }
            });

            // Anti-Collision 2: Opposing Train Single-Track Blocking (Test B)
            let opposingTrain = null;
            this.trains.forEach(other => {
                if (other.trainNumber === train.trainNumber) return;
                if (other.currentTrackNumber !== train.currentTrackNumber) return;
                if (other.direction !== train.direction && other.status !== 'ARRIVED') {
                    opposingTrain = other;
                }
            });

            let effectiveSpeed = train.speedKmh;

            if (opposingTrain) {
                // Test B: Opposite direction blocking
                const isWinner = (train.category === 'EXPRESS' && opposingTrain.category !== 'EXPRESS') ||
                                 (train.delayMinutes <= opposingTrain.delayMinutes);
                if (!isWinner && (train.progressPercent <= 25.0 || train.progressPercent >= 75.0)) {
                    effectiveSpeed = 0;
                    train.signalAspect = 'RED';
                    train.status = 'HELD';
                    train.spacingStatus = 'STANDSTILL_HELD';
                    this.trackSim.setSignalAspect(train.currentTrackNumber, 'red');
                }
            } else if (leadTrain) {
                // Test A: Same direction safe separation
                train.separationDistancePercent = minDistance;
                train.leadTrainNumber = leadTrain.trainNumber;

                if (minDistance < 16.0) {
                    // STOP: Minimum safe separation barrier violated
                    effectiveSpeed = 0;
                    train.signalAspect = 'RED';
                    train.spacingStatus = 'STANDSTILL_HELD';
                    this.trackSim.setSignalAspect(train.currentTrackNumber, 'red');
                } else if (minDistance < 28.0) {
                    // CAUTION: Decelerate
                    effectiveSpeed = Math.min(25, train.speedKmh * 0.35);
                    train.signalAspect = 'AMBER';
                    train.spacingStatus = 'CAUTION_SLOW';
                    this.trackSim.setSignalAspect(train.currentTrackNumber, 'amber');
                } else {
                    train.signalAspect = 'GREEN';
                    train.spacingStatus = 'NOMINAL';
                    this.trackSim.setSignalAspect(train.currentTrackNumber, 'green');
                }
            } else {
                train.signalAspect = 'GREEN';
                train.spacingStatus = 'NOMINAL';
            }

            if (effectiveSpeed > 0) {
                const baseRatePercentPerSec = (effectiveSpeed / 90.0) * 3.8;
                const progressDelta = baseRatePercentPerSec * deltaSec * this.speedMultiplier;
                train.progressPercent += progressDelta;
            }

            if (train.progressPercent >= 100.0) {
                train.progressPercent = 100.0;
                train.status = 'ARRIVED';
                train.dwellRemaining = 8.0;
                this.handleDestinationArrival(train);
            }
        });
    }

    startDiversion(trainNumber, fromTrack, toTrack) {
        const train = this.trains.find(t => t.trainNumber === trainNumber);
        if (!train) return false;

        // Test D Check: Is the connecting side track or destination line occupied?
        const blocker = this.trains.find(t => t.trainNumber !== trainNumber && t.currentTrackNumber === toTrack && t.status !== 'ARRIVED' && Math.abs(t.progressPercent - 36.0) < 18.0);
        if (blocker) {
            if (window.showToast) {
                window.showToast(`🛑 DIVERSION HELD: Track ${toTrack} / Crossover is occupied by Train #${blocker.trainNumber}! Holding at approach signal.`, 'alert');
            }
            train.status = 'HELD';
            train.signalAspect = 'RED';
            train.spacingStatus = 'STANDSTILL_HELD';
            this.trackSim.setSignalAspect(fromTrack, 'red');
            return false;
        }

        train.isDiverting = true;
        train.fromTrack = fromTrack;
        train.toTrack = toTrack;
        train.diversionProgress = 0.0;
        train.status = 'DIVERSION_IN_PROGRESS';
        train.signalAspect = 'GREEN';

        this.trackSim.setSignalAspect(toTrack, 'green');

        document.querySelectorAll('.turnout-line').forEach(el => el.classList.add('turnout-active'));
        const tag = document.getElementById('tag-turnout-1-3');
        if (tag) tag.classList.add('active');

        const trainEl = document.getElementById(`train-visual-${trainNumber}`);
        if (trainEl) trainEl.classList.remove('diverting-train-blink');

        fetch('api/simulation', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=dispatchDiversion&trainNumber=${encodeURIComponent(trainNumber)}&fromTrack=${fromTrack}&toTrack=${toTrack}`
        }).catch(() => {});

        return true;
    }

    handleDestinationArrival(train) {
        const trainRecord = this.trainElements.get(train.trainNumber);
        if (trainRecord && trainRecord.element) {
            trainRecord.element.classList.add('arrived');
            const tag = trainRecord.element.querySelector('.train-overhead-tag');
            if (tag) tag.innerHTML = `<span>#${train.trainNumber}</span><span style="color:#34D399; font-weight:700;">✓ Reached</span>`;
            const tooltip = trainRecord.element.querySelector('.train-hover-tooltip');
            if (tooltip) this.updateTooltipContent(tooltip, train);
        }

        if (!this.notifiedArrivals.has(train.trainNumber)) {
            this.notifiedArrivals.add(train.trainNumber);
            const destName = train.destinationStationName || 'Destination Terminal';
            if (window.showToast) {
                window.showToast(`Train #${train.trainNumber} (${train.trainName}): SUCCESSFULLY REACHED destination (${destName})!`, 'success');
            }
        }
    }

    updatePositions() {
        const layer = document.getElementById('trainsLayer');
        if (!layer) return;

        const containerWidth = layer.clientWidth || 1000;
        const usableWidth = Math.max(300, containerWidth - 280);
        const startX = 140;

        this.trains.forEach(train => {
            const record = this.trainElements.get(train.trainNumber);
            if (!record) return;

            const el = record.element;
            const tag = el.querySelector('.train-overhead-tag');
            if (tag) this.updateOverheadTag(tag, train);

            let xPos = 0;
            let yPos = this.trackSim.getTrackYPosition(train.currentTrackNumber);
            let rot = 0;

            if (train.isDiverting) {
                // Exact Crossover Geometry (Track 1 Switch at 260px down to Track 3 at 475px)
                const pct = (train.diversionProgress || 0) / 100.0;
                const startSwX = 260.0;
                const startSwY = 76.0;   // Track 1 locomotive center
                const endSwX = 475.0;
                const endSwY = 291.0;   // Track 3 locomotive center

                xPos = startSwX + (endSwX - startSwX) * pct;
                yPos = startSwY + (endSwY - startSwY) * pct;

                if (pct > 0.05 && pct < 0.95) {
                    rot = 45.0; // Angled along the physical crossover track!
                } else {
                    rot = 0.0;  // Leveled horizontal
                }
            } else {
                if (train.direction === 'DOWN') {
                    xPos = startX + usableWidth - (train.progressPercent / 100.0) * usableWidth;
                } else {
                    xPos = startX + (train.progressPercent / 100.0) * usableWidth;
                }
                rot = 0.0;
            }

            el.style.left = `${xPos}px`;
            el.style.top = `${yPos}px`;
            el.style.transform = rot ? `rotate(${rot}deg)` : 'none';

            const tooltip = el.querySelector('.train-hover-tooltip');
            if (tooltip && !train.status.includes('ARRIVED')) {
                this.updateTooltipContent(tooltip, train);
            }
        });

        if (window.updateLiveInspectorTelemetry && this.selectedTrainNumber) {
            const selTrain = this.trains.find(t => t.trainNumber === this.selectedTrainNumber);
            if (selTrain) window.updateLiveInspectorTelemetry(selTrain);
        }
    }

    async syncBackendInterlocking(deltaSec) {
        try {
            const resp = await fetch('api/simulation', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `action=step&deltaSec=${deltaSec.toFixed(3)}`
            });
            if (!resp.ok) return;
            const data = await resp.json();
            if (data && data.trains) {
                // If scenario changed, re-sync completely
                if (data.trains.length !== this.trains.length || data.scenario !== this.lastScenario) {
                    this.lastScenario = data.scenario;
                    this.setTrains(data.trains);
                } else {
                    // Update state variables from backend source of truth
                    data.trains.forEach(bt => {
                        const local = this.trains.find(t => t.trainNumber === bt.trainNumber);
                        if (local) {
                            local.signalAspect = bt.signalAspect;
                            local.spacingStatus = bt.spacingStatus;
                            local.speedKmh = bt.speedKmh;
                            if (bt.isDiverting && !local.isDiverting) {
                                local.isDiverting = true;
                                local.diversionProgress = bt.diversionProgress;
                            }
                        }
                    });
                }
            }
        } catch (e) {
            // Passive sync catch
        }
    }

    updateSimControlUI() {
        const pauseResumeBtn = document.getElementById('simPauseResumeBtn');
        if (pauseResumeBtn) {
            if (this.isRunning) {
                pauseResumeBtn.className = 'btn btn-outline btn-sm';
                pauseResumeBtn.innerHTML = '<span>⏸ Pause Simulation</span>';
            } else {
                pauseResumeBtn.className = 'btn btn-primary btn-sm';
                pauseResumeBtn.innerHTML = '<span>▶ Resume Simulation</span>';
            }
        }
    }
}

window.TrainMovementEngine = TrainMovementEngine;
