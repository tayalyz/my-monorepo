package com.microservices.order_service;

import com.microservices.order_service.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

	@ServiceConnection
	static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0");

	@LocalServerPort
	private Integer port;

	@Test
	void contextLoads() {
	}

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mySQLContainer.start();
	}

	@Test
	void shouldPlaceOrder() {
		String requestBody = """
				{
				     "skuCode": "iphone_15",
				     "quantity": 1,
				     "price": 1000
				 }
				""";
		InventoryClientStub.stubInventoryCall("iphone_15", 1);
		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/order")
				.then()
				.statusCode(201)
				.body(Matchers.equalTo("Order placed successfully"));

		String badRequestBody = """
				{
				     "skuCode": "ip15",
				     "quantity": 10,
				     "price": 1000
				 }
				""";
		RestAssured.given()   // without stub
				.contentType("application/json")
				.body(badRequestBody)
				.when()
				.post("/api/order")
				.then()
				.statusCode(500);

		String badRequestBody2 = """
				{
				     "skuCode": "iphone_15",
				     "quantity": 1000,
				     "price": 1000
				 }
				""";
		InventoryClientStub.stubInventoryCall("iphone_15", 1000);
		RestAssured.given()
				.contentType("application/json")
				.body(badRequestBody2)
				.when()
				.post("/api/order")
				.then()
				.statusCode(500);
	}
}
