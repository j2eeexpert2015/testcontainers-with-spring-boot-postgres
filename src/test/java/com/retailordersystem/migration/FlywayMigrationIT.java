package com.retailordersystem.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import com.retailordersystem.constants.DockerImageConstants;
import com.retailordersystem.model.Order;
import com.retailordersystem.repository.OrderRepository;


/**
 * Integration tests specifically for verifying Flyway migrations
 * using Testcontainers.
 */
@SpringBootTest // Loads the full application context, triggering Flyway migrations
@Testcontainers
public class FlywayMigrationIT {

    private static final Integer TIMEOUT = 120;
    private static final Logger logger = LoggerFactory.getLogger(FlywayMigrationIT.class);

    @Container
    @ServiceConnection // Automatically configures datasource properties for the container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(DockerImageConstants.POSTGRES_IMAGE));

    @Autowired
    private JdbcTemplate jdbcTemplate; // For direct SQL checks

    @Autowired
    private OrderRepository orderRepository; // For JPA-based data checks

    @BeforeAll
    static void startContainers() {
        // Ensure PostgreSQL is running
        Awaitility.await().atMost(Duration.ofSeconds(TIMEOUT)).until(postgres::isRunning);
        logger.info("PostgreSQL container is up for Flyway tests.");
        // Spring Boot context loading will trigger Flyway migrations automatically
    }

    /**
     * Verifies that the schema (e.g., the 'orders' table) defined in V1 migration
     * has been created successfully.
     */
    @Test
    void verifySchemaCreation() {
        // A simple way to check if the table exists is to query its count.
        // If the table doesn't exist, this will throw an exception.
        // Alternatively, query information_schema.tables for more robustness.
        logger.info("Verifying schema creation...");
        assertDoesNotThrow(() -> {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
            logger.info("'orders' table exists.");
        }, "Querying the 'orders' table should not throw an exception if V1 migration ran successfully.");

        // More specific check using information_schema (adjust schema name if not 'public')
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'orders'";
        Integer tableCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat(tableCount).isEqualTo(1).as("The 'orders' table should exist in the 'public' schema.");
        logger.info("Verified 'orders' table presence via information_schema.");

        String colSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'orders' AND column_name IN ('id', 'status', 'description')";
        Integer colCount = jdbcTemplate.queryForObject(colSql, Integer.class);
        assertThat(colCount).isEqualTo(3).as("The 'orders' table should have 'id', 'status', and 'description' columns.");
        logger.info("Verified essential columns presence via information_schema.");

    }

    /**
     * Verifies that the initial seed data inserted by the V2 migration
     * is present in the 'orders' table.
     */
    @Test
    void verifyDataPopulation() {
        logger.info("Verifying data population...");
        // Assuming V2__Insert_initial_orders.sql inserts 2 dummy orders
        List<Order> orders = orderRepository.findAll();

        assertThat(orders).isNotNull();
        assertThat(orders.size()).isEqualTo(2)
                .as("Should find exactly 2 orders populated by V2 migration.");

        // Optionally check specific values
        assertThat(orders).allSatisfy(order -> {
            assertThat(order.getStatus()).isEqualTo("PENDING");
            assertThat(order.getDescription()).isEqualTo("DUMMY ORDER");
        });
        logger.info("Verified content of the initial 2 orders.");

        // Alternative check using JdbcTemplate
        List<Map<String, Object>> orderMaps = jdbcTemplate.queryForList("SELECT status, description FROM orders");
         assertThat(orderMaps.size()).isEqualTo(2)
                 .as("JdbcTemplate should find exactly 2 orders populated by V2 migration.");
        assertThat(orderMaps).allSatisfy(row -> {
             assertThat(row.get("status")).isEqualTo("PENDING");
             assertThat(row.get("description")).isEqualTo("DUMMY ORDER");
         });
         logger.info("Verified content of the initial 2 orders via JdbcTemplate.");
    }

}
