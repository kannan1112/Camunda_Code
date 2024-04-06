package com.example.clientMaintenance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.spring.client.EnableZeebeClient;

@SpringBootApplication
@EnableZeebeClient
public class ClientMaintenanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientMaintenanceApplication.class, args);
	}

}
