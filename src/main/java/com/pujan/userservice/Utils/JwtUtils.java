package com.pujan.userservice.Utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${spring.app.JwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.JwtExpirationMs}")
    private int jwtExpirationMs;

    private Authentication authentication;

    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }


    public String generateTokenFromUsername(UserDetails userDetails){

//        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
//
//        String username = userPrincipal.getUsername();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(new Date().getTime()+jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUsernameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private Key key(){
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }

    }



}
