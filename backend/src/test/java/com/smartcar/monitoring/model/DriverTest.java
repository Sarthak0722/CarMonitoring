package com.smartcar.monitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Driver Model Tests")
public class DriverTest {

    private Validator validator;
    private Driver driver;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        user = new User();
        user.setId(1L);
        user.setUsername("testdriver");
        user.setPassword("password123");
        user.setRole(User.UserRole.DRIVER);
        user.setName("John Driver");
        user.setAge(25);
        user.setGender(User.Gender.MALE);
        user.setContactNumber("+1234567890");
        user.setEmail("driver@example.com");
        user.setLicenseNumber("DL123456");
        
        driver = new Driver();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should set default values")
        void defaultConstructorShouldSetDefaultValues() {
            Driver defaultDriver = new Driver();
            
            assertNotNull(defaultDriver.getCreationDate());
            assertNotNull(defaultDriver.getLastUpdateOn());
            assertTrue(defaultDriver.getIsActive());
            assertNull(defaultDriver.getAssignedCarId());
        }
        
        @Test
        @DisplayName("Parameterized constructor with user should set provided values")
        void parameterizedConstructorWithUserShouldSetProvidedValues() {
            Driver paramDriver = new Driver(user);
            
            assertEquals(user, paramDriver.getUser());
            assertNotNull(paramDriver.getCreationDate());
            assertNotNull(paramDriver.getLastUpdateOn());
            assertTrue(paramDriver.getIsActive());
            assertNull(paramDriver.getAssignedCarId());
        }
        
        @Test
        @DisplayName("Parameterized constructor with user and car should set provided values")
        void parameterizedConstructorWithUserAndCarShouldSetProvidedValues() {
            Long assignedCarId = 123L;
            Driver paramDriver = new Driver(user, assignedCarId);
            
            assertEquals(user, paramDriver.getUser());
            assertEquals(assignedCarId, paramDriver.getAssignedCarId());
            assertNotNull(paramDriver.getCreationDate());
            assertNotNull(paramDriver.getLastUpdateOn());
            assertTrue(paramDriver.getIsActive());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Valid driver should pass validation")
        void validDriverShouldPassValidation() {
            driver.setUser(user);
            
            Set<ConstraintViolation<Driver>> violations = validator.validate(driver);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }
        
        @Test
        @DisplayName("User should not be null")
        void userShouldNotBeNull() {
            driver.setUser(null);
            
            Set<ConstraintViolation<Driver>> violations = validator.validate(driver);
            assertFalse(violations.isEmpty(), "Should have validation violations for null user");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID correctly")
        void shouldGetAndSetIdCorrectly() {
            Long id = 123L;
            driver.setId(id);
            assertEquals(id, driver.getId());
        }
        
        @Test
        @DisplayName("Should get and set user correctly")
        void shouldGetAndSetUserCorrectly() {
            driver.setUser(user);
            assertEquals(user, driver.getUser());
        }
        
        @Test
        @DisplayName("Should get and set assigned car ID correctly")
        void shouldGetAndSetAssignedCarIdCorrectly() {
            Long assignedCarId = 456L;
            driver.setAssignedCarId(assignedCarId);
            assertEquals(assignedCarId, driver.getAssignedCarId());
        }
        
        @Test
        @DisplayName("Should get and set creation date correctly")
        void shouldGetAndSetCreationDateCorrectly() {
            LocalDateTime creationDate = LocalDateTime.now();
            driver.setCreationDate(creationDate);
            assertEquals(creationDate, driver.getCreationDate());
        }
        
        @Test
        @DisplayName("Should get and set last update on correctly")
        void shouldGetAndSetLastUpdateOnCorrectly() {
            LocalDateTime lastUpdateOn = LocalDateTime.now();
            driver.setLastUpdateOn(lastUpdateOn);
            assertEquals(lastUpdateOn, driver.getLastUpdateOn());
        }
        
        @Test
        @DisplayName("Should get and set is active correctly")
        void shouldGetAndSetIsActiveCorrectly() {
            Boolean isActive = false;
            driver.setIsActive(isActive);
            assertEquals(isActive, driver.getIsActive());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {
        
        @Test
        @DisplayName("PreUpdate should update lastUpdateOn timestamp")
        void preUpdateShouldUpdateLastUpdateOnTimestamp() throws Exception {
            LocalDateTime originalLastUpdate = driver.getLastUpdateOn();
            
            // Wait a bit to ensure timestamp difference
            Thread.sleep(10);
            
            // Use reflection to call preUpdate method
            driver.getClass().getMethod("preUpdate").invoke(driver);
            
            assertTrue(driver.getLastUpdateOn().isAfter(originalLastUpdate), 
                "Last update timestamp should be updated");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString should include driver information")
        void toStringShouldIncludeDriverInformation() {
            driver.setId(1L);
            driver.setUser(user);
            driver.setAssignedCarId(123L);
            driver.setIsActive(true);
            
            String toString = driver.toString();
            
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("userId=1"));
            assertTrue(toString.contains("assignedCarId=123"));
            assertTrue(toString.contains("isActive=true"));
        }
        
        @Test
        @DisplayName("ToString should handle null user gracefully")
        void toStringShouldHandleNullUserGracefully() {
            driver.setId(1L);
            driver.setUser(null);
            
            String toString = driver.toString();
            
            assertTrue(toString.contains("userId=null"));
            assertFalse(toString.contains("NullPointerException"));
        }
        
        @Test
        @DisplayName("ToString should handle null assigned car ID gracefully")
        void toStringShouldHandleNullAssignedCarIdGracefully() {
            driver.setId(1L);
            driver.setUser(user);
            driver.setAssignedCarId(null);
            
            String toString = driver.toString();
            
            assertTrue(toString.contains("assignedCarId=null"));
            assertFalse(toString.contains("NullPointerException"));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should handle car assignment correctly")
        void shouldHandleCarAssignmentCorrectly() {
            // Initially no car assigned
            assertNull(driver.getAssignedCarId());
            
            // Assign a car
            Long carId = 123L;
            driver.setAssignedCarId(carId);
            assertEquals(carId, driver.getAssignedCarId());
            
            // Unassign the car
            driver.setAssignedCarId(null);
            assertNull(driver.getAssignedCarId());
        }
        
        @Test
        @DisplayName("Should handle driver status changes correctly")
        void shouldHandleDriverStatusChangesCorrectly() {
            // Initially active
            assertTrue(driver.getIsActive());
            
            // Deactivate driver
            driver.setIsActive(false);
            assertFalse(driver.getIsActive());
            
            // Reactivate driver
            driver.setIsActive(true);
            assertTrue(driver.getIsActive());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle multiple car assignments correctly")
        void shouldHandleMultipleCarAssignmentsCorrectly() {
            Long[] carIds = {1L, 2L, 3L, null, 5L};
            
            for (Long carId : carIds) {
                driver.setAssignedCarId(carId);
                assertEquals(carId, driver.getAssignedCarId());
            }
        }
        
        @Test
        @DisplayName("Should handle timestamp precision correctly")
        void shouldHandleTimestampPrecisionCorrectly() {
            LocalDateTime now = LocalDateTime.now();
            driver.setCreationDate(now);
            driver.setLastUpdateOn(now);
            
            assertEquals(now, driver.getCreationDate());
            assertEquals(now, driver.getLastUpdateOn());
        }
        
        @Test
        @DisplayName("Should handle very large car IDs correctly")
        void shouldHandleVeryLargeCarIdsCorrectly() {
            Long largeCarId = Long.MAX_VALUE;
            driver.setAssignedCarId(largeCarId);
            assertEquals(largeCarId, driver.getAssignedCarId());
        }
    }
}