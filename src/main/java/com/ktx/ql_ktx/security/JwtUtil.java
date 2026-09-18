package com.ktx.ql_ktx.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Khoa bi mat de ky token
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long thoiHanMs = 1000L * 60 * 60 * 24; // 24 gio

    public String taoToken(String tenDangNhap, String vaiTro) {
        return Jwts.builder()
                .setSubject(tenDangNhap)
                .claim("vaiTro", vaiTro)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + thoiHanMs))
                .signWith(secretKey)
                .compact();
    }

    public String layTenDangNhap(String token) {
        return layClaims(token).getSubject();
    }

    public boolean hopLe(String token) {
        try {
            Claims claims = layClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims layClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
