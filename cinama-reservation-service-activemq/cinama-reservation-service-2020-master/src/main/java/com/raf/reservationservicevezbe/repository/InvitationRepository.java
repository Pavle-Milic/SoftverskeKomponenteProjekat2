package com.raf.reservationservicevezbe.repository;
import com.raf.reservationservicevezbe.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findById(Long id);
    Optional<Invitation> findByToken(String token);
}
