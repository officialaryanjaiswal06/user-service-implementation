package com.pujan.userservice.Controller;

import com.pujan.userservice.Model.PermissionConstants;
import com.pujan.userservice.Model.Users;
import com.pujan.userservice.Model.Role;
import com.pujan.userservice.Repository.UsersRepo;
import com.pujan.userservice.Repository.RoleRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/users")
public class AdminController {

    private final UsersRepo usersRepo;
    private final RoleRepo roleRepo;

    @PreAuthorize("hasAuthority('IAM:MANAGE_USER_PERMISSIONS')")
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<String>> getUserPermissions(@PathVariable Long id) {
        Users user = usersRepo.findById(id).orElseThrow();

        // union of all permission codes from all roles
        List<String> codes = user.getRoles().stream()
                .flatMap((Role r) -> r.getPermissions().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(codes);
    }

    @PreAuthorize("hasAuthority('IAM:MANAGE_USER_PERMISSIONS')")
    @PutMapping("/{id}/permissions")
    public ResponseEntity<Void> setUserPermissions(@PathVariable Long id,
                                                   @RequestBody List<String> permissionCodes) {
        Users user = usersRepo.findById(id).orElseThrow();

        Set<Role> newRoles = new HashSet<>();


        if (permissionCodes.contains("PROGRAM:ACADEMIC:READ") &&
                !permissionCodes.contains("PROGRAM:ACADEMIC:UPDATE")) {
            roleRepo.findByName("ACADEMIC_VIEWER").ifPresent(newRoles::add);
        }
        if (permissionCodes.contains("PROGRAM:ACADEMIC:UPDATE")) {
            roleRepo.findByName("ACADEMIC_EDITOR").ifPresent(newRoles::add);
        }
        if (permissionCodes.contains("PROGRAM:PROGRAMME:READ") &&
                !permissionCodes.contains("PROGRAM:PROGRAMME:UPDATE")) {
            roleRepo.findByName("PROGRAMME_VIEWER").ifPresent(newRoles::add);
        }
        if (permissionCodes.contains("PROGRAM:PROGRAMME:UPDATE")) {
            roleRepo.findByName("PROGRAMME_EDITOR").ifPresent(newRoles::add);
        }

        Set<String> identityRoles = Set.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER");

        user.getRoles().stream()
                .filter(r -> identityRoles.contains(r.getName()))
                .forEach(newRoles::add);

        // Replace roles
        user.setRoles(newRoles);
        usersRepo.save(user);

        return ResponseEntity.noContent().build();
    }

    // Permission Management Endpoints - Simplified (permissions are now constants)

    @PreAuthorize("hasAuthority('IAM:MANAGE_USER_PERMISSIONS')")
    @GetMapping("/permissions")
    public ResponseEntity<String[]> getAllPermissions() {
        return ResponseEntity.ok(PermissionConstants.getAllPermissions());
    }

    @PreAuthorize("hasAuthority('IAM:MANAGE_USER_PERMISSIONS')")
    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Set<String>> getRolePermissions(@PathVariable Long roleId) {
        Role role = roleRepo.findById(roleId).orElseThrow();
        return ResponseEntity.ok(role.getPermissions());
    }

    @PreAuthorize("hasAuthority('IAM:MANAGE_USER_PERMISSIONS')")
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Void> updateRolePermissions(@PathVariable Long roleId,
                                                      @RequestBody List<String> permissionCodes) {
        Role role = roleRepo.findById(roleId).orElseThrow();

        // Validate that all permission codes are valid constants
        String[] validPermissions = PermissionConstants.getAllPermissions();
        Set<String> validPermsSet = Set.of(validPermissions);

        for (String code : permissionCodes) {
            if (!validPermsSet.contains(code)) {
                return ResponseEntity.badRequest().build(); // Invalid permission code
            }
        }

        role.setPermissions(new HashSet<>(permissionCodes));
        roleRepo.save(role);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('IAM:MANAGE_USER_PERMISSIONS')")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleRepo.findAll();
        return ResponseEntity.ok(roles);
    }
}