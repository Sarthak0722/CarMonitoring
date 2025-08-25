package com.smartcar.monitoring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SmartCarMonitoringApplicationTests {

	@Test
	void applicationClassCanBeInstantiated() {
		// Test that the main application class can be instantiated
		assertDoesNotThrow(() -> {
			SmartCarMonitoringApplication app = new SmartCarMonitoringApplication();
			assertNotNull(app);
		});
	}

	@Test
	void mainMethodExists() {
		// Test that the main method exists and can be called
		assertDoesNotThrow(() -> {
			// Verify the main method exists by checking the class
			Class<?> appClass = SmartCarMonitoringApplication.class;
			assertNotNull(appClass.getMethod("main", String[].class));
		});
	}
}
