package com.raf.reservationservicevezbe.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Game game;

    @Column(nullable = false)
    private Long hostId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Integer maxPlayers;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<SessionPlayer> players = new HashSet<>();

    // === DOMAIN HELPERS ===

    public boolean hasPlayer(Long userId) {
        return players.stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    public int playersCount() {
        return players.size();
    }

    public void addPlayer(SessionPlayer player) {
        players.add(player);
        player.setSession(this);
    }
}
