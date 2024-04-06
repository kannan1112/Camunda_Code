package com.camundaSaas.BankingCustomerOnboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.camundaSaas.BankingCustomerOnboarding.model.KafkaConsumerModel;

@Repository
public interface KafkaConsumerRepo extends JpaRepository<KafkaConsumerModel, Long>{

}
