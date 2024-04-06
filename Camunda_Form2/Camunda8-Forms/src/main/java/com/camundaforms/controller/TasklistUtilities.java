package com.camundaforms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TasklistUtilities {

	RestTemplate restTemp = new RestTemplate();

	public List getALLActiveListOfTasksCustomize() throws Exception {
		String proInsKeyFromZeebe = null;
		List<Map> hitsObjList = null;
		List<Map> getActiveList = new ArrayList<>();
		String zeebeRecordJobURL = "http://localhost:9200/zeebe-record_job_*/_search?size=1000";
		ResponseEntity<Map> forEntity = restTemp.getForEntity(zeebeRecordJobURL, Map.class);
		Map respBody = forEntity.getBody();
		Map hitsObj = (Map) respBody.get("hits");
		hitsObjList = (List) hitsObj.get("hits");
		Map sourceObj = null;
		Set<String> allInsIdList = new HashSet<>();
		for (Map ref : hitsObjList) {
			sourceObj = (Map) ref.get("_source");
			Map valueObj = (Map) sourceObj.get("value");
			long processIns = (long) valueObj.get("processInstanceKey");
			proInsKeyFromZeebe = String.valueOf(processIns);
			allInsIdList.add(proInsKeyFromZeebe);
		}
		Map customHeaders = null;
		List<Long> idsWithOnlyCreated = new ArrayList<>();
		// get all instance ID :
		for (Object ref1 : allInsIdList) {
			String ins = (String) ref1;
			String inst = String.valueOf(ins);
			long ky2 = 0;
			Object prevObj = null;
			if (proInsKeyFromZeebe.equals(ins)) {
				Map<Long, Set<String>> stateMap = new HashMap<>();
				for (Map activeTaskObj : hitsObjList) {
					Map _source = (Map) activeTaskObj.get("_source");
					long ky = (long) _source.get("key"); // important
					String intent = (String) _source.get("intent"); // important
					if (!stateMap.containsKey(ky)) {
						stateMap.put(ky, new HashSet<String>());
					}
					stateMap.get(ky).add(intent);
				}
				for (Map.Entry<Long, Set<String>> entry : stateMap.entrySet()) {
					if (entry.getValue().contains("CREATED") && !entry.getValue().contains("COMPLETED")) {
						idsWithOnlyCreated.add(entry.getKey());
					}
				}
				for (Long refer : idsWithOnlyCreated) {
					String gekey = String.valueOf(refer);
					for (Map actTaskListObj : hitsObjList) {
						Map _source2 = (Map) actTaskListObj.get("_source");
						long keykey = (long) _source2.get("key");
						String keyValue = String.valueOf(keykey);
						if (keyValue.equals(gekey)) {
							getActiveList.add(actTaskListObj);
						}
					}
				}
			}
		}
		// addDocumentsToNewIndex(getActiveList);
		return getActiveList;
	}

	public ResponseEntity<Map> addDocumentsToNewIndex() throws Exception {
		List<Map> getActiveList = getALLActiveListOfTasksCustomize();
		int size = getActiveList.size();
		System.out.println("i want this" + size);
		String newIndexURL = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_doc/_bulk";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		ResponseEntity<Map> response = null;
		StringBuilder bulkRequestBody = new StringBuilder();
		ObjectMapper objectMapper = new ObjectMapper();
		
		if (size > 0) {
			for (Map task : getActiveList) {
				String key = (String) task.get("_id");
				bulkRequestBody.append("{ \"update\" : { \"_id\" : \"" + key + "\", \"retry_on_conflict\": 3 } }\n");
				bulkRequestBody.append("{ \"doc\" : " + objectMapper.writeValueAsString(task.get("_source"))
						+ ", \"doc_as_upsert\" : true }\n");
			}
			HttpEntity<String> requestEntity1 = new HttpEntity<>(bulkRequestBody.toString(), headers);
			response= restTemp.exchange(newIndexURL, HttpMethod.POST, requestEntity1,Map.class);
		} else {
			String newIndexURL1 = "http://localhost:9200/surge_c8_custom_tasklist_all_active/_delete_by_query";
//			HttpHeaders headers1 = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			String queryJson = "{\"query\":{\"match_all\":{}}}";
			HttpEntity<String> requestEntity = new HttpEntity<>(queryJson, headers);
			response = restTemp.postForEntity(newIndexURL1, requestEntity, Map.class);
		}
		System.out.println("this is :"+bulkRequestBody.toString());
	    if (response.getStatusCode().is2xxSuccessful()) {
	        System.out.println("Documents indexed successfully: " + response.getBody());
	    } else {
	        System.err.println("Failed to index documents: " + response.getStatusCode());
	    }
	    return response;
	}	

	// this is for the completed index :
	public ResponseEntity<Map> addDocsToNewIndexComplete(List<Map> completeList) throws Exception {
		List<Map> getActiveList = getALLActiveListOfTasksCustomize();
		String newIndexURL = "http://localhost:9200/surge_c8_custom_tasklist_complete/_doc/_bulk";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		StringBuilder bulkRequestBody = new StringBuilder();
		ObjectMapper objectMapper = new ObjectMapper();
		for (Map task : completeList) {
			String key = (String) task.get("_id");
			bulkRequestBody.append("{ \"update\" : { \"_id\" : \"" + key + "\", \"retry_on_conflict\": 3 } }\n");
			bulkRequestBody.append("{ \"doc\" : " + objectMapper.writeValueAsString(task.get("_source"))
					+ ", \"doc_as_upsert\" : true }\n");
		}
		HttpEntity<String> requestEntity = new HttpEntity<>(bulkRequestBody.toString(), headers);
		ResponseEntity<Map> response = restTemp.postForEntity(newIndexURL, requestEntity, Map.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			System.out.println("Documents indexed successfully: " + response.getBody());
		} else {
			System.err.println("Failed to index documents: " + response.getStatusCode());
		}
		return response;
	}

//		public ResponseEntity<Map> addDocsToNewIndexComplete(List<Map> completeList) {
//			// TODO Auto-generated method stub
//			return null;
//		}	
}