package com.surge.digital.connector;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableZeebeClient
public class MsTeamsOutBoundConnectorApplication {
  //    @Bean
  //    public RestTemplate getRestTemplate() {
  //        return new RestTemplate();
  //    }
  //
  @Autowired private ApplicationContext applicationContext;

  public static void main(String[] args) {
    SpringApplication.run(MsTeamsOutBoundConnectorApplication.class, args);
  }

  @PostConstruct
  public void listBeans() {
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      System.out.println(beanName);
    }
  }
}
