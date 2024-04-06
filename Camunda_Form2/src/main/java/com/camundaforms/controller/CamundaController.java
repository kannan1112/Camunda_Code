package com.camundaforms.controller;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.TaskService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@RestController
@RequestMapping("/camunda")
public class CamundaController {

	@Autowired
	ZeebeClient zeebeClient;

	String url = "http://localhost:9200/_search";

	ObjectMapper mapper = new ObjectMapper();
	RestTemplate rest = new RestTemplate();

	@GetMapping("/getALLAct")
	public List<Variable> getvariableForm() throws TaskListException {
		CamundaTaskListClient client = null;
		String assigneeId = "demo";
		String selfmanagedtaskListUrls = "http://localhost:8083";
		SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
		client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls).shouldReturnVariables()
				.authentication(simpleAuthentication).build();

		List<Task> tasks = client.getTasks(true, TaskState.CREATED, 50, true);

		List<Variable> variables = null;
		for (Task ref : tasks) {
			variables = ref.getVariables();
		}

		System.out.println("this is the variable :---->" + variables);

		return variables;

	}

	// Form API :

	// Create an API for the form
	// get process Instance Id :
	// get form id :

	// The API :

	@GetMapping("/form-formId/{formID}")
	public Map getFormByFormId(@PathVariable String formID) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestBody = "{\r\n" + "    \"query\": {\r\n" + "        \"bool\": {\r\n"
				+ "            \"must\": [\r\n" + "                {\r\n" + "                    \"term\": {\r\n"
				+ "                        \"value.type\": \"io.camunda.zeebe:userTask\"\r\n"
				+ "                    }\r\n" + "                },\r\n" + "                {\r\n"
				+ "                    \"term\": {\r\n"
				+ "                        \"value.processInstanceKey\": 2251799813877410\r\n"
				+ "                    }\r\n" + "                }\r\n" + "            ]\r\n" + "        }\r\n"
				+ "    }\r\n" + "}";
		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
		String string = responseEntity.toString();
		Map body = responseEntity.getBody();
		Map hits = (Map) body.get("hits");
		List hitsObj = (List) hits.get("hits");
		String formKey = null;
		Map getvariables = null;
		for (Object ref : hitsObj) {
			Map mp = (Map) ref;
			Map sourceObj = (Map) mp.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			Map customHeadersObj = (Map) valueObj.get("customHeaders");
			formKey = (String) customHeadersObj.get("io.camunda.zeebe:formKey");
			if (formKey != null && formKey.equals(formID)) {
				getvariables = (Map) valueObj.get("variables");
				System.out.println("getvariables" + getvariables);
			}
		}
		return getvariables;

	}

	// get by processInstance key :

	@GetMapping("/form-instanceId/{instanceId}")
	public Map getFormByInstanceKey(@PathVariable String instanceId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestBody = "{\r\n" + "    \"query\": {\r\n" + "        \"bool\": {\r\n"
				+ "            \"must\": [\r\n" + "                {\r\n" + "                    \"term\": {\r\n"
				+ "                        \"value.type\": \"io.camunda.zeebe:userTask\"\r\n"
				+ "                    }\r\n" + "                },\r\n" + "                {\r\n"
				+ "                    \"term\": {\r\n" + "                        \"value.processInstanceKey\": "
				+ instanceId + "\r\n" + "                    }\r\n" + "                }\r\n" + "            ]\r\n"
				+ "        }\r\n" + "    }\r\n" + "}";
		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
		Map resBody = responseEntity.getBody();
		return resBody;

	}

	// Here, this is the plan :

	// In zeebe record this link having the resource feild it is called as
	// XML. to convert byte to XML in java code.
	// http://localhost:9200/zeebe*/_search

	// API for the the xml file :

	@GetMapping("/demodemo/{processDefinitionKey}")
	public String demo(@PathVariable String processDefinitionKey) {
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:9200/zeebe*/_search";
		ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
		Map resBody = responseEntity.getBody();
		Map hitsObj = (Map) resBody.get("hits");
		List hitsObjList = (List) hitsObj.get("hits");
		String xmlString = null;
		String resourceObj = null;
		Map mp = null;
		List ls = new ArrayList<>();
		for (Object ref : hitsObjList) {
			mp = (Map) ref;
			Map sourceObj = (Map) mp.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			resourceObj = (String) valueObj.get("resource");
			if (resourceObj != null) {
				long pDefinitionKey = (long) valueObj.get("processDefinitionKey");
				String refs = String.valueOf(pDefinitionKey);
				if (refs.equals(processDefinitionKey)) {
					ls.add(resourceObj);
					// convert XML :
					byte[] decodedBytes = Base64.getDecoder().decode(resourceObj);
					// Convert byte array to string
					xmlString = new String(decodedBytes, StandardCharsets.UTF_8);

				}
				// ls.add(mp);
			}
		}
		return xmlString;
	}

	// This is clone one previous :
	// I am passing process definition id & form id to get the json content :

	// It is working fine & Orgin code :

	@GetMapping("/camundaForm/{proDefinitionKey}")
	public Map camundaForm(@PathVariable String proDefinitionKey) {
		Map camundaFormsMap = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:9200/zeebe*/_search";
		ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
		Map resBody = responseEntity.getBody();
		Map hitsObj = (Map) resBody.get("hits");
		List hitsObjList = (List) hitsObj.get("hits");
		String xmlString = null;
		String resourceObj = null;
		Map mp = null;
		List ls = new ArrayList<>();
		for (Object ref : hitsObjList) {
			mp = (Map) ref;
			Map sourceObj = (Map) mp.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			resourceObj = (String) valueObj.get("resource");
			if (resourceObj != null) {
				long pDefinitionKey = (long) valueObj.get("processDefinitionKey");
				String refs = String.valueOf(pDefinitionKey);
				if (refs.equals(proDefinitionKey)) {
					ls.add(resourceObj);
					byte[] decodedBytes = Base64.getDecoder().decode(resourceObj);
					xmlString = new String(decodedBytes, StandardCharsets.UTF_8);
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document document = builder.parse(new InputSource(new StringReader(xmlString)));
						NodeList userTaskForms = document.getElementsByTagName("zeebe:userTaskForm");
						for (int i = 0; i < userTaskForms.getLength(); i++) {
							Element userTaskFormElement = (Element) userTaskForms.item(i);
							String userTaskFormId = userTaskFormElement.getAttribute("id");
							String jsonContent = userTaskFormElement.getTextContent();
							JsonNode jsonNode = objectMapper.readTree(jsonContent);
							JsonNode componentsNode = jsonNode.path("components");
							List lss = new ArrayList<>();
							for (JsonNode componentNode : componentsNode) {
								Map mpp = new HashMap<>();
								mpp.put("Label", componentNode.path("label").asText());
								mpp.put("Type", componentNode.path("type").asText());
								mpp.put("ID", componentNode.path("id").asText());
								mpp.put("Key", componentNode.path("key").asText());
								lss.add(mpp);
							}
							camundaFormsMap.put(userTaskFormId, lss);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return camundaFormsMap;
	}

	// this is clone previous one :

	@GetMapping("/camundaFormInstanceKey/{proDefinitionKey}")
	public Map camundaFormByInstanceKey(@PathVariable String proDefinitionKey) {
		Map camundaFormsMap = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:9200/zeebe*/_search";
		ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
		Map resBody = responseEntity.getBody();
		Map hitsObj = (Map) resBody.get("hits");
		List hitsObjList = (List) hitsObj.get("hits");
		String xmlString = null;
		String resourceObj = null;
		Map mp = null;
		List ls = new ArrayList<>();
		for (Object ref : hitsObjList) {
			mp = (Map) ref;
			Map sourceObj = (Map) mp.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			resourceObj = (String) valueObj.get("resource");
			if (resourceObj != null) {
				long pDefinitionKey = (long) valueObj.get("processDefinitionKey");
				String refs = String.valueOf(pDefinitionKey);
				if (refs.equals(proDefinitionKey)) {
					ls.add(resourceObj);
					byte[] decodedBytes = Base64.getDecoder().decode(resourceObj);
					xmlString = new String(decodedBytes, StandardCharsets.UTF_8);
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document document = builder.parse(new InputSource(new StringReader(xmlString)));
						NodeList userTaskForms = document.getElementsByTagName("zeebe:userTaskForm");
						for (int i = 0; i < userTaskForms.getLength(); i++) {
							Element userTaskFormElement = (Element) userTaskForms.item(i);
							String userTaskFormId = userTaskFormElement.getAttribute("id");
							String jsonContent = userTaskFormElement.getTextContent();
							JsonNode jsonNode = objectMapper.readTree(jsonContent);
							JsonNode componentsNode = jsonNode.path("components");
							List lss = new ArrayList<>();
							for (JsonNode componentNode : componentsNode) {
								Map mpp = new HashMap<>();
								mpp.put("Label", componentNode.path("label").asText());
								mpp.put("Type", componentNode.path("type").asText());
								mpp.put("ID", componentNode.path("id").asText());
								mpp.put("Key", componentNode.path("key").asText());
								lss.add(mpp);
							}
							camundaFormsMap.put(userTaskFormId, lss);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return camundaFormsMap;
	}
	// * This API is currently use : API ----1.

	// @RequestMapping(value = "/startWorkflow", method = RequestMethod.POST,
	// headers = "Accept=*/*", produces = "application/json", consumes =
	// "application/json")
	@CrossOrigin
	@PostMapping("/startWorkflow")
	public long startWorkflow(@RequestBody String var) {

		ProcessInstanceEvent processInstEvent = zeebeClient.newCreateInstanceCommand().bpmnProcessId("FormUser")
				.latestVersion().variables(var).send().join();

		long processInstanceKey = processInstEvent.getProcessInstanceKey();

		return processInstanceKey;

	}

	// get taskID api from zeebe records :
// will continue after this :
	@GetMapping("/getTaskID/{proInstanceKey}")
	public long getTaskID(@PathVariable String proInstanceKey) {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate rest = new RestTemplate();
		Map camundaFormsMap = new HashMap<>();
		String zeebeRecordURL = "http://localhost:9200/zeebe*/_search";
		ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
		Map resBody = responseEntity.getBody();
		Map zeebeHitsObj = (Map) resBody.get("hits");
		List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
//		String zeebeResourceObj = null;
		Map zeebemp = null;
		String processInstanceKey = "";
		long key = 0;
		for (Object zeeberef : zeebeHitsObjList) {
			zeebemp = (Map) zeeberef;
			Map zeebeSourceObj = (Map) zeebemp.get("_source");
			Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
			System.out.println("this is zeebeValueObj" + zeebeValueObj);
//			zeebeResourceObj = (String) zeebeValueObj.get("resource");

			processInstanceKey = (String) zeebeValueObj.get("processInstanceKey");

			if (processInstanceKey != null) {
				long processInstance = (long) zeebeValueObj.get("processInstanceKey");
				String proInstKey = String.valueOf(processInstance);
				if (proInstanceKey.equals(proInstKey)) {
					key = (long) zeebeSourceObj.get("key");
					// }

				}
			}
//			String proInstKey = String.valueOf(processInstanceKey);
			// if(proInstKey != null) {
//				if(proInstanceKey.equals(proInstKey)) {
//					key=(long)zeebeSourceObj.get("key");
//				//}
//				
//			}

		}
		return key;
	}

	// get active task from tasklist :

	@GetMapping("/getActiveTask")
	public List<Task> getUser() throws TaskListException {

		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		List<Task> tasks = client.getTasks(false, TaskState.CREATED, 50);
		List ls = new ArrayList<>();
		String id = null;
		String name = null;
		String processName = null;
		String formKey = null;

		Map mp;
		for (Task ref : tasks) {
			mp = new HashMap<>();
			id = ref.getId();
			name = ref.getName();
			processName = ref.getProcessName();
			formKey = ref.getFormKey();

			mp.put("Taskid", id);
			mp.put("Taskname", name);
			mp.put("processName", processName);
			mp.put("formKeyid", formKey);
			ls.add(mp);
		}
		return tasks;
//		return ls ;
	}

	// *Send To Rajesh
	@GetMapping("/getALLActiveTask1")
	public List<Task> getALLActiveTask1() throws TaskListException {
		CamundaTaskListClient client = null;
		List<Task> assigned = null;
		List<Task> UnAssigned = null;
		List finalList = new ArrayList<>();
		
		String assigneeId = "demo";
		String selfmanagedtaskListUrls = "http://localhost:8083";
		SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
		client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls).shouldReturnVariables()
				.authentication(simpleAuthentication).build();
		assigned = client.getTasks(false, TaskState.CREATED, 50, true);
		UnAssigned = client.getTasks(true, TaskState.CREATED, 50, true);
		finalList.add(assigned);
		finalList.add(UnAssigned);
		
		
		return finalList;
	}
// completed
	// very important api for tasklist must be backup :

	// passing instance key & getting active tasklist :

	// get task ID :

	@GetMapping("/getActiveTaskID/{procesInstanceID}")
	public long getActiveTaskID(@PathVariable String procesInstanceID) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate rest = new RestTemplate();
		Map camundaFormsMap = new HashMap<>();
		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
		Map resBody = responseEntity.getBody();

		Map zeebeHitsObj = (Map) resBody.get("hits");
		List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
		Map zeebemp = null;
		String processInstanceKey = "";
		long TaskID = 0;
		for (Object zeeberef : zeebeHitsObjList) {
			zeebemp = (Map) zeeberef;
			Map zeebeSourceObj = (Map) zeebemp.get("_source");
			Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
			System.out.println("this is zeebeValueObj" + zeebeValueObj);
			long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
			processInstanceKey = String.valueOf(procInstanceKey);
			if (processInstanceKey.equals(procesInstanceID)) {
				TaskID = (long) zeebeSourceObj.get("key");
			}
		}

		// i want active task full details from task list. tasklist having only full
		// details :

		return TaskID;
	}

	// get ALL ACTIVE Task List :
	@GetMapping("/getActiveTaskList")
	public String getActiveTaskList() throws TaskListException {

		CamundaTaskListClient client = null;
		String assigneeId = "demo";
		String selfmanagedtaskListUrls = "http://localhost:8083";
		SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
		client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls).shouldReturnVariables()
				.authentication(simpleAuthentication).build();
		List<Task> tasks = client.getTasks(true, TaskState.CREATED, 50, true);
		System.out.println("tasks" + tasks);
		List<Variable> variables = null;
		String id = null;

		for (Task ref : tasks) {
			id = ref.getId();
			System.out.println("this is taskid" + id);
			variables = ref.getVariables();
		}
		return id;
	}

//		// Assignee check :
//		@GetMapping("/getALLActiveTaskAssigneeCheck")
//		public List<Task> getALLActiveTaskAssigneeCheck() throws TaskListException {
//			CamundaTaskListClient client = null;
//			String assigneeId = "demo";
//			String selfmanagedtaskListUrls = "http://localhost:8083";
//			SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
//			client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls).shouldReturnVariables()
//					.authentication(simpleAuthentication).build();
//			List<Task> assignedTask = client.getTasks(true, TaskState.CREATED, 50, true);
//			List<Task> unAssignedTask = client.getTasks(false, TaskState.CREATED, 50, true);
//			
//			if(assignedTask!=null) {
//				for (Task ref : assignedTask) {
//					String assignee = ref.getAssignee();
//				}
//			}else {
//				for (Task ref : unAssignedTask) {
//					String assignee = ref.getAssignee();
//				}
//			}
//			
//			return tasks;
//		}
@CrossOrigin
	@GetMapping("/assigneeCheck/{procesInstanceID}")
	public Map assigneeCheck(@PathVariable String procesInstanceID) {
		List tasklist = new ArrayList<>();
		String Taskid = null;
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate rest = new RestTemplate();
		Map camundaFormsMap = new HashMap<>();
		String assignee= null;
		List<Task> assignedTask = null;
		List<Task> unAssignedTask = null;
		Map mp = new HashMap<>();

		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		try {
			ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
			Map resBody = responseEntity.getBody();
			Map zeebeHitsObj = (Map) resBody.get("hits");
			List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
			Map zeebemp = null;
			String processInstanceKey = "";
			CamundaTaskListClient client = null;
			for (Object zeeberef : zeebeHitsObjList) {
				zeebemp = (Map) zeeberef;
				Map zeebeSourceObj = (Map) zeebemp.get("_source");
				Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
				System.out.println("this is zeebeValueObj" + zeebeValueObj);
				long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
				long processDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
				String defID = String.valueOf(processDefinitionKey);
				processInstanceKey = String.valueOf(procInstanceKey);
				if (processInstanceKey.equals(procesInstanceID)) {
					tasklist.add(zeebeSourceObj.get("key"));
					for (Object ref : tasklist) {
						String assigneeId = "demo";
						String selfmanagedtaskListUrls = "http://localhost:8083";
						SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
						client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls)
								.shouldReturnVariables().authentication(simpleAuthentication).build();
						assignedTask=client.getTasks(true, TaskState.CREATED, 50, false);
						unAssignedTask=client.getTasks(false, TaskState.CREATED, 50, false);
						 
							for (Task refObj : assignedTask) {
								String processDefinitionId = refObj.getProcessDefinitionId();
								if (processDefinitionId.equals(defID)) {
									 assignee = refObj.getAssignee();
									 if(assignee!= null) {
										 assignee = "Assigned";
										 mp.put("State", assignee);
									 }
									System.out.println("this is the value for assignee:::::::" + assignee);
								}

							}
							for (Task refOb : unAssignedTask) {
								String processDefinitionId = refOb.getProcessDefinitionId();
								if (processDefinitionId.equals(defID)) {
									 assignee = refOb.getAssignee();
									 System.out.println("this is the value :"+assignee);
									 if(assignee==null) {
										 assignee = "UnAssigned";
										 mp.put("State", assignee);
									 }
									System.out.println("this is the value for assignee:::::::" + assignee);
								}
							}
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			String str = "there is no active task for this instance ID....";
			mp.put("error Message", str);
			return mp;
		}
		return mp;
	}



// duplicate : deletable :
@GetMapping("/assigneeCheck1/{procesInstanceID}")
public Map assigneeCheck1(@PathVariable String procesInstanceID) {
	List tasklist = new ArrayList<>();
	String Taskid = null;
	ObjectMapper objectMapper = new ObjectMapper();
	RestTemplate rest = new RestTemplate();
	Map camundaFormsMap = new HashMap<>();
	String assignee= null;
	List<Task> assignedTask = null;
	List<Task> unAssignedTask = null;
	Map mp = new HashMap<>();

	String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
	try {
		ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
		Map resBody = responseEntity.getBody();
		Map zeebeHitsObj = (Map) resBody.get("hits");
		List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
		Map zeebemp = null;
		String processInstanceKey = "";
		CamundaTaskListClient client = null;
		for (Object zeeberef : zeebeHitsObjList) {
			zeebemp = (Map) zeeberef;
			Map zeebeSourceObj = (Map) zeebemp.get("_source");
			Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
			System.out.println("this is zeebeValueObj" + zeebeValueObj);
			long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
			long processDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
			String defID = String.valueOf(processDefinitionKey);
			processInstanceKey = String.valueOf(procInstanceKey);
			if (processInstanceKey.equals(procesInstanceID)) {
				tasklist.add(zeebeSourceObj.get("key"));
				for (Object ref : tasklist) {
					String assigneeId = "demo";
					String selfmanagedtaskListUrls = "http://localhost:8083";
					SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
					client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls)
							.shouldReturnVariables().authentication(simpleAuthentication).build();
					assignedTask=client.getTasks(true, TaskState.CREATED, 50, false);
					unAssignedTask=client.getTasks(false, TaskState.CREATED, 50, false);
					 
						for (Task refObj : assignedTask) {
							String processDefinitionId = refObj.getProcessDefinitionId();
							if (processDefinitionId.equals(defID)) {
								 assignee = refObj.getAssignee();
								 if(assignee!= null) {
									 assignee = "Assigned";
									 mp.put("State", assignee);
								 }
								System.out.println("this is the value for assignee:::::::" + assignee);
							}

						}
						for (Task refOb : unAssignedTask) {
							String processDefinitionId = refOb.getProcessDefinitionId();
							if (processDefinitionId.equals(defID)) {
								 assignee = refOb.getAssignee();
								 System.out.println("this is the value :"+assignee);
								 if(assignee==null) {
									 assignee = "UnAssigned";
									 mp.put("State", assignee);
								 }
								System.out.println("this is the value for assignee:::::::" + assignee);
							}
						}
					
				}
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
		String str = "there is no active task for this instance ID....";
		mp.put("error Message", str);
		return mp;
	}
	
//	if(mp.isEmpty()) {
//		mp.put("", zeebeRecordURL)
//	}
	
	return mp;
}






//	please send to rajesh
	// Claim a user task passing instance id :
	@CrossOrigin
	@GetMapping("/getClaimActiveTaskID/{procesInstanceID}")
	public String getClaimActiveTask(@PathVariable String procesInstanceID) {
		List tasklist = new ArrayList<>();
		String Taskid = null;
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate rest = new RestTemplate();
		Map camundaFormsMap = new HashMap<>();

		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		try {
			ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
			Map resBody = responseEntity.getBody();
			Map zeebeHitsObj = (Map) resBody.get("hits");
			List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
			Map zeebemp = null;
			String processInstanceKey = "";
			CamundaTaskListClient client = null;
			for (Object zeeberef : zeebeHitsObjList) {
				zeebemp = (Map) zeeberef;
				Map zeebeSourceObj = (Map) zeebemp.get("_source");
				Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
				System.out.println("this is zeebeValueObj" + zeebeValueObj);
				long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
				long processDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
				String defID = String.valueOf(processDefinitionKey);
				processInstanceKey = String.valueOf(procInstanceKey);
				if (processInstanceKey.equals(procesInstanceID)) {
					tasklist.add(zeebeSourceObj.get("key"));
					for (Object ref : tasklist) {
						String sr = String.valueOf(ref);
						System.out.println("this is the variable :::::" + ref);
						String taskIDiterate = "hello";
						String assigneeId = "demo";
						String selfmanagedtaskListUrls = "http://localhost:8083";
						SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
						client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls)
								.shouldReturnVariables().authentication(simpleAuthentication).build();
						List<Task> tasks = client.getTasks(false, TaskState.CREATED, 50, false);
						for (Task ref1 : tasks) {
							String processDefinitionId = ref1.getProcessDefinitionId();
							if (processDefinitionId.equals(defID)) {
								Taskid = ref1.getId();
								if (sr.equals(Taskid)) {
									Task ta = client.claim(Taskid, "demo");
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			String str = "there is no active task for this instance ID....";
			return str;
		}
		return "successfully claim a task ";
	}

	// Unclaim API :
	@CrossOrigin
	@GetMapping("/getUnClaimActiveTaskID/{procesInstanceID}")
	public String getUnClaimActiveTask(@PathVariable String procesInstanceID) throws Exception {
		Map camundaFormsMap = new HashMap<>();
		List tasklist = new ArrayList<>();
		String Taskid = null;
		CamundaTaskListClient client = null;
		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		try {
			ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
			Map resBody = responseEntity.getBody();
			Map zeebeHitsObj = (Map) resBody.get("hits");
			List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
			Map zeebemp = null;
			String processInstanceKey = "";
			long TaskID = 0;
			for (Object zeeberef : zeebeHitsObjList) {
				zeebemp = (Map) zeeberef;
				Map zeebeSourceObj = (Map) zeebemp.get("_source");
				Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
				long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
				processInstanceKey = String.valueOf(procInstanceKey);
				long processDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
				String defID = String.valueOf(processDefinitionKey);
				if (processInstanceKey.equals(procesInstanceID)) {
					TaskID = (long) zeebeSourceObj.get("key");
					tasklist.add(zeebeSourceObj.get("key"));
					for (Object ref : tasklist) {
						String sr = String.valueOf(ref);
						System.out.println("this is the variable :::::" + ref);
						String taskIDiterate = "hello";
						String assigneeId = "demo";
						String selfmanagedtaskListUrls = "http://localhost:8083";
						SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
						client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls)
								.shouldReturnVariables().authentication(simpleAuthentication).build();
						List<Task> tasks = client.getTasks(true, TaskState.CREATED, 50, false);
						for (Task ref1 : tasks) {
							String processDefinitionId = ref1.getProcessDefinitionId();
							System.out.println("this is the processDefinitionId :"+processDefinitionId);
							if (processDefinitionId.equals(defID)) {
								System.out.println("enter");
								Taskid = ref1.getId();
								if (sr.equals(Taskid)) {
									System.out.println("this is the Taskid"+Taskid);
									Task ta = client.unclaim(Taskid);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			String str = "there is no active task for this instance ID....";
			return str;
		}

		return "successfully UnClaim a task ";
	}

//	please send to rajesh
	// complete a user task passing instance id :
	@CrossOrigin
	@GetMapping("/getCompleteActiveTaskID/{procesInstanceID}")
	public String getCompleteActiveTask(@PathVariable String procesInstanceID) throws Exception {
		Map camundaFormsMap = new HashMap<>();
		List tasklist = new ArrayList<>();
		String Taskid = null;
		CamundaTaskListClient client = null;
		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		try {
			ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
			Map resBody = responseEntity.getBody();
			Map zeebeHitsObj = (Map) resBody.get("hits");
			List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
			Map zeebemp = null;
			Map mp = new HashMap<>();
			String processInstanceKey = "";
			long TaskID = 0;
			for (Object zeeberef : zeebeHitsObjList) {
				zeebemp = (Map) zeeberef;
				Map zeebeSourceObj = (Map) zeebemp.get("_source");
				Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
				long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
				long processDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
				String defID = String.valueOf(processDefinitionKey);
				processInstanceKey = String.valueOf(procInstanceKey);
				if (processInstanceKey.equals(procesInstanceID)) {
					tasklist.add(zeebeSourceObj.get("key"));
					for (Object ref : tasklist) {
						String sr = String.valueOf(ref);
						System.out.println("this is the variable :::::" + ref);
						String taskIDiterate = "hello";
						String assigneeId = "demo";
						String selfmanagedtaskListUrls = "http://localhost:8083";
						SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
						client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls)
								.shouldReturnVariables().authentication(simpleAuthentication).build();
						List<Task> tasks = client.getTasks(true, TaskState.CREATED, 50, false);
						for (Task ref1 : tasks) {
							String processDefinitionId = ref1.getProcessDefinitionId();
							if (processDefinitionId.equals(defID)) {
								Taskid = ref1.getId();
								if (sr.equals(Taskid)) {
									System.out.println("this is the taskid : "+Taskid);
									Task ts = client.completeTask(Taskid, mp);
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			String str = "there is no active task for this instance ID....";
			return str;
		}
		return "UserTask Successfully Completed";
	}
	
	// claim task :
	@GetMapping("apicomplete")
	public String claimTask() {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

		// Specify the task ID and the user who wants to claim the task
		String taskId = "yourTaskId";
		String userId = "desiredUserId";

		// Claim the task
		TaskService taskService = processEngine.getTaskService();
		taskService.claim(taskId, userId);
		return "";
	}

	// claim a user task through api :
	@GetMapping("/claimTask/{taskID}")
	public Task claimTask(@PathVariable String taskID) throws Exception {

		Map mp1 = new HashMap<>();
		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		Task ta = client.claim(taskID, "demo");
		return ta;

	}

	// un-claim a user task through api :
	@GetMapping("/UnclaimTask")
	public List<Task> UnclaimTask(@PathVariable String taskID) throws TaskListException {

		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		List<Task> tasks = client.getGroupTasks(null, TaskState.CREATED, 50);

		return tasks;
	}

	// complete a user task through api :

	@GetMapping("/Complete/{taskID}")
	public Task Complete(@PathVariable String taskID) throws Exception {
		Map mp = new HashMap<>();
		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		String str = "demo";
		Task ts = client.completeTask(taskID, mp);
		return ts;
	}

	// GetAssignedTask
	@GetMapping("/GetAssignedTask")
	public List<Task> GetAssignedTask() throws TaskListException {

		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		String demo = "demo";
		List<Task> tasks = client.getAssigneeTasks(demo, TaskState.CREATED, 50, true);
		return tasks;
	}

	/// sample getrequest :
	// 2251799813685293
	// passing instance key then get process definition
	// passing definition & get form details
	// * This API is currently use : API ----2.
//@CrossOrigin
//	@GetMapping("/getformbyinstanceKey/{proInstanceKey}")
//	public Map sample(@PathVariable String proInstanceKey) {
//		ObjectMapper objectMapper = new ObjectMapper();
//		RestTemplate rest = new RestTemplate();
//		Map camundaFormsMap = new HashMap<>();
//		String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
//		String zeebeRecordURL = "http://localhost:9200/zeebe*/_search?size=1000";
//		ResponseEntity<Map> forEntity = rest.getForEntity(zeebeRecordJobURL, Map.class);
//		Map respbody = forEntity.getBody();
//		Map hitsObj = (Map) respbody.get("hits");
//		List hitsObjList = (List) hitsObj.get("hits");
//		Map valueObj = null;
//		for (Object ref : hitsObjList) {
//			Map obj = (Map) ref;
//			Map sourceObj = (Map) obj.get("_source");
//			valueObj = (Map) sourceObj.get("value");
//			long processIns = (long) valueObj.get("processInstanceKey");
//			String processInstanceKey = String.valueOf(processIns);
//			if (processInstanceKey.equals(proInstanceKey)) {
//				long processDef = (long) valueObj.get("processDefinitionKey");
//				System.out.println("processDefinitionKey" + processDef);
//				String processDefinitionKey = String.valueOf(processDef);
//				ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
//				Map resBody = responseEntity.getBody();
//				Map zeebeHitsObj = (Map) resBody.get("hits");
//				List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
//				String xmlString = null;
//				String zeebeResourceObj = null;
//				Map zeebemp = null;
//				for (Object zeeberef : zeebeHitsObjList) {
//					zeebemp = (Map) zeeberef;
//					Map zeebeSourceObj = (Map) zeebemp.get("_source");
//					Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
//					zeebeResourceObj = (String) zeebeValueObj.get("resource");
//					if (zeebeResourceObj != null) {
//						long pDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
//						String procDefinitionKey = String.valueOf(pDefinitionKey);
//						if (procDefinitionKey.equals(processDefinitionKey)) {
//							byte[] decodedBytes = Base64.getDecoder().decode(zeebeResourceObj);
//							xmlString = new String(decodedBytes, StandardCharsets.UTF_8);
//							try {
//								DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//								DocumentBuilder builder = factory.newDocumentBuilder();
//								Document document = builder.parse(new InputSource(new StringReader(xmlString)));
//								NodeList userTaskForms = document.getElementsByTagName("zeebe:userTaskForm");
//								for (int i = 0; i < userTaskForms.getLength(); i++) {
//									Element userTaskFormElement = (Element) userTaskForms.item(i);
//									String userTaskFormId = userTaskFormElement.getAttribute("id");
//									String jsonContent = userTaskFormElement.getTextContent();
//									JsonNode jsonNode = objectMapper.readTree(jsonContent);
//									JsonNode componentsNode = jsonNode.path("components");
//									List lss = new ArrayList<>();
//									for (JsonNode componentNode : componentsNode) {
//										Map mpp = new HashMap<>();
//										mpp.put("Label", componentNode.path("label").asText());
//										mpp.put("Type", componentNode.path("type").asText());
//										mpp.put("ID", componentNode.path("id").asText());
//										mpp.put("Key", componentNode.path("key").asText());
//										lss.add(mpp);
//									}
//									camundaFormsMap.put(userTaskFormId, lss);
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}
//			}
//		}
//		return camundaFormsMap;
//	}
//

//passing instanceID and get task id:

	@GetMapping("/getActiveTask/{instanceID}")
	public List<Task> getUserdemo(@PathVariable String instanceID) throws TaskListException {

		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		List<Task> tasks = client.getTasks(false, TaskState.CREATED, 50);

		List ls = new ArrayList<>();
		String id = null;
		String name = null;
		String processName = null;
		String formKey = null;

		Map mp;
		for (Task ref : tasks) {
			mp = new HashMap<>();
			List<Variable> variables = ref.getVariables();
			String id2 = null;
			for (Variable reference : variables) {
				id2 = reference.getId();
				String[] str = id2.split("-");
				String idd = str[0];
				if (idd.equals(instanceID)) {
					System.out.println("this is the active 1 :" + ref);
				}

			}

			id = ref.getId();
			name = ref.getName();
			processName = ref.getProcessName();
			formKey = ref.getFormKey();

			mp.put("Taskid", id);
			mp.put("Taskname", name);
			mp.put("processName", processName);
			mp.put("formKeyid", formKey);
			ls.add(mp);
		}
		return tasks;
//	return ls ;
	}

// Active Tasklist ::
	@GetMapping("/getALLActiveTask")
	public List<Task> getTasklist() throws TaskListException {
		CamundaTaskListClient client = null;
		String assigneeId = "demo";
		String selfmanagedtaskListUrls = "http://localhost:8083";
		SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
		client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls).shouldReturnVariables()
				.authentication(simpleAuthentication).build();
		List<Task> tasks = client.getTasks(true, TaskState.CREATED, 50, true);
		List<Variable> variables = null;
		for (Task ref : tasks) {
			variables = ref.getVariables();
		}
		return tasks;
	}

// alternate api :
//	@GetMapping("/getformdata-byinstanceKey/{proInstanceKey}")
//	public Map getformdata(@PathVariable String proInstanceKey) throws Exception {
//		String getformat = null;
//		Map<String, List<Map<String, String>>> camundaFormsMap = new HashMap<>();
//		List thisisTheList = new ArrayList<>();
//		String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
//		ResponseEntity<Map> forEntity = rest.getForEntity(zeebeRecordJobURL, Map.class);
//		Map respbody = forEntity.getBody();
//		Map hitsObj = (Map) respbody.get("hits");
//		List hitsObjList = (List) hitsObj.get("hits");
//		Map valueObj = null;
//		for (Object ref : hitsObjList) {
//			Map obj = (Map) ref;
//			Map sourceObj = (Map) obj.get("_source");
//			valueObj = (Map) sourceObj.get("value");
//			long processIns = (long) valueObj.get("processInstanceKey");
//			String processInstanceKey = String.valueOf(processIns);
//			if (processInstanceKey.equals(proInstanceKey)) {
//				long processDef = (long) valueObj.get("processDefinitionKey");
//				String processDefinitionKey = String.valueOf(processDef);
//				SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
//				CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
//						.shouldReturnVariables().authentication(sa).build();
//				List<Task> tasks = client.getTasks(false, TaskState.CREATED, 50);
//				for (Task refObj : tasks) {
//					String processDefinitionId = refObj.getProcessDefinitionId();
//					if (processDefinitionKey.equals(processDefinitionId)) {
//						String taskId = refObj.getId();
//						String formKey = refObj.getFormKey();
//						String[] str = formKey.split("bpmn:");
//						getformat = str[1];
//					}
//				}
//				String zeebeRecordURL = "http://localhost:9200/zeebe*/_search?size=1000";
//				ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
//				Map resBody = responseEntity.getBody();
//				Map zeebeHitsObj = (Map) resBody.get("hits");
//				List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
//				String xmlString = null;
//				String zeebeResourceObj = null;
//				Map zeebemp = null;
//				for (Object zeeberef : zeebeHitsObjList) {
//					zeebemp = (Map) zeeberef;
//					Map zeebeSourceObj = (Map) zeebemp.get("_source");
//					Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
//					zeebeResourceObj = (String) zeebeValueObj.get("resource");
//					if (zeebeResourceObj != null) {
//						long pDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
//						String procDefinitionKey = String.valueOf(pDefinitionKey);
//						if (procDefinitionKey.equals(processDefinitionKey)) {
//							byte[] decodedBytes = Base64.getDecoder().decode(zeebeResourceObj);
//							xmlString = new String(decodedBytes, StandardCharsets.UTF_8);
//							try {
//								DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//								DocumentBuilder builder = factory.newDocumentBuilder();
//								Document document = builder.parse(new InputSource(new StringReader(xmlString)));
//								NodeList userTaskForms = document.getElementsByTagName("zeebe:userTaskForm");
//								for (int i = 0; i < userTaskForms.getLength(); i++) {
//									Element userTaskFormElement = (Element) userTaskForms.item(i);
//									String userTaskFormId = userTaskFormElement.getAttribute("id");
//									String jsonContent = userTaskFormElement.getTextContent();
//									JsonNode jsonNode = mapper.readTree(jsonContent);
//									JsonNode componentsNode = jsonNode.path("components");
//									List lss = new ArrayList<>();
//									for (JsonNode componentNode : componentsNode) {
//										Map mpp = new HashMap<>();
//										mpp.put("Label", componentNode.path("label").asText());
//										mpp.put("Type", componentNode.path("type").asText());
//										mpp.put("ID", componentNode.path("id").asText());
//										mpp.put("Key", componentNode.path("key").asText());
//										lss.add(mpp);
//									}
//									camundaFormsMap.put(userTaskFormId, lss);
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}
//			}
//		}
//		List<Map<String, String>> list2 = camundaFormsMap.get(getformat);
//		Map mp = new HashMap<>();
//		mp.put(getformat, list2);
//		return mp;
//	}

// currently in use :
	@CrossOrigin
	@GetMapping("/getformdata-byinstanceKey/{proInstanceKey}")
	public Map getformdatas(@PathVariable String proInstanceKey) throws Exception {
		List<Variable> variables = null;
		String processDefinitionKey = null;
		List<Task> allActiveTask = new ArrayList<>();
		List taskid = new ArrayList<>();
		List listObj = null;
		long TaskID = 0;
		String getformat = null;
		Map camundaFormsMap = new HashMap<>();
		Map finalFieldMap = new HashMap<>();
		List thisisTheList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		ResponseEntity<Map> forEntity = rest.getForEntity(zeebeRecordJobURL, Map.class);
		Map respbody = forEntity.getBody();
		Map hitsObj = (Map) respbody.get("hits");
		List hitsObjList = (List) hitsObj.get("hits");
		Map valueObj = null;
		for (Object ref : hitsObjList) {
			Map obj = (Map) ref;
			Map sourceObj = (Map) obj.get("_source");
			valueObj = (Map) sourceObj.get("value");
			long processIns = (long) valueObj.get("processInstanceKey");
			String processInstanceKey = String.valueOf(processIns);
			if (processInstanceKey.equals(proInstanceKey)) {
				long processDef = (long) valueObj.get("processDefinitionKey");
				processDefinitionKey= String.valueOf(processDef);
				
				TaskID = (long)sourceObj.get("key");
				taskid.add(TaskID);					
//				 String task = String.valueOf(TaskID);

				SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");
				CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
						.shouldReturnVariables().authentication(sa).build();
				List<Task> unassigned = client.getTasks(false, TaskState.CREATED, 50);
				List<Task> assigned = client.getTasks(true, TaskState.CREATED, 50);
				
				allActiveTask.addAll(unassigned);
				allActiveTask.addAll(assigned);
				
				for (Task refObj : allActiveTask) {
					String processDefinitionId = refObj.getProcessDefinitionId();
					if (processDefinitionKey.equals(processDefinitionId)) {
						String taskId = refObj.getId();
						for(Object id:taskid) {
							String task = String.valueOf(id);
							if (taskId.equals(task)) {
								variables = refObj.getVariables();
							}
						}
						String formKey = refObj.getFormKey();
						String[] str = formKey.split("bpmn:");
						getformat = str[1];
					}
				}
				String zeebeRecordURL = "http://localhost:9200/zeebe*/_search?size=1000";
				ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
				Map resBody = responseEntity.getBody();
				Map zeebeHitsObj = (Map) resBody.get("hits");
				List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
				String xmlString = null;
				String zeebeResourceObj = null;
				Map zeebemp = null;
				for (Object zeeberef : zeebeHitsObjList) {
					zeebemp = (Map) zeeberef;
					Map zeebeSourceObj = (Map) zeebemp.get("_source");
					Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
					zeebeResourceObj = (String) zeebeValueObj.get("resource");
					if (zeebeResourceObj != null) {
						long pDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
						String procDefinitionKey = String.valueOf(pDefinitionKey);
						if (procDefinitionKey.equals(processDefinitionKey)) {
							byte[] decodedBytes = Base64.getDecoder().decode(zeebeResourceObj);
							xmlString = new String(decodedBytes, StandardCharsets.UTF_8);
							try {
								DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
								DocumentBuilder builder = factory.newDocumentBuilder();
								Document document = builder.parse(new InputSource(new StringReader(xmlString)));
								NodeList userTaskForms = document.getElementsByTagName("zeebe:userTaskForm");
								for (int i = 0; i < userTaskForms.getLength(); i++) {
									Element userTaskFormElement = (Element) userTaskForms.item(i);
									String userTaskFormId = userTaskFormElement.getAttribute("id");
									String jsonContent = userTaskFormElement.getTextContent();
									JsonNode jsonNode = mapper.readTree(jsonContent);
									JsonNode componentsNode = jsonNode.path("components");
									List lss = new ArrayList<>();
									for (JsonNode componentNode : componentsNode) {
										Map mpp = new HashMap<>();
										mpp.put("Label", componentNode.path("label").asText());
										mpp.put("Type", componentNode.path("type").asText());
										mpp.put("ID", componentNode.path("id").asText());
										mpp.put("Key", componentNode.path("key").asText());
										lss.add(mpp);
									}
									camundaFormsMap.put(userTaskFormId, lss);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
for(Task refOb:allActiveTask) {
List<Variable> variables2 = refOb.getVariables();
String id = refOb.getId();
String formKey = refOb.getFormKey();
String[] str = formKey.split("bpmn:");
getformat = str[1];
listObj = (List) camundaFormsMap.get(getformat);
Map mp = new HashMap<>();
for(Object ref1:listObj) {
	
	for(Variable refob:variables2) {
		String id2 = refob.getId();
		String[] varIns = id2.split("-");
		String VInstaID = varIns[0];
		if(VInstaID.equals(proInstanceKey)) {
			String name = refob.getName();
			System.out.println("name"+name);
			String value = (String) refob.getValue();
			System.out.println("value"+value);
			Map obj1=(Map)ref1;
			String Label = (String) obj1.get("Label");
			if(name.equals(Label)) {
				finalFieldMap.put(Label, value);
			}
		}
	}
}

}
	
//Thread.sleep(6000);
		return finalFieldMap;
	}
//variable :
	@GetMapping("/getVariable/{procesInstanceID}")
	public List<Variable> getVariable(@PathVariable String procesInstanceID) throws Exception {
		List<Variable> variables = null;
		List<Task> allActiveTask= new ArrayList<>();
		Map camundaFormsMap = new HashMap<>();
		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
		Map resBody = responseEntity.getBody();
		Map zeebeHitsObj = (Map) resBody.get("hits");
		List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
		Map zeebemp = null;
		String processInstanceKey = "";
		long TaskID = 0;
		for (Object zeeberef : zeebeHitsObjList) {
			zeebemp = (Map) zeeberef;
			Map zeebeSourceObj = (Map) zeebemp.get("_source");
			Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
			long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
			processInstanceKey = String.valueOf(procInstanceKey);
			if (processInstanceKey.equals(procesInstanceID)) {
				TaskID = (long) zeebeSourceObj.get("key");
				String task = String.valueOf(TaskID);
				CamundaTaskListClient client = null;
				String selfmanagedtaskListUrls = "http://localhost:8083";
				SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
				client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls)
						.shouldReturnVariables().authentication(simpleAuthentication).build();
				List<Task> assigned = client.getTasks(true, TaskState.CREATED, 50, true);
				List<Task> unassigned = client.getTasks(false, TaskState.CREATED, 50, true);
				allActiveTask.addAll(assigned);
				allActiveTask.addAll(unassigned);
				
				for (Task ref : allActiveTask) {
					String taskid = ref.getId();
					if (taskid.equals(task)) {
						variables = ref.getVariables();
					}
				}
			}
		}
		return variables;
	}

//passing instance ID get active task id :
	@GetMapping("/getAllTaskID/{procesInstanceID}")
	public String getAllTaskID(@PathVariable String procesInstanceID) throws Exception {

		List<Variable> variables = null;

		String taskid = null;
		Map camundaFormsMap = new HashMap<>();
		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		ResponseEntity<Map> responseEntity = rest.getForEntity(zeebeRecordURL, Map.class);
		Map resBody = responseEntity.getBody();
		Map zeebeHitsObj = (Map) resBody.get("hits");
		List zeebeHitsObjList = (List) zeebeHitsObj.get("hits");
		Map zeebemp = null;
		String processInstanceKey = "";
		long TaskID = 0;
		for (Object zeeberef : zeebeHitsObjList) {
			zeebemp = (Map) zeeberef;
			Map zeebeSourceObj = (Map) zeebemp.get("_source");
			Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
			long procInstanceKey = (long) zeebeValueObj.get("processInstanceKey");
			processInstanceKey = String.valueOf(procInstanceKey);
			if (processInstanceKey.equals(procesInstanceID)) {
				TaskID = (long) zeebeSourceObj.get("key");
				String task = String.valueOf(TaskID);
				CamundaTaskListClient client = null;
				String selfmanagedtaskListUrls = "http://localhost:8083";
				SimpleAuthentication simpleAuthentication = new SimpleAuthentication("demo", "demo");
				client = new CamundaTaskListClient.Builder().taskListUrl(selfmanagedtaskListUrls)
						.shouldReturnVariables().authentication(simpleAuthentication).build();
				List<Task> tasks = client.getTasks(true, TaskState.CREATED, 50, true);
				for (Task ref : tasks) {
					taskid = ref.getId();
				}
			}
		}
		return taskid;
	}

}
