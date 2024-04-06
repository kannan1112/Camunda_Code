package com.example.clientMaintenance.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.clientMaintenance.model.ClientDetails;
import com.example.clientMaintenance.model.VendorDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;

@RestController
public class ClientMaintenanceController {

	@Autowired
	ZeebeClient client;

	final RestTemplate rest = new RestTemplate();

	@CrossOrigin
	@RequestMapping(value = "/getDetails", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public Map<String, Object> orderFulfillment(@RequestBody ClientDetails input) throws Exception {

		System.out.println("Input from UI : " + input);

		UUID correlationKey = UUID.randomUUID();

//		client.newPublishMessageCommand().messageName("Message_14pe2vg").correlationKey("correlationKey")
//				.variables(input).send();

		ProcessInstanceResult workflowInstanceResult = null;
	
		
		
		
			workflowInstanceResult =  ((CreateProcessInstanceCommandStep3) client.newCreateInstanceCommand()
				.bpmnProcessId("clientMaintenance").latestVersion().variables(input)).withResult().send().join();
		
			String bpmnProcessId	= workflowInstanceResult.getBpmnProcessId();
			Long processDefinitionKey= workflowInstanceResult.getProcessDefinitionKey();
			Long processInstanceKey=	workflowInstanceResult.getProcessInstanceKey();
			
		Map<String, Object> variableMap = workflowInstanceResult.getVariablesAsMap();
		System.out.println("variableMap : "+ variableMap);
		long s =  workflowInstanceResult.getProcessDefinitionKey();
		System.out.println(s);
		
		variableMap.put("bpmnProcessId", bpmnProcessId);
		variableMap.put("processDefinitionKey", processDefinitionKey);
		variableMap.put("processInstanceKey", processInstanceKey);
//		client.newCreateInstanceCommand().bpmnProcessId("clientMaintenance").latestVersion().variables(input).send()
//				.join();

		return variableMap;

//client.newCreateInstanceCommand().bpmnProcessId("Process_0110jws").latestVersion().variables(input).send().join();

		// client.newSetVariablesCommand(Long.valueOf().variables(input).local(true).send().join();

	}
	
	

////////////////////////////get Active Task - zeebe-record_job////////////////////////

	@GetMapping("/getActiveTask")
	@CrossOrigin
	public List<Object> getActiveTaskDetails() throws StreamReadException, DatabindException, IOException {

		HttpHeaders headers = new HttpHeaders();
// headers.setContentType(MediaType.APPLICATION_JSON);
// String str = "{}";
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

		String strQuery = "http://localhost:9200/zeebe-record_process-instance_8.0.2_2023-01-10/_search";

		String strQueryTbd = "http://localhost:9200/zeebe-record_process-instance*/_search";

		ResponseEntity<Map> resp = rest.exchange(strQueryTbd, HttpMethod.POST, requestEntity, Map.class);

		Map body = resp.getBody();

		Map hitsMap = (Map) body.get("hits");

		List<Object> hits = (List<Object>) hitsMap.get("hits");

		List<Object> finalresponse = new ArrayList<Object>();

		for (Object gethits : hits) {

			Map getSource = (Map) gethits;

			Map getsou = (Map) getSource.get("_source");

			Map getValue = (Map) getsou.get("value");

			Long getprocessDefinitionKey = (Long) getValue.get("processDefinitionKey");

			Long getprocessInstanceKey = (Long) getValue.get("processInstanceKey");

			String getbpmnProcessId = (String) getValue.get("bpmnProcessId");

			String getIntent = (String) getsou.get("intent");

			getValue.put("intent", getIntent);

			Map<String, Object> activeTaskDetails = new HashMap();

			activeTaskDetails.put("processDefinitionKey", getprocessDefinitionKey);
			activeTaskDetails.put("processInstanceKey", getprocessInstanceKey);
			activeTaskDetails.put("bpmnProcessId", getbpmnProcessId);

			activeTaskDetails.put("Intent", getIntent);

			finalresponse.add(activeTaskDetails);

		}

		return finalresponse;

	}

//////////////////////////////get assignee ////////////////////////

	@GetMapping("/getAssignee")
	@CrossOrigin
	public List<Object> getVariableFromOperator() throws JsonMappingException, JsonProcessingException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

		ResponseEntity<Map> resp = rest.exchange("http://localhost:9200/zeebe-record_job_8.0.2_2022-12-19/_search",
				HttpMethod.GET, requestEntity, Map.class, headers);

		Map body = resp.getBody();

		System.out.println("body : " + body);

		Map hitsMap = (Map) body.get("hits");

		List<Object> hits = (List<Object>) hitsMap.get("hits");

		List<Object> finalResponse = new ArrayList();
		List<Object> finalResponse2 = new ArrayList();

		for (Object gethits : hits) {

			Map getSource = (Map) gethits;

			Map source = (Map) getSource.get("_source");

			String getintent = (String) source.get("intent");

			if (getintent.equals("CREATED")) {

				System.out.println("getintent : " + getintent);

				Map getValue = (Map) source.get("value");

				Map getcustomHeaders = (Map) getValue.get("customHeaders");

				Long getprocessDefinitionKey = (Long) getValue.get("processDefinitionKey");
				Long getprocessInstanceKey = (Long) getValue.get("processInstanceKey");
				String getbpmnProcessId = (String) getValue.get("bpmnProcessId");
				String assignee = (String) getcustomHeaders.get("io.camunda.zeebe:assignee");
				Long getelementInstanceKey = (Long) getValue.get("elementInstanceKey");

				System.out.println("elementInstanceKey : " + getelementInstanceKey);

				Map<String, Object> getAssigneedetails = new HashMap();

				getAssigneedetails.put("processDefinitionKey", getprocessDefinitionKey);
				getAssigneedetails.put("processInstanceKey", getprocessInstanceKey);
				getAssigneedetails.put("bpmnProcessId", getbpmnProcessId);
				getAssigneedetails.put("assignee", assignee);
				getAssigneedetails.put("key", source.get("key").toString());
				System.out.println("kanna..........." + source.get("key").toString());
				getAssigneedetails.put("elementInstanceKey", getelementInstanceKey);
				getAssigneedetails.put("intent", getintent);

				finalResponse.add(getAssigneedetails);
			}
		}

		return finalResponse;

	}

//////////////////////////// get vendor details in Ui /////////////////////////

	@GetMapping("/getInstanceId/{instanceId}/{jobkey}")
	@CrossOrigin

	public Map<String, Object> getInstanceData(@PathVariable Long instanceId, @PathVariable Long jobkey)
			throws JsonMappingException, JsonProcessingException {
		Map<String, Object> getVariableMap = new HashMap<String, Object>();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// String str = "{\"query\":{\"term\":{\"value.processInstanceKey\":" +
		// instanceId + "}}}";

		String str = "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"value.processInstanceKey\":" + instanceId
				+ "}},{\"term\":{\"key\":" + jobkey + "}}]}}}";

		HttpEntity<String> requestEntity = new HttpEntity<String>(str, headers);

		System.out.println("Given Query :- " + str);

		ResponseEntity<String> s = rest.exchange("http://localhost:9200/zeebe-record_job*/_search", HttpMethod.POST,
				requestEntity, String.class, headers);

		String body = s.getBody();

		System.out.println(body);

		ObjectMapper obj = new ObjectMapper();

		Map rej = obj.readValue(body, Map.class);

		Map hitmap = (Map) rej.get("hits");

		List<Object> hit = (List<Object>) hitmap.get("hits");

		// List<Object> finalList = new ArrayList<Object>();

		for (Object getDetails : hit) {

			Map getSource = (Map) getDetails;
			Map getValue = (Map) getSource.get("_source");
			String getIntent = (String) getValue.get("intent");

			if (getIntent.equals("COMPLETED")) {

				Map getData = (Map) getValue.get("value");

				Map getVariab = (Map) getData.get("variables");

				System.out.println("getVariables : " + getVariab);
				getData.put("intent", getIntent);

				String getfname = (String) getVariab.get("fname");
				String getlname = (String) getVariab.get("lname");
				String getaddress = (String) getVariab.get("address");
				String getphone = (String) getVariab.get("phone");
//				String getcompanyName = (String) getVariab.get("companyName");
//				String getcompanyWebsite = (String) getVariab.get("companyWebsite");
//				String getlandlineNumber = (String) getVariab.get("landlineNumber");
//				String getemail = (String) getVariab.get("email");

				getVariableMap.put("fname", getfname);
				getVariableMap.put("lname", getlname);
				getVariableMap.put("address", getaddress);
				getVariableMap.put("phone", getphone);

//				getVariableMap.put("companyName", getcompanyName);
//				getVariableMap.put("companyWebsite", getcompanyWebsite);
//				getVariableMap.put("landlineNumber", getlandlineNumber);
//				getVariableMap.put("email", getemail);

				// finalList.add(getVariableMap);
			}

		}
		return getVariableMap;

	}

/////////////////////////////////////////////////////////

	@GetMapping("/{getkey}/{decision}")
	public String toatlAmount(@PathVariable Long getkey, @PathVariable String decision) {

		if (decision.equals("aprroved")) {

			return "aprroved";
		} else {

			return "rejected";
		}

	}

/////////////////////////////Multi User Task Complete //////////////////////////////////

	@GetMapping("/completedUserTask/{decision}")
	public String getvariable(@PathVariable String decision) {

		HashMap<String, Object> map = new HashMap<>();
		map.put("decision", decision);

		client.newCreateInstanceCommand().bpmnProcessId("exclusiveWithString").latestVersion().variables(map).send();
		return "Flow Started";

	}

//////////////////////////////Complete Task /////////////////////////////

//	@PostMapping("/taskCompleted/{taskId}")

	@CrossOrigin
	@RequestMapping(value = "/taskCompleted/{taskId}", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public void completedTask(@RequestBody Map dataVendor, @PathVariable long taskId) {

		System.out.println("Complete Task.....: " + taskId);

		System.out.println("Decision : " + dataVendor.toString());

//		String completeTaskStatus = runtimeService.completeTask(taskId, dataVendor);
//
//		System.out.println("completeTaskStatus...:" + completeTaskStatus);

		// client.newCompleteCommand(dataVendor).variables("").send();

		client.newCompleteCommand(taskId).variables(dataVendor).send().join();

	}

	///////////////////////////// TaskList Task //////////////////////////
	@CrossOrigin
	@GetMapping("/getTasklistTask")

	// @RequestMapping(value = "/getTasklistTask", method = RequestMethod.POST,
	// headers = "Accept=*/*", produces = "application/json", consumes =
	// "application/json")

	public List<Object> getTaskList() {

//		final String url = "http://localhost:9999/getActiveTask";
//
//		String variableFromAnotherApi = rest.getForObject(url, String.class);
//		
//		System.out.println("variableFromAnotherApi : " + variableFromAnotherApi);
//		
//		Map<String,Object> stre = new HashMap<String, Object>();
//		
////		stre.put("variableFromAnotherApi", variableFromAnotherApi);
////		System.out.println("Map stre : "+ stre);
//		
//		//List<Object> getApiList = (List<Object>) variableFromAnotherApi.get
//		List<Object> finalli = new ArrayList<Object>();
//		
//		
//	
//		
////		String assigne = variableFromAnotherApi.get("Intent");
////		
//		List<Object> getanotherapiList =(List<Object>) stre.get("Intent");
//		
//		System.out.println(getanotherapiList);
//		

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

		ResponseEntity<Map> str = rest.exchange("http://localhost:9200/tasklist-task-1.3.0_/_search", HttpMethod.GET,
				requestEntity, Map.class, headers);

		Map body = str.getBody();

		Map getHits = (Map) body.get("hits");

		List<Object> getHitsList = (List<Object>) getHits.get("hits");

		List<Object> finalResponse = new ArrayList<Object>();

		for (Object hitList : getHitsList) {

			Map getHitListAsMap = (Map) hitList;

			Map getSource = (Map) getHitListAsMap.get("_source");

			String bpmnProcessId = (String) getSource.get("bpmnProcessId");
			String processDefinitionId = (String) getSource.get("processDefinitionId");
			String processInstanceId = (String) getSource.get("processInstanceId");
			String creationTime = (String) getSource.get("creationTime");
			String state = (String) getSource.get("state");
			String assignee = (String) getSource.get("assignee");
			Long getKey = (Long) getSource.get("key");

			Map<String, Object> taskDetails = new HashMap<String, Object>();

			taskDetails.put("bpmnProcessId", bpmnProcessId);
			taskDetails.put("processDefinitionId", processDefinitionId);
			taskDetails.put("processInstanceId", processInstanceId);
			taskDetails.put("creationTime", creationTime);
			taskDetails.put("state", state);
			taskDetails.put("assignee", assignee);
			taskDetails.put("key", getKey);

			finalResponse.add(taskDetails);
		}

		return finalResponse;

	}

////////////////// Log in ///////////////////////

	@CrossOrigin
	// @GetMapping("/vendorcompany/validateUserIdPassword/{userId}/{userPwd}")

	@RequestMapping(value = "/vendorcompany/validateUserIdPassword/{userId}/{userPwd}", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")

	public void loginvalidate(@PathVariable String userId, @PathVariable String userPwd) {

		System.out.println("userId : " + userId);
		System.out.println("userPwd :" + userPwd);
	}

//////////////////////// User Task with multhi query //////////////////

	@GetMapping("/getUserTask")
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

//		ResponseEntity<String> s = rest.exchange("http://localhost:9200/zeebe-record_job_8.0.2_2023-01-11/_search",
//				HttpMethod.GET, requestEntity, String.class, headers);

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

//////////////////////////////

	@GetMapping("/getUserTaskVariable/{instanceId}")
	@CrossOrigin

	public Map<String, String> getUserTaskVariable(@PathVariable Long instanceId)
			throws JsonMappingException, JsonProcessingException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// String str = "{\"query\":{\"term\":{\"value.processInstanceKey\":" +
		// instanceId + "}}}";

		// "'{\"query\":{\"bool\":{\"must\":[{\"term\":{\"value.type\":\"io.camunda.zeebe:userTask\"}},{\"term\":{\"value.processInstanceKey\":"+
		// instanceId +"}}]}}}'";

		String str = "{\"query\":{\"term\":{\"value.processInstanceKey\":" + instanceId + "}}}";

		HttpEntity<String> requestEntity = new HttpEntity<String>(str, headers);

		System.out.println("Given Query :- " + str);

		// String strQueryTbd = "http://localhost:9200/zeebe-record_job*/_search";

//		ResponseEntity<String> s = rest.exchange("http://localhost:9200/zeebe-record_job_8.0.2_2023-01-11/_search",
//				HttpMethod.GET, requestEntity, String.class, headers);

		ResponseEntity<String> s = rest.exchange("http://localhost:9200/zeebe-record_variable*/_search",
				HttpMethod.POST, requestEntity, String.class, headers);

		String body = s.getBody();

		System.out.println(body);

		ObjectMapper obj = new ObjectMapper();

		Map rej = obj.readValue(body, Map.class);

		Map hitmap = (Map) rej.get("hits");

		List<Object> hit = (List<Object>) hitmap.get("hits");

		List<Object> finalResponse = new ArrayList<Object>();

		Map<String, String> taskVarMap = new HashMap<String, String>();

		for (Object getDetails : hit) {

			Map getSource = (Map) getDetails;
			Map getValue = (Map) getSource.get("_source");
			String getIntent = (String) getValue.get("intent");

			if (getIntent.equals("CREATED")) {

				Map getData = (Map) getValue.get("value");

				String varName = (String) getData.get("name");
				String varValue = (String) getData.get("value");

				System.out.println(varValue);
				varValue = varValue.replaceAll("\"", "");
				System.out.println(varValue);

				// taskDetails.put("name", varName);
				taskVarMap.put(varName, varValue);

				// finalResponse.add(taskDetails);

			}

		}
		return taskVarMap;
	}

}
