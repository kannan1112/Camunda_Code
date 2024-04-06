package com.example.clientMaintenance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDetails {

	private String fname;
	private String lname;
	
	private String phone;
	
	private String address;
	
	private String vendorStatus;

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getVendorStatus() {
		return vendorStatus;
	}

	public void setVendorStatus(String vendorStatus) {
		this.vendorStatus = vendorStatus;
	}

	@Override
	public String toString() {
		return "VendorDetails [fname=" + fname + ", lname=" + lname + ", phone=" + phone + ", address=" + address
				+ ", vendorStatus=" + vendorStatus + "]";
	}

	
	
	
}
