package com.unify.app.jobs;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.unify.app.security.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupProcessingJob {

    private final AuthenticationService authenticationService;

    @Scheduled(cron = "${unify.jobs.token-cleanup-job-cron}")
    public void processTokenCleanup() {
        log.info("Starting token cleanup job at {}", Instant.now());
        authenticationService.processTokenCleanup();
    }

}
