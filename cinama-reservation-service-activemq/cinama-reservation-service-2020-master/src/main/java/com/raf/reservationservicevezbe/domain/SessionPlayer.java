package com.raf.reservationservicevezbe.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Getter
@Setter
public class SessionPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private Session session;

    @Column(nullable = false)
    private Long userId;

    private String username;

    private boolean invited;

    private Boolean present;
}
