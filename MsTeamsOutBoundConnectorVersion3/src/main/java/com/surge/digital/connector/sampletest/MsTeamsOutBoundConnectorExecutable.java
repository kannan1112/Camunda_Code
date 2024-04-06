package com.surge.digital.connector.sampletest;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@OutboundConnector(
    name = "Ms Teams Custom Out Bound Connector",
    inputVariables = {},
    type = "io.camunda:connector-custom:1")
public class MsTeamsOutBoundConnectorExecutable implements OutboundConnectorFunction {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MsTeamsOutBoundConnectorExecutable.class);

  RestTemplate restTemplate = new RestTemplate();
  JSONParser parser = new JSONParser();

  @Override
  public Object execute(final OutboundConnectorContext context) throws Exception {

    String variables = context.getVariables();

    JSONObject json = (JSONObject) parser.parse(variables);
    String jso = json.toString();
    String senderEmailId = (String) json.get("senderEmailId");
    String body = (String) json.get("body");
    String receieverEmailId = (String) json.get("receieverEmailId");
    String accessToken = (String) json.get("AccessToken");

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_JSON);
    // header.set("json", jso);
    header.set("senderEmailId", senderEmailId);
    header.set("receiverEmailId", receieverEmailId);
    header.set("accessToken", accessToken);
    header.set("body", body);

    String url = "http://localhost:8034/demo";
    HttpEntity<String> reqEntity = new HttpEntity<>(header);
    ResponseEntity<String> reponseEntity =
        restTemplate.exchange(url, HttpMethod.GET, reqEntity, String.class);

    String str = reponseEntity.getBody();
    //
    System.out.println("str  response---------" + str);

    LOGGER.info("Executing ReportUrlApi connector with context: " + context);
    var connectorRequest = context.getVariables();
    context.validate(connectorRequest);
    context.replaceSecrets(connectorRequest);
    LOGGER.info("Hello there from " + context.getVariables());
    return context.getVariables();
  }
}
