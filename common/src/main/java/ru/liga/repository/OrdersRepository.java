package ru.liga.repository;

import org.springframework.data.repository.CrudRepository;
import ru.liga.model.entityes.Order;

public interface OrdersRepository extends CrudRepository<Order, Long> {
    Order findOrderByStatus(String status);
}