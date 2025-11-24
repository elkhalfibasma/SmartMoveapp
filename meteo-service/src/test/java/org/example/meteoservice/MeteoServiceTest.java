package org.example.meteoservice;

import org.example.meteoservice.entity.Meteo;
import org.example.meteoservice.model.MeteoRequest;
import org.example.meteoservice.repository.MeteoRepository;
import org.example.meteoservice.service.MeteoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeteoServiceTest {

    @Mock
    private MeteoRepository meteoRepository;

    @InjectMocks
    private MeteoService meteoService;

    @Test
    public void testMettreAJourMeteo() {
        // Given
        MeteoRequest request = new MeteoRequest(25.5, 0.0, 15.0, "Sunny");
        Meteo savedMeteo = new Meteo();
        savedMeteo.setId(1L);
        savedMeteo.setTemperature(25.5);
        savedMeteo.setPrecipitation(0.0);
        savedMeteo.setVent(15.0);
        savedMeteo.setCondition("Sunny");

        when(meteoRepository.save(any(Meteo.class))).thenReturn(savedMeteo);

        // When
        var response = meteoService.mettreAJourMeteo(request);

        // Then
        assertNotNull(response);
        assertEquals(25.5, response.getTemperature());
        assertEquals("Sunny", response.getCondition());
        verify(meteoRepository, times(1)).save(any(Meteo.class));
    }

    @Test
    public void testGetCondition() {
        // Given
        Meteo meteo = new Meteo();
        meteo.setId(1L);
        meteo.setCondition("Cloudy");
        meteo.setTemperature(20.0);

        when(meteoRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(meteo));

        // When
        var response = meteoService.getCondition();

        // Then
        assertNotNull(response);
        assertEquals("Cloudy", response.getCondition());
        assertEquals(20.0, response.getTemperature());
        verify(meteoRepository, times(1)).findTopByOrderByCreatedAtDesc();
    }

    @Test
    public void testGetConditionNotFound() {
        // Given
        when(meteoRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

        // When
        var response = meteoService.getCondition();

        // Then
        assertNull(response);
        verify(meteoRepository, times(1)).findTopByOrderByCreatedAtDesc();
    }
}
