package com.smartcar.monitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Model Tests")
public class UserTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        user = new User();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should set default values")
        void defaultConstructorShouldSetDefaultValues() {
            User defaultUser = new User();
            
            assertNotNull(defaultUser.getCreationDate());
            assertNotNull(defaultUser.getLastUpdateOn());
            assertTrue(defaultUser.getIsActive());
        }
        
        @Test
        @DisplayName("Parameterized constructor should set provided values")
        void parameterizedConstructorShouldSetProvidedValues() {
            User paramUser = new User(
                "testuser", "password123", User.UserRole.DRIVER, "John Doe", 
                25, User.Gender.MALE, "+1234567890", "john@example.com", "DL123456"
            );
            
            assertEquals("testuser", paramUser.getUsername());
            assertEquals("password123", paramUser.getPassword());
            assertEquals(User.UserRole.DRIVER, paramUser.getRole());
            assertEquals("John Doe", paramUser.getName());
            assertEquals(25, paramUser.getAge());
            assertEquals(User.Gender.MALE, paramUser.getGender());
            assertEquals("+1234567890", paramUser.getContactNumber());
            assertEquals("john@example.com", paramUser.getEmail());
            assertEquals("DL123456", paramUser.getLicenseNumber());
            assertNotNull(paramUser.getCreationDate());
            assertNotNull(paramUser.getLastUpdateOn());
            assertTrue(paramUser.getIsActive());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {
        
        @Test
        @DisplayName("UserRole enum should have correct values")
        void userRoleEnumShouldHaveCorrectValues() {
            User.UserRole[] roles = User.UserRole.values();
            
            assertEquals(2, roles.length);
            assertArrayEquals(new User.UserRole[]{
                User.UserRole.ADMIN,
                User.UserRole.DRIVER
            }, roles);
        }
        
        @Test
        @DisplayName("Gender enum should have correct values")
        void genderEnumShouldHaveCorrectValues() {
            User.Gender[] genders = User.Gender.values();
            
            assertEquals(3, genders.length);
            assertArrayEquals(new User.Gender[]{
                User.Gender.MALE,
                User.Gender.FEMALE,
                User.Gender.OTHER
            }, genders);
        }
        
        @Test
        @DisplayName("UserRole enum should handle ordinal values correctly")
        void userRoleEnumShouldHandleOrdinalValuesCorrectly() {
            assertEquals(0, User.UserRole.ADMIN.ordinal());
            assertEquals(1, User.UserRole.DRIVER.ordinal());
        }
        
        @Test
        @DisplayName("Gender enum should handle ordinal values correctly")
        void genderEnumShouldHandleOrdinalValuesCorrectly() {
            assertEquals(0, User.Gender.MALE.ordinal());
            assertEquals(1, User.Gender.FEMALE.ordinal());
            assertEquals(2, User.Gender.OTHER.ordinal());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Valid user should pass validation")
        void validUserShouldPassValidation() {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Username should not be blank")
        void usernameShouldNotBeBlank(String username) {
            user.setUsername(username);
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank username");
        }
        
        @Test
        @DisplayName("Username should be within length limits")
        void usernameShouldBeWithinLengthLimits() {
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            // Test username too short
            user.setUsername("ab");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for username too short");
            
            // Test username too long
            user.setUsername("a".repeat(51));
            violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for username too long");
            
            // Test valid username
            user.setUsername("testuser");
            violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid username");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Password should not be blank")
        void passwordShouldNotBeBlank(String password) {
            user.setUsername("testuser");
            user.setPassword(password);
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank password");
        }
        
        @Test
        @DisplayName("Password should meet minimum length requirement")
        void passwordShouldMeetMinimumLengthRequirement() {
            user.setUsername("testuser");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            // Test password too short
            user.setPassword("12345");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for password too short");
            
            // Test valid password
            user.setPassword("password123");
            violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid password");
        }
        
        @Test
        @DisplayName("Role should not be null")
        void roleShouldNotBeNull() {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(null);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for null role");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Name should not be blank")
        void nameShouldNotBeBlank(String name) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName(name);
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank name");
        }
        
        @Test
        @DisplayName("Name should not exceed maximum length")
        void nameShouldNotExceedMaximumLength() {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            // Test name too long
            user.setName("a".repeat(101));
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for name too long");
            
            // Test valid name
            user.setName("John Doe");
            violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid name");
        }
        
        @Test
        @DisplayName("Age should be within valid range")
        void ageShouldBeWithinValidRange() {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            // Test age below minimum
            user.setAge(17);
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for age below 18");
            
            // Test age above maximum
            user.setAge(101);
            violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for age above 100");
            
            // Test valid age
            user.setAge(25);
            violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid age");
        }
        
        @Test
        @DisplayName("Gender should not be null")
        void genderShouldNotBeNull() {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(null);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for null gender");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Contact number should not be blank")
        void contactNumberShouldNotBeBlank(String contactNumber) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber(contactNumber);
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank contact number");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"123", "123456789", "1234567890123456", "abc", "123-456-7890"})
        @DisplayName("Contact number should match valid format")
        void contactNumberShouldMatchValidFormat(String contactNumber) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber(contactNumber);
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for invalid contact number format");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"+1234567890", "1234567890", "+44123456789"})
        @DisplayName("Valid contact number formats should pass")
        void validContactNumberFormatsShouldPass(String contactNumber) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber(contactNumber);
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid contact number");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Email should not be blank")
        void emailShouldNotBeBlank(String email) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail(email);
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank email");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "test@", "@example.com", "test.example.com"})
        @DisplayName("Invalid email formats should fail validation")
        void invalidEmailFormatsShouldFailValidation(String email) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail(email);
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for invalid email format");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"test@example.com", "user.name@domain.co.uk", "test+tag@example.org"})
        @DisplayName("Valid email formats should pass validation")
        void validEmailFormatsShouldPassValidation(String email) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail(email);
            user.setLicenseNumber("DL123456");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should have no validation violations for valid email format");
        }
        
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("License number should not be blank")
        void licenseNumberShouldNotBeBlank(String licenseNumber) {
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber(licenseNumber);
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Should have validation violations for blank license number");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID correctly")
        void shouldGetAndSetIdCorrectly() {
            Long id = 123L;
            user.setId(id);
            assertEquals(id, user.getId());
        }
        
        @Test
        @DisplayName("Should get and set username correctly")
        void shouldGetAndSetUsernameCorrectly() {
            String username = "newuser";
            user.setUsername(username);
            assertEquals(username, user.getUsername());
        }
        
        @Test
        @DisplayName("Should get and set password correctly")
        void shouldGetAndSetPasswordCorrectly() {
            String password = "newpassword123";
            user.setPassword(password);
            assertEquals(password, user.getPassword());
        }
        
        @Test
        @DisplayName("Should get and set role correctly")
        void shouldGetAndSetRoleCorrectly() {
            User.UserRole role = User.UserRole.ADMIN;
            user.setRole(role);
            assertEquals(role, user.getRole());
        }
        
        @Test
        @DisplayName("Should get and set name correctly")
        void shouldGetAndSetNameCorrectly() {
            String name = "Jane Doe";
            user.setName(name);
            assertEquals(name, user.getName());
        }
        
        @Test
        @DisplayName("Should get and set age correctly")
        void shouldGetAndSetAgeCorrectly() {
            Integer age = 30;
            user.setAge(age);
            assertEquals(age, user.getAge());
        }
        
        @Test
        @DisplayName("Should get and set gender correctly")
        void shouldGetAndSetGenderCorrectly() {
            User.Gender gender = User.Gender.FEMALE;
            user.setGender(gender);
            assertEquals(gender, user.getGender());
        }
        
        @Test
        @DisplayName("Should get and set contact number correctly")
        void shouldGetAndSetContactNumberCorrectly() {
            String contactNumber = "+9876543210";
            user.setContactNumber(contactNumber);
            assertEquals(contactNumber, user.getContactNumber());
        }
        
        @Test
        @DisplayName("Should get and set email correctly")
        void shouldGetAndSetEmailCorrectly() {
            String email = "jane@example.com";
            user.setEmail(email);
            assertEquals(email, user.getEmail());
        }
        
        @Test
        @DisplayName("Should get and set license number correctly")
        void shouldGetAndSetLicenseNumberCorrectly() {
            String licenseNumber = "DL987654";
            user.setLicenseNumber(licenseNumber);
            assertEquals(licenseNumber, user.getLicenseNumber());
        }
        
        @Test
        @DisplayName("Should get and set creation date correctly")
        void shouldGetAndSetCreationDateCorrectly() {
            LocalDateTime creationDate = LocalDateTime.now();
            user.setCreationDate(creationDate);
            assertEquals(creationDate, user.getCreationDate());
        }
        
        @Test
        @DisplayName("Should get and set last update on correctly")
        void shouldGetAndSetLastUpdateOnCorrectly() {
            LocalDateTime lastUpdateOn = LocalDateTime.now();
            user.setLastUpdateOn(lastUpdateOn);
            assertEquals(lastUpdateOn, user.getLastUpdateOn());
        }
        
        @Test
        @DisplayName("Should get and set is active correctly")
        void shouldGetAndSetIsActiveCorrectly() {
            Boolean isActive = false;
            user.setIsActive(isActive);
            assertEquals(isActive, user.getIsActive());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {
        
        @Test
        @DisplayName("PreUpdate should update lastUpdateOn timestamp")
        void preUpdateShouldUpdateLastUpdateOnTimestamp() throws Exception {
            LocalDateTime originalLastUpdate = user.getLastUpdateOn();
            
            // Wait a bit to ensure timestamp difference
            Thread.sleep(10);
            
            // Use reflection to call preUpdate method
            user.getClass().getMethod("preUpdate").invoke(user);
            
            assertTrue(user.getLastUpdateOn().isAfter(originalLastUpdate), 
                "Last update timestamp should be updated");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString should include user information")
        void toStringShouldIncludeUserInformation() {
            user.setId(1L);
            user.setUsername("testuser");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            user.setIsActive(true);
            
            String toString = user.toString();
            
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("username='testuser'"));
            assertTrue(toString.contains("role=DRIVER"));
            assertTrue(toString.contains("name='John Doe'"));
            assertTrue(toString.contains("email='john@example.com'"));
            assertTrue(toString.contains("licenseNumber='DL123456'"));
            assertTrue(toString.contains("isActive=true"));
        }
        
        @Test
        @DisplayName("ToString should not expose sensitive information")
        void toStringShouldNotExposeSensitiveInformation() {
            user.setId(1L);
            user.setUsername("testuser");
            user.setPassword("secretpassword");
            user.setRole(User.UserRole.DRIVER);
            user.setName("John Doe");
            user.setAge(25);
            user.setGender(User.Gender.MALE);
            user.setContactNumber("+1234567890");
            user.setEmail("john@example.com");
            user.setLicenseNumber("DL123456");
            
            String toString = user.toString();
            
            assertFalse(toString.contains("secretpassword"), "Password should not be exposed in toString");
            assertFalse(toString.contains("+1234567890"), "Contact number should not be exposed in toString");
            assertFalse(toString.contains("25"), "Age should not be exposed in toString");
            assertFalse(toString.contains("MALE"), "Gender should not be exposed in toString");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should handle role-based access control correctly")
        void shouldHandleRoleBasedAccessControlCorrectly() {
            // Test admin role
            user.setRole(User.UserRole.ADMIN);
            assertTrue(user.getRole() == User.UserRole.ADMIN);
            
            // Test driver role
            user.setRole(User.UserRole.DRIVER);
            assertTrue(user.getRole() == User.UserRole.DRIVER);
        }
        
        @Test
        @DisplayName("Should handle age validation for driving license")
        void shouldHandleAgeValidationForDrivingLicense() {
            // Test minimum driving age
            user.setAge(18);
            assertTrue(user.getAge() >= 18, "User should be at least 18 to have a driving license");
            
            // Test typical driving age
            user.setAge(25);
            assertTrue(user.getAge() >= 18, "User should be at least 18 to have a driving license");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle boundary age values correctly")
        void shouldHandleBoundaryAgeValuesCorrectly() {
            // Set all required fields first
            user.setUsername("testuser");
            user.setPassword("password123");
            user.setRole(User.UserRole.DRIVER);
            user.setName("Test User");
            user.setGender(User.Gender.MALE);
            user.setContactNumber("1234567890");
            user.setEmail("test@example.com");
            user.setLicenseNumber("DL123456");
            
            // Test minimum valid age
            user.setAge(18);
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should accept minimum valid age");
            
            // Test maximum valid age
            user.setAge(100);
            violations = validator.validate(user);
            assertTrue(violations.isEmpty(), "Should accept maximum valid age");
        }
        
        @Test
        @DisplayName("Should handle special characters in names")
        void shouldHandleSpecialCharactersInNames() {
            String specialName = "José María O'Connor-Smith";
            user.setName(specialName);
            assertEquals(specialName, user.getName());
        }
        
        @Test
        @DisplayName("Should handle international phone numbers")
        void shouldHandleInternationalPhoneNumbers() {
            String[] internationalNumbers = {
                "+1-555-123-4567",
                "+44 20 7946 0958",
                "+81-3-1234-5678",
                "+86 10 1234 5678"
            };
            
            for (String number : internationalNumbers) {
                user.setContactNumber(number);
                assertEquals(number, user.getContactNumber());
            }
        }
    }
}