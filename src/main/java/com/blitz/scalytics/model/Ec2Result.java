package com.blitz.scalytics.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Ec2Result {
    private String metricName;
    private double value;
    private String timestamp;
}
