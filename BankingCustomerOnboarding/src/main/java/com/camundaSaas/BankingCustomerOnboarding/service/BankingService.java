package com.camundaSaas.BankingCustomerOnboarding.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.camundaSaas.BankingCustomerOnboarding.model.AddressModel;
import com.camundaSaas.BankingCustomerOnboarding.model.CustomerDetailsModel;
import com.camundaSaas.BankingCustomerOnboarding.model.EmialValidationModel;
import com.camundaSaas.BankingCustomerOnboarding.model.PersonalDetails;
import com.camundaSaas.BankingCustomerOnboarding.repo.AddressRepo;
import com.camundaSaas.BankingCustomerOnboarding.repo.BankingRepo;
import com.camundaSaas.BankingCustomerOnboarding.repo.EmailValidationRepo;
import com.camundaSaas.BankingCustomerOnboarding.repo.PersonalRepo;

@Service
public class BankingService {
	
	@Autowired
	BankingRepo bankingRepo;
	
	@Autowired
	AddressRepo addressRepo;
	
	@Autowired
	PersonalRepo personalRepo;
	
	@Autowired
	EmailValidationRepo emailValidationRepo;
	

	
//	@Autowired
//    public BankingService(BankingRepo bankingRepo, AddressRepo addressRepo) {
//        this.bankingRepo = bankingRepo;
//        this.addressRepo = addressRepo;
//    }
	
	
	public CustomerDetailsModel saveCustomerDetails(CustomerDetailsModel customerDetails) {
		return bankingRepo.save(customerDetails);
		
		
	}


	public AddressModel saveAddress(AddressModel addresss) {
		
		
		
		return addressRepo.save(addresss);
		// TODO Auto-generated method stub
		
	}


	public PersonalDetails savePersonalDetails(PersonalDetails personalDetails) {
		return personalRepo.save(personalDetails);
		
	}


//	public boolean isUserValid(String email, String password) {
//		EmialValidationModel EmialValid = emailValidationRepo.findByEmail(email);
//		
//		if (EmialValid == null) {
//			return false;
//		}
//		return BCrypt.checkpw(password, EmialValid.getPassword());
//	}

	
	
	/////////////////////////////

	public EmialValidationModel saveEmailValidation(EmialValidationModel emialValidationModel) {
		
		return emailValidationRepo.save(emialValidationModel);
		
		
		
		
	}
	
	
	public boolean validateCredentials(String email, String password) {
		EmialValidationModel user = emailValidationRepo.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return true; // Credentials are valid
        }
        return false; // Credentials are invalid
    }


	
////////////////////////////////// kafka ////////////////////////////////
	
//	 private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
//
//	    @Autowired
//	    public BankingService(KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
//	        this.kafkaTemplate = kafkaTemplate;
//	    }
//
//	    public void publishMessageToKafkaTopic() {
//	        Map<String, Object> dataMap = new HashMap<>();
//	        dataMap.put("key1", "value1");
//	        dataMap.put("key2", 123);
//	        // Add other key-value pairs as needed
//
//	        kafkaTemplate.send("helloTopic", dataMap);
//	    }

	
	

}
