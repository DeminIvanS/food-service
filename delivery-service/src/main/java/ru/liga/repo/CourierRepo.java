package ru.liga.repo;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.liga.model.entity.Courier;
import ru.liga.model.status.CourierStatus;

import java.util.List;

@Repository
@ComponentScan
public interface CourierRepo extends JpaRepository<Courier, Long> {

    List<Courier> findAllByStatus(CourierStatus status);
}
