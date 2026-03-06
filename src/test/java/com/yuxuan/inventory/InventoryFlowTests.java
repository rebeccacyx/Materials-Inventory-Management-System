package com.yuxuan.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryFlowTests {

    @Autowired
    private MockMvc mockMvc;

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":1,"itemId":1,"type":"IN","quantity":10,"reason":"init"}
                                """))
                .andExpect(status().isOk());

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
    void shouldReturnUnifiedErrorFormat() throws Exception {
        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Item not found"))
                .andExpect(jsonPath("$.path").value("/items/999"));
    }
}
