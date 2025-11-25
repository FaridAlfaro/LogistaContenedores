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
        if (routes == null || routes.length == 0) {
            return 0.0; // O lanzar una excepción personalizada controlada
        }
        return routes[0].distance / 1000.0;
    }

    public double getDurationSeconds() {
        if (routes == null || routes.length == 0) {
            return 0.0;
        }
        return routes[0].duration;
    }

}