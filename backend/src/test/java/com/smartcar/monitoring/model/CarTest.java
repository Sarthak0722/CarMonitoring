package com.smartcar.monitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Car Model Tests")
public class CarTest {

    private Validator validator;
    private Car car;
    private Driver driver;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        driver = new Driver();
        driver.setId(1L);
        
        car = new Car();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should set default values")
        void defaultConstructorShouldSetDefaultValues() {
            Car defaultCar = new Car();
            
            assertNotNull(defaultCar.getCreationDate());
            assertNotNull(defaultCar.getLastUpdateOn());
            assertTrue(defaultCar.getIsActive());
            assertEquals(0, defaultCar.getSpeed());
            assertEquals(100, defaultCar.getFuelLevel());
            assertEquals(25, defaultCar.getTemperature());
            assertEquals("IDLE", defaultCar.getStatus());
        }
        
        @Test
        @DisplayName("Parameterized constructor should set provided values")
        void parameterizedConstructorShouldSetProvidedValues() {
            LocalDateTime now = LocalDateTime.now();
            Car paramCar = new Car("MOVING", 60, 75, 30, "New York");
            
            assertEquals("MOVING", paramCar.getStatus());
            assertEquals(60, paramCar.getSpeed());
            assertEquals(75, paramCar.getFuelLevel());
            assertEquals(30, paramCar.getTemperature());
            assertEquals("New York", paramCar.getLocation());
            assertNotNull(paramCar.getCreationDate());
            assertNotNull(paramCar.getLastUpdateOn());
            assertTrue(paramCar.getIsActive());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Valid car should pass validation")
        void validCarShouldPassValidation() {
            car.setStatus("MOVING");
            car.setSpeed(60);
            car.setFuelLevel(75);
            car.setTemperature(25);
            car.setLocation("New York");
            
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Status should not be blank")
        void statusShouldNotBeBlank(String status) {
            car.setStatus(status);
            car.setSpeed(60);
            car.setFuelLevel(75);
            car.setTemperature(25);
            car.setLocation("New York");
            
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank status");
        }
        
        @Test
        @DisplayName("Speed should be within valid range")
        void speedShouldBeWithinValidRange() {
            car.setStatus("MOVING");
            car.setLocation("New York");
            
            // Test negative speed
            car.setSpeed(-10);
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
            assertFalse(violations.isEmpty(), "Should have validation violations for negative speed");
            
            // Test speed exceeding maximum
            car.setSpeed(250);
            violations = validator.validate(car);
            assertFalse(violations.isEmpty(), "Should have validation violations for speed exceeding 200");
            
            // Test valid speed
            car.setSpeed(100);
            violations = validator.validate(car);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid speed");
        }
        
        @Test
        @DisplayName("Fuel level should be within valid range")
        void fuelLevelShouldBeWithinValidRange() {
            car.setStatus("MOVING");
            car.setSpeed(60);
            car.setLocation("New York");
            
            // Test negative fuel level
            car.setFuelLevel(-10);
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
            assertFalse(violations.isEmpty(), "Should have validation violations for negative fuel level");
            
            // Test fuel level exceeding maximum
            car.setFuelLevel(150);
            violations = validator.validate(car);
            assertFalse(violations.isEmpty(), "Should have validation violations for fuel level exceeding 100");
            
            // Test valid fuel level
            car.setFuelLevel(75);
            violations = validator.validate(car);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid fuel level");
        }
        
        @Test
        @DisplayName("Temperature should be within valid range")
        void temperatureShouldBeWithinValidRange() {
            car.setStatus("MOVING");
            car.setSpeed(60);
            car.setLocation("New York");
            
            // Test temperature below minimum
            car.setTemperature(-30);
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
            assertFalse(violations.isEmpty(), "Should have validation violations for temperature below -20");
            
            // Test temperature exceeding maximum
            car.setTemperature(80);
            violations = validator.validate(car);
            assertFalse(violations.isEmpty(), "Should have validation violations for temperature exceeding 60");
            
            // Test valid temperature
            car.setTemperature(25);
            violations = validator.validate(car);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid temperature");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Location should not be blank")
        void locationShouldNotBeBlank(String location) {
            car.setStatus("MOVING");
            car.setSpeed(60);
            car.setFuelLevel(75);
            car.setTemperature(25);
            car.setLocation(location);
            
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
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
            car.setId(id);
            assertEquals(id, car.getId());
        }
        
        @Test
        @DisplayName("Should get and set driver correctly")
        void shouldGetAndSetDriverCorrectly() {
            car.setDriver(driver);
            assertEquals(driver, car.getDriver());
        }
        
        @Test
        @DisplayName("Should get and set status correctly")
        void shouldGetAndSetStatusCorrectly() {
            String status = "MOVING";
            car.setStatus(status);
            assertEquals(status, car.getStatus());
        }
        
        @Test
        @DisplayName("Should get and set speed correctly")
        void shouldGetAndSetSpeedCorrectly() {
            Integer speed = 80;
            car.setSpeed(speed);
            assertEquals(speed, car.getSpeed());
        }
        
        @Test
        @DisplayName("Should get and set fuel level correctly")
        void shouldGetAndSetFuelLevelCorrectly() {
            Integer fuelLevel = 50;
            car.setFuelLevel(fuelLevel);
            assertEquals(fuelLevel, car.getFuelLevel());
        }
        
        @Test
        @DisplayName("Should get and set temperature correctly")
        void shouldGetAndSetTemperatureCorrectly() {
            Integer temperature = 35;
            car.setTemperature(temperature);
            assertEquals(temperature, car.getTemperature());
        }
        
        @Test
        @DisplayName("Should get and set location correctly")
        void shouldGetAndSetLocationCorrectly() {
            String location = "Los Angeles";
            car.setLocation(location);
            assertEquals(location, car.getLocation());
        }
        
        @Test
        @DisplayName("Should get and set creation date correctly")
        void shouldGetAndSetCreationDateCorrectly() {
            LocalDateTime creationDate = LocalDateTime.now();
            car.setCreationDate(creationDate);
            assertEquals(creationDate, car.getCreationDate());
        }
        
        @Test
        @DisplayName("Should get and set last update on correctly")
        void shouldGetAndSetLastUpdateOnCorrectly() {
            LocalDateTime lastUpdateOn = LocalDateTime.now();
            car.setLastUpdateOn(lastUpdateOn);
            assertEquals(lastUpdateOn, car.getLastUpdateOn());
        }
        
        @Test
        @DisplayName("Should get and set is active correctly")
        void shouldGetAndSetIsActiveCorrectly() {
            Boolean isActive = false;
            car.setIsActive(isActive);
            assertEquals(isActive, car.getIsActive());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {
        
        @Test
        @DisplayName("PreUpdate should update lastUpdateOn timestamp")
        void preUpdateShouldUpdateLastUpdateOnTimestamp() throws Exception {
            LocalDateTime originalLastUpdate = car.getLastUpdateOn();
            
            // Wait a bit to ensure timestamp difference
            Thread.sleep(10);
            
            // Use reflection to call preUpdate method
            car.getClass().getMethod("preUpdate").invoke(car);
            
            assertTrue(car.getLastUpdateOn().isAfter(originalLastUpdate), 
                "Last update timestamp should be updated");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString should include car information")
        void toStringShouldIncludeCarInformation() {
            car.setId(1L);
            car.setStatus("MOVING");
            car.setSpeed(60);
            car.setFuelLevel(75);
            car.setTemperature(25);
            car.setLocation("New York");
            car.setIsActive(true);
            
            String toString = car.toString();
            
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("status='MOVING'"));
            assertTrue(toString.contains("speed=60"));
            assertTrue(toString.contains("fuelLevel=75"));
            assertTrue(toString.contains("temperature=25"));
            assertTrue(toString.contains("location='New York'"));
            assertTrue(toString.contains("isActive=true"));
        }
        
        @Test
        @DisplayName("ToString should handle null driver gracefully")
        void toStringShouldHandleNullDriverGracefully() {
            car.setId(1L);
            car.setDriver(null);
            
            String toString = car.toString();
            
            assertTrue(toString.contains("driverId=null"));
            assertFalse(toString.contains("NullPointerException"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle boundary values correctly")
        void shouldHandleBoundaryValuesCorrectly() {
            // Test minimum valid values
            car.setStatus("IDLE");
            car.setLocation("Test Location");
            car.setSpeed(0);
            car.setFuelLevel(0);
            car.setTemperature(-20);
            
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
            assertTrue(violations.isEmpty(), "Should accept minimum valid values");
            
            // Test maximum valid values
            car.setSpeed(200);
            car.setFuelLevel(100);
            car.setTemperature(60);
            
            violations = validator.validate(car);
            assertTrue(violations.isEmpty(), "Should accept maximum valid values");
        }
        
        @Test
        @DisplayName("Should handle null values in optional fields")
        void shouldHandleNullValuesInOptionalFields() {
            car.setStatus("MOVING");
            car.setSpeed(60);
            car.setFuelLevel(75);
            car.setTemperature(25);
            car.setLocation("New York");
            car.setDriver(null); // Driver is optional
            
            Set<ConstraintViolation<Car>> violations = validator.validate(car);
            assertTrue(violations.isEmpty(), "Should accept null driver");
        }
    }
}