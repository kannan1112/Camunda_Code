package com.camundaSaas.BankingCustomerOnboarding;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.camundaSaas.BankingCustomerOnboarding.model.AddressModel;
import com.camundaSaas.BankingCustomerOnboarding.model.CustomerDetailsModel;
import com.camundaSaas.BankingCustomerOnboarding.model.EmialValidationModel;
import com.camundaSaas.BankingCustomerOnboarding.model.PersonalDetails;
import com.camundaSaas.BankingCustomerOnboarding.util.BankingCustomerUtilities;

import io.camunda.tasklist.CamundaTaskListClient;

@Configuration
public class CutomerDetailsConfig {

	@Bean
	public CustomerDetailsModel customerDetailsModel() {

		return new CustomerDetailsModel();
	}
	// BankingCustomerUtilities bankingCustomerUtilities;

	/*
	 * @Bean public CamundaTaskListClient camundaTaskListClient() {
	 * 
	 * return new CamundaTaskListClient(); }
	 */

//	@Bean
//	public BankingCustomerUtilities bankingCustomerUtilities() {
//
//		return new BankingCustomerUtilities();
//	}

	@Bean
	public AddressModel addressModel() {

		return new AddressModel();
	}

	@Bean
	public PersonalDetails personalDetails() {

		return new PersonalDetails();
	}

	@Bean
	public EmialValidationModel emialValidationModel() {

		return new EmialValidationModel();
	}

}
