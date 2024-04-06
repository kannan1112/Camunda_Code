package com.camundaSaas.BankingCustomerOnboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.camundaSaas.BankingCustomerOnboarding.model.AddressModel;

@Repository
public interface AddressRepo extends JpaRepository<AddressModel, Integer>{

	//AddressModel save(AddressModel addresss);


	

}
