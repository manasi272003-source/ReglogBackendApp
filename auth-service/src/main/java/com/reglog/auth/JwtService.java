package com.reglog.auth;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
@Service
public class JwtService {
 private final SecretKey key; private final long minutes;
 public JwtService(@Value("${reglog.jwt.secret}") String secret,@Value("${reglog.jwt.minutes:60}") long minutes){if(secret==null||secret.length()<43)throw new IllegalStateException("JWT_SECRET must be a base64 encoded key of at least 32 bytes");this.key=Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret));this.minutes=minutes;}
 public String issue(Long userId,String sessionId){Instant now=Instant.now();return Jwts.builder().subject(userId.toString()).id(sessionId).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(minutes*60))).signWith(key).compact();}
 public Jws<Claims> parse(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);}
 public Instant expiry(){return Instant.now().plusSeconds(minutes*60);}
}
