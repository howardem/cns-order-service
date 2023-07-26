package com.polarbookshop.order.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import com.polarbookshop.order.domain.Order;
import com.polarbookshop.order.domain.OrderStatus;

@JsonTest
class OrderJsonTests {

    @Autowired
    private JacksonTester<Order> json;

    @Test
    void testSerialize() throws Exception {
        Order order = new Order(394L, "1234567890", "Book Name", 9.90, 1, OrderStatus.ACCEPTED, Instant.now(), Instant.now(), 21);
        var jsonContent = json.write(order);

		assertThat(jsonContent).extractingJsonPathNumberValue("@.id").isEqualTo(order.getId().intValue());
		assertThat(jsonContent).extractingJsonPathStringValue("@.bookIsbn").isEqualTo(order.getBookIsbn());
		assertThat(jsonContent).extractingJsonPathStringValue("@.bookName").isEqualTo(order.getBookName());
		assertThat(jsonContent).extractingJsonPathNumberValue("@.bookPrice").isEqualTo(order.getBookPrice());
		assertThat(jsonContent).extractingJsonPathNumberValue("@.quantity").isEqualTo(order.getQuantity());
		assertThat(jsonContent).extractingJsonPathStringValue("@.status").isEqualTo(order.getStatus().toString());
		assertThat(jsonContent).extractingJsonPathStringValue("@.createdDate").isEqualTo(order.getCreatedDate().toString());
		assertThat(jsonContent).extractingJsonPathStringValue("@.lastModifiedDate").isEqualTo(order.getLastModifiedDate().toString());
		assertThat(jsonContent).extractingJsonPathNumberValue("@.version").isEqualTo(order.getVersion());
    }

}