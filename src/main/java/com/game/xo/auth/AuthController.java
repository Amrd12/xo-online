package com.game.xo.auth;

import com.game.xo.model.Player;
import com.game.xo.repository.PlayerRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PlayerRepository playerRepository;

    public AuthController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @GetMapping("/me")  
    public ResponseEntity<?> getCurrentUser(@CookieValue(name = "PLAYER_ID", required = false) String playerId) {
        if (playerId == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player p = playerOpt.get();
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "id", p.getId(),
                    "username", p.getUsername(),
                    "isGuest", p.isGuest(),
                    "wins", p.getWins(),
                    "losses", p.getLosses()
            ));
        }
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload, 
                                      @CookieValue(name = "PLAYER_ID", required = false) String currentGuestId,
                                      HttpServletResponse response) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (playerRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username taken"));
        }

        Player player;
        if (currentGuestId != null && playerRepository.existsById(currentGuestId)) {
            // Claim the guest account stats
            player = playerRepository.findById(currentGuestId).get();
            if (!player.isGuest()) {
                // If they're already registered on this cookie but trying to register a new account?
                // Just create a new account in this case.
                player = new Player(UUID.randomUUID().toString(), username, false);
            } else {
                player.setUsername(username);
                player.setGuest(false);
            }
        } else {
            player = new Player(UUID.randomUUID().toString(), username, false);
        }

        player.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        playerRepository.save(player);

        setCookie(response, player.getId());
        return ResponseEntity.ok(Map.of("message", "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpServletResponse response) {
        String username = payload.get("username");
        String password = payload.get("password");

        Optional<Player> pOpt = playerRepository.findByUsername(username);
        if (pOpt.isPresent()) {
            Player player = pOpt.get();
            if (BCrypt.checkpw(password, player.getPassword())) {
                setCookie(response, player.getId());
                return ResponseEntity.ok(Map.of("message", "Login successful"));
            }
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("PLAYER_ID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Delete
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logged out. A new guest session will start on next request."));
    }

    private void setCookie(HttpServletResponse response, String playerId) {
        Cookie cookie = new Cookie("PLAYER_ID", playerId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
    }
}
