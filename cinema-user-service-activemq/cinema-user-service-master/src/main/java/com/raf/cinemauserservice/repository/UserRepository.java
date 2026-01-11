package com.raf.cinemauserservice.repository;

import com.raf.cinemauserservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserById(Long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsById(Long id);
}
