package com.logistica.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OsrmClient2Test {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private OsrmClient2 osrmClient;

    @BeforeEach
    void setUp() {
        osrmClient = new OsrmClient2("http://localhost:5000");
        ReflectionTestUtils.setField(osrmClient, "restClient", restClient);
    }

    @Test
    void testGetAlternativeRoutes() {
        // Mocking the chain
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Prepare response
        OsrmDistanceResponse mockResponse = new OsrmDistanceResponse();
        OsrmDistanceResponse.Route route1 = new OsrmDistanceResponse.Route();
        route1.setDistance(1000); // 1000 meters
        route1.setDuration(60); // 60 seconds
        OsrmDistanceResponse.Route route2 = new OsrmDistanceResponse.Route();
        route2.setDistance(1200); // 1200 meters
        route2.setDuration(70); // 70 seconds
        mockResponse.setRoutes(List.of(route1, route2));

        when(responseSpec.body(OsrmDistanceResponse.class)).thenReturn(mockResponse);

        // Execute
        List<OsrmDistanceResponse> results = osrmClient.getAlternativeRoutes(1.0, 1.0, 2.0, 2.0);

        // Verify
        assertNotNull(results);
        assertEquals(2, results.size());

        // Verify first route
        assertEquals(1.0, results.get(0).getDistanceKm(), 0.001);
        assertEquals(60.0, results.get(0).getDurationSeconds(), 0.001);

        // Verify second route
        assertEquals(1.2, results.get(1).getDistanceKm(), 0.001);
        assertEquals(70.0, results.get(1).getDurationSeconds(), 0.001);
    }

    @Test
    void testCalcularDistancia() {
        // Mocking the chain
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Prepare response
        OsrmDistanceResponse mockResponse = new OsrmDistanceResponse();
        OsrmDistanceResponse.Route route1 = new OsrmDistanceResponse.Route();
        route1.setDistance(5000); // 5 km
        route1.setDuration(300); // 5 min
        mockResponse.setRoutes(List.of(route1));

        when(responseSpec.body(OsrmDistanceResponse.class)).thenReturn(mockResponse);

        // Execute
        OsrmDistanceResponse result = osrmClient.calcularDistancia(1.0, 1.0, 2.0, 2.0);

        // Verify
        assertNotNull(result);
        assertEquals(5.0, result.getDistanceKm(), 0.001);
        assertEquals(300.0, result.getDurationSeconds(), 0.001);
    }
}
