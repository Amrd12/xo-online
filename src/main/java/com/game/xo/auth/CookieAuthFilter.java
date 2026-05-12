package com.game.xo.auth;

import com.game.xo.model.Player;
import com.game.xo.repository.PlayerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CookieAuthFilter extends OncePerRequestFilter {

    private final PlayerRepository playerRepository;

    public CookieAuthFilter(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip static resources and WebSocket endpoints to avoid unnecessary database hits if needed,
        // but for now, we ensure everyone gets a cookie.
        String path = request.getRequestURI();
        if (path.startsWith("/css/") || path.startsWith("/js/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String playerId = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("PLAYER_ID".equals(cookie.getName())) {
                    playerId = cookie.getValue();
                    break;
                }
            }
        }

        if (playerId == null || !playerRepository.existsById(playerId)) {
            // Create a new Guest Player
            playerId = UUID.randomUUID().toString();
            Player guest = new Player(playerId, "Guest_" + playerId.substring(0, 4), true);
            playerRepository.save(guest);

            Cookie newCookie = new Cookie("PLAYER_ID", playerId);
            newCookie.setPath("/");
            newCookie.setHttpOnly(true);
            newCookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
            response.addCookie(newCookie);
        }

        filterChain.doFilter(request, response);
    }
}
