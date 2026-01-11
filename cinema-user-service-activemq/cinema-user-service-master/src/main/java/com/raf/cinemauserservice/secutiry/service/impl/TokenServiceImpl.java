package com.raf.cinemauserservice.secutiry.service.impl;

import com.raf.cinemauserservice.secutiry.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class TokenServiceImpl implements TokenService {

    @Value("${oauth.jwt.secret}")
    private String jwtSecret;

    @Override
    public String generate(Claims claims) {
        // Ovo verovatno ne koristis u Reservation servisu, ali neka stoji
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    @Override
    public Claims parseToken(String jwt) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    // BITNO: Koristimo getBytes() da izbegnemo Base64 problem
                    .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e) {
            // Logujemo gresku da vidimo zasto puca
            System.err.println("TokenService Error: " + e.getMessage());
            return null;
        }
        return claims;
    }
}