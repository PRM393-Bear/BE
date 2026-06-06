package com.example.PRM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PrmApplication {

	public static void main(String[] args) {
		System.out.println("=== SERVER STARTED ===");
		SpringApplication.run(PrmApplication.class, args);
	}

}
