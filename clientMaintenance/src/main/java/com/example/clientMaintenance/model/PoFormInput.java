package com.example.clientMaintenance.model;

public class PoFormInput {
	private String name;
	private String item;
	private String quantity;
	private String email;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public String getQuantity() {
		return quantity;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String toString() {
		return "PoFormInput [name=" + name + ", item=" + item + ", quantity=" + quantity + ", email=" + email + "]";
	}
	
	

}
