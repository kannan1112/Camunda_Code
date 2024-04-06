package com.camundaSaas.BankingCustomerOnboarding.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SaasAuthentication;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;

@Component
public class BankingCustomerUtilities {

	final RestTemplate restTemplate = new RestTemplate();
//	
	@Value("${camunda-saas-Authentication-Key}")
	private String saasAuthenticationKey;

	@Value("${camunda-saas-Authentication-Secret}")
	private String saasAuthenticationSecret;

	@Value("${camunda-saas-taskList-Url}")
	private String taskListUrls;

	/*
	 * String taskListUrls =
	 * "https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8";
	 * 
	 * String saasAuthenticationKey= "s~_Hmwz0.zz6CnGGAfdlqFRfhU-3~fQQ";
	 * 
	 * String saasAuthenticationSecret =
	 * ".gvmCoyae7a_rB9o.vjq7RGLOK7kgEqAocqt.WWk4AmHJgtsRo0wBfOoNuWDXC5h";
	 */

	public CamundaTaskListClient getTaskListClient(String environment) {
		CamundaTaskListClient taskListClient = null;
		if ("SAAS".equalsIgnoreCase(environment)) {

			System.out.println("Saas code executed");

			SaasAuthentication sa = new SaasAuthentication("s~_Hmwz0.zz6CnGGAfdlqFRfhU-3~fQQ",
					".gvmCoyae7a_rB9o.vjq7RGLOK7kgEqAocqt.WWk4AmHJgtsRo0wBfOoNuWDXC5h");
			System.out.println("Token---------------------" + sa);
//			SaasAuthentication sa = new SaasAuthentication(saasAuthenticationKey,
//					saasAuthenticationSecret);
//			System.out.println("Token---------------------"+sa);

			try {

				taskListClient = new CamundaTaskListClient.Builder()
						.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
						.shouldReturnVariables().authentication(sa).build();

			} catch (TaskListException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return taskListClient;
			}
		} else {
			System.out.println("Self Managed code executed");

			SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");

			// CamundaTaskListClient taskListClient = null;

			try {
				taskListClient = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
						.shouldReturnVariables().authentication(sa).build();
			} catch (TaskListException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// return taskListClient;

		}
		return taskListClient;

	}

///////////////////////////////////////////////////////////////////////////////

//	public Map<String, Object> getNextTaskForm(String taskListUrl) throws JsonMappingException, JsonProcessingException {
//
//		String activeUrl = taskListUrl;
//System.out.println("activeUrl - "+ activeUrl);
//		HttpHeaders headers = new HttpHeaders();  
//		headers.setContentType(MediaType.APPLICATION_JSON); 
//		headers.set("environment", "saas");
//		// Create a request entity with the request body and headers     
//		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//		
//		
//		
//		//ResponseEntity<List> getActiveTaskList = restTemplate.getForEntity(activeUrl, List.class);
//		 ResponseEntity<List> response = restTemplate.exchange(
//				 activeUrl,
//		            HttpMethod.GET,
//		            requestEntity,
//		            List.class
//		    );
//		 List activeTaskList = response.getBody();
//		 
//		 System.out.println("activeTaskList---- "+ activeTaskList);
//		 
//		 
//		 Map<String,Object> taskVariableMap = new HashMap();
//		for(Object activeTask : activeTaskList) {
//			
//			Map activeTaskMap = (Map)activeTask;
//			
//			 Long elementInstanceKey = (Long)activeTaskMap.get("elementInstanceKey");
//			
//			 Long jobKey = (Long)activeTaskMap.get("jobKey");
//			
//		
//		 
////		 Long elementInstanceKey = (Long)activeTaskMap.get("elementInstanceKey");
////		 activeTaskMap.remove("elementInstanceKey");
////		 Long jobKey = (Long)activeTaskMap.get("jobKey");
////
////		 
//		HttpHeaders header = new HttpHeaders();
//		header.setContentType(MediaType.APPLICATION_JSON);
//
//		String operateVariableUrl ="{\"query\":{\"term\":{\"scopeKey\":" + elementInstanceKey +"}}}";
//
//		HttpEntity<String> reqEntity = new HttpEntity<String>(operateVariableUrl, header);
//
//		ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:9200/operate-variable-1.1.0_*/_search",
//				HttpMethod.POST, requestEntity, String.class, header);
//		String body = responseEntity.getBody();
//
//		System.out.println(body);
//
//		ObjectMapper obj = new ObjectMapper();
//
//		Map responseMap = obj.readValue(body, Map.class);
//		
//		
//		Map getHitsMap = (Map) responseMap.get("hits");
//
//		
//
//		
//		List hitsVariableList = (List) getHitsMap.get("hits");
//
//		for (Object hitsVariable : hitsVariableList) {
//		
//			Map hitsVariableMap = (Map) hitsVariable;
//			
//						Map sourceMap = (Map) hitsVariableMap.get("_source");
//			
//						String name = (String) sourceMap.get("name");
//						
//						if(name.equalsIgnoreCase("localTaskName")) {
//						
//						String getValue = (String) sourceMap.get("value");
//			
//						String value = getValue.replaceAll("\\\"", "");
//			
//						taskVariableMap.put(name, value);
//						
//						}
//		
//		
//	}
//		taskVariableMap.put("jobKey", jobKey);
//		
//	}
//		return taskVariableMap;
//}
//	
	public String getActiveTaskIdForTaskName(String taskListUrl, String taskName) throws JsonMappingException, JsonProcessingException {

		String activeUrl = taskListUrl;
		activeUrl = "http://localhost:8080/getActivedTaskList";
		System.out.println("activeUrl - "+ activeUrl);
		HttpHeaders headers = new HttpHeaders();  
		headers.setContentType(MediaType.APPLICATION_JSON); 
		// Create a request entity with the request body and headers     
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		String taskId = "";
		
		
		
		//ResponseEntity<List> getActiveTaskList = restTemplate.getForEntity(activeUrl, List.class);
		 ResponseEntity<List> response = restTemplate.exchange(
				 activeUrl,
		            HttpMethod.GET,
		            requestEntity,
		            List.class
		    );
		 
		 List activeTaskList = (List)response.getBody();
		 
		 for(Object taskDto: activeTaskList) {
			 
			System.out.println(taskDto);
			
			Map taskDtoMap = (Map) taskDto;
			
			taskId = (String)taskDtoMap.get("id");
			System.out.println("taskId...:"+taskId);
			 
			 
			 
		 }
		 
		 return taskId;
		
	}

}