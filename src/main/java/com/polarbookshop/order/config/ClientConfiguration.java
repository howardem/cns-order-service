package com.polarbookshop.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfiguration {

	@Bean
	WebClient catalogClient(ClientProperties clientProperties, WebClient.Builder webClientBuilder) {
		return webClientBuilder.baseUrl(clientProperties.catalogServiceUri().toString())
				.build();
	}

}
