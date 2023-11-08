package ru.liga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.liga.entity.Order;
import ru.liga.entity.Restaurant;
import ru.liga.entity.RestaurantMenuItem;

import java.util.UUID;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, UUID> {
    Restaurant findByRestaurantMenuItems(RestaurantMenuItem restaurantMenuItems);
    Restaurant findByOrders(Order orders);
}
