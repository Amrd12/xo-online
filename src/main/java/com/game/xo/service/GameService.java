package com.game.xo.service;

import com.game.xo.model.GameRoom;
import com.game.xo.model.Player;
import com.game.xo.repository.GameRoomRepository;
import com.game.xo.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;


// kareem mohamed saleh
@Service
public class GameService {

    private final GameRoomRepository gameRoomRepository;
    private final PlayerRepository playerRepository;

    public GameService(GameRoomRepository gameRoomRepository, PlayerRepository playerRepository) {
        this.gameRoomRepository = gameRoomRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public GameRoom makeMove(Long roomId, String playerId, int position) {
        Optional<GameRoom> roomOpt = gameRoomRepository.findById(roomId);
        if (roomOpt.isEmpty()) return null;

        GameRoom room = roomOpt.get();

        if (!"ACTIVE".equals(room.getStatus())) {
            return room;
        }

        if (!playerId.equals(room.getCurrentTurn())) {
            return room; // Not their turn
        }

        String[] board = room.getBoardState().split(",", -1);
        if (position < 0 || position >= 9 || !board[position].isEmpty()) {
            return room; // Invalid move
        }

        // Determine symbol (P1 = X, P2 = O)
        String symbol = playerId.equals(room.getP1Id()) ? "X" : "O";
        board[position] = symbol;
        room.setBoardState(String.join(",", board));

        // Check for win
        if (checkWin(board, symbol)) {
            room.setStatus("FINISHED");
            room.setWinnerId(playerId);
            updateStats(room.getP1Id(), room.getP2Id(), playerId);
        } else if (checkDraw(board)) {
            room.setStatus("FINISHED");
            room.setWinnerId("DRAW");
        } else {
            // Switch turn
            room.setCurrentTurn(playerId.equals(room.getP1Id()) ? room.getP2Id() : room.getP1Id());
        }

        return gameRoomRepository.save(room);
    }

    private void updateStats(String p1Id, String p2Id, String winnerId) {
        playerRepository.findById(p1Id).ifPresent(p1 -> {
            if (p1Id.equals(winnerId)) p1.setWins(p1.getWins() + 1);
            else p1.setLosses(p1.getLosses() + 1);
            playerRepository.save(p1);
        });
        playerRepository.findById(p2Id).ifPresent(p2 -> {
            if (p2Id.equals(winnerId)) p2.setWins(p2.getWins() + 1);
            else p2.setLosses(p2.getLosses() + 1);
            playerRepository.save(p2);
        });
    }

    private boolean checkWin(String[] board, String s) {
        int[][] winningLines = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Cols
            {0, 4, 8}, {2, 4, 6}             // Diagonals
        };

        for (int[] line : winningLines) {
            if (s.equals(board[line[0]]) && s.equals(board[line[1]]) && s.equals(board[line[2]])) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDraw(String[] board) {
        for (String cell : board) {
            if (cell.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
