package com.example.clientMaintenance.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import com.example.clientMaintenance.model.ClientDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
public class ClientMaintenanceWorker {

	@Autowired
	ZeebeClient client;

	RestTemplate rest = new RestTemplate();

//	@ZeebeWorker(name = "requestDataFromUI", type = "requestDataFromUI")
//	public void inputChangesMailToApprover(final JobClient client, final ActivatedJob job) {
//
//		Map<String, Object> variableMap = (Map<String, Object>) job.getVariablesAsMap();
//
////	Object d =	variableMap.get("naveen");
////		
////		variableMap.put("naveen", d);
//
//		long processesInstanceKey = job.getProcessInstanceKey();
//		long jobKey = job.getKey();
//
//		System.out.println("processesInstanceKey : " + processesInstanceKey);
//		System.out.println("JobKey : "+jobKey);
//
//		System.out.println("variableMap : " + variableMap);
//
//		client.newCompleteCommand(job.getKey()).variables(variableMap).send().join();
//
//	}
	
	
	
/////////////////////////Service Task /////////////////////
	
	
	@ZeebeWorker(name = "approvalMail", type = "approvalMail")
	public void approveMail(final JobClient client, final ActivatedJob job) {
		
		System.out.println("Approve Mail send Successfully");
		
		client.newCompleteCommand(job.getKey()).variables("").send().join();
	}
	
	
	
	@ZeebeWorker(name = "rejectedMail", type = "rejectedMail")
	public void rejectMail(final JobClient client, final ActivatedJob job) {
		
		System.out.println("Rejected Mail send Successfully");
		
		client.newCompleteCommand(job.getKey()).variables("").send().join();
	}
	
	
	
	

//////////////////////// user Task - aprove/reject /////////////////

//	@ZeebeWorker(name = "approver", type = "io.camunda.zeebe:userTask")
//	public void userTaskComplete(final JobClient client, final ActivatedJob job) {
//
//		System.out.println("UserTask Flow started");
//
//		Map<String, Object> variableMap = job.getVariablesAsMap();
//
//		System.out.println("user Task variableMap : " + variableMap);
//
//		String getDecision = (String) job.getVariablesAsMap().get("decision");
//
//		System.out.println("decision : " + getDecision);
//
//		Long getJobKey = job.getKey();
//
//		String url = "http://localhost:9999/" + getJobKey + "/" + getDecision;
//
//		System.out.println(url);
//
//		Map<String, Object> map = job.getVariablesAsMap();
//
//		System.out.println("map : " + map);
//
//		String response = rest.getForObject(url, String.class);
//
//		map.put("total", response);
//
//		System.out.println("map : " + map);
//		System.out.println("naveen");
//
//		client.newCompleteCommand(getJobKey).variables(map).send();
//
//	}

}
