package com.example.bookmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookmarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookmarketApplication.class, args);
	}

}
