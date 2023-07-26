package com.polarbookshop.order.client;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.polarbookshop.order.client.model.Book;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class BookClient {

	private static final String BOOKS_API_PATH = "/books/";

	private final WebClient catalogClient;

	public Mono<Book> getBookByIsbn(String isbn) {
		return this.catalogClient.get()
				.uri(BOOKS_API_PATH + isbn)
				.retrieve()
				.bodyToMono(Book.class)
				.timeout(Duration.ofSeconds(3), Mono.empty())
				.onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
				.retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
				.onErrorResume(Exception.class, e -> Mono.empty());
	}
}
