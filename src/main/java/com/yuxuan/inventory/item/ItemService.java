package com.yuxuan.inventory.item;

import com.yuxuan.inventory.common.ApiException;
import com.yuxuan.inventory.item.dto.CreateItemRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item create(CreateItemRequest request) {
        if (itemRepository.existsBySku(request.sku())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "sku already exists: " + request.sku());
        }

        Item item = new Item();
        item.setSku(request.sku());
        item.setName(request.name());
        item.setUnit(request.unit());

        return itemRepository.save(item);
    }

    public List<Item> list() {
        return itemRepository.findAll();
    }

    public Item getById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
    }
}
