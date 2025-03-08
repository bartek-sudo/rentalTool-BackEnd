@ApplicationModule(
        allowedDependencies = {"shared"},
        type = ApplicationModule.Type.CLOSED,
        displayName = "user"
)

package com.example.rentalTool_BackEnd.user;

import org.springframework.modulith.ApplicationModule;