package com.pujan.userservice.Controller;

import com.pujan.userservice.Model.Role;
import com.pujan.userservice.Model.Users;
import com.pujan.userservice.Repository.RoleRepo;
import com.pujan.userservice.Repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:6969")
public class AuthController {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

@PostMapping("/register")
    public ResponseEntity<String> registeruser (@RequestBody Users users){
        if (usersRepo.existsByUsername(users.getUsername())){
            return ResponseEntity.badRequest().body("Error : username is already taken!");
        }

        Users users1 = new Users();
        users1.setUsername(users.getUsername());
        users1.setPassword(passwordEncoder.encode(users.getPassword()));
        users1.setEmail(users.getEmail());

        // Registration endpoint is public: always register as plain USER, regardless of requested roles
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role Not Found"));
        roles.add(userRole);

        users1.setRoles(roles);
        usersRepo.save(users1);
        return ResponseEntity.ok("User Registered Successfully");



    }




}
