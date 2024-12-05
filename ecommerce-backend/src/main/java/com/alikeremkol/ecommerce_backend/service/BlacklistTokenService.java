package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.model.BlacklistToken;
import com.alikeremkol.ecommerce_backend.repository.BlacklistTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BlacklistTokenService {

    private final BlacklistTokenRepository blacklistTokenRepository;

    public BlacklistTokenService(BlacklistTokenRepository blacklistTokenRepository) {
        this.blacklistTokenRepository = blacklistTokenRepository;
    }

    public void addTokenToBlacklist(String token, Instant expirationTime) {
        BlacklistToken blacklistToken = new BlacklistToken();
        blacklistToken.setToken(token);
        blacklistToken.setExpirationTime(expirationTime);
        blacklistTokenRepository.save(blacklistToken);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistTokenRepository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredTokens() {
        blacklistTokenRepository.deleteExpiredTokens(Instant.now());
    }
}
