package com.game.xo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// rwan and rana
@Entity
@Table(name = "game_rooms")
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String roomCode; // e.g. "XJ92" for private rooms, or null for quick match before fully formed, though could just be UUID or generic for quick match.
    
    private String p1Id;
    
    private String p2Id;
    
    private String boardState = ",,,,,,,,"; // 9 empty spaces separated by commas
    
    private String status; // WAITING, ACTIVE, FINISHED
    
    private String currentTurn; // ID of the player whose turn it is
    
    private String winnerId;

    // Constructors
    public GameRoom() {
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getP1Id() { return p1Id; }
    public void setP1Id(String p1Id) { this.p1Id = p1Id; }

    public String getP2Id() { return p2Id; }
    public void setP2Id(String p2Id) { this.p2Id = p2Id; }

    public String getBoardState() { return boardState; }
    public void setBoardState(String boardState) { this.boardState = boardState; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }
    
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
}
