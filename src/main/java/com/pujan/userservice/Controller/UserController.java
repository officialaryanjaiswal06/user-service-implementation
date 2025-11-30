package com.pujan.userservice.Controller;

import com.pujan.userservice.DTO.CreateUserRequest;
import com.pujan.userservice.DTO.UpdateRolesRequest;
import com.pujan.userservice.DTO.UpdateUserRequest;
import com.pujan.userservice.DTO.UserResponse;
import com.pujan.userservice.Model.Role;
import com.pujan.userservice.Model.Users;
import com.pujan.userservice.Repository.RoleRepo;
import com.pujan.userservice.Repository.UsersRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UsersRepo usersRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserController(UsersRepo usersRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder) {
        this.usersRepo = usersRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // --------- Helpers ---------
    private UserResponse toResponse(Users u) {
        UserResponse res = new UserResponse();
        res.setId(u.getId());
        res.setUsername(u.getUsername());
        res.setEmail(u.getEmail());
        res.setRoles(u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return res;
    }

    private boolean hasAuthority(Authentication auth, String role) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }

    private Set<Role> resolveRolesForCreate(Authentication auth, Set<String> incomingRoles) {
        boolean isSuperAdmin = hasAuthority(auth, "ROLE_SUPER_ADMIN");
        boolean isAdmin = hasAuthority(auth, "ROLE_ADMIN");
        boolean isEditor = hasAuthority(auth, "ROLE_EDITOR");

        // Determine which roles the caller is allowed to assign and the default role
        Set<String> allowed;
        String defaultRole;
        if (isSuperAdmin) {
            // SUPER_ADMIN can create ADMIN, EDITOR, or USER accounts
            allowed = Set.of("ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER");
            defaultRole = "ROLE_USER";
        } else if (isAdmin) {
            // ADMIN can ONLY create EDITOR accounts (no USER, no SUPER_ADMIN)
            allowed = Set.of("ROLE_EDITOR");
            defaultRole = "ROLE_EDITOR";
        } else if (isEditor) {
            // EDITOR can create basic USER accounts only
            allowed = Set.of("ROLE_USER");
            defaultRole = "ROLE_USER";
        } else {
            // Fallback: treat as basic user; only USER is allowed
            allowed = Set.of("ROLE_USER");
            defaultRole = "ROLE_USER";
        }

        Set<String> requested = (incomingRoles == null || incomingRoles.isEmpty())
                ? Set.of(defaultRole)
                : incomingRoles;

        // Intersect requested with allowed
        Set<String> effective = requested.stream()
                .filter(allowed::contains)
                .collect(Collectors.toSet());
        if (effective.isEmpty()) {
            effective = Set.of(defaultRole);
        }

        Set<Role> roles = new HashSet<>();
        for (String r : effective) {
            Role role = roleRepo.findByName(r).orElseThrow(() -> new RuntimeException("Role not found: " + r));
            roles.add(role);
        }
        return roles;
    }

    private void guardAdminTarget(Users target, Authentication auth) {
        boolean targetIsAdminish = target.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN") || r.getName().equals("ROLE_SUPER_ADMIN"));
        if (targetIsAdminish && !hasAuthority(auth, "ROLE_SUPER_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Only SUPER_ADMIN can operate on ADMIN/SUPER_ADMIN accounts");
        }
    }

    // --------- Endpoints ---------

    // Self profile
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        Users u = usersRepo.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(toResponse(u));
    }

    // List all users: ADMIN+
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> list() {
        List<UserResponse> res = usersRepo.findAll().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(res);
    }

    // Get user by id: ADMIN+ or self
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id, authentication)")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        Users u = usersRepo.findById(id).orElseThrow();
        return ResponseEntity.ok(toResponse(u));
    }

    // Create user: ADMIN or EDITOR (EDITOR can only create ROLE_USER)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest req, Authentication auth) {
        if (usersRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        Users u = new Users();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setEmail(req.getEmail());
        u.setRoles(resolveRolesForCreate(auth, req.getRoles()));
        Users saved = usersRepo.save(u);
        return ResponseEntity.created(URI.create("/users/" + saved.getId())).body(toResponse(saved));
    }

    // Update user basic fields (no roles): ADMIN or EDITOR, or self
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR') or @userSecurity.isSelf(#id, authentication)")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UpdateUserRequest req, Authentication auth) {
        Users u = usersRepo.findById(id).orElseThrow();
        // If EDITOR (not ADMIN), prevent editing ADMIN/SUPER_ADMIN targets
        boolean isAdmin = hasAuthority(auth, "ROLE_ADMIN") || hasAuthority(auth, "ROLE_SUPER_ADMIN");
        if (!isAdmin) {
            guardAdminTarget(u, auth);
        }
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        Users saved = usersRepo.save(u);
        return ResponseEntity.ok(toResponse(saved));
    }

    // Update roles: ADMIN+ (but only SUPER_ADMIN can touch ADMIN/SUPER_ADMIN targets)
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRoles(@PathVariable Long id, @RequestBody UpdateRolesRequest req, Authentication auth) {
        Users u = usersRepo.findById(id).orElseThrow();
        guardAdminTarget(u, auth);

        boolean isSuperAdmin = hasAuthority(auth, "ROLE_SUPER_ADMIN");
        boolean isAdmin = hasAuthority(auth, "ROLE_ADMIN");

        // Allowed target roles to set
        Set<String> allowed;
        if (isSuperAdmin) {
            allowed = Set.of("ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER");
        } else if (isAdmin) {
            // ADMIN can only assign EDITOR (no USER, no ADMIN/SUPER_ADMIN)
            allowed = Set.of("ROLE_EDITOR");
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Insufficient privileges to update roles");
        }

        Set<Role> roles = new HashSet<>();
        for (String r : req.getRoles()) {
            if (!allowed.contains(r)) {
                throw new org.springframework.security.access.AccessDeniedException("Not allowed to assign role: " + r);
            }
            Role role = roleRepo.findByName(r).orElseThrow(() -> new RuntimeException("Role not found: " + r));
            roles.add(role);
        }
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("At least one valid role must be provided");
        }
        u.setRoles(roles);
        Users saved = usersRepo.save(u);
        return ResponseEntity.ok(toResponse(saved));
    }

    // Delete user: ADMIN+ (but only SUPER_ADMIN can delete ADMIN/SUPER_ADMIN targets)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Users target = usersRepo.findById(id).orElseThrow();
        guardAdminTarget(target, auth);
        usersRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
