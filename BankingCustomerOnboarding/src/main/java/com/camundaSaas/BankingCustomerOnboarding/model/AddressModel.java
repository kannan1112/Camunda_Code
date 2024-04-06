package com.camundaSaas.BankingCustomerOnboarding.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
public class AddressModel {
	@Override
	public String toString() {
		return "AddressModel [Id=" + Id + ", address=" + address + "]";
	}
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int Id;
	private String address;
	
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
	
	
	
	

}
