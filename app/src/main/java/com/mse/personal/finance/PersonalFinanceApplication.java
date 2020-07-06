package com.mse.personal.finance;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the PFM application.
 *
 * @author T. Dossev
 */
@SpringBootApplication
@EnableScheduling
public class PersonalFinanceApplication {

	/**
	 * Initializes the application. Uses any provided arguments to instantiate it as {@link SpringApplication}.
	 *
	 * @param args - provided command line arguments to the executable
	 */
	public static void main(String[] args) {
		SpringApplication financeApp = 
			new SpringApplicationBuilder(PersonalFinanceApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.build();
		
		financeApp.run(args);
	}
	
}