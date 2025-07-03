package com.blitz.scalytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blitz.scalytics.model.LocustRequest;
import com.blitz.scalytics.model.ReportData;
import com.blitz.scalytics.service.ScalyticsService;

@RestController
@RequestMapping("/scalytics")
public class ScalyticsController {

    @Autowired
    private ScalyticsService scalyticsService;

    @PostMapping("/run-test")
    public ResponseEntity<String> runTest(@RequestBody LocustRequest request) {
        try {
            return ResponseEntity.ok(scalyticsService.runAndSaveTestResult(request.getUsers(), request.getSpawnRate(), request.getDuration()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    @GetMapping("/download-user-vs-cpu-chart")
    public ResponseEntity<byte[]> downloadUserVsCpuChart() {
        try {
            byte[] chartData = scalyticsService.downloadUserVsCpuChart();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=users_vs_cpu_chart.png")
                    .body(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fetch-report-data")
    public ResponseEntity<ReportData> fetchReportData(String reportId) {
        try {
            ReportData reportData = scalyticsService.fetchReportData(reportId);
            return ResponseEntity.ok(reportData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/generate-report")
    public ResponseEntity<byte[]> generateReport() {
        try {
            byte[] pdfBytes = scalyticsService.generateReport();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("load_test_report.pdf")
                    .build());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
