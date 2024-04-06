package com.camundaSaas.BankingCustomerOnboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.camundaSaas.BankingCustomerOnboarding.model.PersonalDetails;

@Repository
public interface PersonalRepo  extends JpaRepository<PersonalDetails, Integer>{

}
