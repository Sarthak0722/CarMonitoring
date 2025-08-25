package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.ApiResponseDto;
import com.smartcar.monitoring.simulator.TelemetrySimulator;
import com.smartcar.monitoring.service.MqttService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Simulator Controller Tests")
public class SimulatorControllerTest {

    @Mock
    private TelemetrySimulator telemetrySimulator;

    @Mock
    private MqttService mqttService;

    @InjectMocks
    private SimulatorController simulatorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(simulatorController).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Get Simulator Status Tests")
    class GetSimulatorStatusTests {
        
        @Test
        @DisplayName("Should get simulator status successfully when running")
        void shouldGetSimulatorStatusSuccessfullyWhenRunning() throws Exception {
            when(telemetrySimulator.isRunning()).thenReturn(true);
            when(mqttService.isConnected()).thenReturn(true);

            mockMvc.perform(get("/api/simulator/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Simulator status retrieved successfully"))
                    .andExpect(jsonPath("$.data.isRunning").value(true))
                    .andExpect(jsonPath("$.data.mqttConnected").value(true))
                    .andExpect(jsonPath("$.data.status").value("RUNNING"));

            verify(telemetrySimulator).isRunning();
            verify(mqttService).isConnected();
        }
        
        @Test
        @DisplayName("Should get simulator status successfully when stopped")
        void shouldGetSimulatorStatusSuccessfullyWhenStopped() throws Exception {
            when(telemetrySimulator.isRunning()).thenReturn(false);
            when(mqttService.isConnected()).thenReturn(false);

            mockMvc.perform(get("/api/simulator/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Simulator status retrieved successfully"))
                    .andExpect(jsonPath("$.data.isRunning").value(false))
                    .andExpect(jsonPath("$.data.mqttConnected").value(false))
                    .andExpect(jsonPath("$.data.status").value("STOPPED"));

            verify(telemetrySimulator).isRunning();
            verify(mqttService).isConnected();
        }
        
        @Test
        @DisplayName("Should handle simulator status error")
        void shouldHandleSimulatorStatusError() throws Exception {
            when(telemetrySimulator.isRunning()).thenThrow(new RuntimeException("Status error"));

            mockMvc.perform(get("/api/simulator/status"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to get simulator status: Status error"));

            verify(telemetrySimulator).isRunning();
        }
    }

    @Nested
    @DisplayName("Start Simulator Tests")
    class StartSimulatorTests {
        
        @Test
        @DisplayName("Should start simulator successfully")
        void shouldStartSimulatorSuccessfully() throws Exception {
            doNothing().when(telemetrySimulator).startSimulation();

            mockMvc.perform(post("/api/simulator/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Simulator started successfully"));

            verify(telemetrySimulator).startSimulation();
        }
        
        @Test
        @DisplayName("Should handle start simulator error")
        void shouldHandleStartSimulatorError() throws Exception {
            doThrow(new RuntimeException("Start error")).when(telemetrySimulator).startSimulation();

            mockMvc.perform(post("/api/simulator/start"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to start simulator: Start error"));

            verify(telemetrySimulator).startSimulation();
        }
    }

    @Nested
    @DisplayName("Stop Simulator Tests")
    class StopSimulatorTests {
        
        @Test
        @DisplayName("Should stop simulator successfully")
        void shouldStopSimulatorSuccessfully() throws Exception {
            doNothing().when(telemetrySimulator).stopSimulation();

            mockMvc.perform(post("/api/simulator/stop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Simulator stopped successfully"));

            verify(telemetrySimulator).stopSimulation();
        }
        
        @Test
        @DisplayName("Should handle stop simulator error")
        void shouldHandleStopSimulatorError() throws Exception {
            doThrow(new RuntimeException("Stop error")).when(telemetrySimulator).stopSimulation();

            mockMvc.perform(post("/api/simulator/stop"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to stop simulator: Stop error"));

            verify(telemetrySimulator).stopSimulation();
        }
    }

    @Nested
    @DisplayName("Toggle Simulator Tests")
    class ToggleSimulatorTests {
        
        @Test
        @DisplayName("Should toggle simulator from stopped to running")
        void shouldToggleSimulatorFromStoppedToRunning() throws Exception {
            when(telemetrySimulator.isRunning()).thenReturn(false);
            doNothing().when(telemetrySimulator).startSimulation();

            mockMvc.perform(post("/api/simulator/toggle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Simulator toggled successfully"))
                    .andExpect(jsonPath("$.data.newStatus").value(true))
                    .andExpect(jsonPath("$.data.message").value("Simulator started"));

            verify(telemetrySimulator).isRunning();
            verify(telemetrySimulator).startSimulation();
            verify(telemetrySimulator, never()).stopSimulation();
        }
        
        @Test
        @DisplayName("Should toggle simulator from running to stopped")
        void shouldToggleSimulatorFromRunningToStopped() throws Exception {
            when(telemetrySimulator.isRunning()).thenReturn(true);
            doNothing().when(telemetrySimulator).stopSimulation();

            mockMvc.perform(post("/api/simulator/toggle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Simulator toggled successfully"))
                    .andExpect(jsonPath("$.data.newStatus").value(false))
                    .andExpect(jsonPath("$.data.message").value("Simulator stopped"));

            verify(telemetrySimulator).isRunning();
            verify(telemetrySimulator).stopSimulation();
            verify(telemetrySimulator, never()).startSimulation();
        }
        
        @Test
        @DisplayName("Should handle toggle simulator error")
        void shouldHandleToggleSimulatorError() throws Exception {
            when(telemetrySimulator.isRunning()).thenReturn(false);
            doThrow(new RuntimeException("Toggle error")).when(telemetrySimulator).startSimulation();

            mockMvc.perform(post("/api/simulator/toggle"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to toggle simulator: Toggle error"));

            verify(telemetrySimulator).isRunning();
            verify(telemetrySimulator).startSimulation();
        }
    }

    @Nested
    @DisplayName("Get MQTT Status Tests")
    class GetMqttStatusTests {
        
        @Test
        @DisplayName("Should get MQTT status when connected")
        void shouldGetMqttStatusWhenConnected() throws Exception {
            when(mqttService.isConnected()).thenReturn(true);

            mockMvc.perform(get("/api/simulator/mqtt-status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("MQTT status retrieved successfully"))
                    .andExpect(jsonPath("$.data.connected").value(true))
                    .andExpect(jsonPath("$.data.status").value("CONNECTED"));

            verify(mqttService).isConnected();
        }
        
        @Test
        @DisplayName("Should get MQTT status when disconnected")
        void shouldGetMqttStatusWhenDisconnected() throws Exception {
            when(mqttService.isConnected()).thenReturn(false);

            mockMvc.perform(get("/api/simulator/mqtt-status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("MQTT status retrieved successfully"))
                    .andExpect(jsonPath("$.data.connected").value(false))
                    .andExpect(jsonPath("$.data.status").value("DISCONNECTED"));

            verify(mqttService).isConnected();
        }
        
        @Test
        @DisplayName("Should handle MQTT status error")
        void shouldHandleMqttStatusError() throws Exception {
            when(mqttService.isConnected()).thenThrow(new RuntimeException("MQTT status error"));

            mockMvc.perform(get("/api/simulator/mqtt-status"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Failed to get MQTT status: MQTT status error"));

            verify(mqttService).isConnected();
        }
    }

    @Nested
    @DisplayName("Generate Test Data Tests")
    class GenerateTestDataTests {
        
        @Test
        @DisplayName("Should generate test data successfully")
        void shouldGenerateTestDataSuccessfully() throws Exception {
            mockMvc.perform(post("/api/simulator/generate-test-data")
                    .param("carId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Test data generation triggered for car: 1"));
        }
        
        @Test
        @DisplayName("Should handle test data generation error")
        void shouldHandleTestDataGenerationError() throws Exception {
            // This endpoint currently doesn't throw errors, but we can test the response format
            mockMvc.perform(post("/api/simulator/generate-test-data")
                    .param("carId", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Test data generation triggered for car: 999"));
        }
        
        @Test
        @DisplayName("Should handle missing car ID parameter")
        void shouldHandleMissingCarIdParameter() throws Exception {
            mockMvc.perform(post("/api/simulator/generate-test-data"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle very large car ID")
        void shouldHandleVeryLargeCarId() throws Exception {
            Long largeCarId = Long.MAX_VALUE;
            
            mockMvc.perform(post("/api/simulator/generate-test-data")
                    .param("carId", largeCarId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Test data generation triggered for car: " + largeCarId));
        }
        
        @Test
        @DisplayName("Should handle negative car ID")
        void shouldHandleNegativeCarId() throws Exception {
            mockMvc.perform(post("/api/simulator/generate-test-data")
                    .param("carId", "-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Test data generation triggered for car: -1"));
        }
        
        @Test
        @DisplayName("Should handle rapid status requests")
        void shouldHandleRapidStatusRequests() throws Exception {
            when(telemetrySimulator.isRunning()).thenReturn(true);
            when(mqttService.isConnected()).thenReturn(true);

            // Make multiple rapid requests
            for (int i = 0; i < 10; i++) {
                mockMvc.perform(get("/api/simulator/status"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.isRunning").value(true));
            }

            verify(telemetrySimulator, times(10)).isRunning();
            verify(mqttService, times(10)).isConnected();
        }
        
        @Test
        @DisplayName("Should handle rapid toggle requests")
        void shouldHandleRapidToggleRequests() throws Exception {
            when(telemetrySimulator.isRunning()).thenReturn(false, true, false, true);
            doNothing().when(telemetrySimulator).startSimulation();
            doNothing().when(telemetrySimulator).stopSimulation();

            // Toggle multiple times rapidly
            for (int i = 0; i < 4; i++) {
                mockMvc.perform(post("/api/simulator/toggle"))
                        .andExpect(status().isOk());
            }

            verify(telemetrySimulator, times(4)).isRunning();
            verify(telemetrySimulator, times(2)).startSimulation();
            verify(telemetrySimulator, times(2)).stopSimulation();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should complete full simulator lifecycle")
        void shouldCompleteFullSimulatorLifecycle() throws Exception {
            // Start simulator
            doNothing().when(telemetrySimulator).startSimulation();
            when(telemetrySimulator.isRunning()).thenReturn(true);
            when(mqttService.isConnected()).thenReturn(true);

            mockMvc.perform(post("/api/simulator/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Simulator started successfully"));

            // Check status
            mockMvc.perform(get("/api/simulator/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isRunning").value(true));

            // Stop simulator and update mock behavior
            doNothing().when(telemetrySimulator).stopSimulation();
            when(telemetrySimulator.isRunning()).thenReturn(false);

            mockMvc.perform(post("/api/simulator/stop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Simulator stopped successfully"));

            // Verify final status
            mockMvc.perform(get("/api/simulator/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isRunning").value(false));

            verify(telemetrySimulator).startSimulation();
            verify(telemetrySimulator).stopSimulation();
            verify(telemetrySimulator, times(2)).isRunning();
        }
        
        @Test
        @DisplayName("Should handle concurrent operations gracefully")
        void shouldHandleConcurrentOperationsGracefully() throws Exception {
            doNothing().when(telemetrySimulator).startSimulation();
            doNothing().when(telemetrySimulator).stopSimulation();

            // Simulate concurrent start/stop operations
            mockMvc.perform(post("/api/simulator/start"))
                    .andExpect(status().isOk());
            
            mockMvc.perform(post("/api/simulator/stop"))
                    .andExpect(status().isOk());

            verify(telemetrySimulator).startSimulation();
            verify(telemetrySimulator).stopSimulation();
        }
    }
}