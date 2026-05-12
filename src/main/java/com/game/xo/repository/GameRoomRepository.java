package com.game.xo.repository;

import com.game.xo.model.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


//shadia
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByRoomCodeAndStatus(String roomCode, String status);
    
    @Query("SELECT g FROM GameRoom g WHERE (g.p1Id = :playerId OR g.p2Id = :playerId) AND g.status = 'ACTIVE'")
    Optional<GameRoom> findActiveGameByPlayerId(@Param("playerId") String playerId);
    
    List<GameRoom> findByStatus(String status);
}
