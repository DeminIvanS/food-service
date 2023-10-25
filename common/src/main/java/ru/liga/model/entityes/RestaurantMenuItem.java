package ru.liga.model.entityes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantMenuItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "restaurant_menu_id")
    private Long id;
    @OneToMany
    private Set<Restaurant> restaurantSet;

    private String name;
    private double price;
    private String imageUrl;
    private String description;
}
