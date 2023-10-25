package ru.liga.model.entityes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    private Long id;
    @Column(name = "customer_id")
    private Long customerId;
    @Column(name = "restaurant_id")
    private Long restaurantId;

    private String status;

    @Column(name = "courier_id")
    @Value(value = "NULL")
    private Long courierId;
    @Basic
    private java.sql.Timestamp timestamp;

}
