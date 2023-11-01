package ru.liga.model.entities;

import lombok.*;
import lombok.experimental.Accessors;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "customers")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Customer{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customers_seq_gen")
    @SequenceGenerator(name = "customers_seq_gen", sequenceName = "customers_seq", allocationSize = 1)
    @Column(name = "customer_id")
    private Long id;

    private String phone;
    private String email;
    private String address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order){
        this.orders.add(order);
        order.setCustomer(this);
    }
    public void deletedOrder(Order order){
        this.orders.remove(order);
        order.setCustomer(null);
    }


}
