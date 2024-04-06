package com.example.clientMaintenance.model;

public class LoginForm {
	
	private String Username;
	private String password;
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "LoginForm [Username=" + Username + ", password=" + password + "]";
	}
	
	
	

}
