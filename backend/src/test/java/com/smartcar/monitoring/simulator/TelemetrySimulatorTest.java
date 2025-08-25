package com.smartcar.monitoring.simulator;

import com.smartcar.monitoring.dto.TelemetryDto;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.service.CarService;
import com.smartcar.monitoring.service.MqttService;
import com.smartcar.monitoring.service.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Telemetry Simulator Tests")
class TelemetrySimulatorTest {

    @Mock
    private MqttService mqttService;

    @Mock
    private CarService carService;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private TelemetrySimulator telemetrySimulator;

    private Car testCar1;
    private Car testCar2;
    private List<Car> testCars;

    @BeforeEach
    void setUp() {
        testCar1 = new Car();
        testCar1.setId(1L);
        testCar1.setStatus("MOVING");
        testCar1.setSpeed(60);
        testCar1.setFuelLevel(75);
        testCar1.setTemperature(25);
        testCar1.setLocation("New York");
        testCar1.setIsActive(true);

        testCar2 = new Car();
        testCar2.setId(2L);
        testCar2.setStatus("IDLE");
        testCar2.setSpeed(0);
        testCar2.setFuelLevel(90);
        testCar2.setTemperature(22);
        testCar2.setLocation("Los Angeles");
        testCar2.setIsActive(true);

        testCars = Arrays.asList(testCar1, testCar2);
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {
        
        @Test
        @DisplayName("Should initialize with default values")
        void shouldInitializeWithDefaultValues() {
            assertFalse(telemetrySimulator.isRunning());
            assertNotNull(telemetrySimulator);
        }
        
        @Test
        @DisplayName("Should set simulator enabled from properties")
        void shouldSetSimulatorEnabledFromProperties() {
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", true);
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorInterval", 5000L);
            ReflectionTestUtils.setField(telemetrySimulator, "carCount", 5);
            
            assertEquals(5000L, ReflectionTestUtils.getField(telemetrySimulator, "simulatorInterval"));
            assertEquals(5, ReflectionTestUtils.getField(telemetrySimulator, "carCount"));
        }
    }

    @Nested
    @DisplayName("Simulation Control Tests")
    class SimulationControlTests {
        
        @Test
        @DisplayName("Should start simulation correctly")
        void shouldStartSimulationCorrectly() {
            assertFalse(telemetrySimulator.isRunning());
            
            telemetrySimulator.startSimulation();
            
            assertTrue(telemetrySimulator.isRunning());
            verify(webSocketService, times(1)).broadcastSimulatorStatus(true);
        }
        
        @Test
        @DisplayName("Should stop simulation correctly")
        void shouldStopSimulationCorrectly() {
            telemetrySimulator.startSimulation();
            assertTrue(telemetrySimulator.isRunning());
            
            telemetrySimulator.stopSimulation();
            
            assertFalse(telemetrySimulator.isRunning());
            verify(webSocketService, times(1)).broadcastSimulatorStatus(false);
        }
        
        @Test
        @DisplayName("Should toggle simulation correctly")
        void shouldToggleSimulationCorrectly() {
            // Start from stopped state
            assertFalse(telemetrySimulator.isRunning());
            
            // Toggle to start
            telemetrySimulator.startSimulation();
            assertTrue(telemetrySimulator.isRunning());
            
            // Toggle to stop
            telemetrySimulator.stopSimulation();
            assertFalse(telemetrySimulator.isRunning());
        }
        
        @Test
        @DisplayName("Should not start simulation if already running")
        void shouldNotStartSimulationIfAlreadyRunning() {
            telemetrySimulator.startSimulation();
            assertTrue(telemetrySimulator.isRunning());
            
            // Try to start again
            telemetrySimulator.startSimulation();
            assertTrue(telemetrySimulator.isRunning());
            
            // Should only broadcast once
            verify(webSocketService, times(1)).broadcastSimulatorStatus(true);
        }
        
        @Test
        @DisplayName("Should not stop simulation if already stopped")
        void shouldNotStopSimulationIfAlreadyStopped() {
            assertFalse(telemetrySimulator.isRunning());
            
            // Try to stop
            telemetrySimulator.stopSimulation();
            assertFalse(telemetrySimulator.isRunning());
            
            // Should not broadcast
            verify(webSocketService, never()).broadcastSimulatorStatus(false);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should set simulator enabled correctly")
        void shouldSetSimulatorEnabledCorrectly() {
            telemetrySimulator.setSimulatorEnabled(true);
            assertTrue(telemetrySimulator.isRunning());
            
            telemetrySimulator.setSimulatorEnabled(false);
            assertFalse(telemetrySimulator.isRunning());
        }
        
        @Test
        @DisplayName("Should set simulator interval correctly")
        void shouldSetSimulatorIntervalCorrectly() {
            long newInterval = 10000L;
            telemetrySimulator.setSimulatorInterval(newInterval);
            
            assertEquals(newInterval, ReflectionTestUtils.getField(telemetrySimulator, "simulatorInterval"));
        }
    }

    @Nested
    @DisplayName("Data Generation Tests")
    class DataGenerationTests {
        
        @Test
        @DisplayName("Should generate telemetry data with valid ranges")
        void shouldGenerateTelemetryDataWithValidRanges() {
            TelemetryDto telemetryDto = telemetrySimulator.generateTelemetryData(testCar1);
            
            assertNotNull(telemetryDto);
            assertNotNull(telemetryDto.getTimestamp());
            assertTrue(telemetryDto.getSpeed() >= 0 && telemetryDto.getSpeed() <= 140);
            assertTrue(telemetryDto.getFuelLevel() >= 5 && telemetryDto.getFuelLevel() <= 100);
            assertTrue(telemetryDto.getTemperature() >= -10 && telemetryDto.getTemperature() <= 70);
            assertNotNull(telemetryDto.getLocation());
        }
        
        @Test
        @DisplayName("Should generate realistic speed distribution")
        void shouldGenerateRealisticSpeedDistribution() {
            int[] speeds = new int[100];
            
            for (int i = 0; i < 100; i++) {
                TelemetryDto telemetryDto = telemetrySimulator.generateTelemetryData(testCar1);
                speeds[i] = telemetryDto.getSpeed();
            }
            
            // Check that we have a reasonable distribution
            long normalSpeedCount = Arrays.stream(speeds).filter(s -> s >= 0 && s <= 80).count();
            long highwaySpeedCount = Arrays.stream(speeds).filter(s -> s > 80 && s <= 120).count();
            long highSpeedCount = Arrays.stream(speeds).filter(s -> s > 120 && s <= 140).count();
            
            // Should have mostly normal speeds (70% probability)
            assertTrue(normalSpeedCount > 50, "Should have mostly normal speeds");
            // Should have some highway speeds (25% probability)
            assertTrue(highwaySpeedCount > 10, "Should have some highway speeds");
            // Should have few high speeds (5% probability)
            assertTrue(highSpeedCount > 0, "Should have some high speeds");
        }
        
        @Test
        @DisplayName("Should generate realistic fuel level distribution")
        void shouldGenerateRealisticFuelLevelDistribution() {
            int[] fuelLevels = new int[100];
            
            for (int i = 0; i < 100; i++) {
                TelemetryDto telemetryDto = telemetrySimulator.generateTelemetryData(testCar1);
                fuelLevels[i] = telemetryDto.getFuelLevel();
            }
            
            // Check that we have a reasonable distribution
            long normalFuelCount = Arrays.stream(fuelLevels).filter(f -> f >= 20 && f <= 100).count();
            long lowFuelCount = Arrays.stream(fuelLevels).filter(f -> f >= 10 && f <= 30).count();
            long veryLowFuelCount = Arrays.stream(fuelLevels).filter(f -> f >= 5 && f <= 15).count();
            
            // Should have mostly normal fuel levels (60% probability)
            assertTrue(normalFuelCount > 40, "Should have mostly normal fuel levels");
            // Should have some low fuel levels (30% probability)
            assertTrue(lowFuelCount > 15, "Should have some low fuel levels");
            // Should have few very low fuel levels (10% probability)
            assertTrue(veryLowFuelCount > 0, "Should have some very low fuel levels");
        }
        
        @Test
        @DisplayName("Should generate realistic temperature distribution")
        void shouldGenerateRealisticTemperatureDistribution() {
            int[] temperatures = new int[100];
            
            for (int i = 0; i < 100; i++) {
                TelemetryDto telemetryDto = telemetrySimulator.generateTelemetryData(testCar1);
                temperatures[i] = telemetryDto.getTemperature();
            }
            
            // Check that we have a reasonable distribution
            long normalTempCount = Arrays.stream(temperatures).filter(t -> t >= 10 && t <= 40).count();
            long extremeTempCount = Arrays.stream(temperatures).filter(t -> t > 40 && t <= 70).count();
            long coldTempCount = Arrays.stream(temperatures).filter(t -> t >= -10 && t < 10).count();
            
            // Should have mostly normal temperatures (80% probability)
            assertTrue(normalTempCount > 60, "Should have mostly normal temperatures");
            // Should have some extreme temperatures (15% probability)
            assertTrue(extremeTempCount > 5, "Should have some extreme temperatures");
            // Should have few cold temperatures (5% probability)
            assertTrue(coldTempCount > 0, "Should have some cold temperatures");
        }
        
        @Test
        @DisplayName("Should generate valid locations")
        void shouldGenerateValidLocations() {
            String[] locations = new String[50];
            
            for (int i = 0; i < 50; i++) {
                TelemetryDto telemetryDto = telemetrySimulator.generateTelemetryData(testCar1);
                locations[i] = telemetryDto.getLocation();
            }
            
            // All locations should be valid
            for (String location : locations) {
                assertNotNull(location);
                assertFalse(location.isEmpty());
                assertTrue(location.contains(","), "Location should contain city and state");
            }
        }
    }

    @Nested
    @DisplayName("Simulation Logic Tests")
    class SimulationLogicTests {
        
        @Test
        @DisplayName("Should simulate telemetry for active cars only")
        void shouldSimulateTelemetryForActiveCarsOnly() {
            when(carService.getAllActiveCars()).thenReturn(testCars);
            when(mqttService.isConnected()).thenReturn(true);
            
            // Start simulation
            telemetrySimulator.startSimulation();
            
            // Simulate telemetry generation
            telemetrySimulator.simulateTelemetry();
            
            // Verify MQTT service was called for each active car
            verify(mqttService, times(2)).publishTelemetry(anyLong(), any(TelemetryDto.class));
            verify(mqttService, atLeastOnce()).publishTelemetry(1L, any(TelemetryDto.class));
            verify(mqttService, atLeastOnce()).publishTelemetry(2L, any(TelemetryDto.class));
        }
        
        @Test
        @DisplayName("Should not simulate when simulator is disabled")
        void shouldNotSimulateWhenSimulatorIsDisabled() {
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", false);
            
            telemetrySimulator.simulateTelemetry();
            
            verify(mqttService, never()).publishTelemetry(anyLong(), any(TelemetryDto.class));
        }
        
        @Test
        @DisplayName("Should not simulate when simulation is not running")
        void shouldNotSimulateWhenSimulationIsNotRunning() {
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", true);
            assertFalse(telemetrySimulator.isRunning());
            
            telemetrySimulator.simulateTelemetry();
            
            verify(mqttService, never()).publishTelemetry(anyLong(), any(TelemetryDto.class));
        }
        
        @Test
        @DisplayName("Should handle empty car list gracefully")
        void shouldHandleEmptyCarListGracefully() {
            when(carService.getAllActiveCars()).thenReturn(Collections.emptyList());
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", true);
            telemetrySimulator.startSimulation();
            
            telemetrySimulator.simulateTelemetry();
            
            verify(mqttService, never()).publishTelemetry(anyLong(), any(TelemetryDto.class));
        }
        
        @Test
        @DisplayName("Should handle inactive cars correctly")
        void shouldHandleInactiveCarsCorrectly() {
            Car inactiveCar = new Car();
            inactiveCar.setId(3L);
            inactiveCar.setIsActive(false);
            
            List<Car> carsWithInactive = Arrays.asList(testCar1, inactiveCar);
            when(carService.getAllActiveCars()).thenReturn(carsWithInactive);
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", true);
            telemetrySimulator.startSimulation();
            
            telemetrySimulator.simulateTelemetry();
            
            // Should only simulate for active cars
            verify(mqttService, times(1)).publishTelemetry(anyLong(), any(TelemetryDto.class));
            verify(mqttService, never()).publishTelemetry(3L, any(TelemetryDto.class));
        }
        
        @Test
        @DisplayName("Should occasionally update car status")
        void shouldOccasionallyUpdateCarStatus() {
            when(carService.getAllActiveCars()).thenReturn(testCars);
            when(mqttService.isConnected()).thenReturn(true);
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", true);
            telemetrySimulator.startSimulation();
            
            // Run simulation multiple times to increase chance of status update
            for (int i = 0; i < 20; i++) {
                telemetrySimulator.simulateTelemetry();
            }
            
            // Should have called publishStatus at least once
            verify(mqttService, atLeastOnce()).publishStatus(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle car service exceptions gracefully")
        void shouldHandleCarServiceExceptionsGracefully() {
            when(carService.getAllActiveCars()).thenThrow(new RuntimeException("Database error"));
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", true);
            telemetrySimulator.startSimulation();
            
            // Should not throw exception
            assertDoesNotThrow(() -> telemetrySimulator.simulateTelemetry());
        }
        
        @Test
        @DisplayName("Should handle MQTT service exceptions gracefully")
        void shouldHandleMqttServiceExceptionsGracefully() {
            when(carService.getAllActiveCars()).thenReturn(testCars);
            when(mqttService.publishTelemetry(anyLong(), any(TelemetryDto.class)))
                .thenThrow(new RuntimeException("MQTT error"));
            ReflectionTestUtils.setField(telemetrySimulator, "simulatorEnabled", true);
            telemetrySimulator.startSimulation();
            
            // Should not throw exception
            assertDoesNotThrow(() -> telemetrySimulator.simulateTelemetry());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should complete full simulation cycle")
        void shouldCompleteFullSimulationCycle() {
            when(carService.getAllActiveCars()).thenReturn(testCars);
            when(mqttService.isConnected()).thenReturn(true);
            
            // Start simulation
            telemetrySimulator.startSimulation();
            assertTrue(telemetrySimulator.isRunning());
            
            // Simulate telemetry
            telemetrySimulator.simulateTelemetry();
            
            // Stop simulation
            telemetrySimulator.stopSimulation();
            assertFalse(telemetrySimulator.isRunning());
            
            // Verify all expected interactions
            verify(mqttService, times(2)).publishTelemetry(anyLong(), any(TelemetryDto.class));
            verify(webSocketService, times(1)).broadcastSimulatorStatus(true);
            verify(webSocketService, times(1)).broadcastSimulatorStatus(false);
        }
        
        @Test
        @DisplayName("Should handle rapid start/stop cycles")
        void shouldHandleRapidStartStopCycles() {
            for (int i = 0; i < 10; i++) {
                telemetrySimulator.startSimulation();
                assertTrue(telemetrySimulator.isRunning());
                
                telemetrySimulator.stopSimulation();
                assertFalse(telemetrySimulator.isRunning());
            }
        }
    }
}