package ru.liga.model.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Table(name = "couriers")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Courier{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "couriers_seq_gen")
    @SequenceGenerator(name = "couriers_seq_gen", sequenceName = "couriers_seq", allocationSize = 1)
    @Column(name = "courier_id")
    private Long id;

    private String phone;
    private String status;
    private String coordinates;

    @OneToMany(mappedBy = "courier")
    private List<Order> orders;
}
