package com.alikeremkol.ecommerce_backend.repository;

import com.alikeremkol.ecommerce_backend.model.BlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, Long> {
    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM BlacklistToken b WHERE b.expirationTime < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") Instant currentTime);
}