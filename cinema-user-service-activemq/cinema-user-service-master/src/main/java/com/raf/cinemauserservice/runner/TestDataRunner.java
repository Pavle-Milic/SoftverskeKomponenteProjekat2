package com.raf.cinemauserservice.runner;

import com.raf.cinemauserservice.domain.Rank;
import com.raf.cinemauserservice.domain.Role;
import com.raf.cinemauserservice.domain.User;
import com.raf.cinemauserservice.repository.RoleRepository;
import com.raf.cinemauserservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Profile({"default"})
@Component
public class TestDataRunner implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataRunner(RoleRepository roleRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // ===== ROLES =====
        Role roleUser = getOrCreateRole("ROLE_USER", "User role");
        Role roleAdmin = getOrCreateRole("ROLE_ADMIN", "Admin role");

        // Ako već ima korisnika – ne seedujemo ponovo
        if (userRepository.count() > 0) {
            System.out.println("Test podaci već postoje – preskačem inicijalizaciju.");
            return;
        }

        // ===== ADMIN =====
        User admin = new User();
        admin.setEmail("admin@gmail.com");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFirstName("Glavni");
        admin.setLastName("Administrator");
        admin.setBirthDate(LocalDate.of(1990, 1, 1));
        admin.setActivated(true);
        admin.setBlocked(false);
        admin.setRole(roleAdmin);
        admin.setRank(Rank.KNEZ);
        admin.setSessionsOrganized(20);
        admin.setSessionsAttended(20);
        admin.setAttendancePercentage(100.0);
        userRepository.save(admin);

        // ===== BASIC USER =====
        User u1 = new User();
        u1.setEmail("user@gmail.com");
        u1.setUsername("korisnik1");
        u1.setPassword(passwordEncoder.encode("user"));
        u1.setFirstName("Petar");
        u1.setLastName("Petrović");
        u1.setBirthDate(LocalDate.of(1995, 5, 15));
        u1.setActivated(true);
        u1.setBlocked(false);
        u1.setRole(roleUser);
        u1.setRank(Rank.NONE);
        u1.setSessionsOrganized(1);
        u1.setSessionsAttended(3);
        u1.setAttendancePercentage(75.0);
        userRepository.save(u1);

        // ===== BULK USERS =====
        String[] firstNames = {"Marko", "Jelena", "Nikola", "Milica", "Stefan", "Ana", "Luka", "Maja"};
        String[] lastNames = {"Ivić", "Stojanović", "Jovanović", "Nikolić", "Kostić", "Babić"};
        Rank[] ranks = Rank.values();

        for (int i = 1; i <= 20; i++) {
            User u = new User();
            u.setEmail("user" + i + "@gmail.com");
            u.setUsername("user_name_" + i);
            u.setPassword(passwordEncoder.encode("password" + i));
            u.setFirstName(firstNames[i % firstNames.length]);
            u.setLastName(lastNames[i % lastNames.length]);
            u.setBirthDate(LocalDate.of(
                    1985 + (i % 15),
                    (i % 12) + 1,
                    (i % 28) + 1
            ));
            u.setRank(ranks[i % ranks.length]);
            u.setActivated(true);
            u.setBlocked(i % 5 == 0);
            u.setRole(roleUser);
            u.setSessionsOrganized(i * 2);
            u.setSessionsAttended(i * 3);
            u.setAttendancePercentage(90.0 + (i % 10));
            userRepository.save(u);
        }

        System.out.println("Test podaci su uspešno ubačeni.");
    }

    private Role getOrCreateRole(String name, String description) {
        Optional<Role> existing = roleRepository.findRoleByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        Role role = new Role(name, description);
        return roleRepository.save(role);
    }
}
