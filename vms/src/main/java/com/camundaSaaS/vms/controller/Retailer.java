package com.camundaSaaS.vms.controller;




public class Retailer {
	
	 private String sno;
	    private String retailerName;
	    private String applicationStatus;
	    
	    
	    
		public String getSno() {
			return sno;
		}



		public void setSno(String sno) {
			this.sno = sno;
		}



		public String getRetailerName() {
			return retailerName;
		}



		public void setRetailerName(String retailerName) {
			this.retailerName = retailerName;
		}



		public String getApplicationStatus() {
			return applicationStatus;
		}



		public void setApplicationStatus(String applicationStatus) {
			this.applicationStatus = applicationStatus;
		}



		@Override
		public String toString() {
			return "Retailer [sno=" + sno + ", retailerName=" + retailerName + ", applicationStatus=" + applicationStatus
					+ "]";
		}
	    

}
