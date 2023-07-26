package com.polarbookshop.order.service;

import org.springframework.stereotype.Service;

import com.polarbookshop.order.client.BookClient;
import com.polarbookshop.order.domain.Order;
import com.polarbookshop.order.domain.OrderStatus;
import com.polarbookshop.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final BookClient bookClient;

	public Flux<Order> getOrders() {
		return this.orderRepository.findAll();
	}

	public Mono<Order> createOrder(String isbn, int quantity) {
		return this.bookClient.getBookByIsbn(isbn)
				.map(book -> new Order(book.isbn(), book.title() + " - " + book.author(), book.price(), quantity, OrderStatus.ACCEPTED))
				.defaultIfEmpty(new Order(isbn, null, null, quantity, OrderStatus.REJECTED))
				.flatMap(this.orderRepository::save);
	}

}
