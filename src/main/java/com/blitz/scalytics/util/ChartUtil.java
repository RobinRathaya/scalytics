package com.blitz.scalytics.util;

import java.awt.*;
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
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

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

        // Find max point
        double maxY = -1;
        double maxX = -1;
        int maxIndex = -1;
        for (int i = 0; i < yData.length; i++) {
            if (yData[i] > maxY) {
                maxY = yData[i];
                maxX = xData[i];
                maxIndex = i;
            }
        }

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Locust Load Test")
                .xAxisTitle("Users")
                .yAxisTitle("Total Requests")
                .build();

        // Plot main series
        XYSeries series = chart.addSeries("Total Requests", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);
        series.setLineColor(Color.BLUE);

        if (maxIndex >= 0) {
            // Max point series
            XYSeries maxSeries = chart.addSeries("Max Point", new double[]{maxX}, new double[]{maxY});
            maxSeries.setLineStyle(new BasicStroke(0f));
            maxSeries.setMarker(SeriesMarkers.DIAMOND);
            maxSeries.setMarkerColor(Color.RED);

            // Vertical guide line
            XYSeries vertical = chart.addSeries(
                    "V Guide",
                    new double[]{maxX, maxX},
                    new double[]{0, maxY}
            );
            vertical.setLineColor(Color.GRAY);
            vertical.setLineStyle(new BasicStroke(
                    1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5.0f, 5.0f}, 0
            ));
            vertical.setMarker(SeriesMarkers.NONE);

            // Horizontal guide line
            XYSeries horizontal = chart.addSeries(
                    "H Guide",
                    new double[]{0, maxX},
                    new double[]{maxY, maxY}
            );
            horizontal.setLineColor(Color.GRAY);
            horizontal.setLineStyle(new BasicStroke(
                    1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5.0f, 5.0f}, 0
            ));
            horizontal.setMarker(SeriesMarkers.NONE);
        }

        String filePath = path + "/locust_chart.png";
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        return filePath;
    }

    public static String generateEc2Chart(List<Ec2Result> ec2Results, String path) throws IOException {
        double[] xData = new double[ec2Results.size()];
        double[] yData = new double[ec2Results.size()];

        ec2Results.sort(Comparator.comparing(Ec2Result::getTimestamp));

        for (int i = 0; i < ec2Results.size(); i++) {
            Instant ts = Instant.parse(ec2Results.get(i).getTimestamp());
            xData[i] = ts.toEpochMilli();
            yData[i] = ec2Results.get(i).getValue();
        }

        // Find max CPU point
        double maxY = -1;
        double maxX = -1;
        int maxIndex = -1;
        for (int i = 0; i < yData.length; i++) {
            if (yData[i] > maxY) {
                maxY = yData[i];
                maxX = xData[i];
                maxIndex = i;
            }
        }

        XYChart chart = new XYChartBuilder()
                .width(1000)
                .height(600)
                .title("EC2 CPU Utilization Over Time")
                .xAxisTitle("Time")
                .yAxisTitle("CPU Utilization (%)")
                .build();

        XYSeries series = chart.addSeries("CPU Utilization", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);
        series.setLineColor(Color.BLUE);

        if (maxIndex >= 0) {
            // Max point series
            XYSeries maxSeries = chart.addSeries("Max CPU Point", new double[]{maxX}, new double[]{maxY});
            maxSeries.setLineStyle(new BasicStroke(0f));
            maxSeries.setMarker(SeriesMarkers.DIAMOND);
            maxSeries.setMarkerColor(Color.RED);

            // Vertical guide line
            XYSeries vertical = chart.addSeries(
                    "V Guide",
                    new double[]{maxX, maxX},
                    new double[]{0, maxY}
            );
            vertical.setLineColor(Color.GRAY);
            vertical.setLineStyle(new BasicStroke(
                    1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5.0f, 5.0f}, 0
            ));
            vertical.setMarker(SeriesMarkers.NONE);

            // Horizontal guide line
            XYSeries horizontal = chart.addSeries(
                    "H Guide",
                    new double[]{xData[0], maxX},
                    new double[]{maxY, maxY}
            );
            horizontal.setLineColor(Color.GRAY);
            horizontal.setLineStyle(new BasicStroke(
                    1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5.0f, 5.0f}, 0
            ));
            horizontal.setMarker(SeriesMarkers.NONE);
        }

        chart.getStyler().setXAxisLabelRotation(45);
        chart.setCustomXAxisTickLabelsFormatter(xValue -> {
            long millis = xValue.longValue();
            return Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        });

        String filePath = path + "/ec2_chart.png";
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

            avgUsersList.add(Math.round(avgUsers));
            cpuList.add(ec2.getValue());
        }

        double[] users = avgUsersList.stream().mapToDouble(Long::longValue).toArray();
        double[] cpu = cpuList.stream().mapToDouble(Double::doubleValue).toArray();

        // Find max CPU point
        double maxCpu = -1;
        double maxCpuUsers = -1;
        int maxIndex = -1;
        for (int i = 0; i < cpu.length; i++) {
            if (cpu[i] > maxCpu) {
                maxCpu = cpu[i];
                maxCpuUsers = users[i];
                maxIndex = i;
            }
        }

        // Create chart
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Average Users vs EC2 CPU Utilization")
                .xAxisTitle("Average Users")
                .yAxisTitle("CPU Utilization (%)")
                .build();

        // Set theme
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setMarkerSize(6);

        // Main data series
        XYSeries mainSeries = chart.addSeries("CPU Utilization", users, cpu);
        mainSeries.setLineColor(Color.BLUE);
        mainSeries.setMarker(SeriesMarkers.CIRCLE);
        mainSeries.setMarkerColor(Color.BLUE);

        // Highlight max point with red diamond
        if (maxIndex >= 0) {
            XYSeries maxSeries = chart.addSeries(
                    "Max CPU",
                    new double[]{maxCpuUsers},
                    new double[]{maxCpu}
            );
            maxSeries.setLineStyle(new BasicStroke(0f));
            maxSeries.setMarker(SeriesMarkers.DIAMOND);
            maxSeries.setMarkerColor(Color.RED);

            // Dotted vertical guide line
            XYSeries verticalLine = chart.addSeries(
                    "V Guide",
                    new double[]{maxCpuUsers, maxCpuUsers},
                    new double[]{0, maxCpu}
            );
            verticalLine.setLineColor(Color.GRAY);
            verticalLine.setLineStyle(new BasicStroke(
                    1.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    0,
                    new float[]{5.0f, 5.0f}, 0
            ));
            verticalLine.setMarker(SeriesMarkers.NONE);

            // Dotted horizontal guide line
            XYSeries horizontalLine = chart.addSeries(
                    "H Guide",
                    new double[]{0, maxCpuUsers},
                    new double[]{maxCpu, maxCpu}
            );
            horizontalLine.setLineColor(Color.GRAY);
            horizontalLine.setLineStyle(new BasicStroke(
                    1.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    0,
                    new float[]{5.0f, 5.0f}, 0
            ));
            horizontalLine.setMarker(SeriesMarkers.NONE);
        }

        // Save chart
        String filePath = path + "/users_vs_cpu_chart.png";
        BitmapEncoder.saveBitmap(chart, filePath, BitmapEncoder.BitmapFormat.PNG);
        return filePath;
    }
}
