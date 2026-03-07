package com.yuxuan.inventory.inbound;

import com.yuxuan.inventory.inbound.dto.InboundOrderLineResponse;
import com.yuxuan.inventory.inbound.dto.InboundOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inbound-orders")
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    public InboundOrderController(InboundOrderService inboundOrderService) {
        this.inboundOrderService = inboundOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InboundOrderResponse create(@Valid @RequestBody CreateInboundOrderRequest request) {
        return toResponse(inboundOrderService.create(request));
    }

    @GetMapping
    public List<InboundOrderResponse> query(@RequestParam(required = false) Long warehouseId,
                                            @RequestParam(required = false) InboundOrderStatus status) {
        return inboundOrderService.query(warehouseId, status).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public InboundOrderResponse get(@PathVariable Long id) {
        return toResponse(inboundOrderService.getById(id));
    }

    @PostMapping("/{id}/post")
    public InboundOrderResponse post(@PathVariable Long id) {
        return toResponse(inboundOrderService.post(id));
    }

    private InboundOrderResponse toResponse(InboundOrder order) {
        return new InboundOrderResponse(
                order.getId(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getPostedAt(),
                order.getLines().stream()
                        .map(line -> new InboundOrderLineResponse(
                                line.getItem().getId(),
                                line.getItem().getSku(),
                                line.getItem().getName(),
                                line.getQuantity()))
                        .toList()
        );
    }
}
