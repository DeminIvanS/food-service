package ru.liga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.liga.model.entities.Courier;
@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    @Transactional
    Courier findCourierById(Long id);
}