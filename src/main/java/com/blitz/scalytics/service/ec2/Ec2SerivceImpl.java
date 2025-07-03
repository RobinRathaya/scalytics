package com.blitz.scalytics.service.ec2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.blitz.scalytics.model.Ec2Result;
import com.blitz.scalytics.util.S3Util;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

@Service
@RequiredArgsConstructor
public class Ec2SerivceImpl implements Ec2Service {

    private final CloudWatchClient cloudWatchClient;

    @Value("${aws.ec2.instanceId}")
    private String defaultInstanceId;

    private final S3Util s3Util;

    @Override
    public List<Ec2Result> fetchCpuUtilization(Instant startTime, Instant endTime, int period) {

        if (startTime == null) {
            startTime = Instant.now();
            endTime = endTime.plusSeconds(300);
        }

        startTime = startTime.minusSeconds(300);

        Dimension dimension = Dimension.builder()
                .name("InstanceId")
                .value(defaultInstanceId)
                .build();

        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .namespace("AWS/EC2")
                .metricName("CPUUtilization")
                .dimensions(dimension)
                .startTime(startTime)
                .endTime(endTime.plusSeconds(60))
                .period(period)
                .statistics(Statistic.AVERAGE)
                .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

        List<Ec2Result> results = new ArrayList<>();
        for (Datapoint dp : response.datapoints()) {
            Ec2Result result = new Ec2Result();
            result.setMetricName("CPUUtilization");
            result.setValue(dp.average());
            result.setTimestamp(dp.timestamp().toString());
            results.add(result);
        }

        return results;
    }

    @Override
    public byte[] downloadChart() {
        return s3Util.downloadFile("ec2_chart.png");
    }

}
