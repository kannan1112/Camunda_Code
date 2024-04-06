package com.surge.digital.connector;

import com.surge.digital.connector.sampletest.MsTeamsOutBoundConnectorExecutable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorFactory {
  @Bean
  public MsTeamsOutBoundConnectorExecutable connectorSampleFunction() {
    return new MsTeamsOutBoundConnectorExecutable();
  }
}
