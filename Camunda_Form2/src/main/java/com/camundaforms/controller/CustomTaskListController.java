package com.camundaforms.controller;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
		List finalLeftSideResList = new ArrayList();
//		ResponseEntity<Map> docDetailsResponse = null;
		try {
			if (allActiveListOfTasksCustomize != null && allActiveListOfTasksCustomize.getBody() != null) {

				List<Map<String, Object>> items = (List<Map<String, Object>>) allActiveListOfTasksCustomize.getBody()
						.get("items");
				for (Map<String, Object> item : items) {
					// Extract document ID from the item

					Map<String, String> indexInfo = (Map<String, String>) item.get("update");
					String docId = indexInfo.get("_id");
					// Use the document ID to get document details
					String getIndexURL = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_doc/" + docId;
					ResponseEntity<Map> docDetailsResponse = docDetailsResponse = restTemp.getForEntity(getIndexURL,
							Map.class);
					if (docDetailsResponse.getStatusCode().is2xxSuccessful()) {
						Map docDetails = docDetailsResponse.getBody();
						String documentId = (String) docDetails.get("_id");
						Map getsource = (Map) docDetails.get("_source");
						Map getvalue = (Map) getsource.get("value");
						
					

						Long processDefinitionKey = (Long) getvalue.get("processDefinitionKey");
						
						Long processInstanceKey = (Long) getvalue.get("processInstanceKey");

						Map getcustomHeaders = (Map) getvalue.get("customHeaders");

						String zeebeFormKey = (String) getcustomHeaders.get("io.camunda.zeebe:formKey");

						// String zeebeFormKey = (String)
						// customHeadersMap.get("io.camunda.zeebe:formKey");
						if (zeebeFormKey != null) {
							String[] str = zeebeFormKey.split("camunda-forms:bpmn:");
							zeebeFormKey = str[1];
						}

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
						finalLeftResMap.put("zeebeFormKey", zeebeFormKey);
						finalLeftResMap.put("Name", bpmnProcessId);
						finalLeftResMap.put("Process", elementId);
						finalLeftResMap.put("CreationTime", formattedDate);
						finalLeftResMap.put("Key", getKey);
						finalLeftResMap.put("processInstanceKey", processInstanceKey);
						finalLeftResMap.put("processDefinitionKey", processDefinitionKey);

						Map finalMap = new HashMap();

						finalMap.put(getKey, finalLeftResMap);

						finalLeftSideResList.add(finalMap);
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

	
	
	
///////////////////// complete task //////////////////////////////
	
	@CrossOrigin
	@PostMapping("/completeTaskAndAddToIndex/{jobKey}/{assigneName}")
	public ResponseEntity<Map<String, String>> completeTaskAndAddToIndex(@PathVariable Long jobKey, @PathVariable String assigneName, @RequestBody Map variableMap)
			throws Exception {
		
		 try {
	            zeebeClient.newCompleteCommand(jobKey).variables(variableMap).send();
	            CompletableFuture.runAsync(() -> {
	                try {
	                    Thread.sleep(5000); // Delay after completing the task
	                    List completeMethod = completeMethod(jobKey, assigneName);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            });
	            Map<String, String> response = new HashMap<>();
	            response.put("message", jobKey +" "+ "Task completed successfully");
	            return ResponseEntity.ok(response);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to complete task"));
	        }
	    }
	

	// after completed :

	public List completeMethod(Long jobKey, String assigneName) throws Exception {
		Map mp = new HashMap<>();
		String documentId = null;
		String zeebe_id = null;
		List<Map> completeList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(zeebeRecordJobURL, Map.class);
		Map respBody = forEntity.getBody();
		Map hitsObj = (Map) respBody.get("hits");
		List<Map> hitsObjList = (List) hitsObj.get("hits");
		Map sourceObj = null;
		Set<String> allInsIdList = new HashSet<>();
		for (Map completeObj : hitsObjList) {
			Map taskDetails = new HashMap<>();
			sourceObj = (Map) completeObj.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			String intent = (String) sourceObj.get("intent");
			if ("CREATED".equals(intent)) {
				Map _source = (Map) completeObj.get("_source");
				long getkey = (long) _source.get("key");
				String ky2 = String.valueOf(getkey);
				String valueOfjobKey = String.valueOf(jobKey);
				if (valueOfjobKey.equals(ky2)) {
					zeebe_id = (String) completeObj.get("_id");
				}
			}
		}
		for (Map completeObj : hitsObjList) {
			Map taskDetails = new HashMap<>();
			sourceObj = (Map) completeObj.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			String intent = (String) sourceObj.get("intent");
			// System.out.println("kannan bro1");
			if ("COMPLETED".equals(intent)) {
				Map _source = (Map) completeObj.get("_source");
				long getkey = (long) _source.get("key");
				String ky2 = String.valueOf(getkey);
				String valueOfjobKey = String.valueOf(jobKey);
				// System.out.println("kannan bro2.."+valueOfjobKey);
				if (valueOfjobKey.equals(ky2)) {
					System.out.println("kannan bro3.." + valueOfjobKey);
					Map customHeaders = (Map) valueObj.get("customHeaders");
					customHeaders.put("io.camunda.zeebe:assignee", assigneName);
					completeList.add(completeObj);
					System.err.println("this record move to index: " + completeList);
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
				documentId = (String) docDetails.get("_id");
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

		// remove index from all active

		String newIndexURL1 = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_delete_by_query";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		System.err.println("documentId :" + documentId);
		String queryJson = "{\"query\":{\"match\":{\"_id\":\"" + zeebe_id + "\"}}}";
		HttpEntity<String> requestEntity = new HttpEntity<>(queryJson, headers);
		ResponseEntity<Map> response = restTemp.postForEntity(newIndexURL1, requestEntity, Map.class);
		System.err.println("all active task deleted successfull......");
		return completeList;
	}

	@CrossOrigin
	@GetMapping("/getActiveTaskDetailsByID22/{taskID}")
	public List getAllDetailByTaskID22(@PathVariable String taskID) throws Exception {
		System.err.println("enter");
		List variableList = new ArrayList<>();
		List finalList = new ArrayList<>();
		String findinskey = null;

		String zeebeRecordJobURL = "http://localhost:9200/zeebe*/_search?size=1000&from=0";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String queryJson = "{\"query\":{\"term\":{\"intent\":\"CREATED\"}}}";

		HttpEntity<String> requestEntity = new HttpEntity<>(queryJson, headers);

		ResponseEntity<Map> response = restTemp.postForEntity(zeebeRecordJobURL, requestEntity, Map.class);
		Map body = response.getBody();
		Map hitsobj = (Map) body.get("hits");
		List<Map> hitslst = (List<Map>) hitsobj.get("hits");

		for (Map ref : hitslst) {
			Map _sourceob = (Map) ref.get("_source");
			long keyobj = (long) _sourceob.get("key");
			String valueOfkey = String.valueOf(keyobj);
			if (valueOfkey.equals(taskID)) {
				if (_sourceob.containsKey("value")) {
					Map valueobj = (Map) _sourceob.get("value");
					long processInstanceKeyobj = (long) valueobj.get("processInstanceKey");
					findinskey = String.valueOf(processInstanceKeyobj);
				}
			}
		}

		// get variable :

		for (Map ref : hitslst) {
			Map _sourceob = (Map) ref.get("_source");
			long keyobj = (long) _sourceob.get("key");
			String valueOfkey = String.valueOf(keyobj);
			if (_sourceob.containsKey("value")) {
				Map valueobj = (Map) _sourceob.get("value");
				if (valueobj.containsKey("processInstanceKey")) {
					long processInstanceKeyobj = (long) valueobj.get("processInstanceKey");
					String getinskey = String.valueOf(processInstanceKeyobj);
					if (findinskey.equals(getinskey)) {
						if (valueobj.containsKey("name")) {
							String name = (String) valueobj.get("name");
							String value = (String) valueobj.get("value");

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
		// complete index :
		String zeebeRecordJobURLs = "http://localhost:9200/surge_c8_custom_tasklist_complete*/_search";

		HttpHeaders headers1 = new HttpHeaders();
		headers1.setContentType(MediaType.APPLICATION_JSON);
		String queryJsonComplete = "{\"query\":{\"term\":{\"intent\":\"COMPLETED\"}}}";
		HttpEntity<String> requestEntity1 = new HttpEntity<>(queryJsonComplete, headers1);
		ResponseEntity<Map> response1 = restTemp.getForEntity(zeebeRecordJobURLs, Map.class);
		Map respbody2 = response1.getBody();
		Map hitsob = (Map) respbody2.get("hits");
		List<Map> hitsls = (List<Map>) hitsob.get("hits");
		String _index = null;
		String _id = null;
		String bpmnProcessId = null;
		String elementId = null;
		String assigne = null;
		String formattedDate = null;
		long key = 0;

		long timestamp = 0;

		for (Map refs : hitsls) {
			_index = (String) refs.get("_index");
			_id = (String) refs.get("_id");
			Map _sources = (Map) refs.get("_source");
			Map values = (Map) _sources.get("value");
			bpmnProcessId = (String) values.get("bpmnProcessId");
			elementId = (String) values.get("elementId");
			key = (long) _sources.get("key");
			timestamp = (long) _sources.get("timestamp");
			Date date = new Date(timestamp);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formattedDate = sdf.format(date);
			Map customHeaders = (Map) values.get("customHeaders");
			assigne = (String) customHeaders.get("io.camunda.zeebe:assignee");
		}
		Map mpmm = new HashMap<>();
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

	// index delete by key :

	@GetMapping("delete/{key}")
	public String indexdeletebykey(@PathVariable String key) {
		String newIndexURL1 = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_delete_by_query";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
//		String queryJson = "{\"query\":{\"match_all\":{}}}";

		String queryJson = "{\"query\":{\"match\":{\"_id\":\"" + key + "\"}}}";
		HttpEntity<String> requestEntity = new HttpEntity<>(queryJson, headers);
		ResponseEntity<Map> response = restTemp.postForEntity(newIndexURL1, requestEntity, Map.class);
		System.err.println("all active task deleted successfull......");
		return "delete success.....";
	}

	// Get Forms Details :

	@GetMapping("/getformbyinstanceKey/{proInstanceKey}")
	public Map sample(@PathVariable String proInstanceKey) {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate rest = new RestTemplate();
		Map camundaFormsMap = new HashMap<>();
		String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		String zeebeRecordURL = "http://localhost:9200/zeebe*/_search?size=1000";
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
				String processDefinitionKey = String.valueOf(processDef);
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
		return camundaFormsMap;
	}

	@GetMapping("/getActiveDetails/{proInstanceKey}")
	public List GetActiveDetails(@PathVariable String proInstanceKey) throws Exception {
		Map<String, Object> finalLeftResMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate rest = new RestTemplate();
		Map camundaFormsMap = new HashMap<>();
		Map valueObj = null;
		long processIns = 0;
		String getformat = null;
		String actproDefinitionKey = null;
		String zeebeprocDefinitionKey = null;
		// variables :
		String documentId = null;
		String getAssignee = null;
		String bpmnProcessId = null;
		String elementId = null;
		String formattedDate = null;
		List<Map<String, Object>> indexAllActiveTasks = indexAllActiveTasks();
		List finalLeftSideResList = new ArrayList<>();
		String actTaskkey = null;
		List<String> actList = new ArrayList<String>();
		for (Map ref : indexAllActiveTasks) {
			long keyObj = (long) ref.get("Key");
			actTaskkey = String.valueOf(keyObj);
			if (proInstanceKey.equals(actTaskkey)) {
				documentId = (String) ref.get("_id");
				getAssignee = (String) ref.get("assignee");
				bpmnProcessId = (String) ref.get("Name");
				elementId = (String) ref.get("Process");
				formattedDate = (String) ref.get("CreationTime");
			}
			actList.add(actTaskkey);
		}
		String zeebeRecordURL = "http://localhost:9200/zeebe*/_search?size=1000";
		ResponseEntity<Map> forEntity = rest.getForEntity(zeebeRecordURL, Map.class);
		Map respbody = forEntity.getBody();
		Map hitsObj = (Map) respbody.get("hits");
		List<Map> hitsObjList = (List<Map>) hitsObj.get("hits");
		for (String id : actList) {
			for (Map refs : hitsObjList) {
				Map sourceObj = (Map) refs.get("_source");
				long keyObj1 = (long) sourceObj.get("key");
				String elasearchkey = String.valueOf(keyObj1);
				valueObj = (Map) sourceObj.get("value");
				if (id.equals(elasearchkey)) {
					if (elasearchkey.equals(proInstanceKey)) {
						long activeprocessDefinitionKey = (long) valueObj.get("processDefinitionKey");
						actproDefinitionKey = String.valueOf(activeprocessDefinitionKey);
						if (valueObj.containsKey("customHeaders")) {
							Map customHeaders = (Map) valueObj.get("customHeaders");
							String getformkey = (String) customHeaders.get("io.camunda.zeebe:formKey");
							if (getformkey != null) {
								String[] str = getformkey.split("camunda-forms:bpmn:");
								getformat = str[1];
							}
						}
					}
				}
			}
		}
		String xmlString = null;
		String zeebeResourceObj = null;
		for (Map zeeberef : hitsObjList) {
			Map zeebeSourceObj = (Map) zeeberef.get("_source");
			Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
			zeebeResourceObj = (String) zeebeValueObj.get("resource");
			if (zeebeResourceObj != null) {
				long pDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
				zeebeprocDefinitionKey = String.valueOf(pDefinitionKey);
				if (zeebeprocDefinitionKey.equals(actproDefinitionKey)) {
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
							if (userTaskFormId.equals(getformat)) {
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
								camundaFormsMap.put(userTaskFormId, componentsNode);
								finalLeftResMap.put("Key", proInstanceKey);
								finalLeftResMap.put("_id", documentId);
								finalLeftResMap.put("assignee", getAssignee);
								finalLeftResMap.put("Name", bpmnProcessId);
								finalLeftResMap.put("Process", elementId);
								finalLeftResMap.put("CreationTime", formattedDate);
								finalLeftResMap.put("Variables", camundaFormsMap);
								finalLeftSideResList.add(finalLeftResMap);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return finalLeftSideResList;
	}

	////////////////////////// test //////////////////////

	@GetMapping("/activeTaskDeatailbyId/{jobKey}")
	public List<Map<String, Object>> activeTaskDeatailbyId(@PathVariable long jobKey) throws Exception {

		

		List<Map<String, Object>> finalReslist = new ArrayList<>();

		
		List finalReponseList = new ArrayList();
		
		List<Map<String, Object>> indexAllActiveTasks = indexAllActiveTasks();
		
		Map<String, Object> resultMap = new HashMap<>();

		for (Map<String, Object> map : indexAllActiveTasks) {
			if (map.containsKey(jobKey)) {
				resultMap = map;
				break;
			}
		}
		System.out.println("resultMap----" + resultMap);

		if (resultMap != null && resultMap.containsKey(jobKey)) {

			Map innerMap = (Map) resultMap.get(jobKey);

			Long processDefinitionKey = (Long) innerMap.get("processDefinitionKey");
			Long processInstanceKey = (Long) innerMap.get("processInstanceKey");
			String zeebeFormKey = (String) innerMap.get("zeebeFormKey");


			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			ObjectMapper mapper = new ObjectMapper();

			Map zeebeRecordJobMap = new HashMap();
			Map finalResponseMap = new HashMap();
			Map finalInsResponseMap = new HashMap();
			
			Map mapResp = new HashMap();
			
			List<Map<String, Object>> componentsList = new ArrayList<>();

			/////////////////////////////////////////////
			//////////duplicate //////////
			
			
			
			String zeebeURL = "http://localhost:9200/zeebe*/_search?size=1000";
			String processDefinitionKeyQuery = "{\"query\":{\"match\":{\"value.processDefinitionKey\":" + processDefinitionKey + "}}}";
			
			HttpEntity<String> zeebeHttpEntity = new HttpEntity<String>(processDefinitionKeyQuery,headers);
			ResponseEntity<Map> zeebeResponseEntity = restTemp.exchange(zeebeURL, HttpMethod.POST, zeebeHttpEntity,Map.class);
			
			Map zeebeResponseEntityMap = zeebeResponseEntity.getBody();
			
			//ResponseEntity<Map> responseEntity2 = restTemp.getForEntity(zeebeURL, Map.class);
			//Map responseBody2 = responseEntity2.getBody();
			Map zhitsMap = (Map) zeebeResponseEntityMap.get("hits");

			List zhitsList = (List) zhitsMap.get("hits");

			boolean processInstanceFound = false;

			for (Object zhits : zhitsList) {

				Map zsingleHit = (Map) zhits;

				Map zsourceMap = (Map) zsingleHit.get("_source");

				Map zvalueMap = (Map) zsourceMap.get("value");

				if (zvalueMap.containsKey("resource")) {

					String resourceValue = (String) zvalueMap.get("resource");

					byte[] decodedBytes = Base64.getDecoder().decode(resourceValue);

					String xmlString = new String(decodedBytes, StandardCharsets.UTF_8);

					System.out.println("decodedBytes,,,,,," + xmlString);

					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder;
						builder = factory.newDocumentBuilder();
						Document document = builder.parse(new InputSource(new StringReader(xmlString)));
						NodeList userTaskForms = document.getElementsByTagName("zeebe:userTaskForm");
						
						
						
						
						for (int i = 0; i < userTaskForms.getLength(); i++) {
							Element userTaskFormElement = (Element) userTaskForms.item(i);
							String userTaskFormId = userTaskFormElement.getAttribute("id");
							if (userTaskFormId.equals(zeebeFormKey)) {
								String jsonContent = userTaskFormElement.getTextContent();
								JsonNode jsonNode = mapper.readTree(jsonContent);
								JsonNode componentsNode = jsonNode.path("components");

								System.out.println("componentsNode----------->" + componentsNode);

								// Convert componentsNode to List<Map<String, Object>>
								// List<Map<String, Object>> componentsList = new ArrayList<>();

								//Map<String, Object> componentMap = new HashMap<>();
								
								for (JsonNode component : componentsNode) {
									Map<String, Object> componentMap = new HashMap<>();
									Iterator<Map.Entry<String, JsonNode>> fieldsIterator = component.fields();
									while (fieldsIterator.hasNext()) {
										Map.Entry<String, JsonNode> fieldEntry = fieldsIterator.next();
										componentMap.put(fieldEntry.getKey(), fieldEntry.getValue().asText());
									}
									componentsList.add(componentMap);
								}
								//componentsList.add(componentMap);
								String zeebeRecordVariableURL = "http://localhost:9200/zeebe-record_variable_*/_search?size=1000";
								String zeebeVariableQuerybyPid = "{\"query\":{\"term\":{\"value.processInstanceKey\":"
										+ processInstanceKey + "}}}";
								HttpEntity<String> httpEntity2 = new HttpEntity<>(zeebeVariableQuerybyPid, headers);

								ResponseEntity<Map> responseEntity3 = restTemp.exchange(zeebeRecordVariableURL,
										HttpMethod.POST, httpEntity2, Map.class, headers);
								Map zeebeRecordVariableResBody = responseEntity3.getBody();

								System.out.println("kannnannnnn---->" + zeebeRecordVariableResBody);

								// Check if the response contains the processInstanceKey
								if (zeebeRecordVariableResBody.containsKey("hits")) {

									Map varHitsMap = (Map) zeebeRecordVariableResBody.get("hits");

									List varHitsList = (List) varHitsMap.get("hits");

									// List<Map<String, Object>> varHitsList = (List<Map<String, Object>>)
									// zeebeRecordVariableResBody.get("hits");

									if (!varHitsList.isEmpty()) {
										for (Object hit : varHitsList) {
											Map singleHitMap = (Map) hit;

											Map getsource = (Map) singleHitMap.get("_source");
											Map getvalue = (Map) getsource.get("value");

											Long hitProcessInstanceKey = (Long) getvalue.get("processInstanceKey");

											if (hitProcessInstanceKey.equals(processInstanceKey)) {
												processInstanceFound = true;
												String getName = (String) getvalue.get("name");
												String varValue = (String) getvalue.get("value");
												varValue = varValue.replaceAll("\"", "");
												Map<String, Object> zeebeRecordVariableMap = new HashMap<>();
												zeebeRecordVariableMap.put(getName, varValue);
												System.out.println(
														"zeebeRecordVariableMap------>" + zeebeRecordVariableMap);
												// Update componentsList with zeebeRecordVariableMap
												for (Map<String, Object> component : componentsList) {
													String componentKey = (String) component.get("key");
													if (componentKey.equals(getName)) {
														component.putAll(zeebeRecordVariableMap);
														break;
													}
												}
											}
										}
									}
								}

								finalResponseMap.put(jobKey, innerMap);
								finalResponseMap.put("componentsNode", componentsList);
								
								
								mapResp.put("taskDetails", innerMap);
								mapResp.put("componentsNode", componentsList);

							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (!processInstanceFound) {
				// If the process instance is not found in the variable records, retrieve it
				// using the zeebe URL
				String zeebeQuery = "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"key\":\"" + jobKey + "\"}}]}}}";
				HttpEntity<String> httpEntity = new HttpEntity<>(zeebeQuery, headers);
				ResponseEntity<Map> responseEntity = restTemp.exchange(zeebeURL, HttpMethod.POST, httpEntity, Map.class,
						headers);
				Map responseBody = responseEntity.getBody();
				Map hitsMap = (Map) responseBody.get("hits");
				List hitsList = (List) hitsMap.get("hits");
				for (Object hits : hitsList) {
					Map singleHit = (Map) hits;
					Map sourceMap = (Map) singleHit.get("_source");
					Map valueMap = (Map) sourceMap.get("value");
					Map zeebeVariableMap = (Map) valueMap.get("variables");
					innerMap.put("variables", zeebeVariableMap);
				}

				finalResponseMap.put(jobKey, innerMap);
				finalResponseMap.put("componentsNode", componentsList);
				
				
				
				
				mapResp.put("taskDetails", innerMap);
				mapResp.put("componentsNode", componentsList);
				
				
			}

			finalReponseList.add(mapResp);
			
		//	finalReslist.add(Collections.singletonMap(String.valueOf(processInstanceKey), finalResponseMap));



		}

		// return finalReslist;

		return finalReponseList;
	}

	@CrossOrigin
	@GetMapping("/GetCompleteFilter/{proTaskID}")
	public List GetCompleteFilter(@PathVariable String proTaskID) throws Exception {
		System.err.println("enter............");
		List lst = new ArrayList<>();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String zeebeRecordURL = "http://localhost:9200/zeebe-record_job*/_search?size=1000&from=0";
		String query = "{\r\n" + "  \"query\": {\r\n" + "    \"term\": {\r\n" + "      \"intent\": \"COMPLETED\"\r\n"
				+ "    }\r\n" + "  }\r\n" + "}";
		HttpEntity<String> httpEntity = new HttpEntity<String>(query, headers);
		ResponseEntity<Map> respEntity = restTemp.exchange(zeebeRecordURL, HttpMethod.POST, httpEntity, Map.class);
		Map body2 = respEntity.getBody();
		Map hits = (Map) body2.get("hits");
		List<Map> hitslst = (List<Map>) hits.get("hits");
		Map variables = null;
		for (Map ref : hitslst) {
			Map _source = (Map) ref.get("_source");
			long getkey = (long) _source.get("key");
			String key = String.valueOf(getkey);
			if (key.equals(proTaskID)) {
				Map value = (Map) _source.get("value");
				variables = (Map) value.get("variables");
				System.err.println("variables" + variables);
				if (variables != null) {
					String completeURL = "http://localhost:9200/surge_c8_custom_tasklist_complete*/_search?size=1000&from=0";
					ResponseEntity<Map> forEntity = restTemp.getForEntity(completeURL, Map.class);
					Map resbody = forEntity.getBody();
					Map hits1 = (Map) resbody.get("hits");
					List<Map> hitslst1 = (List<Map>) hits1.get("hits");
					for (Map refs : hitslst1) {
						Map _source1 = (Map) refs.get("_source");
						long getkey1 = (long) _source1.get("key");
						Map value1 = (Map) _source1.get("value");
						Map customHeaders = (Map) value1.get("customHeaders");
						String assignee = (String) customHeaders.get("io.camunda.zeebe:assignee");
						String ky = String.valueOf(getkey1);
						if (ky.equals(proTaskID)) {
							String Process = (String) value1.get("bpmnProcessId");
							String Name = (String) value1.get("elementId");
							long timestamp = (long) _source1.get("timestamp");
							Date date = new Date(timestamp);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String formattedDate = sdf.format(date);
							Map mp = new HashMap<>();
							mp.put("Process", Process);
							mp.put("Name", Name);
							mp.put("CompletionTime", formattedDate);
							mp.put("Key", getkey1);
							mp.put("Assignee", assignee);
							mp.put("TaskForm", variables);
							System.err.println("jhvhgv" + mp);
							lst.add(mp);
						}
					}
				}
			}
		}
		return lst;
	}

	@CrossOrigin
	@GetMapping("/getCompleteFilternew/{proInstanceKey}")
	public Map getCompleteFilternew(@PathVariable String proInstanceKey) throws Exception {
		// form details :
		JsonNode componentsNode = null;
		String process = null;
		String name = null;
		String formattedDate = null;
		String keyFromComplete1 = null;
		String assignee = null;
//	List<Map> inputForm = new ArrayList<>();
		List<JsonNode> inputForm = new ArrayList<>();
		List<Map> outputForm = new ArrayList<>();
		List<Map> formList = new ArrayList<>();
		List<Map> finalList = new ArrayList<>();
		Map finalMap = new HashMap<>();
		Map mp = new HashMap<>();
		List finalLeftSideResList = new ArrayList<>();
		String customtasklistcompleteURL = "http://localhost:9200/surge_c8_custom_tasklist_complete*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(customtasklistcompleteURL, Map.class);
		Map respBody = forEntity.getBody();
		Map camundaFormsMap = new HashMap<>();
		Map hitsObj = (Map) respBody.get("hits");
		List<Map> hitsObjList = (List) hitsObj.get("hits");
		String keyFromComplete = null;
		String procDefKey = null;
		Map sourceObj = null;
		Map valueObj = null;
		String getformat = null;
		Set<String> allInsIdList = new HashSet<>();
		Map customHeaders = null;
		List<String> allCompleteKey = new ArrayList<String>();
		String zeebeprocDefinitionKey = null;
		ObjectMapper mapper = new ObjectMapper();
		for (Map completeObj : hitsObjList) {
			sourceObj = (Map) completeObj.get("_source");
			valueObj = (Map) sourceObj.get("value");
			Map customHeaders1 = (Map) valueObj.get("customHeaders");
			assignee = (String) customHeaders1.get("io.camunda.zeebe:assignee");
			long processDefinitionKey = (long) valueObj.get("processDefinitionKey");
			long getkey = (long) sourceObj.get("key");
			keyFromComplete1 = String.valueOf(getkey);
			String procDefKey1 = String.valueOf(processDefinitionKey);
			if (proInstanceKey.equals(keyFromComplete1)) {
				process = (String) valueObj.get("bpmnProcessId");
				name = (String) valueObj.get("elementId");
				long timestamp = (long) sourceObj.get("timestamp");
				Date date = new Date(timestamp);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				formattedDate = sdf.format(date);
				keyFromComplete = keyFromComplete1;
				procDefKey = procDefKey1;
			}
		}
		String zeebeRecordURL = "http://localhost:9200/zeebe*/_search?size=10000&from=0";
		ResponseEntity<Map> forEntity1 = restTemp.getForEntity(zeebeRecordURL, Map.class);
		Map respbody1 = forEntity1.getBody();
		Map hitsObj1 = (Map) respbody1.get("hits");
		List<Map> hitsObjList1 = (List<Map>) hitsObj1.get("hits");
		for (Map refss : hitsObjList1) {
			Map sourceObj1 = (Map) refss.get("_source");
			long keyObj1 = (long) sourceObj1.get("key");
			String elasearchkey = String.valueOf(keyObj1);
			if (elasearchkey.equals(keyFromComplete)) {
				Map valueObj1 = (Map) sourceObj1.get("value");
				long activeprocessDefinitionKey = (long) valueObj1.get("processDefinitionKey");
				String actproDefinitionKey1 = String.valueOf(activeprocessDefinitionKey);
				if (valueObj1.containsKey("customHeaders")) {
					Map customHeaders1 = (Map) valueObj1.get("customHeaders");
					String getformkey = (String) customHeaders1.get("io.camunda.zeebe:formKey");
					if (getformkey != null) {
						String[] str = getformkey.split("camunda-forms:bpmn:");
						getformat = str[1];
						System.err.println("this is the form :" + getformat);
					}
				}
			}
		}
		String xmlString = null;
		String zeebeResourceObj = null;
		for (Map zeeberef : hitsObjList1) {
			Map zeebeSourceObj = (Map) zeeberef.get("_source");
			Map zeebeValueObj = (Map) zeebeSourceObj.get("value");
			zeebeResourceObj = (String) zeebeValueObj.get("resource");
			if (zeebeResourceObj != null) {
				long pDefinitionKey = (long) zeebeValueObj.get("processDefinitionKey");
				zeebeprocDefinitionKey = String.valueOf(pDefinitionKey);
				if (procDefKey.equals(zeebeprocDefinitionKey)) {
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
							if (userTaskFormId.equals(getformat)) {
								List lst = new ArrayList<>();
								HttpHeaders headers = new HttpHeaders();
								headers.setContentType(MediaType.APPLICATION_JSON);
								String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job*/_search?size=1000&from=0";
								String query = "{\r\n" + "  \"query\": {\r\n" + "    \"term\": {\r\n"
										+ "      \"intent\": \"COMPLETED\"\r\n" + "    }\r\n" + "  }\r\n" + "}";
								HttpEntity<String> httpEntity = new HttpEntity<String>(query, headers);
								ResponseEntity<Map> respEntity = restTemp.exchange(zeebeRecordJobURL, HttpMethod.POST,
										httpEntity, Map.class, headers);
								Map body2 = respEntity.getBody();
								Map hits = (Map) body2.get("hits");
								List<Map> hitslst = (List<Map>) hits.get("hits");
								Map variables = null;
								for (Map ref : hitslst) {
									Map _source = (Map) ref.get("_source");
									long getkey = (long) _source.get("key");
									String key = String.valueOf(getkey);
									if (key.equals(keyFromComplete)) {
										Map value = (Map) _source.get("value");
										Map customHeadersM = (Map) value.get("customHeaders");
										String getforms = (String) customHeadersM.get("io.camunda.zeebe:formKey");
										String[] str = getforms.split("camunda-forms:bpmn:");
										String getformat1 = str[1];
										if (userTaskFormId.equals(getformat1)) {
											variables = (Map) value.get("variables");
											outputForm.add(variables);
										}
									}
								}
								String jsonContent = userTaskFormElement.getTextContent();
								JsonNode jsonNode = mapper.readTree(jsonContent);
								componentsNode = jsonNode.path("components");
								List lss = new ArrayList<>();
								for (JsonNode componentNode : componentsNode) {
									Map mpp = new HashMap<>();
									mpp.put("Label", componentNode.path("label").asText());
									mpp.put("Type", componentNode.path("type").asText());
									mpp.put("ID", componentNode.path("id").asText());
									mpp.put("Key", componentNode.path("key").asText());
									// inputForm.add(mpp);
									// extra add this on feb 28 :
									inputForm.add(componentsNode);
									lss.add(mpp);
								}
								camundaFormsMap.put(userTaskFormId, lss);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		for (JsonNode ref : componentsNode) {
			String Key = (String) ref.path("key").asText();
			String Label = (String) ref.path("label").asText();
			String Type = (String) ref.path("type").asText();
			JsonNode layoutNode = ref.get("layout");
			for (Map ref1 : outputForm) {
				Object outValue = ref1.get(Key);
				Map mpfin = new HashMap<>();
				mpfin.put(Label, outValue);
				mpfin.put("type", Type);
				mpfin.put("label", Label);
				mpfin.put("key", Key);
				mpfin.put("layout", layoutNode);
				formList.add(mpfin);
			}
		}
		finalMap.put("Process", process);
		finalMap.put("Name", name);
		finalMap.put("CompletionTime", formattedDate);
		finalMap.put("Key", keyFromComplete);
		finalMap.put("Assignee", assignee);
		finalMap.put("Forms", formList);
		return finalMap;
	}

}