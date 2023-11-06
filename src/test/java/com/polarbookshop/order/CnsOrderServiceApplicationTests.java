package com.polarbookshop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarbookshop.order.client.BookClient;
import com.polarbookshop.order.client.model.Book;
import com.polarbookshop.order.domain.Order;
import com.polarbookshop.order.domain.OrderStatus;
import com.polarbookshop.order.model.OrderRequest;
import com.polarbookshop.order.model.event.OrderAcceptedMessage;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestChannelBinderConfiguration.class)
@Testcontainers
class CnsOrderServiceApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.3-alpine"));

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OutputDestination output;	

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
	void whenGetOrdersThenReturn() throws IOException {
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
		assertThat(this.objectMapper.readValue(this.output.receive().getPayload(), OrderAcceptedMessage.class))
			.isEqualTo(new OrderAcceptedMessage(expectedOrder.getId()));

		this.webTestClient.get()
			.uri("/orders")
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectBodyList(Order.class)
			.value(orders -> {
				assertThat(orders.stream().filter(order -> order.getBookIsbn().equals(bookIsbn)).findAny()).isNotEmpty();
			});		
	}

	@Test
	void whenPostRequestAndBookExistsThenOrderAccepted() throws IOException {
		String bookIsbn = "1234567899";
		Book book = new Book(bookIsbn, "Title", "Author", 9.90);
		OrderRequest orderRequest = new OrderRequest(bookIsbn, 3);

		given(bookClient.getBookByIsbn(bookIsbn)).willReturn(Mono.just(book));

		Order createdOrder = this.webTestClient.post()
				.uri("/orders")
				.bodyValue(orderRequest)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Order.class)
				.returnResult()
				.getResponseBody();
		
		assertThat(createdOrder).isNotNull();
		assertThat(createdOrder.getBookIsbn()).isEqualTo(orderRequest.isbn());
		assertThat(createdOrder.getQuantity()).isEqualTo(orderRequest.quantity());
		assertThat(createdOrder.getBookName()).isEqualTo(book.title() + " - " + book.author());
		assertThat(createdOrder.getBookPrice()).isEqualTo(book.price());
		assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.ACCEPTED);

		OrderAcceptedMessage orderAcceptedMessage = this.objectMapper.readValue(this.output.receive().getPayload(), OrderAcceptedMessage.class);
		assertThat(orderAcceptedMessage).isEqualTo(new OrderAcceptedMessage(createdOrder.getId()));
	}

	@Test
	void whenPostRequestAndBookNotExistsThenOrderRejected() {
		String bookIsbn = "1234567894";
		OrderRequest orderRequest = new OrderRequest(bookIsbn, 3);

		given(bookClient.getBookByIsbn(bookIsbn)).willReturn(Mono.empty());

		Order createdOrder = webTestClient.post().uri("/orders")
				.bodyValue(orderRequest)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(Order.class).returnResult().getResponseBody();

		assertThat(createdOrder).isNotNull();
		assertThat(createdOrder.getBookIsbn()).isEqualTo(orderRequest.isbn());
		assertThat(createdOrder.getQuantity()).isEqualTo(orderRequest.quantity());
		assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.REJECTED);		
	}
}
