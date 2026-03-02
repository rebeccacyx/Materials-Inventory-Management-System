package com.yuxuan.inventory.item;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemRepository repo;

    public ItemController(ItemRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Item create(@RequestBody Item item) {
        if (item.getSku() == null || item.getSku().isBlank()) {
            throw new IllegalArgumentException("sku is required");
        }
        if (repo.existsBySku(item.getSku())) {
            throw new IllegalArgumentException("sku already exists: " + item.getSku());
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (item.getUnit() == null || item.getUnit().isBlank()) {
            throw new IllegalArgumentException("unit is required");
        }
        return repo.save(item);
    }

    @GetMapping
    public List<Item> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Item get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("item not found: " + id));
    }
}