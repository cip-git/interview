package com.interview.service.impl;

import com.interview.service.api.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${authorization.secret.key}")
    private String secretKeyB64;

    @Value("${authorization.token.validity:PT24H}")
    private Duration validity;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails user) {
        Map<String, Object> claims = Map
                .of(
                        "roles",
                        user.getAuthorities().stream().map(a -> a.getAuthority()).toList()
        );
        return createToken(claims, user.getUsername());
    }

    public String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validity.toMillis());
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        try {
            String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims parseAllClaims(String token) {
        String jws = token.startsWith("Bearer ") ? token.substring(7) : token;

        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .setAllowedClockSkewSeconds(60) // toleranță 60s
                .build()
                .parseClaimsJws(jws)
                .getBody();
    }

    private Key signingKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKeyB64);
        return Keys.hmacShaKeyFor(bytes);
    }
}
