const DOM = {
    authSection: document.getElementById('auth-section'),
    lobbySection: document.getElementById('lobby-section'),
    gameSection: document.getElementById('game-section'),
    userInfo: document.getElementById('user-info'),
    usernameDisplay: document.getElementById('username-display'),
    winCount: document.getElementById('win-count'),
    lossCount: document.getElementById('loss-count'),
    waitingSpinner: document.getElementById('waiting-spinner'),
    roomCodeDisplay: document.getElementById('room-code-display'),
    lobbyActions: document.querySelector('.lobby-actions'),
    authError: document.getElementById('auth-error'),
    lobbyError: document.getElementById('lobby-error')
};

let currentPlayer = null;
let currentGameRoom = null;

async function fetchMe() {
    try {
        const res = await fetch('/api/auth/me');
        if (res.ok) {
            const data = await res.json();
            if (data.authenticated !== false) {
                currentPlayer = data;
                updateUserInfo();
                checkReconnection();
            } else {
                showView(DOM.authSection);
            }
        } else {
            showView(DOM.authSection);
        }
    } catch (e) {
        showView(DOM.authSection);
    }
}

function updateUserInfo() {
    DOM.userInfo.classList.remove('hidden');
    DOM.usernameDisplay.textContent = currentPlayer.username + (currentPlayer.isGuest ? " (Guest)" : "");
    DOM.winCount.textContent = currentPlayer.wins;
    DOM.lossCount.textContent = currentPlayer.losses;
}

function showView(viewElement) {
    DOM.authSection.classList.add('hidden');
    DOM.lobbySection.classList.add('hidden');
    DOM.gameSection.classList.add('hidden');
    viewElement.classList.remove('hidden');
}

async function checkReconnection() {
    try {
        const res = await fetch('/api/lobby/reconnect');
        if (res.ok) {
            const room = await res.json();
            if (room.active !== false) {
                handleRoomJoined(room);
            } else {
                showView(DOM.lobbySection);
                resetLobbyUI();
            }
        } else {
            showView(DOM.lobbySection);
            resetLobbyUI();
        }
    } catch(e) {
        showView(DOM.lobbySection);
        resetLobbyUI();
    }
}

function resetLobbyUI() {
    DOM.lobbyActions.classList.remove('hidden');
    DOM.waitingSpinner.classList.add('hidden');
    DOM.lobbyError.classList.add('hidden');
    DOM.roomCodeDisplay.textContent = '';
}

// Events
document.getElementById('guest-play-btn').addEventListener('click', async () => {
    // Just hitting /api/auth/me will trigger the filter and create a guest
    await fetchMe();
});

document.getElementById('register-btn').addEventListener('click', async (e) => {
    e.preventDefault();
    const u = document.getElementById('username').value;
    const p = document.getElementById('password').value;
    if(!u || !p) return showAuthError("Username and password required");
    
    const res = await fetch('/api/auth/register', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({username: u, password: p})
    });
    if (res.ok) await fetchMe();
    else { const err = await res.json(); showAuthError(err.error || "Registration failed"); }
});

document.getElementById('login-btn').addEventListener('click', async (e) => {
    e.preventDefault();
    const u = document.getElementById('username').value;
    const p = document.getElementById('password').value;
    if(!u || !p) return showAuthError("Username and password required");

    const res = await fetch('/api/auth/login', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({username: u, password: p})
    });
    if (res.ok) await fetchMe();
    else { const err = await res.json(); showAuthError(err.error || "Login failed"); }
});

document.getElementById('logout-btn').addEventListener('click', async () => {
    await fetch('/api/auth/logout', { method: 'POST' });
    currentPlayer = null;
    DOM.userInfo.classList.add('hidden');
    showView(DOM.authSection);
    if(window.disconnectStomp) window.disconnectStomp();
});

document.getElementById('quick-match-btn').addEventListener('click', async () => {
    const res = await fetch('/api/lobby/quick-match', { method: 'POST' });
    if (res.ok) handleRoomJoined(await res.json());
});

document.getElementById('create-room-btn').addEventListener('click', async () => {
    const res = await fetch('/api/lobby/create-private', { method: 'POST' });
    if (res.ok) handleRoomJoined(await res.json());
});

document.getElementById('join-room-btn').addEventListener('click', async () => {
    const code = document.getElementById('room-code-input').value;
    if(!code) return;
    const res = await fetch('/api/lobby/join-private', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({roomCode: code})
    });
    if (res.ok) handleRoomJoined(await res.json());
    else { const err = await res.json(); showLobbyError(err.error); }
});

document.getElementById('leave-game-btn').addEventListener('click', () => {
    if(window.disconnectStomp) window.disconnectStomp();
    showView(DOM.lobbySection);
    resetLobbyUI();
    fetchMe(); // Refresh stats
});

function handleRoomJoined(room) {
    currentGameRoom = room;
    if (room.status === 'WAITING') {
        DOM.lobbyActions.classList.add('hidden');
        DOM.waitingSpinner.classList.remove('hidden');
        if (room.roomCode) {
            DOM.roomCodeDisplay.textContent = `Room Code: ${room.roomCode}`;
        }
        // Connect STOMP to listen for player 2 joining
        if(window.connectStomp) window.connectStomp(room.id, currentPlayer.id);
    } else if (room.status === 'ACTIVE' || room.status === 'FINISHED') {
        showView(DOM.gameSection);
        if(window.connectStomp) window.connectStomp(room.id, currentPlayer.id);
        if(window.renderBoard) window.renderBoard(room);
    }
}

function showAuthError(msg) {
    DOM.authError.textContent = msg;
    DOM.authError.classList.remove('hidden');
}

function showLobbyError(msg) {
    DOM.lobbyError.textContent = msg;
    DOM.lobbyError.classList.remove('hidden');
}

// Init
fetchMe();
