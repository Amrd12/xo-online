package com.game.xo.controller;

import com.game.xo.model.GameRoom;
import com.game.xo.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

// fatma
@Controller
public class GameWebSocketController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/move/{roomId}")
    public void handleMove(@DestinationVariable Long roomId, @Payload Map<String, Integer> payload, SimpMessageHeaderAccessor headerAccessor) {
        String playerId = (String) headerAccessor.getSessionAttributes().get("playerId");
        if (playerId == null) return;

        int position = payload.get("position");
        GameRoom updatedRoom = gameService.makeMove(roomId, playerId, position);

        if (updatedRoom != null) {
            // Broadcast the updated state to the room
            messagingTemplate.convertAndSend("/topic/room/" + roomId, updatedRoom);
        }
    }
}
