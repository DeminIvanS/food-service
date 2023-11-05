package ru.liga.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import ru.liga.model.entities.Order;
import ru.liga.model.statuses.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findOrderByStatus(OrderStatus status, Pageable pageable);

}