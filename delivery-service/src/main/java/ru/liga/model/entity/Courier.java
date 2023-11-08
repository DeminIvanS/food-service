package ru.liga.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.Accessors;
import ru.liga.model.status.CourierStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


    @Table(name = "courier")
    @Entity

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public class Courier {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "courier_seq_gen")
        @SequenceGenerator(name = "courier_seq_gen", sequenceName = "courier_seq", allocationSize = 1)
        private UUID id;

        private String phone;

        @Enumerated(EnumType.STRING)
        private CourierStatus status;

        private String coordinates;

        @Transient
        private List<String> orders = new ArrayList<>();

    }

