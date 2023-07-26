package com.polarbookshop.order.client.model;

public record Book(
		String isbn,
		String title,
		String author,
		Double price
) {}
