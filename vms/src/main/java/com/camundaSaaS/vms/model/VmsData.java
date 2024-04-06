package com.camundaSaaS.vms.model;

public class VmsData {

	private String companyName;
	private String eMail;
	private String phone;
	private String password;
	private String confirmPassword;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String geteMail() {
		return eMail;
	}

	public void seteMail(String eMail) {
		this.eMail = eMail;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	@Override
	public String toString() {
		return "VmsData [companyName=" + companyName + ", eMail=" + eMail + ", phone=" + phone + ", password="
				+ password + ", confirmPassword=" + confirmPassword + "]";
	}

}
