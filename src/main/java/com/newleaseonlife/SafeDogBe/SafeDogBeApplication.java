package com.newleaseonlife.SafeDogBe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SafeDogBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafeDogBeApplication.class, args);
	}

}
