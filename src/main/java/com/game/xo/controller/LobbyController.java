package com.game.xo.controller;

import com.game.xo.model.GameRoom;
import com.game.xo.service.LobbyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


//aya
@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/quick-match")
    public ResponseEntity<?> quickMatch(@CookieValue(name = "PLAYER_ID", required = false) String playerId) {
        if (playerId == null) return ResponseEntity.status(401).build();
        GameRoom room = lobbyService.quickMatch(playerId);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/create-private")
    public ResponseEntity<?> createPrivate(@CookieValue(name = "PLAYER_ID", required = false) String playerId) {
        if (playerId == null) return ResponseEntity.status(401).build();
        GameRoom room = lobbyService.createPrivateRoom(playerId);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/join-private")
    public ResponseEntity<?> joinPrivate(@RequestBody Map<String, String> payload, 
                                         @CookieValue(name = "PLAYER_ID", required = false) String playerId) {
        if (playerId == null) return ResponseEntity.status(401).build();
        String roomCode = payload.get("roomCode");
        GameRoom room = lobbyService.joinPrivateRoom(playerId, roomCode);
        if (room != null) {
            return ResponseEntity.ok(room);
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Room not found, full, or you are already in a game."));
    }

    @GetMapping("/reconnect")
    public ResponseEntity<?> reconnect(@CookieValue(name = "PLAYER_ID", required = false) String playerId) {
        if (playerId == null) return ResponseEntity.ok(Map.of("active", false));
        Optional<GameRoom> roomOpt = lobbyService.getActiveRoomForPlayer(playerId);
        if (roomOpt.isPresent()) {
            return ResponseEntity.ok(roomOpt.get());
        }
        return ResponseEntity.ok(Map.of("active", false));
    }
}
