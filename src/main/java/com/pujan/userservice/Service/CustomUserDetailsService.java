package com.pujan.userservice.Service;

import com.pujan.userservice.Model.Role;
import com.pujan.userservice.Model.Users; // Import your Entity
import com.pujan.userservice.Repository.UsersRepo; // Import your Repo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service // <--- THIS IS CRITICAL. Without this, Spring can't find the bean.
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepo usersRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Fetch the user from the DB
        Users user = usersRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();


        // 2. Convert your Set<Role> to List<GrantedAuthority>
        List<GrantedAuthority> permissionAuthorities = user.getRoles().stream()
                .flatMap((Role role) -> role.getPermissions().stream())
                .map(SimpleGrantedAuthority::new)
                .distinct()
                .collect(Collectors.toList());

        authorities.addAll(permissionAuthorities);
        // This assumes your Role entity has a method getName() returning strings like "ROLE_USER"
        List<GrantedAuthority> roleAuthorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        authorities.addAll(roleAuthorities);
        // 3. Return the Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}