@ApplicationModule(
        allowedDependencies = {"shared", "tool :: spi"},
        type = ApplicationModule.Type.CLOSED,
        displayName = "reservation"
)

package com.example.rentalTool_BackEnd.reservation;

import org.springframework.modulith.ApplicationModule;