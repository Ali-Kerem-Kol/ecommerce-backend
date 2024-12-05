package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.repository.BlacklistTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenCleanupService {

    @Autowired
    private BlacklistTokenRepository blacklistTokenRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanUpExpiredTokens() {
        blacklistTokenRepository.deleteExpiredTokens(Instant.now());
    }

}

