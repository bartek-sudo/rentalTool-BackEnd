package com.example.rentalTool_BackEnd.modulith;

import com.example.rentalTool_BackEnd.RentalToolBackEndApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class  ModolithTests {
    static ApplicationModules modules = ApplicationModules.of(RentalToolBackEndApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
        // Dokumentacja zostanie wygenerowana w katalogu target/spring-modulith-docs
    }
}
