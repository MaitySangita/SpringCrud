package com.example.springcrud.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Component
public class JwtUtil {


    @Value("${JWT_SECRET}")
    private String secret;

    @Value("${JWT_EXPIRATION}")
    private long expirationTime;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }


    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        JwtParser parser = Jwts.parser().verifyWith((SecretKey) key).build();
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser().verifyWith((SecretKey) key).build();
            parser.parseSignedClaims(token); // Will throw exception if invalid
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
