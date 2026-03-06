package com.yuxuan.inventory.item;

import com.yuxuan.inventory.item.dto.CreateItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Item create(@Valid @RequestBody CreateItemRequest request) {
        return itemService.create(request);
    }

    @GetMapping
    public List<Item> list() {
        return itemService.list();
    }

    @GetMapping("/{id}")
    public Item get(@PathVariable Long id) {
        return itemService.getById(id);
    }
}
