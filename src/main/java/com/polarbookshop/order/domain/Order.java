package com.polarbookshop.order.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("orders")
@NoArgsConstructor
@AllArgsConstructor
@Data	
public class Order {

	@Id
	private Long id;

	private String bookIsbn;
	private String bookName;
	private Double bookPrice;
	private Integer quantity;
	private OrderStatus status;

	@CreatedDate
	private Instant createdDate;

	@LastModifiedDate
	private Instant lastModifiedDate;

	@Version
	private int version;

	public Order(String bookIsbn, String bookName, Double bookPrice, Integer quantity, OrderStatus status) {
		this(null, bookIsbn, bookName, bookPrice, quantity, status, null, null, 0);
	}

}
