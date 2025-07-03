package com.blitz.scalytics.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocustRequest {
    private int users;
    private int spawnRate;
    private int duration;
}
