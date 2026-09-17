package com.raf.reservationservicevezbe.repository;

import com.raf.reservationservicevezbe.domain.Session;
import com.raf.reservationservicevezbe.domain.SessionPlayer;
import com.raf.reservationservicevezbe.domain.SessionType;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionSpecification {

    public static Specification<Session> getSessions(Long gameId,
                                                     SessionType type,
                                                     Integer maxPlayers,
                                                     String description,
                                                     LocalDateTime from,
                                                     LocalDateTime to,
                                                     String sortField,
                                                     String sortDirection,
                                                     Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- STANDARDNI FILTERI ---
            if (gameId != null) {
                predicates.add(criteriaBuilder.equal(root.get("game").get("id"), gameId));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (maxPlayers != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxPlayers"), maxPlayers));
            }
            if (description != null && !description.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }

            // --- MY SESSIONS LOGIKA ---
            if (userId != null) {
                // Uslov 1: Korisnik je HOST
                Predicate isHost = criteriaBuilder.equal(root.get("hostId"), userId);

                // Uslov 2: Korisnik je u listi IGRACA
                // Moramo da joinujemo tabelu igraca da bismo proverili
                Join<Session, SessionPlayer> playersJoin = root.join("players", JoinType.LEFT);
                Predicate isPlayer = criteriaBuilder.equal(playersJoin.get("userId"), userId);

                // Spajamo sa ILI (OR)
                predicates.add(criteriaBuilder.or(isHost, isPlayer));

                // BITNO: Da ne bismo dobili duplikate zbog join-a
                query.distinct(true);
            }

            // --- SORTIRANJE (Isto kao malopre) ---
            if (sortField != null) {
                if ("playerCount".equalsIgnoreCase(sortField)) {

                    Expression<Long> countExpr = criteriaBuilder.count(root.join("players", JoinType.LEFT));
                    query.groupBy(root.get("id"));

                    if ("DESC".equalsIgnoreCase(sortDirection)) {
                        query.orderBy(criteriaBuilder.desc(countExpr));
                    } else {
                        query.orderBy(criteriaBuilder.asc(countExpr));
                    }
                } else if ("time".equalsIgnoreCase(sortField)) {
                    if ("DESC".equalsIgnoreCase(sortDirection)) {
                        query.orderBy(criteriaBuilder.desc(root.get("time")));
                    } else {
                        query.orderBy(criteriaBuilder.asc(root.get("time")));
                    }
                } else {
                    query.orderBy(criteriaBuilder.desc(root.get("id")));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}