package com.camundaSaas.BankingCustomerOnboarding.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity

public class CustomerDetailsModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	private int Id;
	private String firstName;
	private String lastName;
	private int age;
	private String gender;
	private String dob;
	private int phoneNo;

//	@Column(name = "table_a_Id")
//	private String addressMap;
//
//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "table_a_Id", referencedColumnName = "Id")
//	private AddressModel address;

	// private int addressId;
	private String address;
	private String accountType;
	private int annualIncome;
	private int creditScore;
	private String email;
	private String password;
	private String accountNo;
	private String status;

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public int getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(int phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public int getAnnualIncome() {
		return annualIncome;
	}

	public void setAnnualIncome(int annualIncome) {
		this.annualIncome = annualIncome;
	}

	public int getCreditScore() {
		return creditScore;
	}

	public void setCreditScore(int creditScore) {
		this.creditScore = creditScore;
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

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "CustomerDetailsModel [Id=" + Id + ", firstName=" + firstName + ", lastName=" + lastName + ", age=" + age
				+ ", gender=" + gender + ", dob=" + dob + ", phoneNo=" + phoneNo + ", address=" + address
				+ ", accountType=" + accountType + ", annualIncome=" + annualIncome + ", creditScore=" + creditScore
				+ ", email=" + email + ", password=" + password + ", accountNo=" + accountNo + ", status=" + status
				+ "]";
	}

}
