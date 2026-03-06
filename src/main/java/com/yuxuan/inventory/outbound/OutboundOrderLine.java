package com.yuxuan.inventory.outbound;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yuxuan.inventory.item.Item;
import jakarta.persistence.*;

@Entity
@Table(name = "outbound_order_lines")
public class OutboundOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "outbound_order_id", nullable = false)
    private OutboundOrder order;

    @JsonIgnoreProperties({"createdAt"})
    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private long quantity;

    public Long getId() { return id; }
    public OutboundOrder getOrder() { return order; }
    public void setOrder(OutboundOrder order) { this.order = order; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
}
