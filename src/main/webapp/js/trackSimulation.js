/**
 * SmartRail AI - Visual Railway Track Simulation Canvas & Interlocking Renderer
 * High-definition SCADA Control Room Track Diagram with spacious layout,
 * accurate signals, platform markers, and turnout geometries.
 */

class TrackSimulation {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.tracks = [];
        this.currentStation = null;
        this.division = 'MAS';
        // Spacious track spacing across 610px canvas height
        this.trackPositions = {
            1: 90,   // Track 1 - Up Main Line
            2: 195,  // Track 2 - Down Main Line
            3: 305,  // Track 3 - Platform Loop 1
            4: 415,  // Track 4 - Platform Loop 2
            5: 520   // Track 5 - Freight Bypass Siding
        };
    }

    init(tracksData, stationData, divisionCode) {
        this.tracks = tracksData || [];
        this.currentStation = stationData || { code: 'MAS', name: 'Chennai Central', platformsCount: 12 };
        this.division = divisionCode || 'MAS';
        this.render();
    }

    updateStation(stationData, divisionCode) {
        this.currentStation = stationData;
        this.division = divisionCode;
        this.render();
    }

    render() {
        if (!this.container) return;

        // Clear existing static track elements except dynamic trains container
        const existingTrainsContainer = document.getElementById('trainsLayer');
        this.container.innerHTML = '';

        // 1. Render Platform Structures
        this.renderPlatforms();

        // 2. Render Crossover Turnout Switch Points
        this.renderTurnouts();

        // 3. Render 5 Parallel Railway Tracks
        this.renderTracks();

        // 4. Render Railway Signals
        this.renderSignals();

        // 5. Restore / Append trains layer
        const trainsLayer = existingTrainsContainer || document.createElement('div');
        trainsLayer.id = 'trainsLayer';
        trainsLayer.style.position = 'absolute';
        trainsLayer.style.top = '0';
        trainsLayer.style.left = '0';
        trainsLayer.style.width = '100%';
        trainsLayer.style.height = '100%';
        trainsLayer.style.pointerEvents = 'none';
        this.container.appendChild(trainsLayer);
    }

    renderPlatforms() {
        const stationName = this.currentStation ? this.currentStation.name : 'Puratchi Thalaivar Dr. MGR Chennai Central';
        const stationCode = this.currentStation ? this.currentStation.code : 'MAS';

        // Platform 1 & 2 Block (Between Track 2 and 3)
        const plat1 = document.createElement('div');
        plat1.className = 'platform-marker';
        plat1.style.top = '235px';
        plat1.style.left = '42%';
        plat1.style.width = '280px';
        plat1.innerHTML = `
            <div class="platform-name">${stationName}</div>
            <div class="platform-code">STATION CODE: ${stationCode} | PF 1 &amp; 2 (Main)</div>
        `;
        this.container.appendChild(plat1);

        // Platform 3 & 4 Block (Between Track 3 and 4)
        const plat2 = document.createElement('div');
        plat2.className = 'platform-marker';
        plat2.style.top = '345px';
        plat2.style.left = '42%';
        plat2.style.width = '280px';
        plat2.innerHTML = `
            <div class="platform-name">${stationName}</div>
            <div class="platform-code">STATION CODE: ${stationCode} | PF 3 &amp; 4 (Loop)</div>
        `;
        this.container.appendChild(plat2);

        // West Approaching Junction Marker
        const westJunction = document.createElement('div');
        westJunction.className = 'platform-marker';
        westJunction.style.top = '135px';
        westJunction.style.left = '120px';
        westJunction.style.padding = '5px 10px';
        westJunction.innerHTML = `
            <div class="platform-name" style="font-size:0.68rem; letter-spacing:0.04em;">WEST ENTRY INTERLOCKING</div>
            <div class="platform-code" style="font-size:0.6rem; color:var(--teal-primary);">SR-TN/W APPROACH BLOCK</div>
        `;
        this.container.appendChild(westJunction);

        // East Approaching Junction Marker
        const eastJunction = document.createElement('div');
        eastJunction.className = 'platform-marker';
        eastJunction.style.top = '135px';
        eastJunction.style.right = '40px';
        eastJunction.style.padding = '5px 10px';
        eastJunction.innerHTML = `
            <div class="platform-name" style="font-size:0.68rem; letter-spacing:0.04em;">EAST EXIT INTERLOCKING</div>
            <div class="platform-code" style="font-size:0.6rem; color:var(--teal-primary);">SR-TN/E DEPARTURE BLOCK</div>
        `;
        this.container.appendChild(eastJunction);
    }

    renderTurnouts() {
        // Precise physical turnout crossover side track (Track 1 -> Track 2 -> Track 3)
        // Track 1 is at y=90px, Track 2 is at y=195px (dy=105px, dx=105px, len=148.5px)
        // Track 3 is at y=305px (dy=110px, dx=110px, len=155.6px)
        const turnouts = [
            // West Entry Turnout: Track 1 (260, 90) down to Track 2 (365, 195)
            { id: 'turnout-west-1', top: 90, left: '260px', width: '148.5px', angle: 45 },
            // West Entry Turnout: Track 2 (365, 195) down to Track 3 (475, 305)
            { id: 'turnout-west-2', top: 195, left: '365px', width: '155.6px', angle: 45 },
            // East Exit Turnout: Track 3 (760, 305) up to Track 2 (870, 195)
            { id: 'turnout-east-2', top: 305, left: '760px', width: '155.6px', angle: -45 },
            // East Exit Turnout: Track 2 (870, 195) up to Track 1 (975, 90)
            { id: 'turnout-east-1', top: 195, left: '870px', width: '148.5px', angle: -45 }
        ];

        turnouts.forEach(t => {
            const svgTurnout = document.createElement('div');
            svgTurnout.className = 'turnout-line';
            if (t.id) svgTurnout.id = t.id;
            svgTurnout.style.top = `${t.top}px`;
            svgTurnout.style.left = t.left;
            svgTurnout.style.width = t.width;
            svgTurnout.style.transform = `rotate(${t.angle}deg)`;
            svgTurnout.style.transformOrigin = '0 0';
            this.container.appendChild(svgTurnout);
        });

        // Interlocking Turnout Switch 1A (Entry) & Switch 1B (Exit) Markers
        const tag1 = document.createElement('div');
        tag1.className = 'turnout-tag';
        tag1.id = 'tag-turnout-1-3';
        tag1.style.top = '175px';
        tag1.style.left = '320px';
        tag1.innerHTML = 'SWITCH 1A ➔ ENTRY TO SIDE TRACK (1➔3)';
        this.container.appendChild(tag1);

        const tag2 = document.createElement('div');
        tag2.className = 'turnout-tag';
        tag2.id = 'tag-turnout-3-1';
        tag2.style.top = '175px';
        tag2.style.left = '815px';
        tag2.innerHTML = 'SWITCH 1B ➔ EXIT TO MAIN LINE (3➔1)';
        this.container.appendChild(tag2);
    }

    renderTracks() {
        const defaultTracks = [
            { num: 1, name: 'Track 1 - Up Main Line', type: 'MAIN_UP', speed: 110, dir: 'EASTBOUND (UP) ➔' },
            { num: 2, name: 'Track 2 - Down Main Line', type: 'MAIN_DOWN', speed: 110, dir: '⬅ WESTBOUND (DOWN)' },
            { num: 3, name: 'Track 3 - Platform Loop 1', type: 'LOOP_1', speed: 65, dir: 'EASTBOUND (UP) ➔' },
            { num: 4, name: 'Track 4 - Platform Loop 2', type: 'LOOP_2', speed: 65, dir: '⬅ WESTBOUND (DOWN)' },
            { num: 5, name: 'Track 5 - Freight Bypass Siding', type: 'FREIGHT_SIDING', speed: 50, dir: 'BIDIRECTIONAL ⇄' }
        ];

        defaultTracks.forEach(t => {
            const yPos = this.trackPositions[t.num] || (t.num * 100);

            const row = document.createElement('div');
            row.className = 'track-row';
            row.style.top = `${yPos}px`;

            // Track Number Badge
            const numTag = document.createElement('div');
            numTag.className = 'track-number-tag';
            numTag.innerHTML = `<strong>TRK ${t.num}</strong> | ${t.name.split(' - ')[1] || t.name} <span style="color:var(--teal-primary)">[${t.speed} KM/H]</span>`;
            row.appendChild(numTag);

            // Track Line Visual
            const trackLine = document.createElement('div');
            let trackClass = 'track-main-up';
            if (t.type === 'MAIN_DOWN') trackClass = 'track-main-down';
            else if (t.type.includes('LOOP')) trackClass = 'track-loop';
            else if (t.type.includes('SIDING')) trackClass = 'track-siding';

            trackLine.className = `track-line-visual ${trackClass}`;
            row.appendChild(trackLine);

            // Direction Arrow
            const dirArrow = document.createElement('div');
            dirArrow.className = 'track-dir-arrow';
            dirArrow.textContent = t.dir;
            row.appendChild(dirArrow);

            this.container.appendChild(row);
        });
    }

    renderSignals() {
        // Signals for Track 1, 2, 3, 4, 5 at West & East approaches
        const signalConfigs = [
            { trackNum: 1, x: 230, side: 'west', aspect: 'green' },
            { trackNum: 1, x: '82%', side: 'east', aspect: 'green' },
            { trackNum: 2, x: 230, side: 'west', aspect: 'amber' },
            { trackNum: 2, x: '82%', side: 'east', aspect: 'green' },
            { trackNum: 3, x: 300, side: 'west', aspect: 'green' },
            { trackNum: 3, x: '75%', side: 'east', aspect: 'amber' },
            { trackNum: 4, x: 300, side: 'west', aspect: 'green' },
            { trackNum: 4, x: '75%', side: 'east', aspect: 'green' },
            { trackNum: 5, x: 250, side: 'west', aspect: 'amber' },
            { trackNum: 5, x: '80%', side: 'east', aspect: 'green' }
        ];

        signalConfigs.forEach(s => {
            const yPos = (this.trackPositions[s.trackNum] || (s.trackNum * 100)) - 32;
            const signal = document.createElement('div');
            signal.className = 'signal-head';
            signal.id = `signal-track-${s.trackNum}-${s.side}`;
            signal.style.top = `${yPos}px`;
            if (typeof s.x === 'string') {
                signal.style.left = s.x;
            } else {
                signal.style.left = `${s.x}px`;
            }

            signal.innerHTML = `
                <div class="signal-lamp ${s.aspect === 'green' ? 'green' : ''}"></div>
                <div class="signal-lamp ${s.aspect === 'amber' ? 'amber' : ''}"></div>
                <div class="signal-lamp ${s.aspect === 'red' ? 'red' : ''}"></div>
            `;
            this.container.appendChild(signal);
        });
    }

    setSignalAspect(trackNum, aspect, side = 'west') {
        const signal = document.getElementById(`signal-track-${trackNum}-${side}`) ||
                       document.getElementById(`signal-track-${trackNum}-west`);
        if (!signal) return;
        const lamps = signal.querySelectorAll('.signal-lamp');
        lamps.forEach(l => {
            l.className = 'signal-lamp';
        });
        if (aspect === 'green' && lamps[0]) lamps[0].classList.add('green');
        else if (aspect === 'amber' && lamps[1]) lamps[1].classList.add('amber');
        else if (aspect === 'red' && lamps[2]) lamps[2].classList.add('red');
    }

    getTrackYPosition(trackNumber) {
        return (this.trackPositions[trackNumber] || 100) - 14;
    }
}

window.TrackSimulation = TrackSimulation;
