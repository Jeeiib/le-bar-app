package fr.lebarapp.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Point d'entrée de l'application Spring Boot : démarre le serveur et l'API.
@SpringBootApplication
public class BarAppApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarAppApiApplication.class, args);
	}

}
