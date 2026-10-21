package com.whenmo.repository.database;

import com.whenmo.model.database.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByName(String name);

    Optional<User> findByName(String name);

    List<User> findByIsAdmin(Boolean isAdmin);
}