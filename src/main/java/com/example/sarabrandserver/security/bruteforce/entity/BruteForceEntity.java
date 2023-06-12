package com.example.sarabrandserver.security.bruteforce.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("Student") @Getter @Setter
public class BruteForceEntity implements Serializable {
    private int failedAttempt;
    private String principal; // Represents username and email

    public BruteForceEntity(int failedAttempt, String principal) {
        this.failedAttempt = failedAttempt;
        this.principal = principal;
    }
}
