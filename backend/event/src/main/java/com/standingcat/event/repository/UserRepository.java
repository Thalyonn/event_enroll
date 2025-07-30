package com.standingcat.event.repository;

import com.standingcat.event.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); //optional for 0 or 1
    Optional<User> findByEmail(String email);
}
