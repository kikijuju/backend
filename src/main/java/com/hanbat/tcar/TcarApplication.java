package com.hanbat.tcar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class TcarApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("DB_name", dotenv.get("DB_name"));
		System.setProperty("DB_driver", dotenv.get("DB_driver"));
		System.setProperty("DB_url", dotenv.get("DB_url"));
		System.setProperty("DB_username", dotenv.get("DB_username"));
		System.setProperty("DB_password", dotenv.get("DB_password"));
		System.setProperty("JWT_secret_key", dotenv.get("JWT_secret_key"));

		SpringApplication.run(TcarApplication.class, args);
	}

}
