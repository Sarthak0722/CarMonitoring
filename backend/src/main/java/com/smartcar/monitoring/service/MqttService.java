package com.smartcar.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcar.monitoring.dto.TelemetryDto;
import com.smartcar.monitoring.model.Alert;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.model.Telemetry;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private TelemetryService telemetryService;

    @Autowired
    private CarService carService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mqtt.topic.prefix}")
    private String topicPrefix;

    @Value("${mqtt.client.id}")
    private String clientId;

    @PostConstruct
    public void init() {
        try {
            setupMessageCallback();
            subscribeToTopics();
            logger.info("MQTT Service initialized successfully. Client ID: {}", clientId);
            
            // Broadcast MQTT connection status
            webSocketService.broadcastMqttStatus(true);
        } catch (Exception e) {
            logger.error("Failed to initialize MQTT Service", e);
            webSocketService.broadcastMqttStatus(false);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client disconnected and closed");
            }
            webSocketService.broadcastMqttStatus(false);
        } catch (MqttException e) {
            logger.error("Error during MQTT cleanup", e);
        }
    }

    private void setupMessageCallback() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.error("MQTT connection lost", cause);
                webSocketService.broadcastMqttStatus(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {
                    handleIncomingMessage(topic, message);
                } catch (Exception e) {
                    logger.error("Error handling incoming MQTT message", e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.debug("MQTT message delivery completed");
            }
        });
    }

    private void subscribeToTopics() throws MqttException {
        // Subscribe to telemetry topics for all cars
        String telemetryTopic = topicPrefix + "/+/telemetry";
        mqttClient.subscribe(telemetryTopic, 1);
        logger.info("Subscribed to telemetry topic: {}", telemetryTopic);

        // Subscribe to status topics
        String statusTopic = topicPrefix + "/+/status";
        mqttClient.subscribe(statusTopic, 1);
        logger.info("Subscribed to status topic: {}", statusTopic);
    }

    private void handleIncomingMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            logger.debug("Received MQTT message on topic: {} - Payload: {}", topic, payload);

            // Parse topic to extract car ID
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 3) {
                String carIdStr = topicParts[1];
                String messageType = topicParts[2];

                Long carId = Long.parseLong(carIdStr);

                switch (messageType) {
                    case "telemetry":
                        handleTelemetryMessage(carId, payload);
                        break;
                    case "status":
                        handleStatusMessage(carId, payload);
                        break;
                    default:
                        logger.warn("Unknown message type: {}", messageType);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing MQTT message", e);
        }
    }

    private void handleTelemetryMessage(Long carId, String payload) {
        try {
            TelemetryDto telemetryDto = objectMapper.readValue(payload, TelemetryDto.class);
            telemetryDto.setCarId(carId);
            telemetryDto.setTimestamp(LocalDateTime.now());

            // Save telemetry data
            Car car = carService.getCarById(carId);
            Telemetry telemetry = new Telemetry();
            telemetry.setCar(car);
            telemetry.setSpeed(telemetryDto.getSpeed());
            telemetry.setFuel(telemetryDto.getFuelLevel());
            telemetry.setTemperature(telemetryDto.getTemperature());
            telemetry.setLocation(telemetryDto.getLocation());
            telemetry.setTimestamp(telemetryDto.getTimestamp());

            Telemetry savedTelemetry = telemetryService.createTelemetry(telemetry);

            // Check for alerts
            Alert createdAlert = checkAndCreateAlerts(car, telemetryDto);

            // Broadcast real-time updates via WebSocket
            webSocketService.broadcastTelemetryUpdate(telemetryDto);
            webSocketService.sendTelemetryToCar(carId, telemetryDto);
            webSocketService.broadcastCarLocation(carId, telemetryDto.getLocation());

            // Send critical alerts to admin dashboard
            if (createdAlert != null && createdAlert.getSeverity() == Alert.AlertSeverity.CRITICAL) {
                webSocketService.sendCriticalAlertToAdmins(createdAlert);
            }

            logger.info("Telemetry data saved and broadcasted for car {}: speed={}, fuel={}, temp={}", 
                       carId, telemetryDto.getSpeed(), telemetryDto.getFuelLevel(), telemetryDto.getTemperature());

        } catch (Exception e) {
            logger.error("Error handling telemetry message for car {}", carId, e);
        }
    }

    private void handleStatusMessage(Long carId, String payload) {
        try {
            // Handle car status updates
            logger.info("Status update received for car {}: {}", carId, payload);
            
            // Broadcast status update via WebSocket
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("carId", carId);
            statusUpdate.put("status", payload);
            webSocketService.broadcastSystemStatus(statusUpdate);
            
        } catch (Exception e) {
            logger.error("Error handling status message for car {}", carId, e);
        }
    }

    private Alert checkAndCreateAlerts(Car car, TelemetryDto telemetryDto) {
        try {
            Alert createdAlert = null;

            // Check fuel level
            if (telemetryDto.getFuelLevel() < 20) {
                String alertMessage = "Low fuel level: " + telemetryDto.getFuelLevel() + "%";
                Alert.AlertSeverity severity = telemetryDto.getFuelLevel() < 10 ? 
                    Alert.AlertSeverity.CRITICAL : Alert.AlertSeverity.HIGH;
                
                createdAlert = alertService.createAlert(car, "LOW_FUEL", severity.toString(), alertMessage);
                webSocketService.broadcastAlertUpdate(createdAlert);
                logger.warn("Low fuel alert created for car {}: {}%", car.getId(), telemetryDto.getFuelLevel());
            }

            // Check temperature
            if (telemetryDto.getTemperature() > 50) {
                String alertMessage = "High temperature: " + telemetryDto.getTemperature() + "°C";
                Alert.AlertSeverity severity = telemetryDto.getTemperature() > 60 ? 
                    Alert.AlertSeverity.CRITICAL : Alert.AlertSeverity.HIGH;
                
                createdAlert = alertService.createAlert(car, "HIGH_TEMPERATURE", severity.toString(), alertMessage);
                webSocketService.broadcastAlertUpdate(createdAlert);
                logger.warn("High temperature alert created for car {}: {}°C", car.getId(), telemetryDto.getTemperature());
            }

            // Check speed
            if (telemetryDto.getSpeed() > 120) {
                String alertMessage = "High speed: " + telemetryDto.getSpeed() + " km/h";
                Alert.AlertSeverity severity = telemetryDto.getSpeed() > 150 ? 
                    Alert.AlertSeverity.CRITICAL : Alert.AlertSeverity.MEDIUM;
                
                createdAlert = alertService.createAlert(car, "HIGH_SPEED", severity.toString(), alertMessage);
                webSocketService.broadcastAlertUpdate(createdAlert);
                logger.warn("High speed alert created for car {}: {} km/h", car.getId(), telemetryDto.getSpeed());
            }

            return createdAlert;

        } catch (Exception e) {
            logger.error("Error checking alerts for car {}", car.getId(), e);
            return null;
        }
    }

    public CompletableFuture<Void> publishTelemetry(Long carId, TelemetryDto telemetryDto) {
        return CompletableFuture.runAsync(() -> {
            try {
                String topic = topicPrefix + "/" + carId + "/telemetry";
                String payload = objectMapper.writeValueAsString(telemetryDto);
                
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                message.setRetained(false);

                mqttClient.publish(topic, message);
                logger.debug("Published telemetry to topic: {} - Car: {}", topic, carId);

            } catch (Exception e) {
                logger.error("Error publishing telemetry for car {}", carId, e);
            }
        });
    }

    public CompletableFuture<Void> publishStatus(Long carId, String status) {
        return CompletableFuture.runAsync(() -> {
            try {
                String topic = topicPrefix + "/" + carId + "/status";
                String payload = "{\"status\":\"" + status + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
                
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                message.setRetained(false);

                mqttClient.publish(topic, message);
                logger.debug("Published status to topic: {} - Car: {} - Status: {}", topic, carId, status);

            } catch (Exception e) {
                logger.error("Error publishing status for car {}", carId, e);
            }
        });
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}
