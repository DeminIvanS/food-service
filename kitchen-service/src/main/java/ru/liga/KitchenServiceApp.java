package ru.liga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class KitchenServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(KitchenServiceApp.class, args);
    }
}