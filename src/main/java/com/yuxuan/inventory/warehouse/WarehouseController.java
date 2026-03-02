package com.yuxuan.inventory.warehouse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {

    private final WarehouseRepository repo;

    public WarehouseController(WarehouseRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Warehouse create(@RequestBody Warehouse w) {
        if (w.getName() == null || w.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return repo.save(w);
    }

    @GetMapping
    public List<Warehouse> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Warehouse get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("warehouse not found: " + id));
    }
}