package com.pujan.userservice.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class UpdateRolesRequest {
    private Set<String> roles;

}
