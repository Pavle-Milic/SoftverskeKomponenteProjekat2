package com.raf.reservationservicevezbe.service;

import com.raf.reservationservicevezbe.client.UserServiceClient;
import com.raf.reservationservicevezbe.domain.*;
import com.raf.reservationservicevezbe.dto.CreateSessionDto;
import com.raf.reservationservicevezbe.dto.UserClientDto;
import com.raf.reservationservicevezbe.repository.GameRepository;
import com.raf.reservationservicevezbe.repository.InvitationRepository; // Napravi ovaj repo
import com.raf.reservationservicevezbe.repository.SessionRepository;
import com.raf.reservationservicevezbe.repository.SessionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final GameRepository gameRepository;
    private final InvitationRepository invitationRepository;
    private final UserServiceClient userServiceClient;

    public SessionService(SessionRepository sessionRepository,
                          GameRepository gameRepository,
                          InvitationRepository invitationRepository,
                          UserServiceClient userServiceClient) {
        this.sessionRepository = sessionRepository;
        this.gameRepository = gameRepository;
        this.invitationRepository = invitationRepository;
        this.userServiceClient = userServiceClient;
    }

    // --- SEARCH ---
    @Transactional(readOnly = true)
    public Page<Session> searchSessions(Long gameId, SessionType type, Integer maxPlayers, String description, Pageable pageable) {

        String sortField = "time";
        String sortDirection = "ASC";

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            sortField = order.getProperty();
            sortDirection = order.getDirection().name();
        }

        // 2. Pravimo Specifikaciju i saljemo joj parametre za sortiranje
        Specification<Session> spec = SessionSpecification.getSessions(
                gameId, type, maxPlayers, description, null, null, sortField, sortDirection,null
        );

        // 3. Pravimo novi Pageable BEZ sortiranja za repository poziv.
        // Zašto? Zato što smo sortiranje rešili ručno unutar Specifikacije (naročito za playerCount).
        // Ako ostavimo sort u pageable, Spring će pokušati da sortira opet i možda pukne na "playerCount" polju koje ne postoji u bazi.
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return sessionRepository.findAll(spec, unsortedPageable);
    }

    // --- CREATE ---
    public Session createSession(CreateSessionDto dto, Long hostId) {
        // 1. Provera preko User Servisa (Da li je blokiran i da li ima rank/procenat)
        // Ovo menja tvoju logiku rucnog racunanja procenta
        boolean eligible = userServiceClient.checkEligibility(hostId);
        if (!eligible) {
            throw new RuntimeException("User is not eligible to create a session (Blocked or Low Attendance).");
        }

        Game game = gameRepository.findById(dto.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found."));

        Session session = new Session();
        session.setGame(game);
        session.setHostId(hostId);
        session.setTitle(dto.getTitle());
        session.setDescription(dto.getDescription());
        session.setMaxPlayers(dto.getMaxPlayers());
        session.setTime(dto.getTime());
        session.setType(dto.getType());
        session.setStatus(SessionStatus.SCHEDULED);

        return sessionRepository.save(session);
    }

    // --- JOIN ---
    public void joinSession(Long sessionId, Long userId,String username, String invitationToken) {
        // Provera da li je user blokiran
        UserClientDto user = userServiceClient.getUser(userId);
        if (user != null && user.isBlocked()) {
            throw new RuntimeException("User is blocked.");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found."));

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new RuntimeException("Session is not active.");
        }

        if (session.hasPlayer(userId)) {
            throw new RuntimeException("User already joined.");
        }

        boolean invited = false;

        // Logika za zatvorene sesije
        if (session.getType() == SessionType.CLOSED) {
            if (invitationToken == null) {
                // Ako je host taj koji ulazi (ne bi trebalo, ali za svaki slucaj), ili posebna logika
                throw new RuntimeException("Invitation token required for closed session.");
            }

            Invitation invitation = invitationRepository.findByToken(invitationToken)
                    .orElseThrow(() -> new RuntimeException("Invalid invitation token."));

            if (invitation.isUsed()) {
                throw new RuntimeException("Invitation already used.");
            }

            if (!invitation.getSessionId().equals(sessionId)) {
                throw new RuntimeException("Invitation does not belong to this session.");
            }

            invitation.setUsed(true);
            invitationRepository.save(invitation);
            invited = true;
        }

        if (session.playersCount() >= session.getMaxPlayers()) {
            throw new RuntimeException("Session is full.");
        }

        SessionPlayer player = new SessionPlayer();
        player.setUserId(userId);
        player.setInvited(invited);
        player.setUsername(username);
        player.setPresent(null); // Jos nije odrzana

        session.addPlayer(player);
        sessionRepository.save(session);

        // Javljamo User Servisu da poveca brojac prijava
        try {
            userServiceClient.incrementRegisteredSession(userId);
        } catch (Exception e) {
            // Log error, ali ne zaustavljaj transakciju ako servis nije dostupan?
            // Ili rollback? Zavisi od biznis logike. Ovde cemo pustiti da pukne ako je servis down.
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<Session> getMySessions(Long userId, Pageable pageable) {

        // 1. Izvlacenje sortiranja (Isto kao u searchSessions)
        String sortField = "time";
        String sortDirection = "ASC";

        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            sortField = order.getProperty();
            sortDirection = order.getDirection().name();
        }

        // 2. Pozivamo specifikaciju
        // Svi filteri su null osim 'userId' koji je kljucan ovde
        Specification<Session> spec = SessionSpecification.getSessions(
                null,  // gameId
                null,  // type
                null,  // maxPlayers
                null,  // description
                null,  // from
                null,  // to
                sortField,
                sortDirection,
                userId
        );

        // 3. Unsorted pageable
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return sessionRepository.findAll(spec, unsortedPageable);
    }

    @Transactional(readOnly = true)
    public Set<SessionPlayer> findSessionPlayers(Long id) {
        Set<SessionPlayer> sessionPlayers = new HashSet<>();
        Session session=sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found."));

        sessionPlayers.addAll(session.getPlayers());

        return sessionPlayers;
    }

    // --- INVITE ---
    public String inviteUser(Long sessionId, Long hostId, Long userIdToInvite) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found."));

        if (!session.getHostId().equals(hostId)) {
            throw new RuntimeException("Only host can invite users.");
        }

        Invitation invitation = new Invitation();
        invitation.setSessionId(sessionId);
        invitation.setInvitedUserId(userIdToInvite);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setUsed(false);

        invitationRepository.save(invitation);

        return invitation.getToken();
    }

    // --- CANCEL ---
    public void cancelSession(Long sessionId, Long requesterId, boolean isAdmin) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found."));

        if (!session.getHostId().equals(requesterId) && !isAdmin) {
            throw new RuntimeException("Not allowed to cancel this session.");
        }

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new RuntimeException("Session is not active (Maybe already finished or canceled).");
        }

        session.setStatus(SessionStatus.CANCELED);
        sessionRepository.save(session);
    }

    // --- CONCLUDE ---
    public void concludeSession(Long sessionId, Long hostId, List<Long> presentUserIds) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found."));

        if (!session.getHostId().equals(hostId)) {
            throw new RuntimeException("Only host can conclude session.");
        }

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new RuntimeException("Session is not active (Maybe already finished or canceled).");
        }

        // Ažuriranje prisustva
        for (SessionPlayer p : session.getPlayers()) {
            boolean present = presentUserIds != null && presentUserIds.contains(p.getUserId());
            p.setPresent(present);

            // Javljamo User servisu za svakog igraca
            userServiceClient.updateSessionAttendance(p.getUserId(), present);
        }

        // Nagradjujemo Host-a
        userServiceClient.incrementHostSessions(hostId);

        session.setStatus(SessionStatus.FINISHED);
        sessionRepository.save(session);
    }


}
