package com.blitz.scalytics.service.locust;

import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.blitz.scalytics.model.LocustResult;
import com.blitz.scalytics.model.ReportData;
import com.blitz.scalytics.util.S3Util;
import com.opencsv.CSVReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocustServiceImpl implements LocustService {

    @Value("${locust.path}")
    private String locustPath;

    @Value("${locust.csv.path}")
    private String locustCsvPath;

    @Value("${locust.active.profile}")
    private String locustActiveProfile;

    private final S3Util s3Util;

    @Override
    public List<LocustResult> runLocust(ReportData reportData) throws Exception {
        String locustCommand = String.format(
                "cd %s && locust -f locustfile.py --headless -u %d -r %d -t %d --csv=locust_report --csv-full-history",
                locustPath, reportData.getInputUsers(), reportData.getInputSpawnRate(), reportData.getInputDuration()
        );

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", locustCommand
        );
        builder.environment().put("active-profile", locustActiveProfile);
        builder.inheritIO();
        Process process = builder.start();

        int exitCode = process.waitFor();

        if (exitCode == 127) {
            throw new RuntimeException("Locust binary not found!");
        } else if (exitCode != 0) {
            System.err.println("WARNING: Locust exited with code " + exitCode);
        }

        return fetchLocustResult();
    }

    @Override
    public List<LocustResult> fetchLocustResult() throws Exception {
        List<LocustResult> locustResults = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(locustCsvPath))) {
            reader.readAll().stream()
                    .skip(1)
                    .filter(row -> row.length >= 23 && "Aggregated".equalsIgnoreCase(row[3].trim()))
                    .map(this::setLocustResult)
                    .forEach(locustResults::add);
        }

        return locustResults;
    }

    private LocustResult setLocustResult(String[] row) {
        return new LocustResult(
                Instant.ofEpochSecond(Long.parseLong(row[0].trim())),
                Integer.parseInt(row[1].trim()),
                Double.parseDouble(row[4].trim()),
                Double.parseDouble(row[20].trim()),
                Integer.parseInt(row[17].trim()),
                Integer.parseInt(row[18].trim()),
                Double.parseDouble(row[22].trim())
        );
    }

    @Override
    public byte[] downloadChart() {
        return s3Util.downloadFile("locust_chart.png");
    }

}