package com.yuxuan.inventory.warehouse;

import com.yuxuan.inventory.warehouse.dto.CreateWarehouseRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Warehouse create(@Valid @RequestBody CreateWarehouseRequest request) {
        return warehouseService.create(request);
    }

    @GetMapping
    public List<Warehouse> list() {
        return warehouseService.list();
    }

    @GetMapping("/{id}")
    public Warehouse get(@PathVariable Long id) {
        return warehouseService.getById(id);
    }
}
