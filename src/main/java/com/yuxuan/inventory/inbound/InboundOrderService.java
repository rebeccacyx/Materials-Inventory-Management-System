package com.yuxuan.inventory.inbound;

import com.yuxuan.inventory.common.ApiException;
import com.yuxuan.inventory.item.Item;
import com.yuxuan.inventory.item.ItemRepository;
import com.yuxuan.inventory.stock.StockService;
import com.yuxuan.inventory.stockMovement.MovementType;
import com.yuxuan.inventory.warehouse.Warehouse;
import com.yuxuan.inventory.warehouse.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InboundOrderService {

    private final InboundOrderRepository inboundOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final StockService stockService;

    public InboundOrderService(InboundOrderRepository inboundOrderRepository,
                               WarehouseRepository warehouseRepository,
                               ItemRepository itemRepository,
                               StockService stockService) {
        this.inboundOrderRepository = inboundOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.stockService = stockService;
    }

    @Transactional
    public InboundOrder create(CreateInboundOrderRequest request, String operator) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        InboundOrder order = new InboundOrder();
        order.setWarehouse(warehouse);
        order.setStatus(InboundOrderStatus.DRAFT);
        order.setCreatedBy(normalizeOperator(operator));

        for (CreateInboundOrderLineRequest lineRequest : request.lines()) {
            Item item = itemRepository.findById(lineRequest.itemId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
            InboundOrderLine line = new InboundOrderLine();
            line.setOrder(order);
            line.setItem(item);
            line.setQuantity(lineRequest.quantity());
            order.getLines().add(line);
        }

        return inboundOrderRepository.save(order);
    }

    public List<InboundOrder> query(Long warehouseId, InboundOrderStatus status) {
        if (warehouseId != null && status != null) {
            return inboundOrderRepository.findByWarehouseIdAndStatus(warehouseId, status);
        }
        if (warehouseId != null) {
            return inboundOrderRepository.findByWarehouseId(warehouseId);
        }
        if (status != null) {
            return inboundOrderRepository.findByStatus(status);
        }
        return inboundOrderRepository.findAll();
    }

    public InboundOrder getById(Long id) {
        return inboundOrderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inbound order not found"));
    }

    @Transactional
    public InboundOrder post(Long id, String operator) {
        InboundOrder order = inboundOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inbound order not found"));

        if (order.getStatus() == InboundOrderStatus.POSTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order already posted");
        }

        for (InboundOrderLine line : order.getLines()) {
            stockService.applyMovement(
                    order.getWarehouse().getId(),
                    line.getItem().getId(),
                    MovementType.IN,
                    line.getQuantity(),
                    null,
                    "inbound-order:" + order.getId(),
                    normalizeOperator(operator)
            );
        }

        order.setStatus(InboundOrderStatus.POSTED);
        order.setPostedBy(normalizeOperator(operator));
        order.setPostedAt(Instant.now());
        return inboundOrderRepository.save(order);
    }

    private String normalizeOperator(String operator) {
        return (operator == null || operator.isBlank()) ? "system" : operator.trim();
    }
}
