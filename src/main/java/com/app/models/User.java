package com.app.models;

import jakarta.persistence.*;
import java.util.List;

import lombok.Data;
import com.app.models.Order;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.app.models.CartItem;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@Entity
@Table(name = "users")  // <-- Fix here
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String role;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    @OneToMany(mappedBy = "user")
    private List<CartItem> cartItems;
}
