package com.example.clearstreet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ClearStreetApplication implements CommandLineRunner {

	static void main(String[] args) {
		SpringApplication.run(ClearStreetApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("ClearStreet Application started.");
	}
}

