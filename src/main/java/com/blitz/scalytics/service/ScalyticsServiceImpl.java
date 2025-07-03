package com.blitz.scalytics.service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.blitz.scalytics.model.Ec2Result;
import com.blitz.scalytics.model.LocustResult;
import com.blitz.scalytics.model.ReportData;
import com.blitz.scalytics.service.ec2.Ec2Service;
import com.blitz.scalytics.service.locust.LocustService;
import com.blitz.scalytics.util.ChartUtil;
import com.blitz.scalytics.util.DynamoDbUtil;
import com.blitz.scalytics.util.PdfUtil;
import com.blitz.scalytics.util.S3Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScalyticsServiceImpl implements ScalyticsService {

    private static final String LOCUST_CHART_FILE = "locust_chart.png";
    private static final String EC2_CHART_FILE = "ec2_chart.png";
    private static final String USERS_VS_CPU_CHART_FILE = "users_vs_cpu_chart.png";
    private static final int EC2_DATA_WAIT_MILLIS = 180_000;


    @Value("${report.base.path}")
    private String reportPath;

    @Value("${aws.s3.reports.folder}")
    private String reportsFolder;

    private final LocustService locustService;
    private final Ec2Service ec2Service;
    private final S3Util s3Util;
    private final DynamoDbUtil dynamoDbUtil;
    private final PdfUtil pdfUtil;

    @Override
    public String runAndSaveTestResult(int users, int spawnRate, int duration) throws Exception {
        ReportData reportData = new ReportData();
        reportData.setInputUsers(users);
        reportData.setInputSpawnRate(spawnRate);
        reportData.setInputDuration(duration);
        Instant startTime = Instant.now();
        reportData.setStartTime(startTime);
        List<LocustResult> locustResultList = locustService.runLocust(reportData);
        reportData.setLocustResults(locustResultList);
        Instant endTime = startTime.plusSeconds(duration);
        reportData.setEndTime(endTime);
        Thread.sleep(EC2_DATA_WAIT_MILLIS); // Wait for EC2 CPU utilization data to be updated
        List<Ec2Result> ec2ResultList = ec2Service.fetchCpuUtilization(startTime.minusSeconds(300), endTime, 60);
        reportData.setEc2Results(ec2ResultList);
        // Charts generation and saving to S3
        executeS3Upload(locustResultList, ec2ResultList, reportData);
        // Save report data to dynamodb
        dynamoDbUtil.saveReportData(reportData);
        return "Load Test completed successfully";
    }

    private void executeS3Upload(List<LocustResult> locustResultList, List<Ec2Result> ec2ResultList, ReportData reportData) {
        s3Util.deleteFolder();
        try {
            reportData.setLocustChartUrl(uploadLocustChart(locustResultList));
            reportData.setEc2ChartUrl(uploadEc2Chart(ec2ResultList));
            reportData.setUserVsCpuChartUrl(uploadUsersVsCpuChart(locustResultList, ec2ResultList));
        } catch (IOException e) {
            System.err.println("Error generating chart: " + e.getMessage());
        }
    }

    private String uploadLocustChart(List<LocustResult> locustResultList) throws IOException {
        String filePath = ChartUtil.generateLocustChart(locustResultList, reportPath);
        String s3Key = reportsFolder + LOCUST_CHART_FILE;
        return s3Util.uploadFile(s3Key, new File(filePath));
    }

    private String uploadEc2Chart(List<Ec2Result> ec2ResultList) throws IOException {
        String filePath = ChartUtil.generateEc2Chart(ec2ResultList, reportPath);
        String s3Key = reportsFolder + EC2_CHART_FILE;
        return  s3Util.uploadFile(s3Key, new File(filePath));
    }

    private String uploadUsersVsCpuChart(List<LocustResult> locustResultList, List<Ec2Result> ec2ResultList) throws IOException {
        String filePath = ChartUtil.generateUsersVsCpuChart(locustResultList, ec2ResultList, reportPath);
        String s3Key = reportsFolder + USERS_VS_CPU_CHART_FILE;
        return s3Util.uploadFile(s3Key, new File(filePath));
    }

    @Override
    public byte[] downloadUserVsCpuChart() throws IOException {
        return s3Util.downloadFile(reportsFolder + USERS_VS_CPU_CHART_FILE);
    }

    @Override
    public ReportData fetchReportData(String reportId) {
        return dynamoDbUtil.fetchReportData();
    }

    @Override
    public byte[] generateReport() throws Exception {
        ReportData reportData = dynamoDbUtil.fetchReportData();
        if (reportData != null) {
            return pdfUtil.generateLoadTestReport(reportData);
        }
        return new byte[0];
    }
}
