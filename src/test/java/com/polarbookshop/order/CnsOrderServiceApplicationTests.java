package com.polarbookshop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.polarbookshop.order.client.BookClient;
import com.polarbookshop.order.client.model.Book;
import com.polarbookshop.order.domain.Order;
import com.polarbookshop.order.domain.OrderStatus;
import com.polarbookshop.order.model.OrderRequest;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class CnsOrderServiceApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.3-alpine"));

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private BookClient bookClient;

	@DynamicPropertySource
	static void postgresqlProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.r2dbc.url", CnsOrderServiceApplicationTests::r2dbcUrl);
		registry.add("spring.r2dbc.username", postgresql::getUsername);
		registry.add("spring.r2dbc.password", postgresql::getPassword);
		registry.add("spring.flyway.url", postgresql::getJdbcUrl);
	}

	private static String r2dbcUrl() {
		return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(), postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
	}

	@Test
	void whenGetOrdersThenReturn() {
		String bookIsbn = "1234567893";
		Book book = new Book(bookIsbn, "Title", "Author", 9.90);
		
		given(this.bookClient.getBookByIsbn(bookIsbn)).willReturn(Mono.just(book));

		OrderRequest orderRequest = new OrderRequest(bookIsbn, 1);
		Order expectedOrder = this.webTestClient.post()
				.uri("/orders")
				.bodyValue(orderRequest)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.CREATED)
				.expectBody(Order.class)
				.returnResult()
				.getResponseBody();

		assertThat(expectedOrder).isNotNull();
		assertThat(expectedOrder.getStatus()).isEqualTo(OrderStatus.ACCEPTED);

		this.webTestClient.get()
			.uri("/orders")
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectBodyList(Order.class)
			.value(orders -> {
				assertThat(orders.stream().filter(order -> order.getBookIsbn().equals(bookIsbn)).findAny()).isNotEmpty();
			});		
		
	}
}
