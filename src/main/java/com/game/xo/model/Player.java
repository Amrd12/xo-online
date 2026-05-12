package com.game.xo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class Player {

    @Id
    private String id; // UUID
    
    private String username;
    
    private String password; // Hashed password, null for guests
    
    private boolean isGuest;
    
    private int wins = 0;
    
    private int losses = 0;

    // Constructors
    public Player() {
    }

    public Player(String id, String username, boolean isGuest) {
        this.id = id;
        this.username = username;
        this.isGuest = isGuest;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isGuest() { return isGuest; }
    public void setGuest(boolean guest) { isGuest = guest; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
}
