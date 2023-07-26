package com.polarbookshop.order.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.polarbookshop.order.config.DataConfiguration;
import com.polarbookshop.order.repository.OrderRepository;

import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(DataConfiguration.class)
@Testcontainers
class OrderRepositoryTests {

	@Container
	static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.3-alpine"));

	@Autowired
	private OrderRepository orderRepository;

	@DynamicPropertySource
	static void postgresqlProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.r2dbc.url", OrderRepositoryTests::r2dbcUrl);
		registry.add("spring.r2dbc.username", postgresql::getUsername);
		registry.add("spring.r2dbc.password", postgresql::getPassword);
		registry.add("spring.flyway.url", postgresql::getJdbcUrl);
	}

	private static String r2dbcUrl() {
		return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(), postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
	}

	@Test
	void createRejectedOrder() {
		Order rejectedOrder = new Order("1234567890", null, null, 3, OrderStatus.REJECTED);

		StepVerifier.create(this.orderRepository.save(rejectedOrder))
			.expectNextMatches(order -> order.getStatus().equals(OrderStatus.REJECTED))
			.verifyComplete();
	}
	
	@Test
	void findOrderByIdWhenNotExisting() {
		StepVerifier.create(this.orderRepository.findById(394L))
			.expectNextCount(0)
			.verifyComplete();
	}
} 
