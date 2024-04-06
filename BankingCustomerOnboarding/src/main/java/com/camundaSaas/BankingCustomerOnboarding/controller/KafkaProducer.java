//package com.camundaSaas.BankingCustomerOnboarding.controller;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Component
//public class KafkaProducer {
//	
//	
////	@Bean
////	public KafkaTemplate<String, String> kafkaTemplate(){
////		return new KafkaTemplate<>(producerFactory());
////		
////	}
////
////	@Bean
////	public ProducerFactory<String, String> producerFactory(){
////		
////		Map<String, Object> configProps = new HashMap<String, Object>();
////		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
////		
////		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
////		
////		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
////		
////		
////		return new DefaultKafkaProducerFactory<>(configProps);
////		
////	}
////	
//	
////	 public  KafkaTemplate<String, String> kafkaTemplate;
////	    private static final String TOPIC = "helloTopic"; // Replace with your Kafka topic name
////
////	    @Autowired
////	    public void KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
////	        this.kafkaTemplate = kafkaTemplate;
////	    }
////	    
////	    public void sendMessage(Map<String, Object> mapValue) {
////	        // Convert the Map value to JSON string
////	        ObjectMapper objectMapper = new ObjectMapper();
////	        String jsonString = null;
////	        try {
////	            jsonString = objectMapper.writeValueAsString(mapValue);
////	        } catch (JsonProcessingException e) {
////	            e.printStackTrace();
////	        }
////
////	        // Send the JSON string as a message to the Kafka topic
////	        kafkaTemplate.send(TOPIC, jsonString);
////	    }
//
//}
