package com.raf.reservationservicevezbe.runner;

import com.raf.reservationservicevezbe.domain.*;
import com.raf.reservationservicevezbe.repository.GameRepository;
import com.raf.reservationservicevezbe.repository.SessionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Profile({"default"})
@Component
public class TestDataRunner implements CommandLineRunner {

    private final GameRepository gameRepository;
    private final SessionRepository sessionRepository;

    public TestDataRunner(GameRepository gameRepository, SessionRepository sessionRepository) {
        this.gameRepository = gameRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Provera da ne dupliramo podatke pri svakom pokretanju
        if (gameRepository.count() > 0) {
            return;
        }

        // 1. KREIRANJE IGARA
        Game lol = new Game();
        lol.setName("League of Legends");
        lol.setDescription("5v5 MOBA igra, veoma popularna.");
        lol.setGenre("MOBA");
        gameRepository.save(lol);

        Game cs2 = new Game();
        cs2.setName("Counter-Strike 2");
        cs2.setDescription("Taktička pucačina iz prvog lica.");
        cs2.setGenre("FPS");
        gameRepository.save(cs2);

        Game dnd = new Game();
        dnd.setName("Dungeons & Dragons");
        dnd.setDescription("Stona rol-plej igra mašte.");
        dnd.setGenre("RPG");
        gameRepository.save(dnd);

        Game amongUs = new Game();
        amongUs.setName("Among Us");
        amongUs.setDescription("Igra dedukcije i prevare u svemiru.");
        amongUs.setGenre("Social Deduction");
        gameRepository.save(amongUs);

        List<Game> games = List.of(lol, cs2, dnd, amongUs);
        Random random = new Random();

        System.out.println("Igre su kreirane...");

        // 2. KREIRANJE SESIJA
        // Pretpostavljamo da u User servisu imamo ID-eve od 1 (Admin) do ~22 (Generisani)

        // Primer 1: Završena sesija (Juče)
        Session s1 = new Session();
        s1.setGame(lol);
        s1.setHostId(2L); // Petar Petrović je host
        s1.setTitle("Večernji LoL Rank");
        s1.setDescription("Igramo flex queue, treba nam support.");
        s1.setMaxPlayers(5);
        s1.setTime(LocalDateTime.now().minusDays(1).withHour(20)); // Juče u 20h
        s1.setType(SessionType.OPEN);
        s1.setStatus(SessionStatus.FINISHED);

        // Dodajemo igrače koji su bili (ID 3, 4, 5)
        addPlayerToSession(s1, 3L, true); // Bio prisutan
        addPlayerToSession(s1, 4L, true); // Bio prisutan
        addPlayerToSession(s1, 5L, false); // Nije došao (abandoned)

        sessionRepository.save(s1);

        // Primer 2: Zakazana sesija (Sutra)
        Session s2 = new Session();
        s2.setGame(cs2);
        s2.setHostId(3L); // Neki random user je host
        s2.setTitle("CS2 Dust 2 Only");
        s2.setDescription("Samo ozbiljni igrači, global elite.");
        s2.setMaxPlayers(10);
        s2.setTime(LocalDateTime.now().plusDays(1).withHour(18)); // Sutra u 18h
        s2.setType(SessionType.OPEN);
        s2.setStatus(SessionStatus.SCHEDULED);

        // Neki su se već prijavili
        addPlayerToSession(s2, 2L, false); // Petar se prijavio
        addPlayerToSession(s2, 6L, false);

        sessionRepository.save(s2);

        // Primer 3: Zatvorena sesija (Za 5 dana)
        Session s3 = new Session();
        s3.setGame(dnd);
        s3.setHostId(1L); // Admin organizuje
        s3.setTitle("DnD One Shot Campaign");
        s3.setDescription("Kampanja za početnike.");
        s3.setMaxPlayers(6);
        s3.setTime(LocalDateTime.now().plusDays(5));
        s3.setType(SessionType.CLOSED);
        s3.setStatus(SessionStatus.SCHEDULED);

        sessionRepository.save(s3);

        // 3. GENERISANJE JOŠ 15 RANDOM SESIJA
        for (int i = 0; i < 15; i++) {
            Session s = new Session();
            s.setGame(games.get(random.nextInt(games.size())));

            // Hostovi su random useri od ID 2 do 10
            long hostId = 2L + random.nextInt(9);
            s.setHostId(hostId);

            s.setTitle("Random Sesija #" + (i + 1));
            s.setDescription("Opis za sesiju broj " + (i + 1));
            s.setMaxPlayers(4 + random.nextInt(6)); // 4 do 10 igrača

            // Vreme: Random između pre 10 dana i za 10 dana
            boolean isPast = random.nextBoolean();
            LocalDateTime time;
            if (isPast) {
                time = LocalDateTime.now().minusDays(random.nextInt(10) + 1);
                s.setStatus(SessionStatus.FINISHED);
            } else {
                time = LocalDateTime.now().plusDays(random.nextInt(10) + 1);
                s.setStatus(SessionStatus.SCHEDULED);
            }
            s.setTime(time);

            s.setType(random.nextBoolean() ? SessionType.OPEN : SessionType.CLOSED);

            // Popunjavanje igračima (Random 0 do 3 igrača po sesiji)
            int playersCount = random.nextInt(4);
            for (int j = 0; j < playersCount; j++) {
                // Generišemo random user ID (od 11 do 20) da ne budu isti kao hostovi
                long playerId = 11L + random.nextInt(10);

                // Pazimo da ne dodamo istog igrača dvaput u istu sesiju
                boolean alreadyIn = s.getPlayers().stream()
                        .anyMatch(p -> p.getUserId().equals(playerId));

                if (!alreadyIn && playerId != hostId) {
                    // Ako je sesija prošla, randomizujemo da li je bio prisutan
                    boolean present = isPast && random.nextBoolean();
                    addPlayerToSession(s, playerId, present);
                }
            }

            sessionRepository.save(s);
        }

        System.out.println("Session test podaci su uspešno ubačeni!");
    }

    private void addPlayerToSession(Session session, Long userId, boolean present) {
        if (session.getPlayers() == null) {
            session.setPlayers(new HashSet<>());
        }

        SessionPlayer player = new SessionPlayer();
        player.setSession(session);
        player.setUserId(userId);
        player.setUsername("TestUser_" + userId);

        player.setInvited(false);
        player.setPresent(present);

        session.getPlayers().add(player);
    }
}