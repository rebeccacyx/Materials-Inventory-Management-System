package com.yuxuan.inventory;

import com.yuxuan.inventory.operationlog.OperationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Test
    void outboundPostShouldReduceStockAndWriteMovement() throws Exception {
        mockMvc.perform(post("/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"W1","location":"SZ"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","name":"Item 1","unit":"PCS"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/stock-movements")
                        .header("X-Role", "OPERATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":1,"itemId":1,"type":"IN","quantity":10,"reason":"init"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mockMvc.perform(post("/stock-movements/1/post").header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"));

        mockMvc.perform(post("/outbound-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":1,"lines":[{"itemId":1,"quantity":4}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        mockMvc.perform(post("/outbound-orders/1/post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"));

        mockMvc.perform(get("/stocks/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(6));
    }

    @Test
    void shouldSupportIdempotencyAndRolePermission() throws Exception {
        mockMvc.perform(post("/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"W2","location":"SZ"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-2","name":"Item 2","unit":"PCS"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/stock-movements")
                        .header("X-Role", "VIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":2,"itemId":2,"type":"IN","quantity":5}
                                """))
                .andExpect(status().isForbidden());

        MvcResult first = mockMvc.perform(post("/stock-movements")
                        .header("X-Role", "OPERATOR")
                        .header("X-Idempotency-Key", "dup-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":2,"itemId":2,"type":"IN","quantity":5}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/stock-movements")
                        .header("X-Role", "OPERATOR")
                        .header("X-Idempotency-Key", "dup-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":2,"itemId":2,"type":"IN","quantity":5}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Integer firstId = JsonPath.read(first.getResponse().getContentAsString(), "$.id");
        Integer secondId = JsonPath.read(second.getResponse().getContentAsString(), "$.id");
        assertThat(secondId).isEqualTo(firstId);

        assertThat(operationLogRepository.countByAction("CREATE_DRAFT")).isGreaterThan(0);
    }


    @Test
    void inboundPostShouldIncreaseStock() throws Exception {
        MvcResult warehouseResult = mockMvc.perform(post("/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"W-IN","location":"SH"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult itemResult = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-IN-1","name":"Inbound Item","unit":"PCS"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Long warehouseId = JsonPath.read(warehouseResult.getResponse().getContentAsString(), "$.id");
        Long itemId = JsonPath.read(itemResult.getResponse().getContentAsString(), "$.id");

        MvcResult orderResult = mockMvc.perform(post("/inbound-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":%d,"lines":[{"itemId":%d,"quantity":8}]}
                                """.formatted(warehouseId, itemId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        Long orderId = JsonPath.read(orderResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/inbound-orders/{id}/post", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"));

        mockMvc.perform(get("/stocks/{warehouseId}/{itemId}", warehouseId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(8));
    }



    @Test
    void shouldSupportQueryEndpointsAndResponseDto() throws Exception {
        mockMvc.perform(get("/outbound-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/inbound-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        MvcResult warehouse = mockMvc.perform(post("/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"W-Q","location":"HZ"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult item = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-Q-1","name":"Item Q1","unit":"PCS"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Long warehouseId = JsonPath.read(warehouse.getResponse().getContentAsString(), "$.id");
        Long itemId = JsonPath.read(item.getResponse().getContentAsString(), "$.id");

        MvcResult movement = mockMvc.perform(post("/stock-movements")
                        .header("X-Role", "OPERATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":%d,"itemId":%d,"type":"IN","quantity":1,"reason":"q"}
                                """.formatted(warehouseId, itemId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warehouseId").exists())
                .andExpect(jsonPath("$.itemId").exists())
                .andExpect(jsonPath("$.type").value("IN"))
                .andReturn();

        Integer movementId = JsonPath.read(movement.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/stock-movements")
                        .param("status", "DRAFT")
                        .param("type", "IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(post("/stock-movements/{id}/cancel", movementId).header("X-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnValidationErrorDetails() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","name":"Item","unit":"PCS"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.validationErrors").isArray())
                .andExpect(jsonPath("$.details.validationErrors[0].field").exists())
                .andExpect(jsonPath("$.details.validationErrors[0].message").exists());
    }

    @Test
    void shouldReturnUnifiedErrorFormat() throws Exception {
        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("Item not found"))
                .andExpect(jsonPath("$.path").value("/items/999"));
    }
}
