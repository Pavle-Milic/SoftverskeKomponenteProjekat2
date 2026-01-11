package com.raf.reservationservicevezbe.client;

import com.raf.reservationservicevezbe.dto.UserClientDto;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceClient {

    private final RestTemplate restTemplate;

    public UserServiceClient(RestTemplate userServiceRestTemplate) {
        this.restTemplate = userServiceRestTemplate;
    }

    // --- GET METODE ---

    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public UserClientDto getUser(Long userId) {
        try {
            return restTemplate.getForObject("/user/" + userId, UserClientDto.class);
        } catch (HttpClientErrorException e) {
            // Ako korisnik ne postoji (404), vraćamo null ili bacamo grešku dalje
            if (e.getStatusCode().value() == 404) return null;
            throw e;
        }
    }

    /**
     * Proverava da li korisnik ispunjava uslove za kreiranje sesije.
     * (Nije blokiran i ima procenat >= 90%)
     */
    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean checkEligibility(Long userId) {
        try {
            // Gađa endpoint: GET /user/{id}/eligibility
            Boolean eligible = restTemplate.getForObject("/user/" + userId + "/eligibility", Boolean.class);
            return eligible != null && eligible;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Ako servis ne radi, podrazumevano ne dozvoljavamo kreiranje (fail-safe)
        }
    }

    // --- POST METODE (Ažuriranje statistike) ---

    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void incrementRegisteredSession(Long userId) {
        // Gađa endpoint: POST /user/{id}/increment-registered
        restTemplate.postForEntity("/user/" + userId + "/increment-registered", null, Void.class);
    }

    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void updateSessionAttendance(Long userId, boolean present) {
        // Gađa endpoint: POST /user/{id}/attendance?present=true/false
        // PAŽNJA: U User servisu smo ga nazvali "/attendance", ne "update-attendance"
        restTemplate.postForEntity("/user/" + userId + "/attendance?present=" + present, null, Void.class);
    }

    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void incrementHostSessions(Long hostId) {
        // Gađa endpoint: POST /user/{id}/organizer-stats
        // PAŽNJA: U User servisu smo ga nazvali "/organizer-stats"
        restTemplate.postForEntity("/user/" + hostId + "/organizer-stats", null, Void.class);
    }
}