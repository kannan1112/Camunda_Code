package com.camundaSaas.BankingCustomerOnboarding.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class EmialValidationModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username;
	@Override
	public String toString() {
		return "EmialValidationModel [id=" + id + ", username=" + username + ", email=" + email + ", password="
				+ password + "]";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private String email;
	private String password;
//	private int phoneNo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

//	public int getPhoneNo() {
//		return phoneNo;
//	}
//
//	public void setPhoneNo(int phoneNo) {
//		this.phoneNo = phoneNo;
//	}

	

}
