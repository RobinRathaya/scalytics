package com.blitz.scalytics.util;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import com.blitz.scalytics.model.Ec2Result;
import com.blitz.scalytics.model.LocustResult;

public class ChartUtil {

    public static String generateLocustChart(List<LocustResult> locustResults, String path) throws IOException {
        double[] xData = new double[locustResults.size()];
        double[] yData = new double[locustResults.size()];

        locustResults.sort(Comparator.comparing(LocustResult::getUsers));

        for (int i = 0; i < locustResults.size(); i++) {
            xData[i] = locustResults.get(i).getUsers();
            yData[i] = locustResults.get(i).getTotalRequests();
        }

        XYChart chart = QuickChart.getChart(
                "Locust Load Test",
                "Users",
                "Total Requests",
                "total_requests",
                xData,
                yData
        );
        String filePath = path + "\\locust_chart.png";
        BitmapEncoder.saveBitmap(chart, filePath, BitmapFormat.PNG);
        return filePath;
    }

    public static String generateEc2Chart(List<Ec2Result> ec2Results, String path) throws IOException {
        double[] xData = new double[ec2Results.size()];
        double[] yData = new double[ec2Results.size()];
        ec2Results.sort(Comparator.comparing(Ec2Result::getTimestamp));

        for (int i = 0; i < ec2Results.size(); i++) {
            Instant ts = Instant.parse(ec2Results.get(i).getTimestamp());
            xData[i] = ts.toEpochMilli();  // use milliseconds for better spacing
            yData[i] = ec2Results.get(i).getValue();
        }

        XYChart chart = new XYChartBuilder()
                .width(1000)
                .height(600)
                .title("EC2 CPU Utilization Over Time")
                .xAxisTitle("Time")
                .yAxisTitle("CPU Utilization (%)")
                .build();

        chart.addSeries("CPU Utilization", xData, yData);

        chart.getStyler().setXAxisLabelRotation(45);
        chart.setCustomXAxisTickLabelsFormatter(xValue -> {
            long millis = xValue.longValue();
            return Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        });
        String filePath = path + "\\ec2_chart.png";
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        return filePath;
    }

    public static String generateUsersVsCpuChart(List<LocustResult> locustList, List<Ec2Result> ec2List, String path) throws IOException {
        List<Long> avgUsersList = new ArrayList<>();
        List<Double> cpuList = new ArrayList<>();
        ec2List.sort(Comparator.comparing(Ec2Result::getTimestamp));
        locustList.sort(Comparator.comparing(LocustResult::getTimestamp));
        for (Ec2Result ec2 : ec2List) {
            Instant ec2Timestamp = Instant.parse(ec2.getTimestamp());

            Instant windowStart = ec2Timestamp.minus(Duration.ofMinutes(5));
            Instant windowEnd = ec2Timestamp.plus(Duration.ofMinutes(1));
            List<LocustResult> locustInWindow = locustList.stream()
                    .filter(lr -> {
                        Instant ts = lr.getTimestamp().truncatedTo(ChronoUnit.SECONDS);
                        return !ts.isBefore(windowStart) && !ts.isAfter(windowEnd);
                    })
                    .toList();

            double avgUsers = locustInWindow.stream()
                    .mapToInt(LocustResult::getUsers)
                    .average()
                    .orElse(0.0);

            System.out.printf("EC2 @ %s → Avg Users: %.2f → CPU: %.2f%% %n",
                    ec2.getTimestamp(), avgUsers, ec2.getValue());

            avgUsersList.add(Math.round(avgUsers));
            cpuList.add(ec2.getValue() * 100.0); // Convert to percentage
        }

        double[] users = avgUsersList.stream().mapToDouble(Long::longValue).toArray();
        double[] cpu = cpuList.stream().mapToDouble(Double::doubleValue).toArray();

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Average Users vs EC2 CPU Utilization")
                .xAxisTitle("Average Users")
                .yAxisTitle("CPU Utilization (%)")
                .build();

        chart.addSeries("CPU Utilization", users, cpu);
        String filePath = path + "\\users_vs_cpu_chart.png";
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        return filePath;
    }
}
