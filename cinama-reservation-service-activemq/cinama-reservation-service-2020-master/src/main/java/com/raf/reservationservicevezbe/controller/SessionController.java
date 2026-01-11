package com.raf.reservationservicevezbe.controller;

import com.raf.reservationservicevezbe.domain.Session;
import com.raf.reservationservicevezbe.domain.SessionPlayer;
import com.raf.reservationservicevezbe.domain.SessionType;
import com.raf.reservationservicevezbe.dto.CreateSessionDto;
import com.raf.reservationservicevezbe.dto.GameDto;
import com.raf.reservationservicevezbe.repository.SessionRepository;
import com.raf.reservationservicevezbe.secutiry.CheckSecurity;
import com.raf.reservationservicevezbe.secutiry.service.TokenService;
import com.raf.reservationservicevezbe.service.GameService;
import com.raf.reservationservicevezbe.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/session")
public class SessionController {

    private final SessionService sessionService;
    private final ObjectMapper objectMapper;
    private final SessionRepository sessionRepository;
    private final TokenService tokenService;

    public SessionController(SessionService sessionService, ObjectMapper objectMapper, SessionRepository sessionRepository, TokenService tokenService) {
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
        this.sessionRepository = sessionRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/create")
    @CheckSecurity(roles = {"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<Session> createSession(@RequestHeader("Authorization") String token,
                                                 @RequestBody CreateSessionDto dto) {
        Long userId = extractIdFromToken(token);
        return ResponseEntity.ok(sessionService.createSession(dto, userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Session>> searchSessions(@RequestParam(required = false) Long gameId,
                                                        @RequestParam(required = false) SessionType type,
                                                        @RequestParam(required = false) Integer maxPlayers,
                                                        @RequestParam(required = false) String description,
                                                        Pageable pageable) {
        return ResponseEntity.ok(sessionService.searchSessions(gameId, type, maxPlayers, description, pageable));
    }

    @PostMapping("/{id}/join")
    @CheckSecurity(roles = {"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<Void> joinSession(@RequestHeader("Authorization") String token,
                                            @PathVariable Long id,
                                            @RequestParam(required = false) String invitationToken) {
        Long userId = extractIdFromToken(token);
        String username=extractUsernameFromToken(token);
        sessionService.joinSession(id, userId,username, invitationToken);
        return ResponseEntity.ok().build();
    }

    // --- NOVE METODE KOJE SU FALILE ---

    @PostMapping("/{id}/invite")
    @CheckSecurity(roles = {"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<String> inviteUser(@RequestHeader("Authorization") String token,
                                             @PathVariable Long id,
                                             @RequestParam Long userId) {
        Long hostId = extractIdFromToken(token);
        String inviteToken = sessionService.inviteUser(id, hostId, userId);
        return new ResponseEntity<>(inviteToken, HttpStatus.OK);
    }

    @PostMapping("/{id}/conclude")
    @CheckSecurity(roles = {"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<Void> concludeSession(@RequestHeader("Authorization") String token,
                                                @PathVariable Long id,
                                                @RequestBody List<Long> presentUserIds) {
        Long hostId = extractIdFromToken(token);
        sessionService.concludeSession(id, hostId, presentUserIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @CheckSecurity(roles = {"ROLE_USER", "ROLE_ADMIN"})
    public ResponseEntity<Void> cancelSession(@RequestHeader("Authorization") String token,
                                              @PathVariable Long id) {
        Long userId = extractIdFromToken(token);
        boolean isAdmin = extractRoleFromToken(token).equals("ROLE_ADMIN");
        sessionService.cancelSession(id, userId, isAdmin);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-sessions")
    @CheckSecurity(roles = {"ROLE_USER", "ROLE_ADMIN"}) // Dozvoli i obicnim korisnicima
    public ResponseEntity<List<Session>> getMySessions(@RequestHeader("Authorization") String authorization) {
        // 1. Izvuci token
        String token = authorization.split(" ")[1];

        // 2. Parsiraj token da dobijes ID korisnika (koristimo tvoj TokenService)
        Claims claims = tokenService.parseToken(token);
        Long userId = claims.get("id", Long.class); // Mora da se poklapa sa onim sto si stavio u claims pri loginu
        List<Session> sessions = sessionService.getMySessions(userId);

        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<Set<SessionPlayer>> getSessionPlayers(@PathVariable Long id) {
        Set<SessionPlayer> players = sessionService.findSessionPlayers(id);
        return ResponseEntity.ok(players);
    }

    // --- HELPER METODE ---

    private Long extractIdFromToken(String token) {
        Map<String, Object> claims = getClaims(token);
        return ((Number) claims.get("id")).longValue();
    }

    private String extractUsernameFromToken(String token) {
        Map<String, Object> claims = getClaims(token);
        return ((String) claims.get("username"));
    }

    private String extractRoleFromToken(String token) {
        Map<String, Object> claims = getClaims(token);
        return (String) claims.get("role");
    }

    private Map<String, Object> getClaims(String token) {
        try {
            String[] chunks = token.replace("Bearer ", "").split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }
}