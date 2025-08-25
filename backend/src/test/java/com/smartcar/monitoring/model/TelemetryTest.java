package com.smartcar.monitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Telemetry Model Tests")
class TelemetryTest {

    private Validator validator;
    private Telemetry telemetry;
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
        
        telemetry = new Telemetry();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should set default values")
        void defaultConstructorShouldSetDefaultValues() {
            Telemetry defaultTelemetry = new Telemetry();
            
            assertNotNull(defaultTelemetry.getCreationDate());
            assertNotNull(defaultTelemetry.getLastUpdateOn());
            assertNotNull(defaultTelemetry.getTimestamp());
            assertTrue(defaultTelemetry.getIsActive());
        }
        
        @Test
        @DisplayName("Parameterized constructor should set provided values")
        void parameterizedConstructorShouldSetProvidedValues() {
            LocalDateTime timestamp = LocalDateTime.now();
            Telemetry paramTelemetry = new Telemetry(car, 80, 50, 35, "Los Angeles");
            
            assertEquals(car, paramTelemetry.getCar());
            assertEquals(80, paramTelemetry.getSpeed());
            assertEquals(50, paramTelemetry.getFuel());
            assertEquals(35, paramTelemetry.getTemperature());
            assertEquals("Los Angeles", paramTelemetry.getLocation());
            assertNotNull(paramTelemetry.getCreationDate());
            assertNotNull(paramTelemetry.getLastUpdateOn());
            assertNotNull(paramTelemetry.getTimestamp());
            assertTrue(paramTelemetry.getIsActive());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Valid telemetry should pass validation")
        void validTelemetryShouldPassValidation() {
            telemetry.setCar(car);
            telemetry.setSpeed(80);
            telemetry.setFuel(50);
            telemetry.setTemperature(35);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }
        
        @Test
        @DisplayName("Car should not be null")
        void carShouldNotBeNull() {
            telemetry.setCar(null);
            telemetry.setSpeed(80);
            telemetry.setFuel(50);
            telemetry.setTemperature(35);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for null car");
        }
        
        @Test
        @DisplayName("Timestamp should not be null")
        void timestampShouldNotBeNull() {
            telemetry.setCar(car);
            telemetry.setSpeed(80);
            telemetry.setFuel(50);
            telemetry.setTemperature(35);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(null);
            
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for null timestamp");
        }
        
        @Test
        @DisplayName("Speed should be within valid range")
        void speedShouldBeWithinValidRange() {
            telemetry.setCar(car);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(LocalDateTime.now());
            
            // Test negative speed
            telemetry.setSpeed(-10);
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for negative speed");
            
            // Test speed exceeding maximum
            telemetry.setSpeed(250);
            violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for speed exceeding 200");
            
            // Test valid speed
            telemetry.setSpeed(100);
            violations = validator.validate(telemetry);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid speed");
        }
        
        @Test
        @DisplayName("Fuel should be within valid range")
        void fuelShouldBeWithinValidRange() {
            telemetry.setCar(car);
            telemetry.setSpeed(80);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(LocalDateTime.now());
            
            // Test negative fuel
            telemetry.setFuel(-10);
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for negative fuel");
            
            // Test fuel exceeding maximum
            telemetry.setFuel(150);
            violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for fuel exceeding 100");
            
            // Test valid fuel
            telemetry.setFuel(75);
            violations = validator.validate(telemetry);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid fuel");
        }
        
        @Test
        @DisplayName("Temperature should be within valid range")
        void temperatureShouldBeWithinValidRange() {
            telemetry.setCar(car);
            telemetry.setSpeed(80);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(LocalDateTime.now());
            
            // Test temperature below minimum
            telemetry.setTemperature(-30);
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for temperature below -20");
            
            // Test temperature exceeding maximum
            telemetry.setTemperature(80);
            violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for temperature exceeding 60");
            
            // Test valid temperature
            telemetry.setTemperature(25);
            violations = validator.validate(telemetry);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid temperature");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Location should not be blank")
        void locationShouldNotBeBlank(String location) {
            telemetry.setCar(car);
            telemetry.setSpeed(80);
            telemetry.setFuel(50);
            telemetry.setTemperature(35);
            telemetry.setLocation(location);
            telemetry.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank location");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID correctly")
        void shouldGetAndSetIdCorrectly() {
            Long id = 123L;
            telemetry.setId(id);
            assertEquals(id, telemetry.getId());
        }
        
        @Test
        @DisplayName("Should get and set car correctly")
        void shouldGetAndSetCarCorrectly() {
            telemetry.setCar(car);
            assertEquals(car, telemetry.getCar());
        }
        
        @Test
        @DisplayName("Should get and set timestamp correctly")
        void shouldGetAndSetTimestampCorrectly() {
            LocalDateTime timestamp = LocalDateTime.now();
            telemetry.setTimestamp(timestamp);
            assertEquals(timestamp, telemetry.getTimestamp());
        }
        
        @Test
        @DisplayName("Should get and set speed correctly")
        void shouldGetAndSetSpeedCorrectly() {
            Integer speed = 80;
            telemetry.setSpeed(speed);
            assertEquals(speed, telemetry.getSpeed());
        }
        
        @Test
        @DisplayName("Should get and set fuel correctly")
        void shouldGetAndSetFuelCorrectly() {
            Integer fuel = 50;
            telemetry.setFuel(fuel);
            assertEquals(fuel, telemetry.getFuel());
        }
        
        @Test
        @DisplayName("Should get and set temperature correctly")
        void shouldGetAndSetTemperatureCorrectly() {
            Integer temperature = 35;
            telemetry.setTemperature(temperature);
            assertEquals(temperature, telemetry.getTemperature());
        }
        
        @Test
        @DisplayName("Should get and set location correctly")
        void shouldGetAndSetLocationCorrectly() {
            String location = "Los Angeles";
            telemetry.setLocation(location);
            assertEquals(location, telemetry.getLocation());
        }
        
        @Test
        @DisplayName("Should get and set creation date correctly")
        void shouldGetAndSetCreationDateCorrectly() {
            LocalDateTime creationDate = LocalDateTime.now();
            telemetry.setCreationDate(creationDate);
            assertEquals(creationDate, telemetry.getCreationDate());
        }
        
        @Test
        @DisplayName("Should get and set last update on correctly")
        void shouldGetAndSetLastUpdateOnCorrectly() {
            LocalDateTime lastUpdateOn = LocalDateTime.now();
            telemetry.setLastUpdateOn(lastUpdateOn);
            assertEquals(lastUpdateOn, telemetry.getLastUpdateOn());
        }
        
        @Test
        @DisplayName("Should get and set is active correctly")
        void shouldGetAndSetIsActiveCorrectly() {
            Boolean isActive = false;
            telemetry.setIsActive(isActive);
            assertEquals(isActive, telemetry.getIsActive());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {
        
        @Test
        @DisplayName("PreUpdate should update lastUpdateOn timestamp")
        void preUpdateShouldUpdateLastUpdateOnTimestamp() throws Exception {
            LocalDateTime originalLastUpdate = telemetry.getLastUpdateOn();
            
            // Wait a bit to ensure timestamp difference
            Thread.sleep(10);
            
            // Use reflection to call preUpdate method
            telemetry.getClass().getMethod("preUpdate").invoke(telemetry);
            
            assertTrue(telemetry.getLastUpdateOn().isAfter(originalLastUpdate), 
                "Last update timestamp should be updated");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString should include telemetry information")
        void toStringShouldIncludeTelemetryInformation() {
            telemetry.setId(1L);
            telemetry.setCar(car);
            telemetry.setSpeed(80);
            telemetry.setFuel(50);
            telemetry.setTemperature(35);
            telemetry.setLocation("Los Angeles");
            telemetry.setIsActive(true);
            
            String toString = telemetry.toString();
            
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("carId=1"));
            assertTrue(toString.contains("speed=80"));
            assertTrue(toString.contains("fuel=50"));
            assertTrue(toString.contains("temperature=35"));
            assertTrue(toString.contains("location='Los Angeles'"));
            assertTrue(toString.contains("isActive=true"));
        }
        
        @Test
        @DisplayName("ToString should handle null car gracefully")
        void toStringShouldHandleNullCarGracefully() {
            telemetry.setId(1L);
            telemetry.setCar(null);
            
            String toString = telemetry.toString();
            
            assertTrue(toString.contains("carId=null"));
            assertFalse(toString.contains("NullPointerException"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle boundary values correctly")
        void shouldHandleBoundaryValuesCorrectly() {
            telemetry.setCar(car);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(LocalDateTime.now());
            
            // Test minimum valid values
            telemetry.setSpeed(0);
            telemetry.setFuel(0);
            telemetry.setTemperature(-20);
            
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertTrue(violations.isEmpty(), "Should accept minimum valid values");
            
            // Test maximum valid values
            telemetry.setSpeed(200);
            telemetry.setFuel(100);
            telemetry.setTemperature(60);
            
            violations = validator.validate(telemetry);
            assertTrue(violations.isEmpty(), "Should accept maximum valid values");
        }
        
        @Test
        @DisplayName("Should handle zero values correctly")
        void shouldHandleZeroValuesCorrectly() {
            telemetry.setCar(car);
            telemetry.setSpeed(0);
            telemetry.setFuel(0);
            telemetry.setTemperature(0);
            telemetry.setLocation("Los Angeles");
            telemetry.setTimestamp(LocalDateTime.now());
            
            Set<ConstraintViolation<Telemetry>> violations = validator.validate(telemetry);
            assertTrue(violations.isEmpty(), "Should accept zero values");
        }
    }
}