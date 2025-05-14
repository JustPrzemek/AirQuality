package com.air.quality.prediction;

import com.air.quality.prediction.service.AirQualityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PredictionApplication {

	public static void main(String[] args) {
		SpringApplication.run(PredictionApplication.class, args);
	}

	@Bean
	public CommandLineRunner initApplication(AirQualityService airQualityService) {
		return args -> {
			// Initialize the CSV directory and start monitoring
			airQualityService.initCsvDirectory();
			// Trigger initial file check
			airQualityService.checkForNewFiles();
		};
	}

}
