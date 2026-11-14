/**
 * SmartRail AI - Opening Screen & Transition Controller
 * Plays an Indian railway-themed train animation automatically on every open,
 * then transitions smoothly into the main dashboard with continuous train movement.
 */

document.addEventListener('DOMContentLoaded', () => {
    initLoadingScreen();
});

function initLoadingScreen() {
    const loadingScreen = document.getElementById('loadingScreen');
    const progressBar = document.getElementById('loadingProgressBar');
    const percentText = document.getElementById('loadingPercent');
    const statusText = document.getElementById('loadingStatusText');
    const skipBtn = document.getElementById('skipLoadingBtn');

    if (!loadingScreen) {
        // If loading screen is not present, start movement directly
        if (window.trainMovementEngine) {
            window.trainMovementEngine.start();
        }
        return;
    }

    // Ensure screen is visible whenever application is opened
    loadingScreen.classList.remove('hidden');

    const stages = [
        { pct: 15, msg: 'Connecting to Southern Railway Telemetry Hub (Chennai)...' },
        { pct: 35, msg: 'Loading Tamil Nadu Division Topologies (MAS, SA, TPJ, MDU)...' },
        { pct: 60, msg: 'Calibrating Electronic Interlocking & Signal Relays...' },
        { pct: 85, msg: 'Synchronizing Live Line Blocks & Automated Train Streams...' },
        { pct: 100, msg: 'SmartRail AI Control System Operational. Launching Dashboard...' }
    ];

    let currentProgress = 0;
    const targetProgress = 100;
    const duration = 2400; // 2.4 seconds
    const intervalTime = 30;
    const increment = (targetProgress / (duration / intervalTime));

    const progressInterval = setInterval(() => {
        currentProgress += increment;
        if (currentProgress >= 100) {
            currentProgress = 100;
            clearInterval(progressInterval);
            finishLoading();
        }

        if (progressBar) progressBar.style.width = `${currentProgress}%`;
        if (percentText) percentText.textContent = `${Math.floor(currentProgress)}%`;

        // Update status stage text
        for (let i = stages.length - 1; i >= 0; i--) {
            if (currentProgress >= stages[i].pct) {
                if (statusText) statusText.textContent = stages[i].msg;
                break;
            }
        }
    }, intervalTime);

    function finishLoading() {
        setTimeout(() => {
            loadingScreen.classList.add('hidden');
            setTimeout(() => {
                loadingScreen.style.display = 'none';
            }, 600);
            if (window.unlockAudio) window.unlockAudio();
            // Start train movement simulation automatically when dashboard is revealed
            if (window.trainMovementEngine) {
                window.trainMovementEngine.updatePositions();
                window.trainMovementEngine.start();
            }
        }, 300);
    }

    if (skipBtn) {
        skipBtn.onclick = () => {
            clearInterval(progressInterval);
            if (progressBar) progressBar.style.width = '100%';
            if (percentText) percentText.textContent = '100%';
            finishLoading();
        };
    }
}

// Global function to trigger intro if needed
window.replayIntro = function() {
    const loadingScreen = document.getElementById('loadingScreen');
    if (!loadingScreen) return;
    loadingScreen.classList.remove('hidden');
    initLoadingScreen();
};
