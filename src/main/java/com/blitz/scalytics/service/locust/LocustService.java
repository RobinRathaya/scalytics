package com.blitz.scalytics.service.locust;

import java.util.List;

import com.blitz.scalytics.model.LocustResult;
import com.blitz.scalytics.model.ReportData;

public interface LocustService {

    List<LocustResult> runLocust(ReportData reportData) throws Exception;
    List<LocustResult> fetchLocustResult() throws Exception;

    byte[] downloadChart();
}
