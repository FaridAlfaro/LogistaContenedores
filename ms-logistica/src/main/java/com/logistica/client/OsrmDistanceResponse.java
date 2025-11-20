package com.logistica.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OsrmDistanceResponse {
    @JsonProperty("routes")
    private Route[] routes;

    @Data
    public static class Route {
        @JsonProperty("distance")
        private double distance; // en metros

        @JsonProperty("duration")
        private double duration; // en segundos
    }

    public double getDistanceKm() {
        return routes[0].distance / 1000.0;
    }

    public double getDurationSeconds() {
        return routes[0].duration;
    }
}