package com.yuxuan.inventory.outbound;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outbound-orders")
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    public OutboundOrderController(OutboundOrderService outboundOrderService) {
        this.outboundOrderService = outboundOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OutboundOrder create(@Valid @RequestBody CreateOutboundOrderRequest request) {
        return outboundOrderService.create(request);
    }

    @GetMapping("/{id}")
    public OutboundOrder get(@PathVariable Long id) {
        return outboundOrderService.getById(id);
    }

    @PostMapping("/{id}/post")
    public OutboundOrder post(@PathVariable Long id) {
        return outboundOrderService.post(id);
    }
}
