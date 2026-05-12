let stompClient = null;
let currentRoomId = null;
let myPlayerId = null;

const GDOM = {
    board: document.getElementById('board'),
    cells: document.querySelectorAll('.cell'),
    status: document.getElementById('game-status'),
    p1Indicator: document.getElementById('p1-indicator'),
    p2Indicator: document.getElementById('p2-indicator'),
    leaveBtn: document.getElementById('leave-game-btn')
};

window.connectStomp = function(roomId, playerId) {
    if (stompClient && stompClient.connected) {
        if(currentRoomId === roomId) return;
        disconnectStomp();
    }
    
    currentRoomId = roomId;
    myPlayerId = playerId;
    
    const socket = new SockJS('/ws-game');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Disable debug logs
    
    stompClient.connect({}, function (frame) {
        stompClient.subscribe('/topic/room/' + roomId, function (message) {
            const room = JSON.parse(message.body);
            // If we were waiting and it became active, show game view
            if (room.status === 'ACTIVE' && document.getElementById('lobby-section').classList.contains('hidden') === false) {
                document.getElementById('lobby-section').classList.add('hidden');
                document.getElementById('game-section').classList.remove('hidden');
            }
            renderBoard(room);
        });
    });
};

window.disconnectStomp = function() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    stompClient = null;
    currentRoomId = null;
};

window.renderBoard = function(room) {
    const boardState = room.boardState.split(',');
    
    // Update cells
    GDOM.cells.forEach((cell, index) => {
        const val = boardState[index];
        cell.textContent = val;
        cell.className = 'cell'; // reset
        if (val) {
            cell.classList.add(val.toLowerCase());
            cell.classList.add('disabled');
        } else if (room.status !== 'ACTIVE' || room.currentTurn !== myPlayerId) {
            cell.classList.add('disabled');
        }
    });

    // Update Turn Indicators
    GDOM.p1Indicator.classList.remove('active-turn');
    GDOM.p2Indicator.classList.remove('active-turn');
    
    if (room.status === 'ACTIVE') {
        GDOM.leaveBtn.classList.add('hidden');
        if (room.currentTurn === room.p1Id) {
            GDOM.p1Indicator.classList.add('active-turn');
            GDOM.status.textContent = myPlayerId === room.p1Id ? "Your Turn (X)" : "Opponent's Turn";
            GDOM.status.style.color = myPlayerId === room.p1Id ? "var(--primary)" : "var(--text-light)";
        } else {
            GDOM.p2Indicator.classList.add('active-turn');
            GDOM.status.textContent = myPlayerId === room.p2Id ? "Your Turn (O)" : "Opponent's Turn";
            GDOM.status.style.color = myPlayerId === room.p2Id ? "var(--secondary)" : "var(--text-light)";
        }
    } else if (room.status === 'FINISHED') {
        GDOM.cells.forEach(c => c.classList.add('disabled'));
        GDOM.leaveBtn.classList.remove('hidden');
        
        if (room.winnerId === 'DRAW') {
            GDOM.status.textContent = "It's a Draw!";
            GDOM.status.style.color = "var(--text-light)";
        } else if (room.winnerId === myPlayerId) {
            GDOM.status.textContent = "You Won! 🎉";
            GDOM.status.style.color = "var(--accent)";
        } else {
            GDOM.status.textContent = "You Lost! 😢";
            GDOM.status.style.color = "var(--danger)";
        }
    }
};

// Handle clicks on board
GDOM.cells.forEach(cell => {
    cell.addEventListener('click', (e) => {
        if (e.target.classList.contains('disabled')) return;
        if (!stompClient || !stompClient.connected) return;
        
        const position = parseInt(e.target.getAttribute('data-index'));
        stompClient.send(`/app/move/${currentRoomId}`, {}, JSON.stringify({position: position}));
    });
});
