package ru.liga.model.entities;

import lombok.*;
import lombok.experimental.Accessors;
import ru.liga.model.enums.OrderStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Table(name = "orders")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Order{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq_gen")
    @SequenceGenerator(name = "order_seq_gen", sequenceName = "order_seq", allocationSize = 1)
    @Column(name = "order_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Basic
    private java.sql.Timestamp timestamp;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> itemList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "courier_courier_id")
    private Courier courier;

    @ManyToOne
    @JoinColumn(name = "customer_customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "restaurant_restaurant_id")
    private Restaurant restaurant;

    public void addOrderItem(OrderItem orderItem){
        this.itemList.add(orderItem);
        orderItem.setOrder(this);
    }
    public void removeOrderItem(OrderItem orderItem){
        this.itemList.remove(orderItem);
        orderItem.setOrder(null);
    }

}
