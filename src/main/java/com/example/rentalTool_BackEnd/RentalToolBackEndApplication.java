package com.example.rentalTool_BackEnd;

import com.example.rentalTool_BackEnd.shared.properties.RsaKeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Modulith
@EnableCaching
@EnableAsync
@EnableConfigurationProperties({RsaKeyProperties.class})

@RequiredArgsConstructor

public class RentalToolBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalToolBackEndApplication.class, args);
	}

}
