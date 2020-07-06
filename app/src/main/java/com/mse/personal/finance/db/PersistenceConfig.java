package com.mse.personal.finance.db;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Marker class for configuring Spring Boot JPA capabilities.
 *
 * @author T. Dossev
 */
@Configuration
@EnableJpaRepositories
@EnableJpaAuditing
public class PersistenceConfig {
	
}