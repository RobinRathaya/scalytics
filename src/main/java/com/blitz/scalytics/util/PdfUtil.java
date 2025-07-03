package com.blitz.scalytics.util;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.blitz.scalytics.model.Ec2Result;
import com.blitz.scalytics.model.LocustResult;
import com.blitz.scalytics.model.ReportData;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PdfUtil {

    private final TemplateEngine templateEngine;

    public byte[] generateLoadTestReport(ReportData reportData) throws Exception {

        Map<String, Object> model = new HashMap<>();

        model.put("summary", "This report summarizes the Scalytics load test performed on " +
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.ENGLISH)
                        .format(Instant.now().atZone(java.time.ZoneId.systemDefault())));

        model.put("userCount", reportData.getInputUsers());
        model.put("spawnRate", reportData.getInputSpawnRate());
        model.put("duration", reportData.getInputDuration());
        model.put("instanceType", "EC2");
        model.put("startTime", TimeFormatterUtil.formatInstant(reportData.getStartTime()));
        model.put("endTime", TimeFormatterUtil.formatInstant(reportData.getEndTime()));

        double peakCpu = getMaxCpuUsage(reportData.getEc2Results());
        int maxUsers = getMaxUsers(reportData.getLocustResults());
        double maxResponseTime = getMaxResponseTime(reportData.getLocustResults());
        double avgResponseTime = getAvgResponseTime(reportData.getLocustResults());
        int totalRequests = getTotalRequests(reportData.getLocustResults());
        int totalFailures = getTotalFailures(reportData.getLocustResults());

        model.put("peakCpu", String.format("%.2f", peakCpu));
        model.put("maxUsers", maxUsers);
        model.put("avgResponseTime", String.format("%.2f", avgResponseTime));
        model.put("totalRequests", totalRequests);
        model.put("totalFailures", totalFailures);

        model.put("combinedChartPath", reportData.getUserVsCpuChartUrl());
        model.put("ec2ChartPath", reportData.getEc2ChartUrl());
        model.put("locustChartPath", reportData.getLocustChartUrl());

        model.put("recommendations", generateRecommendations(maxUsers, peakCpu, maxResponseTime));
        model.put("conclusion", generateConclusion(maxUsers, peakCpu, avgResponseTime, totalFailures));

        return renderPdf(model);
    }

    private double getMaxResponseTime(List<LocustResult> locustResults) {
        return locustResults.stream()
                .mapToDouble(LocustResult::getMaxResponseTime)
                .max()
                .orElse(0.0);
    }

    private double getAvgResponseTime(List<LocustResult> locustResults) {
        return locustResults.stream()
                .mapToDouble(LocustResult::getAvgResponseTime)
                .average()
                .orElse(0.0);
    }

    private int getMaxUsers(List<LocustResult> locustResults) {
        return locustResults.stream()
                .mapToInt(LocustResult::getUsers)
                .max()
                .orElse(0);
    }

    private double getMaxCpuUsage(List<Ec2Result> ec2Results) {
        return ec2Results.stream().mapToDouble(Ec2Result::getValue)
                .max()
                .orElse(0.0);
    }


    private int getTotalRequests(List<LocustResult> locustResults) {
        return locustResults.stream()
                .mapToInt(LocustResult::getTotalRequests)
                .sum();
    }

    private int getTotalFailures(List<LocustResult> locustResults) {
        return locustResults.stream()
                .mapToInt(LocustResult::getTotalFailures)
                .sum();
    }

    private byte[] renderPdf(Map<String, Object> model) throws Exception {
        Context ctx = new Context(Locale.ENGLISH, model);
        String htmlContent = templateEngine.process("report", ctx);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(os);
        builder.run();

        return os.toByteArray();
    }

    private String generateRecommendations(int users, double peakCpu, double maxResponseTime) {
        StringBuilder sb = new StringBuilder();

        if (peakCpu > 80) {
            sb.append("Consider upgrading the EC2 instance type for higher workloads. ");
        } else {
            sb.append("Current EC2 instance appears adequate for tested load. ");
        }

        if (maxResponseTime > 400) {
            sb.append("Investigate application optimizations to reduce response time under peak load. ");
        }

        return sb.toString();
    }

    private String generateConclusion(int users, double peakCpu, double avgResponseTime, int totalFailures) {
        StringBuilder sb = new StringBuilder();
        sb.append("Under a load of ").append(users).append(" users, the system ");
        if (totalFailures > 0) {
            sb.append("experienced ").append(totalFailures).append(" errors, ");
        } else {
            sb.append("handled all requests successfully, ");
        }

        sb.append("with peak CPU usage reaching ").append(String.format("%.2f%%", peakCpu))
                .append(" and average response times around ")
                .append(String.format("%.2f ms", avgResponseTime)).append(".");

        if (peakCpu > 80) {
            sb.append(" The EC2 instance may be approaching capacity and scaling is advisable.");
        } else {
            sb.append(" The system performance is satisfactory under the tested load.");
        }
        return sb.toString();
    }
}

