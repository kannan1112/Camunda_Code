package com.camundaSaas.BankingCustomerOnboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.spring.client.EnableZeebeClient;

@SpringBootApplication
@EnableZeebeClient
public class BankingCustomerOnboardingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingCustomerOnboardingApplication.class, args);
	}

}
