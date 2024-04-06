package com.camundaSaaS.vms.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.camundaSaaS.vms.model.VendorRegistration;
import com.camundaSaaS.vms.model.VmsData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SaasAuthentication;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;

@RestController
//@RequestMapping("/hello/{message}")
public class VmsController {

	@Autowired
	ZeebeClient zeebeClient;

	final RestTemplate rest = new RestTemplate();

	@PostMapping("/startWorkFlow")
	public void startWorkFlow() throws Exception {
		System.out.println("starting");

//		JSONParser jsonparser = new JSONParser();
//		Object obj = jsonparser.parse(new FileReader("D:\\FileReader\\WounderSoft\\shopaid.txt"));
//		JSONObject jsonObject = (JSONObject) obj;
//		System.out.println(jsonObject);
//
//		String str = jsonObject.toString();
//		System.out.println("String===" + str);
//		Map jsonObjMap = jsonObject;
//		System.out.println(jsonObjMap);

		zeebeClient.newCreateInstanceCommand().bpmnProcessId("VendorManagementSystem").latestVersion().send().join();

		// client.newCreateInstanceCommand().bpmnProcessId("UserTask1").latestVersion().variables(str).send().join();
		System.out.println("started");
	}

	@CrossOrigin
	@RequestMapping(value = "/startWorkFlow", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public VendorRegistration getVendorDetails(@RequestBody VendorRegistration vendorReg) {
		System.out.println("Vendor Details : " + vendorReg);
		


		zeebeClient.newCreateInstanceCommand().bpmnProcessId("VendorManagementSystem").latestVersion()
				.variables(vendorReg).send().join();
		System.out.println("flow  Started");

		return vendorReg;
	}

///////////////////////// Get Active Task List ///////////////////////////////

	@GetMapping("/getCreatedTask")
	public List<Task> getTask() throws TaskListException {

//		String inputBaseUrl="https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8";
//		String inputAuthUrl="https://login.cloud.camunda.io/oauth/token";
//		String inputClientId = "jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ";
//		String inputClientSecret = "wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf";

		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		// return client.getTasks(true, TaskState.CREATED, 50);
		return client.getTasks(true, TaskState.CREATED, 5000, true);

//		System.out.println("TaskList---: "+client.toString());
//
//		List<Task> getTaskList = client.getTasks(false, TaskState.CREATED, 50);
//		
//		 for(Object getTask : getTaskList ) {
//			 
//			 System.out.println(getTask.toString());
//			// Map getTaskAsMap = (Map) getTask;
//			 //System.out.println(getTaskAsMap);
//		 }
//		 System.out.println("TaskList---: "+getTaskList.toString());
//		
//		 return getTaskList;

//		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
//
//		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8080")
//				.shouldReturnVariables().authentication(sa).build();
//
		// List<Task> tasks = client.getAssigneeTasks(null, TaskState.CREATED, null);
//		
//		System.out.println("Active Task List : "+tasks);
//
////		 Task task = client.getTask(jobkey);

	}

//////////////////////////////////////////////////

	@CrossOrigin
	@RequestMapping(value = "/taskCompleted/{taskId}", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public String completedTask(@RequestBody Map dataVendor, @PathVariable long taskId) {

		System.out.println("Complete Task123....: " + taskId);

		System.out.println("Decision : " + dataVendor.toString());

//		String completeTaskStatus = runtimeService.completeTask(taskId, dataVendor);
//
//		System.out.println("completeTaskStatus...:" + completeTaskStatus);

		// client.newCompleteCommand(dataVendor).variables("").send();

		zeebeClient.newCompleteCommand(taskId).variables(dataVendor).send().join();
		return "Success";
	}

////////////////////////// Claim Task /////////////////////////////
	@CrossOrigin

	@GetMapping("/cliamTask/{jobKey}/{assigne}")
	public Task claimTask(@PathVariable String jobKey, @PathVariable String assigne) throws TaskListException {

		System.out.println("cliam Task");

		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
				.shouldReturnVariables().authentication(sa).build();
		Task task = client.claim(jobKey, assigne);

		return task;

	}

//////////////////////////UnClaim Task /////////////////////////////
	@CrossOrigin

	@GetMapping("/uncliamTask/{jobKey}")
	public Task unClaimTask(@PathVariable String jobKey) throws TaskListException {

		System.out.println("cliam Task");

		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
				.shouldReturnVariables().authentication(sa).build();
		Task task = client.unclaim(jobKey);

		return task;

	}

/////////////////////////////// Complete user Task /////////////////////////////////////

//	@GetMapping("/completeTask/{jokKey}")
//	public Task completeTask(@RequestBody Map mapValue, @PathVariable String jokKey) throws TaskListException {

	@CrossOrigin
	@RequestMapping(value = "/completeTask/{taskId}", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
	public Task claimTask(@RequestBody Map dataVendor, @PathVariable String taskId) throws TaskListException {

		System.out.println("Complete Task");

		System.out.println("Task Id.....: " + taskId);

		System.out.println("Decision : " + dataVendor.toString());

		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder()
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8")
				.shouldReturnVariables().authentication(sa).build();

		Task task = client.completeTask(taskId, dataVendor);
	

		return task;

	}

////////////////////////////// get individual variable /////////////////	
	@CrossOrigin
	@GetMapping("/getTaskvariable/{taskId}")
	public Task getvariabletask(@PathVariable String taskId) throws TaskListException {

		System.out.println(taskId);
		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		return client.getTask(taskId, true);

	}

///////////////////////////// Get Variables ///////////////
	@CrossOrigin
	@GetMapping("/getIndividualVariable/{taskId}")
	public List<Task> getVariable(@PathVariable String taskId) {

		System.out.println("Get Variable Method Called");

		String url = "http://localhost:8081/getTaskvariable/" + taskId;
		System.out.println("url--" + url);

		RestTemplate restTemplate = new RestTemplate();
		Map result = restTemplate.getForObject(url, Map.class);

		System.out.println("result ===" + result);

		List<Object> getVariableList = (List<Object>) result.get("variables");

		List finalVarList = new ArrayList<>();

		Map<String, String> finalVariable = new HashMap<String, String>();
		
		for (Object getVariable : getVariableList) {

			Map veriableMap = (Map) getVariable;
			String varName = (String) veriableMap.get("name");
			String varValue = "";
			System.out.println(varName);
			try {
				varValue = (String) veriableMap.get("value");
			}catch(ClassCastException ccs) {
				Integer intValue = (Integer)veriableMap.get("value");
				varValue = intValue.toString();
			}
			

			finalVariable.put(varName, varValue);

			System.out.println(finalVarList);
		}
		finalVarList.add(finalVariable);

		// System.out.println(finalVarList);

		return finalVarList;

	}

///////////////////////Get Assigned Task  ////////////////////////////

	@CrossOrigin
	@GetMapping("/getAssignedTask/{userName}")
	public List<Task> getAssignedTask(@PathVariable String userName) throws TaskListException {

		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		// return client.getTasks(true, TaskState.CREATED, 50);
		// return client.getTasks(true, TaskState.CREATED, 5000, true);
		return client.getAssigneeTasks(userName, TaskState.CREATED, 50);
		

	}

	@CrossOrigin
	@GetMapping("/assignedTaskList")
	public List<Object> getTaskList() {

//		HttpHeaders headers = new HttpHeaders();
//		HttpEntity<String> reqEntity = new HttpEntity<String>(headers);
		String url = "http://localhost:8081/getAssignedTask/murali.muthu@surgetechinc.in";

		RestTemplate restTemplate = new RestTemplate();
		List<Object> result = restTemplate.getForObject(url, List.class);

		List<Object> finalTaskList = new ArrayList<Object>();

		for (Object getList : result) {

			// System.out.println("getList--"+getList);

			Map getListAsMap = (Map) getList;

			String getId = (String) getListAsMap.get("id");
			String getName = (String) getListAsMap.get("name");
			String getAssignee = (String) getListAsMap.get("assignee");
			String getProcessDefinitionId = (String) getListAsMap.get("processDefinitionId");

			Map<String, Object> finalMap = new HashMap<String, Object>();

			finalMap.put("id", getId);
			finalMap.put("name", getName);
			finalMap.put("assignee", getAssignee);
			finalMap.put("processDefinitionId", getProcessDefinitionId);

			finalTaskList.add(finalMap);

		}
		return finalTaskList;

	}

//////////////////////////  Get Unassigned Task  //////////////////////////////////////
	@CrossOrigin
	@GetMapping("/unClaimTaskDetails")
	public List<Task> getunClaimTaskDetails() throws TaskListException {

		SaasAuthentication sa = new SaasAuthentication("jiIaOU5bGP1HJbyR3jZ.bhqsiCpTMTZZ",
				"wz0YxMw.oapyIi48t8aUrqOMXfubR9953gBuwa8cMqMG-595cyhM16wPAhNIKdJf");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
				.taskListUrl("https://bru-2.tasklist.camunda.io/1a8d8e18-4054-4bd2-afad-6f2adf8c58b8/").build();

		return client.getTasks(false, TaskState.CREATED, 50, true);

	}

	@CrossOrigin
	@GetMapping("/unclaimTaskList")
	public List<Object> unclaimTaskList() {

		String url = "http://localhost:8081/unClaimTaskDetails";
		System.out.println("url -- " + url);
		RestTemplate restTemplate = new RestTemplate();
		List<Object> result = restTemplate.getForObject(url, List.class);

		System.out.println("result" + result);

		List<Object> finalTaskList = new ArrayList<Object>();

		for (Object getList : result) {

			Map getListAsMap = (Map) getList;

			String getId = (String) getListAsMap.get("id");
			String getName = (String) getListAsMap.get("name");
			String getAssignee = (String) getListAsMap.get("assignee");
			String getProcessDefinitionId = (String) getListAsMap.get("processDefinitionId");

			Map<String, Object> finalMap = new HashMap<String, Object>();

			finalMap.put("id", getId);
			finalMap.put("name", getName);
			finalMap.put("assignee", getAssignee);
			finalMap.put("processDefinitionId", getProcessDefinitionId);

			finalTaskList.add(finalMap);

		}
		return finalTaskList;

	}

////////////////////////////// hari ///////////////////

	@CrossOrigin
	@GetMapping("/readjson")

	public Object jsonFileReader1() throws Exception {
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(new FileReader(
				"D:\\WorkSpace Eclipse\\Camunda SaaS\\vms\\src\\main\\resources\\Address.json"));
		JSONArray obj2 = (JSONArray) obj;
		String string = obj2.toString();

		ObjectMapper objectMapper = new ObjectMapper();

		List<Retailer> listCar = objectMapper.readValue(string, new TypeReference<List<Retailer>>() {
		});
		return listCar;

	}

	////////////////// Admin User :-

	@CrossOrigin
	@GetMapping("/getAdminUser")

	public Object adminUser() throws Exception {
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(new FileReader(
				"D:\\WorkSpace Eclipse\\Camunda SaaS\\vms\\src\\main\\resources\\AdminUsers.json"));
		JSONArray obj2 = (JSONArray) obj;
		return obj2;

	}

	@CrossOrigin
	@GetMapping("/loginjson")

	public List<Map<String, String>> getCredentials() throws Exception {

		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(new FileReader(
				"D:\\WorkSpace Eclipse\\Camunda SaaS\\vms\\src\\main\\resources\\users.json"));
		JSONArray obj2 = (JSONArray) obj;
		String string = obj2.toString();

		ObjectMapper objectMapper = new ObjectMapper();

		List<Map<String, String>> credentials = new ArrayList<>();
		List<VmsData> users = objectMapper.readValue(string, new TypeReference<List<VmsData>>() {

		});

		for (VmsData person : users) {
			Map<String, String> credential = new LinkedHashMap<>();
			credential.put("email", person.geteMail());
			credential.put("password", person.getPassword());
			credentials.add(credential);
		}

		return credentials;
	}

	@CrossOrigin
	@PostMapping("/users")
	public ResponseEntity<String> createEmployee(@RequestBody VmsData employee) throws Exception {

		System.out.println(employee);
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File(
				"D:\\WorkSpace Eclipse\\Camunda SaaS\\vms\\src\\main\\resources\\users.json");

		// Create the file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}
		// Read the existing data from the file into a list
		List<VmsData> employees = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			TypeReference<List<VmsData>> typeReference = new TypeReference<List<VmsData>>() {
			};
			employees = objectMapper.readValue(fileInputStream, typeReference);
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set the ID for the new employee

		// Add the new employee to the list
		employees.add(employee);

		// Write the updated data to the file
		try {
			FileWriter fileWriter = new FileWriter(file);
			objectMapper.writeValue(fileWriter, employees);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok("Employee created");
	}

	// sample API :-

	// List Json :-

//	[
//	    {
//	        "sno": "1",
//	        "retailerName": "AMAZON",
//	        "applicationStatus": "SUBSCRIBED"
//	    },
//	    {
//	        "sno": "2",
//	        "retailerName": "FLIPKART",
//	        "applicationStatus": "UNSUBSCRIBED"
//	    },
//	    {
//	        "sno": "3",
//	        "retailerName": "MEESHO",
//	        "applicationStatus": "UNSUBSCRIBED"
//	    },
//	    {
//	        "sno": "4",
//	        "retailerName": "SHOPIFY",
//	        "applicationStatus": "SUBSCRIBED"
//	    }
//	]

	@GetMapping("/getRequestBodyinListFormat")
	public List getRequestBodyinListFormat(@RequestBody List<Object> demo) {
		ObjectMapper mp = new ObjectMapper();
		List mpm = mp.convertValue(demo, List.class);
		System.out.println(mpm);
		return mpm;
	}

	// Map Json :-

//	{
//	    "companyName":"123",
//	    "eMail":"123",
//	    "phone":"6515416541",
//	    "password":"123",
//	    "confirmPassword":"123"
//	   
//	}

	// getRequestBodyinMapFormat --------------------->
	@GetMapping("/getRequestBodyinMapFormat")
	public Map getRequestBodyinMapFormat(@RequestBody Map demo) {
		ObjectMapper mp = new ObjectMapper();
		Map mpm = mp.convertValue(demo, Map.class);
		System.out.println(mpm);
		return mpm;
	}

	// getRequestBodyinStringFormat ---------------------->
	@GetMapping("/getRequestBodyinStringFormat")
	public String getRequestBodyinStringFormat(@RequestBody String demo) {
		ObjectMapper mp = new ObjectMapper();
		String mpm = mp.convertValue(demo, String.class);
		System.out.println(mpm);
		return mpm;
	}
	
	
	////////////////////////////// Kafka /////////////////////////////
	
	
	
	@Autowired
	private KafkaTemplate<String, String> kafkatemplate;
	
	@GetMapping("/{message}")
	//@GetMapping
	public String publish(@PathVariable("message") String publishMessage) {
		kafkatemplate.send("helloTopic", publishMessage);
		System.out.println("posted msg "+publishMessage);
		return "Message Published :" +publishMessage;
		
		
	}

}
