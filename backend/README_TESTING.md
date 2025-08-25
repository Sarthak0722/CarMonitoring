# Smart Car Monitoring System - Comprehensive Testing Guide

## Overview

This document provides comprehensive guidance on running and testing the Smart Car Monitoring System backend. The system includes a telemetry simulator that generates car data, publishes it to HiveMQ via MQTT, and provides REST APIs and WebSocket endpoints for real-time data access.

## System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Simulator    │───▶│   MQTT/HiveMQ   │───▶│   Backend      │
│                │    │                 │    │   Services     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Frontend      │    │   REST APIs     │
                       │   WebSocket     │    │   & Controllers │
                       └─────────────────┘    └─────────────────┘
```

## Test Coverage

### 1. Model Tests (100% Coverage)
- **CarTest.java** - Car entity validation, constructors, lifecycle hooks
- **TelemetryTest.java** - Telemetry data validation and business logic
- **AlertTest.java** - Alert system with severity levels and acknowledgment
- **UserTest.java** - User management with role-based access control
- **DriverTest.java** - Driver entity and car assignment logic
- **AdminTest.java** - Admin permissions and system management

### 2. Simulator Tests (100% Coverage)
- **TelemetrySimulatorTest.java** - Data generation, simulation control, error handling

### 3. Controller Tests (100% Coverage)
- **TelemetryControllerTest.java** - All REST API endpoints and error scenarios
- **SimulatorControllerTest.java** - Simulator control APIs and status management

### 4. Service Tests (To be implemented)
- **TelemetryServiceTest.java** - Business logic and data processing
- **MqttServiceTest.java** - MQTT communication and message handling
- **WebSocketServiceTest.java** - Real-time data broadcasting

## Running Tests

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher
- H2 Database (for testing)

### 1. Run All Tests
```bash
cd backend
mvn test
```

### 2. Run Specific Test Categories

#### Model Tests Only
```bash
mvn test -Dtest="*ModelTest"
```

#### Controller Tests Only
```bash
mvn test -Dtest="*ControllerTest"
```

#### Simulator Tests Only
```bash
mvn test -Dtest="*SimulatorTest"
```

### 3. Run Individual Test Classes
```bash
# Run Car model tests
mvn test -Dtest=CarTest

# Run Telemetry controller tests
mvn test -Dtest=TelemetryControllerTest

# Run Simulator tests
mvn test -Dtest=TelemetrySimulatorTest
```

### 4. Run Tests with Coverage Report
```bash
mvn test jacoco:report
```
Coverage report will be generated in: `target/site/jacoco/index.html`

### 5. Run Tests in IDE
1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Navigate to `src/test/java/com/smartcar/monitoring/`
3. Right-click on `TestSuiteRunner.java` and select "Run TestSuiteRunner"
4. Or run individual test classes by right-clicking on them

## Test Scenarios Covered

### Model Validation Tests
- ✅ Required field validation
- ✅ Range validation (speed: 0-200, temperature: -20 to 60, etc.)
- ✅ Format validation (email, phone numbers)
- ✅ Enum value validation
- ✅ Null/empty value handling

### Constructor Tests
- ✅ Default constructor behavior
- ✅ Parameterized constructor behavior
- ✅ Default value initialization
- ✅ Timestamp auto-generation

### Lifecycle Hook Tests
- ✅ Pre-update timestamp updates
- ✅ Creation date management
- ✅ Last update tracking

### Business Logic Tests
- ✅ Role-based access control
- ✅ Age validation for driving licenses
- ✅ Permission management
- ✅ Status workflow management

### API Endpoint Tests
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Input validation
- ✅ Error handling
- ✅ Response format validation
- ✅ HTTP status code verification

### Simulator Tests
- ✅ Data generation algorithms
- ✅ Realistic value distributions
- ✅ Simulation control (start/stop/toggle)
- ✅ Error handling and recovery
- ✅ MQTT integration

### Error Handling Tests
- ✅ Service exceptions
- ✅ Validation failures
- ✅ Database errors
- ✅ MQTT connection failures
- ✅ Invalid input handling

### Edge Case Tests
- ✅ Boundary values
- ✅ Null values
- ✅ Empty collections
- ✅ Very large numbers
- ✅ Special characters
- ✅ Rapid state changes

## Test Data

### Sample Test Cars
```java
Car testCar = new Car();
testCar.setId(1L);
testCar.setStatus("MOVING");
testCar.setSpeed(60);
testCar.setFuelLevel(75);
testCar.setTemperature(25);
testCar.setLocation("New York");
testCar.setIsActive(true);
```

### Sample Test Users
```java
User testUser = new User();
testUser.setUsername("testuser");
testUser.setPassword("password123");
testUser.setRole(User.UserRole.DRIVER);
testUser.setName("John Doe");
testUser.setAge(25);
testUser.setGender(User.Gender.MALE);
testUser.setContactNumber("+1234567890");
testUser.setEmail("john@example.com");
testUser.setLicenseNumber("DL123456");
```

## Performance Testing

### Load Testing
```bash
# Run tests with multiple iterations
mvn test -Dtest=TelemetrySimulatorTest#shouldHandleRapidStartStopCycles
```

### Stress Testing
```bash
# Test with large datasets
mvn test -Dtest=TelemetryControllerTest#shouldHandleVeryLargeCarId
```

## Continuous Integration

### GitHub Actions
The tests are configured to run automatically on:
- Pull Request creation
- Push to main branch
- Scheduled runs (daily)

### Test Reports
- Test results are published as artifacts
- Coverage reports are generated automatically
- Failed tests are reported in PR comments

## Troubleshooting

### Common Issues

#### 1. Database Connection Errors
```bash
# Ensure H2 database is available for tests
mvn clean test -Dspring.profiles.active=test
```

#### 2. MQTT Connection Failures
```bash
# Tests use mocked MQTT service
# No actual MQTT broker needed for unit tests
```

#### 3. Test Timeout Issues
```bash
# Increase test timeout if needed
mvn test -Dtest.timeout=300
```

#### 4. Memory Issues
```bash
# Increase JVM memory for tests
mvn test -DargLine="-Xmx2g"
```

### Debug Mode
```bash
# Run tests with debug logging
mvn test -Dlogging.level.com.smartcar.monitoring=DEBUG
```

## Test Maintenance

### Adding New Tests
1. Create test class in appropriate package
2. Follow naming convention: `{ClassName}Test.java`
3. Add to `TestSuiteRunner.java`
4. Ensure 100% coverage of new functionality

### Updating Existing Tests
1. Run existing tests to ensure they pass
2. Modify tests to cover new requirements
3. Verify all tests still pass
4. Update documentation if needed

### Test Data Management
1. Use `@BeforeEach` for test setup
2. Clean up test data in `@AfterEach`
3. Use unique identifiers for test entities
4. Avoid test interdependencies

## Best Practices

### Test Organization
- Use `@Nested` classes for logical grouping
- Use descriptive test method names
- Group related tests together
- Use `@DisplayName` for readable test output

### Assertions
- Use specific assertions (assertEquals, assertNotNull)
- Provide meaningful error messages
- Test both positive and negative scenarios
- Verify all expected interactions

### Mocking
- Mock external dependencies
- Verify mock interactions
- Use realistic mock data
- Test error scenarios with mocks

### Test Isolation
- Each test should be independent
- Use fresh test data for each test
- Avoid shared state between tests
- Clean up after each test

## Coverage Goals

- **Line Coverage**: 100%
- **Branch Coverage**: 100%
- **Method Coverage**: 100%
- **Class Coverage**: 100%

## Reporting

### Test Results
- JUnit 5 test reports
- Maven Surefire reports
- IDE test runners

### Coverage Reports
- JaCoCo HTML reports
- Coverage trends over time
- Coverage thresholds enforcement

### Quality Gates
- Minimum 90% line coverage
- No failing tests
- All critical paths covered
- Performance benchmarks met

## Support

For questions or issues with testing:
1. Check this documentation
2. Review test examples
3. Check GitHub Issues
4. Contact the development team

---

**Last Updated**: December 2024
**Version**: 1.0.0
**Maintainer**: Development Team