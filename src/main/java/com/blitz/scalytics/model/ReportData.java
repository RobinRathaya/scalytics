package com.blitz.scalytics.model;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportData {
    private String reportId;
    private int inputUsers;
    private int inputSpawnRate;
    private int inputDuration;
    private Instant startTime;
    private Instant endTime;
    List<LocustResult> locustResults;
    List<Ec2Result> ec2Results;
    private String locustChartUrl;
    private String ec2ChartUrl;
    private String userVsCpuChartUrl;
}
