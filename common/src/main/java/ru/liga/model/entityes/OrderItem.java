package ru.liga.model.entityes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_item_id")
    private Long id;
    @ManyToOne
    @Column(name = "order_id")
    private Long orderId;
    @OneToOne
    @Column(name = "restaurant_menu_item_id")
    private Long restaurantMenuItem;

    private double price;
    private double quantity;

}
