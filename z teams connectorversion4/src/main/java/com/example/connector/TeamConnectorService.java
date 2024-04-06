package com.example.connector;

import java.util.Map;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TeamConnectorService {
	
//	@Autowired
//	ZeebeClient zeebeclient;
//
//	@ZeebeWorker(name = "TeamConnector", type = "TeamConnector")
//	public void ProductionCompleted(final JobClient client, final ActivatedJob job) throws Exception {
	
	
	RestTemplate restTemplate = new RestTemplate();
	
	public void ProductionCompleted() {
//		Map<String, Object> getvar = job.getVariablesAsMap();
//		String senderEmailId = (String) getvar.get("senderEmailId");
//		String receieverEmailId = (String) getvar.get("receieverEmailId");
//		String accessToken = (String) getvar.get("accessToken");
//		Map bodys = (Map) getvar.get("body");
//		String Message = (String) bodys.get("content");
		
		
		//
		String senderEmailId = "";
		String receieverEmailId = "";
		String accessToken = "";
		String bodys = "";
		String Message = "";
		
		
		
		
		

		String urll = "https://graph.microsoft.com/v1.0/chats";

		String urlGetSenderUserId = String.format("https://graph.microsoft.com/v1.0/users/" + senderEmailId);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Content-Type", "application/json");

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<Map> response = restTemplate.exchange(urlGetSenderUserId, HttpMethod.GET, requestEntity,
				Map.class);
		Map map = response.getBody();
		String senderid = (String) map.get("id");

		String urlGetReceieverEmailId = String.format("https://graph.microsoft.com/v1.0/users/" + receieverEmailId);

		ResponseEntity<Map> response1 = restTemplate.exchange(urlGetReceieverEmailId, HttpMethod.GET, requestEntity,
				Map.class);
		Map maps = response1.getBody();

		String getReceiver = (String) maps.get("id");

		String requestBody = "{\n" + "    \"chatType\": \"oneOnOne\",\n" + "    \"members\": [\n" + "        {\n"
				+ "            \"@odata.type\": \"#microsoft.graph.aadUserConversationMember\",\n"
				+ "            \"roles\": [\n" + "                \"owner\"\n" + "            ],\n"
				+ "            \"user@odata.bind\": \"https://graph.microsoft.com/v1.0/users('" + senderid + "')\"\n"
				+ "        },\n" + "        {\n"
				+ "            \"@odata.type\": \"#microsoft.graph.aadUserConversationMember\",\n"
				+ "            \"roles\": [\n" + "                \"owner\"\n" + "            ],\n"
				+ "            \"user@odata.bind\": \"https://graph.microsoft.com/v1.0/users('" + getReceiver + "')\"\n"
				+ "        }\n" + "    ]\n" + "}";

		HttpEntity<String> requestEntitys = new HttpEntity<>(requestBody, headers);
		ResponseEntity<Map> responses = restTemplate.exchange(urll, HttpMethod.POST, requestEntitys, Map.class);

		Map body = responses.getBody();

		String chatid = (String) body.get("id");

		String sendUrl = "http://localhost:8080/api/" + chatid + "/messages";
		String messageDto = "{\r\n" + "    \"body\": {\r\n" + "        \"content\": \"" + Message + "\"\r\n"
				+ "    }\r\n" + "}";
		HttpEntity<String> requestEntityss = new HttpEntity<>(messageDto, headers);
		ResponseEntity<Map> responsess = restTemplate.exchange(sendUrl, HttpMethod.POST, requestEntityss, Map.class);
//		zeebeclient.newCompleteCommand(job.getKey()).variables(" ").send().join();
	}

}
