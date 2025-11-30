package com.pujan.userservice.Repository;

import com.pujan.userservice.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository< Users , Long > {

    Optional<Users> findByUsername(String username);
    Boolean existsByUsername(String username);

}
