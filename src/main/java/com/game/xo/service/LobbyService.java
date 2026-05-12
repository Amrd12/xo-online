package com.game.xo.service;

import com.game.xo.model.GameRoom;
import com.game.xo.repository.GameRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

//eptesam and eman

@Service
public class LobbyService {

    private final GameRoomRepository gameRoomRepository;

    public LobbyService(GameRoomRepository gameRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
    }

    @Transactional
    public GameRoom quickMatch(String playerId) {
        // First check if player is already in an active game
        Optional<GameRoom> activeOpt = gameRoomRepository.findActiveGameByPlayerId(playerId);
        if (activeOpt.isPresent()) {
            return activeOpt.get();
        }

        // Find a room in WAITING state
        List<GameRoom> waitingRooms = gameRoomRepository.findByStatus("WAITING");
        for (GameRoom room : waitingRooms) {
            // Prevent player from joining their own room
            if (!playerId.equals(room.getP1Id())) {
                room.setP2Id(playerId);
                room.setStatus("ACTIVE");
                // P1 always goes first for simplicity
                room.setCurrentTurn(room.getP1Id());
                return gameRoomRepository.save(room);
            }
        }

        // If no WAITING room, create one
        GameRoom newRoom = new GameRoom();
        newRoom.setP1Id(playerId);
        newRoom.setStatus("WAITING");
        return gameRoomRepository.save(newRoom);
    }

    @Transactional
    public GameRoom createPrivateRoom(String playerId) {
        Optional<GameRoom> activeOpt = gameRoomRepository.findActiveGameByPlayerId(playerId);
        if (activeOpt.isPresent()) {
            return activeOpt.get();
        }

        GameRoom room = new GameRoom();
        room.setP1Id(playerId);
        room.setStatus("WAITING");
        room.setRoomCode(generateCode());
        return gameRoomRepository.save(room);
    }

    @Transactional
    public GameRoom joinPrivateRoom(String playerId, String roomCode) {
        Optional<GameRoom> activeOpt = gameRoomRepository.findActiveGameByPlayerId(playerId);
        if (activeOpt.isPresent()) {
            return activeOpt.get();
        }

        Optional<GameRoom> roomOpt = gameRoomRepository.findByRoomCodeAndStatus(roomCode.toUpperCase(), "WAITING");
        if (roomOpt.isPresent()) {
            GameRoom room = roomOpt.get();
            if (!playerId.equals(room.getP1Id())) {
                room.setP2Id(playerId);
                room.setStatus("ACTIVE");
                room.setCurrentTurn(room.getP1Id());
                return gameRoomRepository.save(room);
            }
        }
        return null;
    }

    public Optional<GameRoom> getActiveRoomForPlayer(String playerId) {
        return gameRoomRepository.findActiveGameByPlayerId(playerId);
    }

    private String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
