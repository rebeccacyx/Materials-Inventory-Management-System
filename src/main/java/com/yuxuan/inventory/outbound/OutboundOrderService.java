package com.yuxuan.inventory.outbound;

import com.yuxuan.inventory.common.ApiException;
import com.yuxuan.inventory.item.Item;
import com.yuxuan.inventory.item.ItemRepository;
import com.yuxuan.inventory.stockMovement.MovementType;
import com.yuxuan.inventory.warehouse.Warehouse;
import com.yuxuan.inventory.warehouse.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OutboundOrderService {

    private final OutboundOrderRepository outboundOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final com.yuxuan.inventory.stock.StockService stockService;

    public OutboundOrderService(OutboundOrderRepository outboundOrderRepository,
                                WarehouseRepository warehouseRepository,
                                ItemRepository itemRepository,
                                com.yuxuan.inventory.stock.StockService stockService) {
        this.outboundOrderRepository = outboundOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.stockService = stockService;
    }

    @Transactional
    public OutboundOrder create(CreateOutboundOrderRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        OutboundOrder order = new OutboundOrder();
        order.setWarehouse(warehouse);
        order.setStatus(OutboundOrderStatus.DRAFT);

        for (CreateOutboundOrderLineRequest lineRequest : request.lines()) {
            Item item = itemRepository.findById(lineRequest.itemId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
            OutboundOrderLine line = new OutboundOrderLine();
            line.setOrder(order);
            line.setItem(item);
            line.setQuantity(lineRequest.quantity());
            order.getLines().add(line);
        }

        return outboundOrderRepository.save(order);
    }


    public List<OutboundOrder> query(Long warehouseId, OutboundOrderStatus status) {
        if (warehouseId != null && status != null) {
            return outboundOrderRepository.findByWarehouseIdAndStatus(warehouseId, status);
        }
        if (warehouseId != null) {
            return outboundOrderRepository.findByWarehouseId(warehouseId);
        }
        if (status != null) {
            return outboundOrderRepository.findByStatus(status);
        }
        return outboundOrderRepository.findAll();
    }

    public OutboundOrder getById(Long id) {
        return outboundOrderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Outbound order not found"));
    }

    @Transactional
    public OutboundOrder post(Long id) {
        OutboundOrder order = getById(id);

        if (order.getStatus() == OutboundOrderStatus.POSTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order already posted");
        }

        for (OutboundOrderLine line : order.getLines()) {
            stockService.applyMovement(
                    order.getWarehouse().getId(),
                    line.getItem().getId(),
                    MovementType.OUT,
                    line.getQuantity(),
                    null,
                    "outbound-order:" + order.getId()
            );
        }

        order.setStatus(OutboundOrderStatus.POSTED);
        order.setPostedAt(Instant.now());
        return outboundOrderRepository.save(order);
    }
}
