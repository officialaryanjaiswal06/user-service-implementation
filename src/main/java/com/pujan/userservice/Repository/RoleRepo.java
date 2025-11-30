package com.pujan.userservice.Repository;

import com.pujan.userservice.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository< Role,Long> {

    Optional<Role> findByName(String name);

    @Override
    Optional<Role> findById(Long id);
}
