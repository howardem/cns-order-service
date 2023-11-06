package com.polarbookshop.order.config;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.polarbookshop.order.model.event.OrderDispatchedMessage;
import com.polarbookshop.order.service.OrderService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Configuration
@Slf4j
public class StreamConfiguration {

	@Bean
	Consumer<Flux<OrderDispatchedMessage>> dispatchOrder(OrderService orderService) {
		return stream -> orderService.processOrderDispatchedEvent(stream)
				.doOnNext(order -> log.info("The order with id {} is dispatched", order.getId()))
				.subscribe();
				
	}

}
