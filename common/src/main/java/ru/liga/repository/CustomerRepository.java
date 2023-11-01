package ru.liga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.liga.model.entities.Customer;
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Transactional
    Customer findCustomerById(Long id);
}