package com.smartcar.monitoring.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Admin Model Tests")
public class AdminTest {

    private Validator validator;
    private Admin admin;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        user = new User();
        user.setId(1L);
        user.setUsername("testadmin");
        user.setPassword("password123");
        user.setRole(User.UserRole.ADMIN);
        user.setName("John Admin");
        user.setAge(30);
        user.setGender(User.Gender.MALE);
        user.setContactNumber("+1234567890");
        user.setEmail("admin@example.com");
        user.setLicenseNumber("DL123456");
        
        admin = new Admin();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should set default values")
        void defaultConstructorShouldSetDefaultValues() {
            Admin defaultAdmin = new Admin();
            
            assertNotNull(defaultAdmin.getCreationDate());
            assertNotNull(defaultAdmin.getLastUpdateOn());
            assertTrue(defaultAdmin.getIsActive());
        }
        
        @Test
        @DisplayName("Parameterized constructor should set provided values")
        void parameterizedConstructorShouldSetProvidedValues() {
            String permissions = "READ,WRITE,DELETE";
            Admin paramAdmin = new Admin(user, permissions);
            
            assertEquals(user, paramAdmin.getUser());
            assertEquals(permissions, paramAdmin.getPermissions());
            assertNotNull(paramAdmin.getCreationDate());
            assertNotNull(paramAdmin.getLastUpdateOn());
            assertTrue(paramAdmin.getIsActive());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Valid admin should pass validation")
        void validAdminShouldPassValidation() {
            admin.setUser(user);
            admin.setPermissions("READ,WRITE,DELETE");
            
            Set<ConstraintViolation<Admin>> violations = validator.validate(admin);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }
        
        @Test
        @DisplayName("User should not be null")
        void userShouldNotBeNull() {
            admin.setUser(null);
            admin.setPermissions("READ,WRITE,DELETE");
            
            Set<ConstraintViolation<Admin>> violations = validator.validate(admin);
            assertFalse(violations.isEmpty(), "Should have validation violations for null user");
        }
        
        @ParameterizedTest
        @NullSource
        @DisplayName("Permissions should not be null")
        void permissionsShouldNotBeNull(String permissions) {
            admin.setUser(user);
            admin.setPermissions(permissions);
            
            Set<ConstraintViolation<Admin>> violations = validator.validate(admin);
            assertFalse(violations.isEmpty(), "Should have validation violations for null permissions");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID correctly")
        void shouldGetAndSetIdCorrectly() {
            Long id = 123L;
            admin.setId(id);
            assertEquals(id, admin.getId());
        }
        
        @Test
        @DisplayName("Should get and set user correctly")
        void shouldGetAndSetUserCorrectly() {
            admin.setUser(user);
            assertEquals(user, admin.getUser());
        }
        
        @Test
        @DisplayName("Should get and set permissions correctly")
        void shouldGetAndSetPermissionsCorrectly() {
            String permissions = "READ,WRITE,DELETE,MANAGE_USERS";
            admin.setPermissions(permissions);
            assertEquals(permissions, admin.getPermissions());
        }
        
        @Test
        @DisplayName("Should get and set creation date correctly")
        void shouldGetAndSetCreationDateCorrectly() {
            LocalDateTime creationDate = LocalDateTime.now();
            admin.setCreationDate(creationDate);
            assertEquals(creationDate, admin.getCreationDate());
        }
        
        @Test
        @DisplayName("Should get and set last update on correctly")
        void shouldGetAndSetLastUpdateOnCorrectly() {
            LocalDateTime lastUpdateOn = LocalDateTime.now();
            admin.setLastUpdateOn(lastUpdateOn);
            assertEquals(lastUpdateOn, admin.getLastUpdateOn());
        }
        
        @Test
        @DisplayName("Should get and set is active correctly")
        void shouldGetAndSetIsActiveCorrectly() {
            Boolean isActive = false;
            admin.setIsActive(isActive);
            assertEquals(isActive, admin.getIsActive());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {
        
        @Test
        @DisplayName("PreUpdate should update lastUpdateOn timestamp")
        void preUpdateShouldUpdateLastUpdateOnTimestamp() throws Exception {
            LocalDateTime originalLastUpdate = admin.getLastUpdateOn();
            
            // Wait a bit to ensure timestamp difference
            Thread.sleep(10);
            
            // Use reflection to call preUpdate method
            admin.getClass().getMethod("preUpdate").invoke(admin);
            
            assertTrue(admin.getLastUpdateOn().isAfter(originalLastUpdate), 
                "Last update timestamp should be updated");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("ToString should include admin information")
        void toStringShouldIncludeAdminInformation() {
            admin.setId(1L);
            admin.setUser(user);
            admin.setPermissions("READ,WRITE,DELETE");
            admin.setIsActive(true);
            
            String toString = admin.toString();
            
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("userId=1"));
            assertTrue(toString.contains("permissions='READ,WRITE,DELETE'"));
            assertTrue(toString.contains("isActive=true"));
        }
        
        @Test
        @DisplayName("ToString should handle null user gracefully")
        void toStringShouldHandleNullUserGracefully() {
            admin.setId(1L);
            admin.setUser(null);
            
            String toString = admin.toString();
            
            assertTrue(toString.contains("userId=null"));
            assertFalse(toString.contains("NullPointerException"));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should handle permission management correctly")
        void shouldHandlePermissionManagementCorrectly() {
            // Test different permission formats
            String[] permissions = {
                "READ",
                "READ,WRITE",
                "READ,WRITE,DELETE",
                "READ,WRITE,DELETE,MANAGE_USERS",
                "FULL_ACCESS"
            };
            
            for (String permission : permissions) {
                admin.setPermissions(permission);
                assertEquals(permission, admin.getPermissions());
            }
        }
        
        @Test
        @DisplayName("Should handle admin status changes correctly")
        void shouldHandleAdminStatusChangesCorrectly() {
            // Initially active
            assertTrue(admin.getIsActive());
            
            // Deactivate admin
            admin.setIsActive(false);
            assertFalse(admin.getIsActive());
            
            // Reactivate admin
            admin.setIsActive(true);
            assertTrue(admin.getIsActive());
        }
        
        @Test
        @DisplayName("Should handle user role validation correctly")
        void shouldHandleUserRoleValidationCorrectly() {
            // Admin should have ADMIN role
            assertEquals(User.UserRole.ADMIN, user.getRole());
            
            // Set admin user
            admin.setUser(user);
            assertEquals(user, admin.getUser());
            assertEquals(User.UserRole.ADMIN, admin.getUser().getRole());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle very long permission strings")
        void shouldHandleVeryLongPermissionStrings() {
            String longPermissions = "READ,WRITE,DELETE,MANAGE_USERS,MANAGE_CARS,MANAGE_DRIVERS," +
                                   "MANAGE_TELEMETRY,MANAGE_ALERTS,MANAGE_SYSTEM,VIEW_LOGS," +
                                   "EXPORT_DATA,IMPORT_DATA,MANAGE_CONFIGURATION";
            
            admin.setPermissions(longPermissions);
            assertEquals(longPermissions, admin.getPermissions());
        }
        
        @Test
        @DisplayName("Should handle special characters in permissions")
        void shouldHandleSpecialCharactersInPermissions() {
            String specialPermissions = "READ,WRITE,DELETE,MANAGE_USERS@ADMIN,VIEW_LOGS#SYSTEM";
            
            admin.setPermissions(specialPermissions);
            assertEquals(specialPermissions, admin.getPermissions());
        }
        
        @Test
        @DisplayName("Should handle empty permission strings")
        void shouldHandleEmptyPermissionStrings() {
            String emptyPermissions = "";
            admin.setPermissions(emptyPermissions);
            assertEquals(emptyPermissions, admin.getPermissions());
        }
        
        @Test
        @DisplayName("Should handle timestamp precision correctly")
        void shouldHandleTimestampPrecisionCorrectly() {
            LocalDateTime now = LocalDateTime.now();
            admin.setCreationDate(now);
            admin.setLastUpdateOn(now);
            
            assertEquals(now, admin.getCreationDate());
            assertEquals(now, admin.getLastUpdateOn());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        
        @Test
        @DisplayName("Should not expose sensitive user information in toString")
        void shouldNotExposeSensitiveUserInformationInToString() {
            admin.setId(1L);
            admin.setUser(user);
            admin.setPermissions("READ,WRITE,DELETE");
            
            String toString = admin.toString();
            
            // Should not expose password, contact number, email, etc.
            assertFalse(toString.contains("password123"), "Password should not be exposed");
            assertFalse(toString.contains("+1234567890"), "Contact number should not be exposed");
            assertFalse(toString.contains("admin@example.com"), "Email should not be exposed");
            assertFalse(toString.contains("DL123456"), "License number should not be exposed");
        }
        
        @Test
        @DisplayName("Should handle permission validation correctly")
        void shouldHandlePermissionValidationCorrectly() {
            // Test various permission formats
            String[] validPermissions = {
                "READ",
                "READ,WRITE",
                "READ,WRITE,DELETE",
                "ADMIN"
            };
            
            for (String permission : validPermissions) {
                admin.setPermissions(permission);
                assertNotNull(admin.getPermissions());
                assertEquals(permission, admin.getPermissions());
            }
        }
    }
}