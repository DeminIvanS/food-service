package ru.liga.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.Accessors;
import ru.liga.status.KitchenStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table(name = "restaurant")
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restaurant_seq_gen")
    @SequenceGenerator(name = "restaurant_seq_gen", sequenceName = "restaurant_seq", allocationSize = 1)
    private UUID id;

    private String address;

    @Enumerated(EnumType.STRING)
    private KitchenStatus status;

    private String name;


    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "restaurantId", fetch = FetchType.LAZY)
    private List<RestaurantMenuItem> restaurantMenuItems = new ArrayList<>();

    public void addMenuItem(RestaurantMenuItem menuItem) {
        restaurantMenuItems.add(menuItem);
        menuItem.setRestaurantId(this.id);
    }

    public void removeMenuItem(RestaurantMenuItem menuItem) {
        restaurantMenuItems.remove(menuItem);
        menuItem.setRestaurantId(null);
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setRestaurant(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setRestaurant(null);
    }
}
