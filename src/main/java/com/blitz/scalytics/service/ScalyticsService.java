package com.blitz.scalytics.service;

import java.io.IOException;

import com.blitz.scalytics.model.ReportData;

public interface ScalyticsService {

    String runAndSaveTestResult(int users, int spawnRate, int duration) throws Exception;

    byte[] downloadUserVsCpuChart() throws IOException;

    ReportData fetchReportData(String reportId);

    byte[] generateReport() throws Exception;
}
