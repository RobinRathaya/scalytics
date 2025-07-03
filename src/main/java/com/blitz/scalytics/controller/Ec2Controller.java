package com.blitz.scalytics.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blitz.scalytics.model.Ec2Result;
import com.blitz.scalytics.service.ec2.Ec2Service;

@RestController
@RequestMapping("/ec2")
public class Ec2Controller {

    private final Ec2Service ec2Service;

    @Autowired
    public Ec2Controller(Ec2Service ec2Service) {
        this.ec2Service = ec2Service;
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<Ec2Result>> fetchCpuUtilization() {
        try {
            return ResponseEntity.ok(ec2Service.fetchCpuUtilization(null,null, 60));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("download-chart")
    public ResponseEntity<byte[]> downloadChart() {
        try {
            byte[] chartData = ec2Service.downloadChart();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=ec2_chart.png")
                    .body(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
