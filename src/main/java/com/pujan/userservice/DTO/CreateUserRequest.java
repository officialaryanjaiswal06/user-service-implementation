package com.pujan.userservice.DTO;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Data
@Getter
@Setter
@ToString
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private Set<String> roles; // optional; default to ROLE_USER

}
