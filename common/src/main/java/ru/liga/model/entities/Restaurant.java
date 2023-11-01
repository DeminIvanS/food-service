package ru.liga.model.entities;

import lombok.*;
import lombok.experimental.Accessors;
import ru.liga.model.enums.OrderStatus;

import javax.persistence.*;
import java.util.List;

@Table(name = "restaurants")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Restaurant{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restaurant_seq_gen")
    @SequenceGenerator(name = "restaurant_seq_gen", sequenceName = "restaurant_seq", allocationSize = 1)
    @Column(name = "restaurant_id")
    private Long id;

    private String address;

    @Enumerated(EnumType.STRING)
    private OrderStatus kitchenStatus;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantMenuItem> restaurantMenuItems;

    public void addItemMenu(RestaurantMenuItem menuItem) {
        this.restaurantMenuItems.add(menuItem);
        menuItem.setRestaurant(this);
    }

    public void deletedItemMenu(RestaurantMenuItem menuItem){
        this.restaurantMenuItems.remove(menuItem);
        menuItem.setRestaurant(null);
    }
    public void addOrder(Order order) {
        this.orders.add(order);
        order.setRestaurant(this);
    }
    public void deletedOrder(Order order) {
        this.orders.remove(order);
        order.setRestaurant(null);
    }

}
