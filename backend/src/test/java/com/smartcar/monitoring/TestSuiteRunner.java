package com.smartcar.monitoring;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive Test Suite for Smart Car Monitoring System
 * 
 * This test suite covers all components of the system:
 * - Model classes (Car, Telemetry, Alert, User, Driver, Admin)
 * - Simulator components (TelemetrySimulator)
 * - Controllers (TelemetryController, SimulatorController)
 * - Services (will be added as they are implemented)
 * - Integration tests
 * 
 * Test Coverage Areas:
 * 1. Model Validation Tests
 * 2. Constructor Tests
 * 3. Getter/Setter Tests
 * 4. Lifecycle Hook Tests
 * 5. Business Logic Tests
 * 6. Edge Case Tests
 * 7. Error Handling Tests
 * 8. API Endpoint Tests
 * 9. Simulator Logic Tests
 * 10. Integration Tests
 */
@Suite
@SuiteDisplayName("Smart Car Monitoring System - Complete Test Suite")
@SelectClasses({
    // Model Tests
    com.smartcar.monitoring.model.CarTest.class,
    com.smartcar.monitoring.model.TelemetryTest.class,
    com.smartcar.monitoring.model.AlertTest.class,
    com.smartcar.monitoring.model.UserTest.class,
    com.smartcar.monitoring.model.DriverTest.class,
    com.smartcar.monitoring.model.AdminTest.class,
    
    // Simulator Tests
    com.smartcar.monitoring.simulator.TelemetrySimulatorTest.class,
    
    // Controller Tests
    com.smartcar.monitoring.controller.TelemetryControllerTest.class,
    com.smartcar.monitoring.controller.SimulatorControllerTest.class,
    
    // Main Application Test
    com.smartcar.monitoring.SmartCarMonitoringApplicationTests.class
})
public class TestSuiteRunner {
    
    /**
     * Test Execution Instructions:
     * 
     * 1. Run All Tests:
     *    mvn test
     * 
     * 2. Run Specific Test Class:
     *    mvn test -Dtest=CarTest
     * 
     * 3. Run Tests with Coverage:
     *    mvn test jacoco:report
     * 
     * 4. Run Tests in IDE:
     *    Right-click on TestSuiteRunner.java and select "Run TestSuiteRunner"
     * 
     * 5. Run Individual Test Methods:
     *    Right-click on specific test method and select "Run"
     * 
     * Test Categories:
     * - Unit Tests: Individual component testing
     * - Integration Tests: Component interaction testing
     * - Validation Tests: Data validation testing
     * - Error Handling Tests: Exception and error scenario testing
     * - Edge Case Tests: Boundary condition testing
     * - Performance Tests: Load and stress testing
     */
}