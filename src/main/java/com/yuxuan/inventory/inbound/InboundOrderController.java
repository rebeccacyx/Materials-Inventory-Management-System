package com.yuxuan.inventory.inbound;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inbound-orders")
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    public InboundOrderController(InboundOrderService inboundOrderService) {
        this.inboundOrderService = inboundOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InboundOrder create(@Valid @RequestBody CreateInboundOrderRequest request) {
        return inboundOrderService.create(request);
    }

    @GetMapping("/{id}")
    public InboundOrder get(@PathVariable Long id) {
        return inboundOrderService.getById(id);
    }

    @PostMapping("/{id}/post")
    public InboundOrder post(@PathVariable Long id) {
        return inboundOrderService.post(id);
    }
}
