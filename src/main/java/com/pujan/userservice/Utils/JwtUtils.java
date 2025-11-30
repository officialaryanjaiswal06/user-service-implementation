package com.pujan.userservice.Utils;

import com.pujan.userservice.Model.Role;
import com.pujan.userservice.Model.Users;
import com.pujan.userservice.Repository.UsersRepo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${spring.app.JwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.JwtExpirationMs}")
    private int jwtExpirationMs;

    private final UsersRepo usersRepo;

    public JwtUtils(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public String generateTokenFromUsername(UserDetails userDetails){

        Users user = usersRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
//        List<String> roles = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//
//        // perms claim: list of permission codes (e.g. PROGRAM:ACADEMIC:READ)
//        List<String> perms = user.getRoles().stream()
//                .flatMap((Role role) -> role.getPermissions().stream())
//                .map(Permission::getCode)
//                .distinct()
//                .collect(Collectors.toList());
        // roles claim: only real role names from the DB (e.g. ROLE_ADMIN, ROLE_SUPER_ADMIN)
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // perms claim: list of permission codes (e.g. PROGRAM:ACADEMIC:READ)
        List<String> perms = user.getRoles().stream()
                .flatMap((Role role) -> role.getPermissions().stream())
                .distinct()
                .collect(Collectors.toList());


        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("perms", perms)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
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

    public List<String> getRolesFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", List.class);
    }

    public List<String> getPermsFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("perms", List.class);
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
