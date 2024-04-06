package com.camundaforms.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.ZeebeClient;

@CrossOrigin
@RestController
public class CustomTaskListController {

	@Autowired
	TasklistUtilities tasklistUtilities;

	@Autowired
	ZeebeClient zeebeClient;

	RestTemplate restTemp = new RestTemplate();

	// All Active Task List From New Index :

	// Need to add exception when there is no active task :

///////////// active new index ///////////////////

	// get All active task list :

	@CrossOrigin
	@GetMapping("/surge/camunda/tasklist/newIndex-all-active")
	public List indexAllActiveTasks() throws Exception {

		tasklistUtilities = new TasklistUtilities();

		ResponseEntity<Map> allActiveListOfTasksCustomize = tasklistUtilities.addDocumentsToNewIndex();

		System.out.println("allActiveListOfTasksCustomize================>" + allActiveListOfTasksCustomize);
		List finalLeftSideResList = new ArrayList();
//		ResponseEntity<Map> docDetailsResponse = null;

		try {
			if (allActiveListOfTasksCustomize != null && allActiveListOfTasksCustomize.getBody() != null) {

				List<Map<String, Object>> items = (List<Map<String, Object>>) allActiveListOfTasksCustomize.getBody()
						.get("items");

				System.out.println(items);

				for (Map<String, Object> item : items) {
					// Extract document ID from the item

					Map<String, String> indexInfo = (Map<String, String>) item.get("update");
					System.out.println("indexInfo :" + indexInfo);
					String docId = indexInfo.get("_id");

					System.out.println("docId : " + docId);

					// Use the document ID to get document details
					String getIndexURL = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_doc/" + docId;
					ResponseEntity<Map> docDetailsResponse =docDetailsResponse = restTemp.getForEntity(getIndexURL, Map.class);

					if (docDetailsResponse.getStatusCode().is2xxSuccessful()) {
						Map docDetails = docDetailsResponse.getBody();
						System.out.println("Document details: " + docDetails);
						// Process the document details as needed
						String documentId = (String) docDetails.get("_id");
						Map getsource = (Map) docDetails.get("_source");
						Map getvalue = (Map) getsource.get("value");
						Map getcustomHeaders = (Map) getvalue.get("customHeaders");
						String getAssignee = (String) getcustomHeaders.get("io.camunda.zeebe:assignee");
						System.out.println("getAssignee : " + getAssignee);
						String bpmnProcessId = (String) getvalue.get("bpmnProcessId");
						String elementId = (String) getvalue.get("elementId");
						Long creationTimestamp = (Long) getsource.get("timestamp");
						Long getKey = (Long) getsource.get("key");

						Date date = new Date(creationTimestamp);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDate = sdf.format(date);

						Map finalLeftResMap = new HashMap();

						finalLeftResMap.put("_id", documentId);
						finalLeftResMap.put("assignee", getAssignee);
						finalLeftResMap.put("Name", bpmnProcessId);
						finalLeftResMap.put("Process", elementId);
						finalLeftResMap.put("CreationTime", formattedDate);
						finalLeftResMap.put("Key", getKey);
						finalLeftSideResList.add(finalLeftResMap);
					} else {
						System.err
								.println("Failed to retrieve document details: " + docDetailsResponse.getStatusCode());
						// Handle failure
					}
				}
			} else {
//				System.err.println("Failed to retrieve document details: " + docDetailsResponse.getStatusCode());
				// Handle failure
			}
		} catch (Exception e) {
			System.err.println("Exception occurred while processing active tasks: " + e.getMessage());
			// Handle the exception, if necessary
		}
		return finalLeftSideResList;

	}

	// get active task by id :
	// API 2 right side with variable :
	@CrossOrigin
	@GetMapping("/getActiveTaskDetailsByID/{taskID}")
	public List getAllDetailByTaskID(@PathVariable String taskID) throws Exception {
		System.err.println("enter.............");
		List activeKey = new ArrayList<>();
		List<Map> hitsObjList = null;
		List<Map> getActiveList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/surge_c8_custom_tasklist_all_active*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(zeebeRecordJobURL, Map.class);
		Map respBody = forEntity.getBody();
		Map hitsObj = (Map) respBody.get("hits");
		// this is hit list object :
		hitsObjList = (List) hitsObj.get("hits");
		// initialize area :
		String processInstanceKey = null;
		String activeDefinitionKey = null;
		String key = null;
		// List Object Area :
		List<String> activeDefList = new ArrayList<>();
		Set<String> allActInstanceID = new HashSet<>();
		Set<String> allActInstanceID2 = new HashSet<>();
		Set<String> allActTaskKey = new HashSet<>();
		// iterate :

		// 1st iterating for all def, ins, key
		for (Map ref : hitsObjList) {
			Map sourceObj = (Map) ref.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			long processDefinitionKeyObj = (long) valueObj.get("processDefinitionKey");
			long processIns = (long) valueObj.get("processInstanceKey");
			long keyObj = (long) sourceObj.get("key");
			// string convert area :
			activeDefinitionKey = String.valueOf(processDefinitionKeyObj);
			processInstanceKey = String.valueOf(processIns);
			key = String.valueOf(keyObj);
			activeDefList.add(activeDefinitionKey);
			allActInstanceID.add(processInstanceKey);
			allActTaskKey.add(key);
		}
		// 2nd iterating for all get all ins :
		for (String defID : activeDefList) {
			for (Map hitOb : hitsObjList) {
				Map sourceObj1 = (Map) hitOb.get("_source");
				Map valueObj1 = (Map) sourceObj1.get("value");
				long defid = (long) valueObj1.get("processDefinitionKey");
				String convert = String.valueOf(defid);
				if (defID.equals(convert)) {
					long inst = (long) valueObj1.get("processInstanceKey");
					String inss = String.valueOf(inst);
					allActInstanceID2.add(inss);
				}
			}
		}

		// 3rd iterating for matching instance get key & intent :
		Set<Long> idsWithOnlyCreated = new HashSet<Long>();
		for (String instID : allActInstanceID2) {
			for (Map hitOb : hitsObjList) {
				Map sourceObj1 = (Map) hitOb.get("_source");
				Map valueObj1 = (Map) sourceObj1.get("value");
				long inst = (long) valueObj1.get("processInstanceKey");
				String inss = String.valueOf(inst);
				if (instID.equals(inss)) {
					Map<Long, Set<String>> stateMap = new HashMap<>();
					for (Map finalhitOb : hitsObjList) {
						Map finalsource = (Map) finalhitOb.get("_source");
						long finalky = (long) finalsource.get("key"); // important
						String intent = (String) finalsource.get("intent"); // important
						if (!stateMap.containsKey(finalky)) {
							stateMap.put(finalky, new HashSet<String>());
						}
						stateMap.get(finalky).add(intent);
					}
					for (Map.Entry<Long, Set<String>> entry : stateMap.entrySet()) {
						if (entry.getValue().contains("CREATED") && !entry.getValue().contains("COMPLETED")) {
							idsWithOnlyCreated.add(entry.getKey());
						}
					}

				}
			}
		}
		long ky2 = 0;
		Map customHeaders = null;
		// Final iterating for matching instance get key & intent :
		List variableList = new ArrayList<>();
		for (Long refer : idsWithOnlyCreated) {
			String gekey = String.valueOf(refer);
			for (Map actTaskListObj : hitsObjList) {
				Map taskDetails = new HashMap<>();
				Map _source2 = (Map) actTaskListObj.get("_source");
				long keykey = (long) _source2.get("key");
				String keyValue = String.valueOf(keykey);
				if (keyValue.equals(gekey)) {
					if (taskID.equals(gekey)) {
						long timestamp = (long) _source2.get("timestamp");
						Map value = (Map) _source2.get("value");
						customHeaders = (Map) value.get("customHeaders");
						Date date = new Date(timestamp);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedDate = sdf.format(date);
						String index = (String) actTaskListObj.get("_index");
						String id = (String) actTaskListObj.get("_id");
						ky2 = (long) _source2.get("key");
						String bpmnProcessId = (String) value.get("bpmnProcessId");
						String elementId = (String) value.get("elementId");
						String assignee = (String) customHeaders.get("io.camunda.zeebe:assignee");
						long proInstanceKeyVal = (long) value.get("processInstanceKey");
						String proInsVari = String.valueOf(proInstanceKeyVal);
						System.out.println("this is instance key: " + proInsVari);
						String zeebeVariableURL = "http://localhost:9200/zeebe-record_variable_*/_search?size=1000&from=0";
						ResponseEntity<Map> respEntity = restTemp.getForEntity(zeebeVariableURL, Map.class);
						Map resbody = respEntity.getBody();
						Map hits = (Map) resbody.get("hits");
						List<Map> hitList = (List<Map>) hits.get("hits");
						for (Map hitObj : hitList) {
							Map sourceVar = (Map) hitObj.get("_source");
							if (sourceVar.containsKey("value")) {
								System.out.println("okay");
								Map valueVar = (Map) sourceVar.get("value");
								long processInstancObj = (long) valueVar.get("processInstanceKey");
								String insVari = String.valueOf(processInstancObj);
								if (insVari.equals(proInsVari)) {
									System.out.println("enter");
									String lable = (String) valueVar.get("name");
									String valueVariabl = (String) valueVar.get("value");
									ObjectMapper objectMapper = new ObjectMapper();
									JsonNode jsonNode = objectMapper.readTree(valueVariabl);
									String stringValue = jsonNode.asText();
									Map mp = new HashMap<>();
									mp.put(lable, stringValue);
									variableList.add(mp);
								}
							}
						}
						taskDetails.put("_index", index);
						taskDetails.put("_id", id);
						taskDetails.put("Key", ky2);
						taskDetails.put("Name", bpmnProcessId);
						taskDetails.put("Process", elementId);
						taskDetails.put("assignee", assignee);
						taskDetails.put("CreationTime", formattedDate);
						taskDetails.put("Variable", variableList);
						System.err.println(taskDetails);
						getActiveList.add(taskDetails);
					}
				}
			}
		}
		return getActiveList;
	}

	// API : 3 Claim : Add assignee :
	@CrossOrigin
	@PostMapping("/surge/camunda/tasklist/update-assignee")
	public Map updateAssignee(@RequestBody Map jsonData) {
		String indexStr = (String) jsonData.get("_index");
		String docId = (String) jsonData.get("_id");
		String assignee = (String) jsonData.get("assignee");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// String updateUrl = "http://localhost:9200/" + indexStr + "/_update/" + docId;
		String updateUrl = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_update/" + docId;
		String updateAssigneeQuery = "{\"doc\":{\"value\":{\"customHeaders\":{\"io.camunda.zeebe:assignee\":\""
				+ assignee + "\"}}}}";
		HttpEntity<String> httpEntity = new HttpEntity<String>(updateAssigneeQuery, headers);
		System.out.println("this is the httpEntity : " + httpEntity);
		ResponseEntity<String> respEntity = restTemp.exchange(updateUrl, HttpMethod.POST, httpEntity, String.class,
				headers);
		return jsonData;
	}
	// API : 4 Unclaim : Remove Assignee :
////////////////////////////remove assignee////////////////

	@CrossOrigin
	@PostMapping("/surge/camunda/tasklist/remove-assignee-customeHeader")
	public String removeAssignee(@RequestBody Map jsonData) {
		String indexStr = (String) jsonData.get("_index");
		String docId = (String) jsonData.get("_id");
		System.out.println("doc id ------------>" + docId);

		String updateQuery = "{\"script\":{\"source\":\"ctx._source.value.customHeaders.remove('io.camunda.zeebe:assignee')\",\"lang\":\"painless\"}}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String updateUrl = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_update/" + docId;
		HttpEntity<String> httpEntity = new HttpEntity<String>(updateQuery, headers);
		ResponseEntity<Map> respEntity = restTemp.exchange(updateUrl, HttpMethod.POST, httpEntity, Map.class, headers);
		Map respEntityMap = respEntity.getBody();
		String result = (String) respEntityMap.get("result");
		if ("updated".equals(result)) {
			System.out.println("Assignee field removed successfully");
		} else {
			System.out.println("Failed to remove assignee field");
		}
		return null;
	}


	// Filter Parts :

	/////////////// claimed by me /////////

	// Get Claim from new index :
	@CrossOrigin
	@GetMapping("/claimedByMeNewIndex/{assigne}")
	public List claimedByMeNewIndex(@PathVariable String assigne) throws Exception {
		String proInsKeyFromZeebe = null;
		List<Map> finalList = new ArrayList<>();
		List<Map> claimedByMeList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/surge_c8_custom_tasklist_all_active*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(zeebeRecordJobURL, Map.class);
		Map respBody = forEntity.getBody();
		Map hitsObj = (Map) respBody.get("hits");
		List<Map> hitsObjList = (List) hitsObj.get("hits");
		Map sourceObj = null;
		long getkey = 0;
		for (Map hitObj : hitsObjList) {
			sourceObj = (Map) hitObj.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			Map customHeaders = (Map) valueObj.get("customHeaders");
			if (customHeaders.containsKey("io.camunda.zeebe:assignee")) {
				String getAssignee = (String) customHeaders.get("io.camunda.zeebe:assignee");
				if (getAssignee.equals(assigne)) {
					Map taskDetails = new HashMap<>();
					getkey = (long) sourceObj.get("key");
					long timestamp = (long) sourceObj.get("timestamp");
					Date date = new Date(timestamp);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDate = sdf.format(date);
					String bpmnProcessId = (String) valueObj.get("bpmnProcessId");
					String elementId = (String) valueObj.get("elementId");
					customHeaders = (Map) valueObj.get("customHeaders");
					String assignee = (String) customHeaders.get("io.camunda.zeebe:assignee");
					String index = (String) hitObj.get("_index");
					String id = (String) hitObj.get("_id");
					taskDetails.put("_index", index);
					taskDetails.put("_id", id);
					taskDetails.put("Name", bpmnProcessId);
					taskDetails.put("Process", elementId);
					taskDetails.put("Key", getkey);
					taskDetails.put("assignee", assignee);
					taskDetails.put("CreationTime", formattedDate);
					claimedByMeList.add(taskDetails);
				}
			}
		}
		return claimedByMeList;
	}

	////////// unclaim task ///////////////

	@CrossOrigin
	@GetMapping("/unclaimedByMeNewIndex")
	public List unclaimedByMeNewIndex() throws Exception {
		String proInsKeyFromZeebe = null;
		List<Map> unclaimedList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/surge_c8_custom_tasklist_all_active*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(zeebeRecordJobURL, Map.class);
		Map respBody = forEntity.getBody();
		Map hitsObj = (Map) respBody.get("hits");
		List<Map> hitsObjList = (List) hitsObj.get("hits");
		long getkey = 0;
		for (Map hitObj : hitsObjList) {
			Map _source = (Map) hitObj.get("_source");
			Map valueObj = (Map) _source.get("value");
			Map customHeaders = (Map) valueObj.get("customHeaders");
			if (!customHeaders.containsKey("io.camunda.zeebe:assignee")) {
				Map taskDetails = new HashMap<>();
				getkey = (long) _source.get("key");
				long timestamp = (long) _source.get("timestamp");
				Date date = new Date(timestamp);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = sdf.format(date);
				String bpmnProcessId = (String) valueObj.get("bpmnProcessId");
				String elementId = (String) valueObj.get("elementId");
				customHeaders = (Map) valueObj.get("customHeaders");
				String assignee = (String) customHeaders.get("io.camunda.zeebe:assignee");
				String index = (String) hitObj.get("_index");
				String id = (String) hitObj.get("_id");
				taskDetails.put("_index", index);
				taskDetails.put("_id", id);
				taskDetails.put("Name", bpmnProcessId);
				taskDetails.put("Process", elementId);
				taskDetails.put("Key", getkey);
				taskDetails.put("assignee", assignee);
				taskDetails.put("CreationTime", formattedDate);
				unclaimedList.add(taskDetails);
			}
		}
		return unclaimedList;
	}

	/////////////// completed task //////////////

	@CrossOrigin
	@GetMapping("/completedField")
	public List completedField() throws Exception {
		List<Map> completeList = new ArrayList<>();
		String proInsKeyFromZeebe = null;
		List finalList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/surge_c8_custom_tasklist_complete*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(zeebeRecordJobURL, Map.class);
		Map respBody = forEntity.getBody();
		Map hitsObj = (Map) respBody.get("hits");
		List<Map> hitsObjList = (List) hitsObj.get("hits");
		Map sourceObj = null;
		Set<String> allInsIdList = new HashSet<>();
		Map customHeaders = null;
		for (Map completeObj : hitsObjList) {
			Map taskDetails = new HashMap<>();
			sourceObj = (Map) completeObj.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			String intent = (String) sourceObj.get("intent");
			if ("COMPLETED".equals(intent)) {
				Map _source = (Map) completeObj.get("_source");
				Map value = (Map) _source.get("value");
				long getkey = (long) _source.get("key");
				long timestamp = (long) _source.get("timestamp");
				Date date = new Date(timestamp);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = sdf.format(date);
				String ky2 = String.valueOf(getkey);
				String bpmnProcessId = (String) value.get("bpmnProcessId");
				String elementId = (String) value.get("elementId");
				customHeaders = (Map) value.get("customHeaders");
				String assignee1 = (String) customHeaders.get("io.camunda.zeebe:assignee");
				taskDetails.put("Key", ky2);
				taskDetails.put("Name", bpmnProcessId);
				taskDetails.put("Process", elementId);
				taskDetails.put("assignee", assignee1);
				taskDetails.put("CreationTime", formattedDate);
				completeList.add(taskDetails);
			}
		}
		return completeList;
	}

	// after completed :

	@CrossOrigin
	@PostMapping("/completeTask1/{jobKey}/{assigneName}")
	public List completeTask1(@PathVariable Long jobKey, @PathVariable String assigneName, @RequestBody Map var)
			throws Exception {
		System.out.println("enter.....");

		zeebeClient.newCompleteCommand(jobKey).variables(var).send();
		Thread.sleep(500);
		System.out.println("Task completed successfully from UI ");
		List completeMethod = completeMethod(jobKey, assigneName);
		return completeMethod;
	}

	// after completed :

	public List completeMethod(Long jobKey, String assigneName) throws Exception {
		Map mp = new HashMap<>();
		List<Map> completeList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(zeebeRecordJobURL, Map.class);
		Map respBody = forEntity.getBody();
		Map hitsObj = (Map) respBody.get("hits");
		List<Map> hitsObjList = (List) hitsObj.get("hits");
		Map sourceObj = null;
		Set<String> allInsIdList = new HashSet<>();
//		Map customHeaders = null;
		for (Map completeObj : hitsObjList) {
			Map taskDetails = new HashMap<>();
			sourceObj = (Map) completeObj.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			String intent = (String) sourceObj.get("intent");
			if ("COMPLETED".equals(intent)) {
				Map _source = (Map) completeObj.get("_source");
				long getkey = (long) _source.get("key");
				String ky2 = String.valueOf(getkey);
				String valueOfjobKey = String.valueOf(jobKey);
				// checks :
				if (valueOfjobKey.equals(ky2)) {
					Map customHeaders = (Map) valueObj.get("customHeaders");
					customHeaders.put("io.camunda.zeebe:assignee", assigneName);
					completeList.add(completeObj);
				}
			}
		}
		ResponseEntity<Map> allActiveListOfTasksCustomize = tasklistUtilities.addDocsToNewIndexComplete(completeList);
		List<Map<String, Object>> items = (List<Map<String, Object>>) allActiveListOfTasksCustomize.getBody()
				.get("items");
		List finalLeftSideResList = new ArrayList();
		for (Map<String, Object> item : items) {
			Map<String, String> indexInfo = (Map<String, String>) item.get("update");
			String docId = indexInfo.get("_id");
			String getIndexURL = "http://localhost:9200/surge_c8_custom_tasklist_complete/_doc/" + docId;
			ResponseEntity<Map> docDetailsResponse = restTemp.getForEntity(getIndexURL, Map.class);
			if (docDetailsResponse.getStatusCode().is2xxSuccessful()) {
				Map docDetails = docDetailsResponse.getBody();
				String documentId = (String) docDetails.get("_id");
				Map getsource = (Map) docDetails.get("_source");
				Map getvalue = (Map) getsource.get("value");
				Map getcustomHeaders = (Map) getvalue.get("customHeaders");
				String getAssignee = (String) getcustomHeaders.get("io.camunda.zeebe:assignee");
				String bpmnProcessId = (String) getvalue.get("bpmnProcessId");
				String elementId = (String) getvalue.get("elementId");
				Long creationTimestamp = (Long) getsource.get("timestamp");
				Long getKey = (Long) getsource.get("key");
				Date date = new Date(creationTimestamp);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formattedDate = sdf.format(date);
				Map finalLeftResMap = new HashMap();
				finalLeftResMap.put("_id", documentId);
				finalLeftResMap.put("assignee", getAssignee);
				finalLeftResMap.put("Name", bpmnProcessId);
				finalLeftResMap.put("Process", elementId);
				finalLeftResMap.put("CreationTime", formattedDate);
				finalLeftResMap.put("Key", getKey);
				finalLeftSideResList.add(finalLeftResMap);
			} else {
				System.err.println("Failed to retrieve document details: " + docDetailsResponse.getStatusCode());
				// Handle failure
			}

		}
		return completeList;
	}

	
	
	
	
	
	
	
	@CrossOrigin
	@GetMapping("/getActiveTaskDetailsByID22/{taskID}")
	public List getAllDetailByTaskID22(@PathVariable String taskID) throws Exception {
		System.err.println("enter");
		List variableList = new ArrayList<>();
		List finalList = new ArrayList<>();
		String findinskey= null;
		
		String zeebeRecordJobURL = "http://localhost:9200/zeebe*/_search?size=1000&from=0";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);	
		String queryJson = "{\"query\":{\"term\":{\"intent\":\"CREATED\"}}}";
		
		HttpEntity<String> requestEntity = new HttpEntity<>(queryJson, headers);
		
		ResponseEntity<Map> response = restTemp.postForEntity(zeebeRecordJobURL, requestEntity, Map.class);
		Map body = response.getBody();
		Map hitsobj = (Map)body.get("hits");
		List<Map> hitslst= (List<Map>)hitsobj.get("hits");
		
		for(Map ref: hitslst) {
			Map _sourceob = (Map)ref.get("_source");
			long keyobj =(long)_sourceob.get("key");
			String valueOfkey = String.valueOf(keyobj);
			if(valueOfkey.equals(taskID)) {
				if(_sourceob.containsKey("value")) {
					Map valueobj = (Map)_sourceob.get("value");	
					long processInstanceKeyobj=(long)valueobj.get("processInstanceKey");
					 findinskey = String.valueOf(processInstanceKeyobj);
				}
			}
		}
		
		// get variable :
		
		for(Map ref: hitslst) {
			Map _sourceob = (Map)ref.get("_source");
			long keyobj =(long)_sourceob.get("key");
			String valueOfkey = String.valueOf(keyobj);
				if(_sourceob.containsKey("value")) {
					Map valueobj = (Map)_sourceob.get("value");	
					if(valueobj.containsKey("processInstanceKey")) {
					long processInstanceKeyobj=(long)valueobj.get("processInstanceKey");
					 String getinskey = String.valueOf(processInstanceKeyobj);
					 if(findinskey.equals(getinskey)) {
						 if(valueobj.containsKey("name")) {
							 String name=(String) valueobj.get("name");
							 String value= (String) valueobj.get("value");
							
								ObjectMapper objectMapper = new ObjectMapper();
								JsonNode jsonNode = objectMapper.readTree(value);
								String stringValue = jsonNode.asText();
								Map mp = new HashMap<>();
								mp.put(name, stringValue);
								variableList.add(mp);
						 }
					 }
				}
				}
		}
		
		// step 2: for getting from complete details
 
//		String zeebeRecordJobURLs = "http://localhost:9200/zeebe-record_job*/_search?size=1000&from=0";
		// complete index  :
		String zeebeRecordJobURLs ="http://localhost:9200/surge_c8_custom_tasklist_complete*/_search";
		
		
		HttpHeaders headers1 = new HttpHeaders();
		headers1.setContentType(MediaType.APPLICATION_JSON);	
		String queryJsonComplete = "{\"query\":{\"term\":{\"intent\":\"COMPLETED\"}}}";
		HttpEntity<String> requestEntity1 = new HttpEntity<>(queryJsonComplete, headers1);
		ResponseEntity<Map> response1 = restTemp.getForEntity(zeebeRecordJobURLs, Map.class);
		Map respbody2 = response1.getBody();	
		Map hitsob=(Map)respbody2.get("hits");
		List<Map> hitsls = (List<Map>)hitsob.get("hits");
		String _index = null;
		String _id= null;
		String bpmnProcessId =null;
		String elementId= null;
		String assigne= null;
		String formattedDate=null;
		long key =0;
		
		long timestamp=0;
		
		for(Map refs: hitsls) {
			 _index= (String) refs.get("_index");
			 _id= (String)refs.get("_id");
			 Map _sources = (Map)refs.get("_source");
			 Map values= (Map)_sources.get("value");
			  bpmnProcessId = (String)values.get("bpmnProcessId");
			  elementId= (String)values.get("elementId");
			  key=(long)_sources.get("key");
			  timestamp=(long)_sources.get("timestamp");
			  Date date = new Date(timestamp);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				formattedDate = sdf.format(date);
			  Map customHeaders= (Map)values.get("customHeaders");
			   assigne= (String)customHeaders.get("io.camunda.zeebe:assignee");
		}
		Map mpmm= new HashMap<>();
		mpmm.put("_index", _index);
		mpmm.put("_id", _id);
		mpmm.put("Process", bpmnProcessId);
		mpmm.put("Name", elementId);
		mpmm.put("Key", key);
		mpmm.put("assignee", assigne);
		mpmm.put("CreationTime", formattedDate);
		mpmm.put("CompletionTime", formattedDate);
		mpmm.put("Variable", variableList);
		
		
		finalList.add(mpmm);
		return finalList;
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
