package com.polarbookshop.order.service;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polarbookshop.order.client.BookClient;
import com.polarbookshop.order.domain.Order;
import com.polarbookshop.order.domain.OrderStatus;
import com.polarbookshop.order.model.event.OrderAcceptedMessage;
import com.polarbookshop.order.model.event.OrderDispatchedMessage;
import com.polarbookshop.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final BookClient bookClient;
	private final StreamBridge streamBridge; 

	public Flux<Order> getOrders() {
		return this.orderRepository.findAll();
	}

	@Transactional
	public Mono<Order> createOrder(String isbn, int quantity) {
		return this.bookClient.getBookByIsbn(isbn)
				.map(book -> new Order(book.isbn(), book.title() + " - " + book.author(), book.price(), quantity, OrderStatus.ACCEPTED))
				.defaultIfEmpty(new Order(isbn, null, null, quantity, OrderStatus.REJECTED))
				.flatMap(this.orderRepository::save)
				.doOnNext(this::publishOrderAcceptedEvent);
	}

	public Flux<Order> processOrderDispatchedEvent(Flux<OrderDispatchedMessage> messagesStream) {
		return messagesStream.flatMap(message -> this.orderRepository.findById(message.orderId()))
				.doOnNext(order -> order.setStatus(OrderStatus.DISPATCHED))
				.flatMap(this.orderRepository::save);
	}

	private void publishOrderAcceptedEvent(Order order) {
		if (order.getStatus().equals(OrderStatus.ACCEPTED)) {
			OrderAcceptedMessage orderAcceptedMessage = new OrderAcceptedMessage(order.getId());
			
			log.info("Sending order accepted event wirth id: {}", order.getId());
			boolean result = this.streamBridge.send("acceptOrder-out-0", orderAcceptedMessage);
			log.info("Resuilt of sending event for order with id {}: {}", order.getId(), result);
			
		}
	}

}
