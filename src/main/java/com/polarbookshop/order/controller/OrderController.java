package com.polarbookshop.order.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.polarbookshop.order.domain.Order;
import com.polarbookshop.order.model.OrderRequest;
import com.polarbookshop.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@GetMapping
	public Flux<Order> getOrders() {
		return this.orderService.getOrders();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Order> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
		return this.orderService.createOrder(orderRequest.isbn(), orderRequest.quantity());
	}

}
