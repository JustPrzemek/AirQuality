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
		System.out.println("PM10 Prediction Service started on http://localhost:8080");
		System.out.println("Endpoint: POST /api/pm10/predictions");
		System.out.println("Health check: GET /api/pm10/health");
	}

	@Bean
	public CommandLineRunner initApplication(AirQualityService airQualityService) {
		return args -> {
			airQualityService.initCsvDirectory();
			airQualityService.checkForNewFiles();
		};
	}

}
