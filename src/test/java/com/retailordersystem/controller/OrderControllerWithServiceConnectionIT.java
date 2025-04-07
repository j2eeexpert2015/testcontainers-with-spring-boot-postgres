package com.retailordersystem.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailordersystem.constants.DockerImageConstants;
import com.retailordersystem.model.Order;
import com.retailordersystem.repository.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers

public class OrderControllerWithServiceConnectionIT {

    private static final Integer TIMEOUT = 120;
    private static final Logger logger = LoggerFactory.getLogger(OrderControllerWithServiceConnectionIT.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(DockerImageConstants.POSTGRES_IMAGE));

    @BeforeAll
    static void startContainers() {
        // Ensure PostgreSQL is running
        Awaitility.await().atMost(Duration.ofSeconds(TIMEOUT)).until(postgres::isRunning);
        logger.info("PostgreSQL is up and running!");
    }

    /*
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

    }
    */

    @Test
    public void shouldCreateOrderAndPublishEvent() throws Exception {

        // Create a new order
        Order order = new Order("DUMMY_STATUS", "Order from Integration Test");

        // Perform POST request to create the order (using objectMapper)
        MvcResult mvcResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("DUMMY_STATUS")))
                .andReturn();
        // Extract the created order's ID from the response
        String responseContent = mvcResult.getResponse().getContentAsString();
        Long orderId = Long.parseLong(responseContent.substring(responseContent.indexOf("\"id\":") + 5, responseContent.indexOf(",")));

        // Verify order saved in the database
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(savedOrder.getStatus()).isEqualTo("DUMMY_STATUS");
    }

}