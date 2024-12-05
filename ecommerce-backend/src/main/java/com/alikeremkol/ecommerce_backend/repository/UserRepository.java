package com.alikeremkol.ecommerce_backend.repository;

import com.alikeremkol.ecommerce_backend.model.Role;
import com.alikeremkol.ecommerce_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);


    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRolesContains(@Param("role") Role role);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
