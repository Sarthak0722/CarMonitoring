package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.ApiResponseDto;
import com.smartcar.monitoring.dto.TelemetryDto;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.model.Telemetry;
import com.smartcar.monitoring.service.CarService;
import com.smartcar.monitoring.service.TelemetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Telemetry Controller Tests")
public class TelemetryControllerTest {

    @Mock
    private TelemetryService telemetryService;

    @Mock
    private CarService carService;

    @InjectMocks
    private TelemetryController telemetryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Car testCar;
    private Telemetry testTelemetry;
    private TelemetryDto testTelemetryDto;
    private List<Telemetry> testTelemetryList;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // This will register JSR310 module
        
        mockMvc = MockMvcBuilders.standaloneSetup(telemetryController).build();

        // Setup test car
        testCar = new Car();
        testCar.setId(1L);
        testCar.setStatus("MOVING");
        testCar.setSpeed(60);
        testCar.setFuelLevel(75);
        testCar.setTemperature(25);
        testCar.setLocation("New York");
        testCar.setIsActive(true);

        // Setup test telemetry
        testTelemetry = new Telemetry();
        testTelemetry.setId(1L);
        testTelemetry.setCar(testCar);
        testTelemetry.setSpeed(60);
        testTelemetry.setFuel(75);
        testTelemetry.setTemperature(25);
        testTelemetry.setLocation("New York");
        testTelemetry.setTimestamp(LocalDateTime.now());
        testTelemetry.setIsActive(true);

        // Setup test telemetry DTO
        testTelemetryDto = new TelemetryDto();
        testTelemetryDto.setId(1L);
        testTelemetryDto.setCarId(1L);
        testTelemetryDto.setSpeed(60);
        testTelemetryDto.setFuelLevel(75);
        testTelemetryDto.setTemperature(25);
        testTelemetryDto.setLocation("New York");
        testTelemetryDto.setTimestamp(LocalDateTime.now());

        testTelemetryList = Arrays.asList(testTelemetry);
    }

    @Nested
    @DisplayName("Create Telemetry Tests")
    class CreateTelemetryTests {
        
        @Test
        @DisplayName("Should create telemetry successfully")
        void shouldCreateTelemetrySuccessfully() throws Exception {
            when(carService.getCarById(1L)).thenReturn(testCar);
            when(telemetryService.createTelemetry(any(Telemetry.class))).thenReturn(testTelemetry);

            mockMvc.perform(post("/api/telemetry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testTelemetryDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry created successfully"))
                    .andExpect(jsonPath("$.data").exists());

            verify(carService).getCarById(1L);
            verify(telemetryService).createTelemetry(any(Telemetry.class));
        }
        
        @Test
        @DisplayName("Should handle car not found error")
        void shouldHandleCarNotFoundError() throws Exception {
            when(carService.getCarById(1L)).thenThrow(new RuntimeException("Car not found"));

            mockMvc.perform(post("/api/telemetry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testTelemetryDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to create telemetry: Car not found"));

            verify(carService).getCarById(1L);
            verify(telemetryService, never()).createTelemetry(any(Telemetry.class));
        }
        
        @Test
        @DisplayName("Should handle telemetry service error")
        void shouldHandleTelemetryServiceError() throws Exception {
            when(carService.getCarById(1L)).thenReturn(testCar);
            when(telemetryService.createTelemetry(any(Telemetry.class)))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(post("/api/telemetry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testTelemetryDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to create telemetry: Database error"));

            verify(carService).getCarById(1L);
            verify(telemetryService).createTelemetry(any(Telemetry.class));
        }
        
        @Test
        @DisplayName("Should handle invalid telemetry data")
        void shouldHandleInvalidTelemetryData() throws Exception {
            TelemetryDto invalidDto = new TelemetryDto();
            // Missing required fields

            mockMvc.perform(post("/api/telemetry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(carService, never()).getCarById(anyLong());
            verify(telemetryService, never()).createTelemetry(any(Telemetry.class));
        }
    }

    @Nested
    @DisplayName("Get All Telemetry Tests")
    class GetAllTelemetryTests {
        
        @Test
        @DisplayName("Should get all telemetry successfully")
        void shouldGetAllTelemetrySuccessfully() throws Exception {
            when(telemetryService.getAllActiveTelemetry()).thenReturn(testTelemetryList);

            mockMvc.perform(get("/api/telemetry"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(telemetryService).getAllActiveTelemetry();
        }
        
        @Test
        @DisplayName("Should handle empty telemetry list")
        void shouldHandleEmptyTelemetryList() throws Exception {
            when(telemetryService.getAllActiveTelemetry()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/telemetry"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));

            verify(telemetryService).getAllActiveTelemetry();
        }
        
        @Test
        @DisplayName("Should handle service error")
        void shouldHandleServiceError() throws Exception {
            when(telemetryService.getAllActiveTelemetry())
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/telemetry"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to retrieve telemetry: Service error"));

            verify(telemetryService).getAllActiveTelemetry();
        }
    }

    @Nested
    @DisplayName("Get Telemetry By ID Tests")
    class GetTelemetryByIdTests {
        
        @Test
        @DisplayName("Should get telemetry by ID successfully")
        void shouldGetTelemetryByIdSuccessfully() throws Exception {
            when(telemetryService.getTelemetryById(1L)).thenReturn(testTelemetry);

            mockMvc.perform(get("/api/telemetry/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry retrieved successfully"))
                    .andExpect(jsonPath("$.data").exists());

            verify(telemetryService).getTelemetryById(1L);
        }
        
        @Test
        @DisplayName("Should handle telemetry not found")
        void shouldHandleTelemetryNotFound() throws Exception {
            when(telemetryService.getTelemetryById(1L))
                    .thenThrow(new RuntimeException("Telemetry not found"));

            mockMvc.perform(get("/api/telemetry/1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Telemetry not found: Telemetry not found"));

            verify(telemetryService).getTelemetryById(1L);
        }
    }

    @Nested
    @DisplayName("Get Telemetry By Car Tests")
    class GetTelemetryByCarTests {
        
        @Test
        @DisplayName("Should get telemetry by car successfully")
        void shouldGetTelemetryByCarSuccessfully() throws Exception {
            when(telemetryService.getTelemetryByCar(1L)).thenReturn(testTelemetryList);

            mockMvc.perform(get("/api/telemetry/car/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(telemetryService).getTelemetryByCar(1L);
        }
        
        @Test
        @DisplayName("Should handle empty car telemetry")
        void shouldHandleEmptyCarTelemetry() throws Exception {
            when(telemetryService.getTelemetryByCar(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/telemetry/car/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));

            verify(telemetryService).getTelemetryByCar(1L);
        }
        
        @Test
        @DisplayName("Should handle service error for car telemetry")
        void shouldHandleServiceErrorForCarTelemetry() throws Exception {
            when(telemetryService.getTelemetryByCar(1L))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/telemetry/car/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to retrieve telemetry: Service error"));

            verify(telemetryService).getTelemetryByCar(1L);
        }
    }

    @Nested
    @DisplayName("Get Latest Telemetry Tests")
    class GetLatestTelemetryTests {
        
        @Test
        @DisplayName("Should get latest telemetry by car successfully")
        void shouldGetLatestTelemetryByCarSuccessfully() throws Exception {
            when(telemetryService.getLatestTelemetryByCar(1L)).thenReturn(testTelemetryList);

            mockMvc.perform(get("/api/telemetry/car/1/latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest telemetry retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(telemetryService).getLatestTelemetryByCar(1L);
        }
        
        @Test
        @DisplayName("Should get latest telemetry for all cars successfully")
        void shouldGetLatestTelemetryForAllCarsSuccessfully() throws Exception {
            when(telemetryService.getLatestTelemetryForAllCars()).thenReturn(testTelemetryList);

            mockMvc.perform(get("/api/telemetry/latest/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest telemetry for all cars retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(telemetryService).getLatestTelemetryForAllCars();
        }
        
        @Test
        @DisplayName("Should handle latest telemetry not found")
        void shouldHandleLatestTelemetryNotFound() throws Exception {
            when(telemetryService.getLatestTelemetryByCar(1L))
                    .thenThrow(new RuntimeException("Latest telemetry not found"));

            mockMvc.perform(get("/api/telemetry/car/1/latest"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Latest telemetry not found: Latest telemetry not found"));

            verify(telemetryService).getLatestTelemetryByCar(1L);
        }
    }

    @Nested
    @DisplayName("Get Telemetry By Time Range Tests")
    class GetTelemetryByTimeRangeTests {
        
        @Test
        @DisplayName("Should get telemetry by time range successfully")
        void shouldGetTelemetryByTimeRangeSuccessfully() throws Exception {
            LocalDateTime startTime = LocalDateTime.now().minusDays(1);
            LocalDateTime endTime = LocalDateTime.now();
            
            when(telemetryService.getTelemetryByCarAndTimestampRange(1L, startTime, endTime))
                    .thenReturn(testTelemetryList);

            mockMvc.perform(get("/api/telemetry/car/1/range")
                    .param("startTime", startTime.toString())
                    .param("endTime", endTime.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(telemetryService).getTelemetryByCarAndTimestampRange(1L, startTime, endTime);
        }
        
        @Test
        @DisplayName("Should handle time range service error")
        void shouldHandleTimeRangeServiceError() throws Exception {
            LocalDateTime startTime = LocalDateTime.now().minusDays(1);
            LocalDateTime endTime = LocalDateTime.now();
            
            when(telemetryService.getTelemetryByCarAndTimestampRange(1L, startTime, endTime))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/telemetry/car/1/range")
                    .param("startTime", startTime.toString())
                    .param("endTime", endTime.toString()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to retrieve telemetry: Service error"));

            verify(telemetryService).getTelemetryByCarAndTimestampRange(1L, startTime, endTime);
        }
    }

    @Nested
    @DisplayName("Get Telemetry Statistics Tests")
    class GetTelemetryStatisticsTests {
        
        @Test
        @DisplayName("Should get telemetry statistics successfully")
        void shouldGetTelemetryStatisticsSuccessfully() throws Exception {
            TelemetryService.TelemetryStatistics stats = new TelemetryService.TelemetryStatistics();
            stats.setAverageSpeed(60.0);
            stats.setMaxSpeed(100);
            stats.setMinSpeed(0);
            stats.setAverageFuel(75.0);
            stats.setAverageTemperature(25.0);
            
            when(telemetryService.getTelemetryStatistics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(stats);

            mockMvc.perform(get("/api/telemetry/stats/car/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry statistics retrieved successfully"))
                    .andExpect(jsonPath("$.data").exists());

            verify(telemetryService).getTelemetryStatistics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        }
        
        @Test
        @DisplayName("Should use default time range when not provided")
        void shouldUseDefaultTimeRangeWhenNotProvided() throws Exception {
            TelemetryService.TelemetryStatistics stats = new TelemetryService.TelemetryStatistics();
            when(telemetryService.getTelemetryStatistics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(stats);

            mockMvc.perform(get("/api/telemetry/stats/car/1"))
                    .andExpect(status().isOk());

            verify(telemetryService).getTelemetryStatistics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        }
        
        @Test
        @DisplayName("Should handle statistics service error")
        void shouldHandleStatisticsServiceError() throws Exception {
            when(telemetryService.getTelemetryStatistics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/telemetry/stats/car/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to retrieve telemetry statistics: Service error"));

            verify(telemetryService).getTelemetryStatistics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Delete Telemetry Tests")
    class DeleteTelemetryTests {
        
        @Test
        @DisplayName("Should deactivate telemetry successfully")
        void shouldDeactivateTelemetrySuccessfully() throws Exception {
            doNothing().when(telemetryService).deactivateTelemetry(1L);

            mockMvc.perform(delete("/api/telemetry/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry deactivated successfully"));

            verify(telemetryService).deactivateTelemetry(1L);
        }
        
        @Test
        @DisplayName("Should handle deactivation error")
        void shouldHandleDeactivationError() throws Exception {
            doThrow(new RuntimeException("Deactivation error")).when(telemetryService).deactivateTelemetry(1L);

            mockMvc.perform(delete("/api/telemetry/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to deactivate telemetry: Deactivation error"));

            verify(telemetryService).deactivateTelemetry(1L);
        }
    }

    @Nested
    @DisplayName("Reactivate Telemetry Tests")
    class ReactivateTelemetryTests {
        
        @Test
        @DisplayName("Should reactivate telemetry successfully")
        void shouldReactivateTelemetrySuccessfully() throws Exception {
            doNothing().when(telemetryService).reactivateTelemetry(1L);

            mockMvc.perform(put("/api/telemetry/1/reactivate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Telemetry reactivated successfully"));

            verify(telemetryService).reactivateTelemetry(1L);
        }
        
        @Test
        @DisplayName("Should handle reactivation error")
        void shouldHandleReactivationError() throws Exception {
            doThrow(new RuntimeException("Reactivation error")).when(telemetryService).reactivateTelemetry(1L);

            mockMvc.perform(put("/api/telemetry/1/reactivate"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to reactivate telemetry: Reactivation error"));

            verify(telemetryService).reactivateTelemetry(1L);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle null telemetry data")
        void shouldHandleNullTelemetryData() throws Exception {
            when(telemetryService.getAllActiveTelemetry()).thenReturn(null);

            mockMvc.perform(get("/api/telemetry"))
                    .andExpect(status().isInternalServerError());

            verify(telemetryService).getAllActiveTelemetry();
        }
        
        @Test
        @DisplayName("Should handle very large car ID")
        void shouldHandleVeryLargeCarId() throws Exception {
            Long largeCarId = Long.MAX_VALUE;
            when(telemetryService.getTelemetryByCar(largeCarId)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/telemetry/car/" + largeCarId))
                    .andExpect(status().isOk());

            verify(telemetryService).getTelemetryByCar(largeCarId);
        }
        
        @Test
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() throws Exception {
            mockMvc.perform(post("/api/telemetry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());

            verify(carService, never()).getCarById(anyLong());
            verify(telemetryService, never()).createTelemetry(any(Telemetry.class));
        }
    }
}