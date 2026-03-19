package com.stockdata.project_data_provider.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
  @Bean
  RestTemplate restTemplate() { // get server 的功能
    return new RestTemplate();
  }
}