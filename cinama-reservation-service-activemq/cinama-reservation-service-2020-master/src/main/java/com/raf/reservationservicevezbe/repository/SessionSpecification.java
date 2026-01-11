package com.raf.reservationservicevezbe.repository;

import com.raf.reservationservicevezbe.domain.Session;
import com.raf.reservationservicevezbe.domain.SessionType;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionSpecification {

    public static Specification<Session> getSessions(Long gameId,
                                                     SessionType type,
                                                     Integer maxPlayers,
                                                     String description,
                                                     LocalDateTime from,
                                                     LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            // Dodatno: Filtriraj samo sesije koje se još nisu desile ili završile
            // predicates.add(criteriaBuilder.greaterThan(root.get("time"), LocalDateTime.now()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}