package com.blitz.scalytics.service.ec2;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.blitz.scalytics.model.Ec2Result;

public interface Ec2Service {

    List<Ec2Result> fetchCpuUtilization(Instant startTime, Instant endTime, int period) throws IOException;

    byte[] downloadChart();
}
