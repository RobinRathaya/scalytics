package com.blitz.scalytics.model;

import java.time.Instant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocustResult {
    private Instant timestamp;
    private int users;
    private double requestsPerSec;
    private double avgResponseTime;
    private int totalRequests;
    private int totalFailures;
    private double maxResponseTime;

    public LocustResult(Instant timestamp, int users, double requestsPerSec, double avgResponseTime, int totalRequests, int totalFailures, double maxResponseTime) {
        this.timestamp = timestamp;
        this.users = users;
        this.requestsPerSec = requestsPerSec;
        this.avgResponseTime = avgResponseTime;
        this.totalRequests = totalRequests;
        this.totalFailures = totalFailures;
        this.maxResponseTime = maxResponseTime;
    }
}
