package com.example.clientMaintenance.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.clientMaintenance.model.ClientDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.netty.util.Timeout;

@RestController
public class NewController {

	@Value("${camunda-usertask-timeout-param}")
	private Long timeout;
	
	@Autowired
	ZeebeClient client;
	
	

	final RestTemplate rest = new RestTemplate();

	@CrossOrigin
	@RequestMapping(value = "/startWorkFlow", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public Map<String, Object> orderFulfillment(@RequestBody ClientDetails input) throws Exception {

		System.out.println("Input from UI : " + input);

		UUID correlationKey = UUID.randomUUID();
		

//		client.newPublishMessageCommand().messageName("Message_14pe2vg").correlationKey("correlationKey")
//				.variables(input).send();

		//ProcessInstanceResult workflowInstanceResult = null;
		
		

		ProcessInstanceResult	processInstance = ((CreateProcessInstanceCommandStep3) client.newCreateInstanceCommand()
				.bpmnProcessId("clientMaintenance").latestVersion().variables(input).requestTimeout(Duration.ofMillis(timeout))).withResult().send().join();

//		client.newCreateInstanceCommand().bpmnProcessId("clientMaintenance").latestVersion().variables(input).send()
//				.join();
				
				

		return processInstance.getVariablesAsMap();
	}

////////////////////////User Task with multhi query //////////////////

	@GetMapping("/getActiveUserTask")
	@CrossOrigin

	public List<Object> getUserTaskdetails() throws JsonMappingException, JsonProcessingException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

// String str = "{\"query\":{\"term\":{\"value.processInstanceKey\":" +
// instanceId + "}}}";

// "'{\"query\":{\"bool\":{\"must\":[{\"term\":{\"value.type\":\"io.camunda.zeebe:userTask\"}},{\"term\":{\"value.processInstanceKey\":"+
// instanceId +"}}]}}}'";

		String str = "{\"query\":{\"term\":{\"value.type\":\"io.camunda.zeebe:userTask\"}}}";

		HttpEntity<String> requestEntity = new HttpEntity<String>(str, headers);

		System.out.println("Given Query :- " + str);

// String strQueryTbd = "http://localhost:9200/zeebe-record_job*/_search";

//ResponseEntity<String> s = rest.exchange("http://localhost:9200/zeebe-record_job_8.0.2_2023-01-11/_search",
//HttpMethod.GET, requestEntity, String.class, headers);

		ResponseEntity<String> s = rest.exchange("http://localhost:9200/zeebe-record_job*/_search", HttpMethod.POST,
				requestEntity, String.class, headers);

		String body = s.getBody();

		System.out.println(body);

		ObjectMapper obj = new ObjectMapper();

		Map rej = obj.readValue(body, Map.class);

		Map hitmap = (Map) rej.get("hits");

		List<Object> hit = (List<Object>) hitmap.get("hits");

		List<Object> finalResponse = new ArrayList<Object>();

		for (Object getDetails : hit) {

			Map getSource = (Map) getDetails;
			Map getValue = (Map) getSource.get("_source");
			String getIntent = (String) getValue.get("intent");

			if (getIntent.equals("CREATED")) {

				Map getData = (Map) getValue.get("value");

				String type = (String) getData.get("type");

				Map getcustomHeaders = (Map) getData.get("customHeaders");

				String bpmnProcessId = (String) getData.get("bpmnProcessId");
				Long processDefinitionId = (Long) getData.get("processDefinitionKey");
				Long processInstanceId = (Long) getData.get("processInstanceKey");
				String assignee = (String) getcustomHeaders.get("io.camunda.zeebe:assignee");
				Long getKey = (Long) getValue.get("key");

				Map<String, Object> taskDetails = new HashMap<String, Object>();

				taskDetails.put("bpmnProcessId", bpmnProcessId);
				taskDetails.put("processDefinitionId", processDefinitionId);
				taskDetails.put("processInstanceId", processInstanceId);
				taskDetails.put("assignee", assignee);
				taskDetails.put("key", getKey);

				finalResponse.add(taskDetails);

			}

		}
		return finalResponse;
	}

	///////////////////// complete user task /////////////////////////

	@CrossOrigin
	@RequestMapping(value = "/Completed/{taskId}", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public void completedTask(@RequestBody Map dataVendor, @PathVariable long taskId) {

		System.out.println("Complete Task.....: " + taskId);

		System.out.println("Decision : " + dataVendor.toString());

		client.newCompleteCommand(taskId).variables(dataVendor).send().join();

	}

}
