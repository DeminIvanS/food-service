package ru.liga.model.entities;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Table(name = "restaurant_menu_items")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class RestaurantMenuItem{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_item_seq_gen")
    @SequenceGenerator(name = "menu_item_seq_gen", sequenceName = "menu_item_seq", allocationSize = 1)
    @Column(name = "menu_item_id")
    private Long id;

    private String name;
    private Double price;
    private String imageUrl;
    private String description;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

}
