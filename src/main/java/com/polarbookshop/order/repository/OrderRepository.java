package com.polarbookshop.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.polarbookshop.order.domain.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}
