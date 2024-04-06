package com.camundaSaas.BankingCustomerOnboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.camundaSaas.BankingCustomerOnboarding.model.EmialValidationModel;

@Repository
public interface EmailValidationRepo extends JpaRepository<EmialValidationModel, Long>{
	
	EmialValidationModel findByEmail(String email);

}
