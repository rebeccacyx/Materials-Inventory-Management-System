package com.yuxuan.inventory.outbound;

import com.yuxuan.inventory.outbound.dto.OutboundOrderLineResponse;
import com.yuxuan.inventory.outbound.dto.OutboundOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/outbound-orders")
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    public OutboundOrderController(OutboundOrderService outboundOrderService) {
        this.outboundOrderService = outboundOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OutboundOrderResponse create(@Valid @RequestBody CreateOutboundOrderRequest request,
                                        @RequestHeader(value = "X-Operator", required = false) String operator) {
        return toResponse(outboundOrderService.create(request, operator));
    }

    @GetMapping
    public List<OutboundOrderResponse> query(@RequestParam(required = false) Long warehouseId,
                                             @RequestParam(required = false) OutboundOrderStatus status) {
        return outboundOrderService.query(warehouseId, status).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public OutboundOrderResponse get(@PathVariable Long id) {
        return toResponse(outboundOrderService.getById(id));
    }

    @PostMapping("/{id}/post")
    public OutboundOrderResponse post(@PathVariable Long id,
                                      @RequestHeader(value = "X-Operator", required = false) String operator) {
        return toResponse(outboundOrderService.post(id, operator));
    }

    private OutboundOrderResponse toResponse(OutboundOrder order) {
        return new OutboundOrderResponse(
                order.getId(),
                order.getWarehouse().getId(),
                order.getWarehouse().getName(),
                order.getStatus().name(),
                order.getCreatedBy(),
                order.getPostedBy(),
                order.getCreatedAt(),
                order.getPostedAt(),
                order.getLines().stream()
                        .map(line -> new OutboundOrderLineResponse(
                                line.getItem().getId(),
                                line.getItem().getSku(),
                                line.getItem().getName(),
                                line.getQuantity()))
                        .toList()
        );
    }
}
