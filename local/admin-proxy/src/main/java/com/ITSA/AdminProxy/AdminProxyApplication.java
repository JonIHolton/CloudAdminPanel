package com.ITSA.AdminProxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AdminProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminProxyApplication.class, args);
	}
}
