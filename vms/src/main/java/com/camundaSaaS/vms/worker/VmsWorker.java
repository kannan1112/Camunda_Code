package com.camundaSaaS.vms.worker;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
public class VmsWorker {
	
	
	@Autowired
	ZeebeClient zeebeClient;
	
	
	final RestTemplate rest = new RestTemplate();
	
	@ZeebeWorker(type ="approved" , name = "approved")
	public void approvedMail(final JobClient client, final ActivatedJob job ) {
		
		System.out.println("Approved worker started");
		
		//String variableMap = job.getVariables();
		//System.out.println(variableMap);
		
		
		//final Topology topology = zeebeClient.newTopologyRequest().send().join();
		
		
		
		
		client.newCompleteCommand(job.getKey()).variables("").send().join();
		
		
		
		System.out.println("approved flow end");
		
	}
	
	
	@ZeebeWorker(type ="rejected" , name = "rejected")
	public void rejectedMail(final JobClient client, final ActivatedJob job ) {
		
		System.out.println("rejected worker started");
		
		client.newCompleteCommand(job.getKey()).variables("").send().join();
		
		System.out.println("rejected flow end");
	
	}
	
	
	
	@ZeebeWorker(type="postClarification" , name ="postClarification")
	public void clarificationMail(final JobClient client, final ActivatedJob job) {
		
		System.out.println("Clarification worker started");
		
	
		
		client.newCompleteCommand(job.getKey()).variables("").send().join();
		
	}
	
}
