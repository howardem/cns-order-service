package com.polarbookshop.order.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.polarbookshop.order.controller.OrderController;
import com.polarbookshop.order.domain.Order;
import com.polarbookshop.order.domain.OrderStatus;
import com.polarbookshop.order.model.OrderRequest;
import com.polarbookshop.order.service.OrderService;

import reactor.core.publisher.Mono;

@WebFluxTest(OrderController.class)
class OrderControllerTests {

	@Autowired
	private WebTestClient webClient;

	@MockBean
	private OrderService orderService;

	@Test
	void whenBookNotAvailableThenRejectOrder() {
		OrderRequest orderRequest = new OrderRequest("1234567890", 3);
		Order expectedOrder = new Order(orderRequest.isbn(), null, null, orderRequest.quantity(), OrderStatus.REJECTED);

		given(this.orderService.createOrder(orderRequest.isbn(), orderRequest.quantity()))
			.willReturn(Mono.just(expectedOrder));

		this.webClient.post()
			.uri("/orders")
			.bodyValue(orderRequest)
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectBody(Order.class)
			.value(actualOrder -> {
				assertThat(actualOrder).isNotNull();
				assertThat(actualOrder.getStatus()).isEqualTo(OrderStatus.REJECTED);
			});
	}

}
