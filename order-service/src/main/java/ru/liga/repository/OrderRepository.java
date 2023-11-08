package ru.liga.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@ComponentScan
public interface OrderRepository extends JpaRepository<Order, UUID> {
}
