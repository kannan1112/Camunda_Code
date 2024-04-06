package com.example.connector;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.connector.dto.MessageDto;

@RestController
public class TeamConnectorController {

//	@Autowired
//	ZeebeClient zeebeClient;

	RestTemplate restTemplate = new RestTemplate();
	String accessToken="eyJ0eXAiOiJKV1QiLCJub25jZSI6Ik4yZFdLN2hBN2FITXZDb0p3dENqYmxta293SFBXR1VXZ3B6eDB1T0JJMWciLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9hNGM3NDI3MC0wOWJhLTRmYWMtYjJiNi0zOWRhZDE2YTE1YWUvIiwiaWF0IjoxNjkzMTk4NDcyLCJuYmYiOjE2OTMxOTg0NzIsImV4cCI6MTY5MzI4NTE3MywiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFWUUFxLzhVQUFBQXNGRW9TVW5OYk5Dd1NmOG5kTEViV2pSN2RHRzRrbU52OVNPeDkvN2pXSUNrMWFGV2hWakVzMVBneXFiSUp4aWNEZ2krVGNsMy9oanZsZlNBNGZDV29heG1iQ2lVdUk3NlZOUjcvWUNqYWFzPSIsImFtciI6WyJwd2QiLCJyc2EiLCJtZmEiXSwiYXBwX2Rpc3BsYXluYW1lIjoiR3JhcGggRXhwbG9yZXIiLCJhcHBpZCI6ImRlOGJjOGI1LWQ5ZjktNDhiMS1hOGFkLWI3NDhkYTcyNTA2NCIsImFwcGlkYWNyIjoiMCIsImRldmljZWlkIjoiNjlkNGZmODktNzEyMi00MTkxLWIyNjYtMmVjYmM1ZjBlNmMwIiwiZmFtaWx5X25hbWUiOiJNdXJ1Z2FkYXNzIiwiZ2l2ZW5fbmFtZSI6IkJhbGFtYW5jaGFyaSIsImlkdHlwIjoidXNlciIsImlwYWRkciI6IjEzNi4xODUuMTYuMjM0IiwibmFtZSI6IkJhbGFtYW5jaGFyaSBNdXJ1Z2FkYXNzIiwib2lkIjoiYTI0MzIxY2EtMWMxMC00NWEyLTk0ZTEtOTc5ZDVhZTk0YmIxIiwib25wcmVtX3NpZCI6IlMtMS01LTIxLTE4MDEwNjUwNjMtODY5Mzc3ODQ5LTEzNjk3NjUyNDQtMTE1OCIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzMjAwMjE3NkQ0NEI4IiwicmgiOiIwLkFVa0FjRUxIcExvSnJFLXl0am5hMFdvVnJnTUFBQUFBQUFBQXdBQUFBQUFBQUFCSkFEZy4iLCJzY3AiOiJDaGF0LkNyZWF0ZSBDaGF0LlJlYWQgQ2hhdC5SZWFkQmFzaWMgQ2hhdC5SZWFkV3JpdGUgQ2hhdE1lc3NhZ2UuUmVhZCBDaGF0TWVzc2FnZS5TZW5kIG9wZW5pZCBwcm9maWxlIFVzZXIuUmVhZCBVc2VyLlJlYWRCYXNpYy5BbGwgVXNlci5SZWFkV3JpdGUgVXNlckFjdGl2aXR5LlJlYWRXcml0ZS5DcmVhdGVkQnlBcHAgZW1haWwiLCJzaWduaW5fc3RhdGUiOlsia21zaSJdLCJzdWIiOiJDSlYxaVdkY041RDJZYnI2VTA2b29za1Jtak5XemhxQ0VFUkRlbWgzYkFVIiwidGVuYW50X3JlZ2lvbl9zY29wZSI6IkFTIiwidGlkIjoiYTRjNzQyNzAtMDliYS00ZmFjLWIyYjYtMzlkYWQxNmExNWFlIiwidW5pcXVlX25hbWUiOiJTVFMxNzhAc3VyZ2V0ZWNoaW5jLmluIiwidXBuIjoiU1RTMTc4QHN1cmdldGVjaGluYy5pbiIsInV0aSI6Ik1IY1k2ekIyYUVDU21FeU9nRU4wQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbImI3OWZiZjRkLTNlZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdLCJ4bXNfY2MiOlsiQ1AxIl0sInhtc19zc20iOiIxIiwieG1zX3N0Ijp7InN1YiI6IkU1aEo4dWZMbXlWanlKSl9MNjhmTk5EX205ZXRfV0gtOFM3T1FQS25USkUifSwieG1zX3RjZHQiOjE1OTEzMzEwMDh9.I_OxWm_PFpOC38IfA1aDvgxF7BX1MC8jwUhyFYdvJx4v1TFKhPNLAUl4hkevdrFpoHXst-cONHpxXZXirR1LbVZP850ZxlDCbNar6UvAYDrYhv4zNeG2TkTNYxudbFhwL0gXAVDPslGfZ_9Xln6u-bCVeQevTcUH7AODYTN2GtVTaYRhMJhqjMW6JsvBwhApTWeRVuuIefQ41O9BMvowTvvu6MUoFF-PPByq3A0pM9278IU70BDjqKHSIJ2bbs3ApTA9IN9CEhQwwfp8884PBCZOyC9A5rMV4WVURZPNfmFy-e5t0eG1SSjr_qtGyXJXp_Z96szplP6RXUP4xKILQw";	String getsSenderUserId = "sts178@surgetechinc.in";
	String getReceiver = "rajesh.sebastin@surgetechinc.in";


	

	// getSender :-
	@GetMapping("/getsSenderUserId/{email}")
	public String SenderUserId(@PathVariable("email") String email) {
		String urlGetSenderUserId = String.format("https://graph.microsoft.com/v1.0/users/" + email);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Content-Type", "application/json");

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(urlGetSenderUserId, HttpMethod.GET, requestEntity,
					Map.class);
			Map map = response.getBody();
			String senderid = (String) map.get("id");
			return senderid;
		} catch (HttpClientErrorException.Unauthorized ex) {
			System.out.println("Unauthorized Request: " + ex.getMessage());
			throw ex;
		} catch (RestClientException ex) {
			System.out.println("Rest Client Exception: " + ex.getMessage());
			throw ex;
		}
	}

	// getReceiver :-

	@GetMapping("/getReceiver/{email}")
	public String getReceiverUserId(@PathVariable("email") String email) {
		String urlGetSenderUserId = String.format("https://graph.microsoft.com/v1.0/users/" + email);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Content-Type", "application/json");

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(urlGetSenderUserId, HttpMethod.GET, requestEntity,
					Map.class);
			Map map = response.getBody();

			// SenderuserId = extractSenderUserIdFromResponse(responseBody);

			String getReceiver = (String) map.get("id");

			return getReceiver;
		} catch (HttpClientErrorException.Unauthorized ex) {
			System.out.println("Unauthorized Request: " + ex.getMessage());
			throw ex;
		} catch (RestClientException ex) {
			System.out.println("Rest Client Exception: " + ex.getMessage());
			throw ex;
		}
	}

	// getall :-

	@GetMapping("/getAll")
	public String sendTeamsMessage(@RequestBody MessageDto messageDto) {

		String urll = "https://graph.microsoft.com/v1.0/chats";

		// GraphApiController controller = new GraphApiController();
		String url = "http://localhost:8080/getsSenderUserId/" + getsSenderUserId;
		ResponseEntity<String> object = restTemplate.getForEntity(url, String.class);
		String getsSenderUserId = object.getBody();
		String url2 = "http://localhost:8080/getReceiver/" + getReceiver;
		ResponseEntity<String> object1 = restTemplate.getForEntity(url2, String.class);
		String getReceiver = object1.getBody();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		headers.set("Authorization", "Bearer " + accessToken);
		headers.set("Content-Type", "application/json");

		String requestBody = "{\n" + "    \"chatType\": \"oneOnOne\",\n" + "    \"members\": [\n" + "        {\n"
				+ "            \"@odata.type\": \"#microsoft.graph.aadUserConversationMember\",\n"
				+ "            \"roles\": [\n" + "                \"owner\"\n" + "            ],\n"
				+ "            \"user@odata.bind\": \"https://graph.microsoft.com/v1.0/users('" + getsSenderUserId
				+ "')\"\n" + "        },\n" + "        {\n"
				+ "            \"@odata.type\": \"#microsoft.graph.aadUserConversationMember\",\n"
				+ "            \"roles\": [\n" + "                \"owner\"\n" + "            ],\n"
				+ "            \"user@odata.bind\": \"https://graph.microsoft.com/v1.0/users('" + getReceiver + "')\"\n"
				+ "        }\n" + "    ]\n" + "}";

		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
		ResponseEntity<Map> response = restTemplate.exchange(urll, HttpMethod.POST, requestEntity, Map.class);

		Map body = response.getBody();

		String id = (String) body.get("id");
		String sendUrl = "http://localhost:8080/api/" + id + "/messages";

		HttpEntity<MessageDto> requestEntitys = new HttpEntity<>(messageDto, headers);
		ResponseEntity<Map> responses = restTemplate.exchange(sendUrl, HttpMethod.POST, requestEntitys, Map.class);

		return "Chat Sent";
	}

	@PostMapping("/api/{chatId}/messages")
	public ResponseEntity<String> callGraphApi(@PathVariable String chatId, @RequestBody MessageDto messageDto) {
		String apiEndpoint = String.format("https://graph.microsoft.com/v1.0/chats/" + chatId + "/messages");

		// Set up headers with the bearer token
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken); // Include the access token
		headers.set("Content-Type", "application/json");


		HttpEntity<MessageDto> requestEntity = new HttpEntity<>(messageDto, headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(apiEndpoint, requestEntity, String.class);
			return response;
		} catch (HttpClientErrorException.Unauthorized ex) {
			System.out.println("Unauthorized Request: " + ex.getMessage());
			throw ex;
		}
	}
	
	
	
	// without camunda :-
	
	@GetMapping
	public void ProductionCompleted() {

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
	
	
	
	
	// get connector response :-
	
	@GetMapping("/demo")
	public void getvariable(
		
		@RequestHeader("senderEmailId")  String senderEmailId,
        @RequestHeader("receiverEmailId")  String receiverEmailId,
        @RequestHeader("accessToken")  String accessToken,
        @RequestHeader("body")  String body
        ) {
		HttpHeaders headers = new HttpHeaders();
        headers.set("senderEmailId", senderEmailId);
        headers.set("receiverEmailId", receiverEmailId);
        headers.set("accessToken", accessToken);
        headers.set("body", body);
        
		
        HttpEntity<String> reqEntity = new HttpEntity<>(headers);
        System.out.println("reqEntity-------------"+reqEntity);

        String urll = "https://graph.microsoft.com/v1.0/chats";

        String urlGetSenderUserId =
            String.format("https://graph.microsoft.com/v1.0/users/" + senderEmailId);
        

        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
            restTemplate.exchange(urlGetSenderUserId, HttpMethod.GET, requestEntity, Map.class);
        Map map = response.getBody();
        String senderid = (String) map.get("id");
        
        System.out.println(senderEmailId+"==========="+senderid);

        String urlGetReceieverEmailId =
            String.format("https://graph.microsoft.com/v1.0/users/" + receiverEmailId);
        
       

        ResponseEntity<Map> response1 =
            restTemplate.exchange(urlGetReceieverEmailId, HttpMethod.GET, requestEntity, Map.class);
        Map maps = response1.getBody();

        String getReceiver = (String) maps.get("id");
        
        System.out.println(receiverEmailId+"========"+getReceiver);

        String requestBody =
            "{\n"
                + "    \"chatType\": \"oneOnOne\",\n"
                + "    \"members\": [\n"
                + "        {\n"
                + "            \"@odata.type\": \"#microsoft.graph.aadUserConversationMember\",\n"
                + "            \"roles\": [\n"
                + "                \"owner\"\n"
                + "            ],\n"
                + "            \"user@odata.bind\": \"https://graph.microsoft.com/v1.0/users('"
                + senderid
                + "')\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"@odata.type\": \"#microsoft.graph.aadUserConversationMember\",\n"
                + "            \"roles\": [\n"
                + "                \"owner\"\n"
                + "            ],\n"
                + "            \"user@odata.bind\": \"https://graph.microsoft.com/v1.0/users('"
                + getReceiver
                + "')\"\n"
                + "        }\n"
                + "    ]\n"
                + "}";

        HttpEntity<String> requestEntitys = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> responses =
            restTemplate.exchange(urll, HttpMethod.POST, requestEntitys, Map.class);

        Map resBody = responses.getBody();

        String chatid = (String) resBody.get("id");
        
        System.out.println("chat id created......."+chatid);

        String sendUrl =
            String.format("https://graph.microsoft.com/v1.0/chats/" + chatid + "/messages");
        // String sendUrl = "http://localhost:8080/api/" + chatid + "/messages";
        String messageDto =
            "{\r\n"
                + "    \"body\": {\r\n"
                + "        \"content\": \""
                + body
                + "\"\r\n"
                + "    }\r\n"
                + "}";
        HttpEntity<String> requestEntityss = new HttpEntity<>(messageDto, headers);
        ResponseEntity<Map> responsess =
            restTemplate.exchange(sendUrl, HttpMethod.POST, requestEntityss, Map.class);
        
        System.out.println("message sent.....");

	}	
}
