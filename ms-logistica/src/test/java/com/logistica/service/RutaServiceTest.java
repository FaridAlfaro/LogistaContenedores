package com.logistica.service;

import com.logistica.client.OsrmClient2;
import com.logistica.client.OsrmDistanceResponse;
import com.logistica.dto.response.DistanciaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutaServiceTest {

    @Mock
    private OsrmClient2 osrmClient;

    @InjectMocks
    private RutaService rutaService;

    @Test
    void testObtenerRutasAlternativas() {
        // Arrange
        Double lat1 = 1.0, lon1 = 1.0, lat2 = 2.0, lon2 = 2.0;

        // Mock response 1
        OsrmDistanceResponse resp1 = new OsrmDistanceResponse();
        OsrmDistanceResponse.Route r1 = new OsrmDistanceResponse.Route();
        r1.setDistance(1000);
        r1.setDuration(60);
        resp1.setRoutes(List.of(r1));

        // Mock response 2
        OsrmDistanceResponse resp2 = new OsrmDistanceResponse();
        OsrmDistanceResponse.Route r2 = new OsrmDistanceResponse.Route();
        r2.setDistance(1200);
        r2.setDuration(70);
        resp2.setRoutes(List.of(r2));

        when(osrmClient.getAlternativeRoutes(lat1, lon1, lat2, lon2))
                .thenReturn(List.of(resp1, resp2));

        // Act
        List<DistanciaResponse> result = rutaService.obtenerRutasAlternativas(lat1, lon1, lat2, lon2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1.0, result.get(0).getDistanciaKm());
        assertEquals(60.0, result.get(0).getTiempoSegundos());

        assertEquals(1.2, result.get(1).getDistanciaKm());
        assertEquals(70.0, result.get(1).getTiempoSegundos());

        verify(osrmClient).getAlternativeRoutes(lat1, lon1, lat2, lon2);
    }
}
