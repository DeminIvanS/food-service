package ru.liga.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.liga.entity.Order;

import java.util.UUID;

@Repository
@ComponentScan
public interface OrderRepository extends JpaRepository<Order, UUID> {
}
