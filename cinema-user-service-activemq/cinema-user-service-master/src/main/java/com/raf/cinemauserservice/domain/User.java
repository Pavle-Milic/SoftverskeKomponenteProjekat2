package com.raf.cinemauserservice.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(columnList = "username", unique = true),
        @Index(columnList = "email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    @ManyToOne(optional = false)
    private Role role;

    private boolean activated = false; // Za registraciju
    private boolean blocked = false;   // Admin može da blokira

    // Statistika
    private int sessionsRegistered = 0; // Ukupno prijavljenih
    private int sessionsAttended = 0;   // Uspešno prisustvovao
    private int sessionsAbandoned = 0;  // Nije se pojavio / napustio
    private int sessionsOrganized = 0;  // Uspešno organizovao

    private double attendancePercentage = 100.0; // Inicijalno 100%

    @Enumerated(EnumType.STRING)
    private Rank rank = Rank.NONE;

    // ---- DOMEN LOGIKA ----

    // Poziva se kada se igrač prijavi na sesiju (samo uvećava brojač)
    public void incrementRegistered() {
        this.sessionsRegistered++;
        // Ovde NE menjamo procenat, jer sesija još nije završena
    }

    // Poziva se kada se sesija završi
    public void concludeSession(boolean present) {
        if (present) {
            this.sessionsAttended++;
        } else {
            this.sessionsAbandoned++;
        }
        recalcAttendance();
    }

    // Poziva se kada organizator uspešno završi sesiju
    public void incrementOrganized() {
        this.sessionsOrganized++;
        recalcRank();
    }

    private void recalcAttendance() {
        int totalConcluded = sessionsAttended + sessionsAbandoned;
        if (totalConcluded == 0) {
            this.attendancePercentage = 100.0;
        } else {
            this.attendancePercentage = ((double) sessionsAttended / totalConcluded) * 100.0;
        }
    }

    private void recalcRank() {
        if (sessionsOrganized >= 100) this.rank = Rank.KNEZ;
        else if (sessionsOrganized >= 50) this.rank = Rank.VOJVODA;
        else if (sessionsOrganized >= 25) this.rank = Rank.HAJDUK;
        else if (sessionsOrganized >= 10) this.rank = Rank.BARJAKTAR;
        else this.rank = Rank.NONE;
    }

    // Provera uslova za kreiranje sesije
    public boolean canCreateSession() {
        return !blocked && attendancePercentage >= 90.0;
    }
}