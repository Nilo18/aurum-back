package com.aurum.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class AurumBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(AurumBackApplication.class, args);
	}

}
