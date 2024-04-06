  package com.camundaforms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.spring.client.EnableZeebeClient;

@SpringBootApplication
@EnableZeebeClient
public class CamundaFormsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamundaFormsApplication.class, args);
	}

}
