package com.smartcar.monitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.EnumSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Alert Model Tests")
public class AlertTest {

    private Validator validator;
    private Alert alert;
    private Car car;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        car = new Car();
        car.setId(1L);
        car.setStatus("MOVING");
        car.setSpeed(60);
        car.setFuelLevel(75);
        car.setTemperature(25);
        car.setLocation("New York");
        
        alert = new Alert();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should set default values")
        void defaultConstructorShouldSetDefaultValues() {
            Alert defaultAlert = new Alert();
            
            assertNotNull(defaultAlert.getCreationDate());
            assertNotNull(defaultAlert.getLastUpdateOn());
            assertNotNull(defaultAlert.getTimestamp());
            assertTrue(defaultAlert.getIsActive());
            assertFalse(defaultAlert.getAcknowledged());
        }
        
        @Test
        @DisplayName("Parameterized constructor should set provided values")
        void parameterizedConstructorShouldSetProvidedValues() {
            Alert paramAlert = new Alert(car, "LOW_FUEL", Alert.AlertSeverity.HIGH);
            
            assertEquals(car, paramAlert.getCar());
            assertEquals("LOW_FUEL", paramAlert.getType());
            assertEquals(Alert.AlertSeverity.HIGH, paramAlert.getSeverity());
            assertNotNull(paramAlert.getCreationDate());
            assertNotNull(paramAlert.getLastUpdateOn());
            assertNotNull(paramAlert.getTimestamp());
            assertTrue(paramAlert.getIsActive());
            assertFalse(paramAlert.getAcknowledged());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {
        
        @Test
        @DisplayName("AlertSeverity enum should have correct values")
        void alertSeverityEnumShouldHaveCorrectValues() {
            Alert.AlertSeverity[] severities = Alert.AlertSeverity.values();
            
            assertEquals(4, severities.length);
            assertArrayEquals(new Alert.AlertSeverity[]{
                Alert.AlertSeverity.LOW,
                Alert.AlertSeverity.MEDIUM,
                Alert.AlertSeverity.HIGH,
                Alert.AlertSeverity.CRITICAL
            }, severities);
        }
        
        @Test
        @DisplayName("AlertSeverity enum should handle ordinal values correctly")
        void alertSeverityEnumShouldHandleOrdinalValuesCorrectly() {
            assertEquals(0, Alert.AlertSeverity.LOW.ordinal());
            assertEquals(1, Alert.AlertSeverity.MEDIUM.ordinal());
            assertEquals(2, Alert.AlertSeverity.HIGH.ordinal());
            assertEquals(3, Alert.AlertSeverity.CRITICAL.ordinal());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Valid alert should pass validation")
        void validAlertShouldPassValidation() {
            alert.setCar(car);
            alert.setType("LOW_FUEL");
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Alert>> violations = validator.validate(alert);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }
        
        @Test
        @DisplayName("Car should not be null")
        void carShouldNotBeNull() {
            alert.setCar(null);
            alert.setType("LOW_FUEL");
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Alert>> violations = validator.validate(alert);
            assertFalse(violations.isEmpty(), "Should have validation violations for null car");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Alert type should not be blank")
        void alertTypeShouldNotBeBlank(String type) {
            alert.setCar(car);
            alert.setType(type);
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Alert>> violations = validator.validate(alert);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank alert type");
        }
        
        @Test
        @DisplayName("Severity should not be null")
        void severityShouldNotBeNull() {
            alert.setCar(car);
            alert.setType("LOW_FUEL");
            alert.setSeverity(null);
            alert.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Alert>> violations = validator.validate(alert);
            assertFalse(violations.isEmpty(), "Should have validation violations for null severity");
        }
        
        @Test
        @DisplayName("Timestamp should not be null")
        void timestampShouldNotBeNull() {
            alert.setCar(car);
            alert.setType("LOW_FUEL");
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setTimestamp(null);
            
            Set<ConstraintViolation<Alert>> violations = validator.validate(alert);
            assertFalse(violations.isEmpty(), "Should have validation violations for null timestamp");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID correctly")
        void shouldGetAndSetIdCorrectly() {
            Long id = 123L;
            alert.setId(id);
            assertEquals(id, alert.getId());
        }
        
        @Test
        @DisplayName("Should get and set car correctly")
        void shouldGetAndSetCarCorrectly() {
            alert.setCar(car);
            assertEquals(car, alert.getCar());
        }
        
        @Test
        @DisplayName("Should get and set type correctly")
        void shouldGetAndSetTypeCorrectly() {
            String type = "ENGINE_TEMPERATURE_HIGH";
            alert.setType(type);
            assertEquals(type, alert.getType());
        }
        
        @Test
        @DisplayName("Should get and set severity correctly")
        void shouldGetAndSetSeverityCorrectly() {
            Alert.AlertSeverity severity = Alert.AlertSeverity.CRITICAL;
            alert.setSeverity(severity);
            assertEquals(severity, alert.getSeverity());
        }
        
        @Test
        @DisplayName("Should get and set timestamp correctly")
        void shouldGetAndSetTimestampCorrectly() {
            LocalDateTime timestamp = LocalDateTime.now();
            alert.setTimestamp(timestamp);
            assertEquals(timestamp, alert.getTimestamp());
        }
        
        @Test
        @DisplayName("Should get and set acknowledged correctly")
        void shouldGetAndSetAcknowledgedCorrectly() {
            Boolean acknowledged = true;
            alert.setAcknowledged(acknowledged);
            assertEquals(acknowledged, alert.getAcknowledged());
        }
        
        @Test
        @DisplayName("Should get and set creation date correctly")
        void shouldGetAndSetCreationDateCorrectly() {
            LocalDateTime creationDate = LocalDateTime.now();
            alert.setCreationDate(creationDate);
            assertEquals(creationDate, alert.getCreationDate());
        }
        
        @Test
        @DisplayName("Should get and set last update on correctly")
        void shouldGetAndSetLastUpdateOnCorrectly() {
            LocalDateTime lastUpdateOn = LocalDateTime.now();
            alert.setLastUpdateOn(lastUpdateOn);
            assertEquals(lastUpdateOn, alert.getLastUpdateOn());
        }
        
        @Test
        @DisplayName("Should get and set is active correctly")
        void shouldGetAndSetIsActiveCorrectly() {
            Boolean isActive = false;
            alert.setIsActive(isActive);
            assertEquals(isActive, alert.getIsActive());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {
        
        @Test
        @DisplayName("PreUpdate should update lastUpdateOn timestamp")
        void preUpdateShouldUpdateLastUpdateOnTimestamp() throws Exception {
            LocalDateTime originalLastUpdate = alert.getLastUpdateOn();
            
            // Wait a bit to ensure timestamp difference
            Thread.sleep(10);
            
            // Use reflection to call preUpdate method
            alert.getClass().getMethod("preUpdate").invoke(alert);
            
            assertTrue(alert.getLastUpdateOn().isAfter(originalLastUpdate), 
                "Last update timestamp should be updated");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString should include alert information")
        void toStringShouldIncludeAlertInformation() {
            alert.setId(1L);
            alert.setCar(car);
            alert.setType("LOW_FUEL");
            alert.setSeverity(Alert.AlertSeverity.HIGH);
            alert.setIsActive(true);
            
            String toString = alert.toString();
            
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("carId=1"));
            assertTrue(toString.contains("type='LOW_FUEL'"));
            assertTrue(toString.contains("severity=HIGH"));
            assertTrue(toString.contains("isActive=true"));
        }
        
        @Test
        @DisplayName("ToString should handle null car gracefully")
        void toStringShouldHandleNullCarGracefully() {
            alert.setId(1L);
            alert.setCar(null);
            
            String toString = alert.toString();
            
            assertTrue(toString.contains("carId=null"));
            assertFalse(toString.contains("NullPointerException"));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should handle different alert types correctly")
        void shouldHandleDifferentAlertTypesCorrectly() {
            String[] alertTypes = {"LOW_FUEL", "HIGH_TEMPERATURE", "SPEED_LIMIT_EXCEEDED", "MAINTENANCE_DUE"};
            
            for (String type : alertTypes) {
                alert.setType(type);
                assertEquals(type, alert.getType());
            }
        }
        
        @Test
        @DisplayName("Should handle severity escalation correctly")
        void shouldHandleSeverityEscalationCorrectly() {
            // Test severity escalation logic
            alert.setSeverity(Alert.AlertSeverity.LOW);
            assertTrue(alert.getSeverity().ordinal() < Alert.AlertSeverity.CRITICAL.ordinal());
            
            alert.setSeverity(Alert.AlertSeverity.CRITICAL);
            assertEquals(Alert.AlertSeverity.CRITICAL, alert.getSeverity());
        }
        
        @Test
        @DisplayName("Should handle acknowledgment workflow correctly")
        void shouldHandleAcknowledgmentWorkflowCorrectly() {
            // Initially not acknowledged
            assertFalse(alert.getAcknowledged());
            
            // Acknowledge the alert
            alert.setAcknowledged(true);
            assertTrue(alert.getAcknowledged());
            
            // Unacknowledge the alert
            alert.setAcknowledged(false);
            assertFalse(alert.getAcknowledged());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle all severity levels correctly")
        void shouldHandleAllSeverityLevelsCorrectly() {
            for (Alert.AlertSeverity severity : Alert.AlertSeverity.values()) {
                alert.setSeverity(severity);
                assertEquals(severity, alert.getSeverity());
            }
        }
        
        @Test
        @DisplayName("Should handle long alert type strings")
        void shouldHandleLongAlertTypeStrings() {
            String longType = "VERY_LONG_ALERT_TYPE_NAME_THAT_MIGHT_BE_VERY_DESCRIPTIVE";
            alert.setType(longType);
            assertEquals(longType, alert.getType());
        }
        
        @Test
        @DisplayName("Should handle special characters in alert type")
        void shouldHandleSpecialCharactersInAlertType() {
            String specialType = "ALERT_TYPE_WITH_SPECIAL_CHARS_@#$%^&*()";
            alert.setType(specialType);
            assertEquals(specialType, alert.getType());
        }
    }
}