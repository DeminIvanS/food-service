package ru.liga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.liga.entity.RestaurantMenuItem;

import java.util.UUID;

@Repository
public interface RestaurantMenuItemRepo extends JpaRepository<RestaurantMenuItem, UUID> {
}
