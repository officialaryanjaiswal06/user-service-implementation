package com.pujan.userservice;

import com.pujan.userservice.Model.PermissionConstants;
import com.pujan.userservice.Model.Role;
import com.pujan.userservice.Repository.RoleRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}

	@Bean
	CommandLineRunner initRoles(RoleRepo roleRepo) {
		return args -> {
			String[] roleNames = {"ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER",
					"ACADEMIC_VIEWER", "ACADEMIC_EDITOR", "PROGRAMME_VIEWER", "PROGRAMME_EDITOR"};
			for (String roleName : roleNames) {
				roleRepo.findByName(roleName)
						.orElseGet(() -> roleRepo.save(Role.builder().name(roleName).build()));
			}
		};
	}

	@Bean
	CommandLineRunner initRolePermissions(RoleRepo roleRepo) {
		return args -> {
			// Assign permissions to roles using constants
			String[] roleNames = {"ACADEMIC_VIEWER", "ACADEMIC_EDITOR", "PROGRAMME_VIEWER",
					"PROGRAMME_EDITOR", "ROLE_SUPER_ADMIN", "ROLE_ADMIN"};

			for (String roleName : roleNames) {
				roleRepo.findByName(roleName).ifPresent(role -> {
					String[] permissions = PermissionConstants.getPermissionsForRole(roleName);
					role.getPermissions().clear();
					for (String perm : permissions) {
						role.getPermissions().add(perm);
					}
					roleRepo.save(role);
				});
			}
		};
	}


}


