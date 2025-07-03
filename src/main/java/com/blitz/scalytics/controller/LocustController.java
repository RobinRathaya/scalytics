package com.blitz.scalytics.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blitz.scalytics.model.LocustResult;
import com.blitz.scalytics.service.locust.LocustService;

@RestController
@RequestMapping("/locust")
public class LocustController {

    private final LocustService locustService;

    @Autowired
    public LocustController(LocustService locustService) {
        this.locustService = locustService;
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<LocustResult>> fetchLocustData() {
        try {
           return ResponseEntity.ok(locustService.fetchLocustResult());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download-chart")
    public ResponseEntity<byte[]> downloadChart() {
        try {
            byte[] chartData = locustService.downloadChart();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=locust_chart.png")
                    .body(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
