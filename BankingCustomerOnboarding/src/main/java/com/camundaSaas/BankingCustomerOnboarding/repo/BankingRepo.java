package com.camundaSaas.BankingCustomerOnboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.camundaSaas.BankingCustomerOnboarding.model.AddressModel;
import com.camundaSaas.BankingCustomerOnboarding.model.CustomerDetailsModel;

@Repository
public interface BankingRepo extends JpaRepository<CustomerDetailsModel, Integer> {

	

	

	

}
