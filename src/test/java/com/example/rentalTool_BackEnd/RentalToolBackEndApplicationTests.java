package com.example.rentalTool_BackEnd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class RentalToolBackEndApplicationTests {

	@Test
	void contextLoads() {
		var modules = ApplicationModules.of(RentalToolBackEndApplication.class);

		for (var m : modules) {
			System.out.print("module: " + m.getDisplayName() + " : " + m.getBasePackage());
		}

		modules.verify();
	}

}
