package ru.liga.model.entities;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Table(name = "order_items")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class OrderItem{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_items_seq_gen")
    @SequenceGenerator(name = "order_items_seq_gen", sequenceName = "order_items_seq", allocationSize = 1)
    @Column(name = "order_item_id")
    private Long id;

    @OneToOne
    @Column(name = "restaurant_menu_item_id")
    private RestaurantMenuItem restaurantMenuItem;

    private Double price;
    private Double quantity;

    @ManyToOne
    @JoinColumn(name = "order_order_id")
    private Order order;



}
