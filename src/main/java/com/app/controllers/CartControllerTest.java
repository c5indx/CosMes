package com.app.controllers;

import com.app.models.*;
import com.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/cart")
public class CartControllerTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<CartItem> getCartItemsByUserId(@PathVariable Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<?> addCartItem(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        if (quantity <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be greater than zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            return ResponseEntity.badRequest().body("Not enough stock available");
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        CartItem item = new CartItem();
        item.setUser(user);
        item.setProduct(product);
        item.setQuantity(quantity);

        return ResponseEntity.ok(cartItemRepository.save(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int newQuantity = Integer.parseInt(body.get("quantity").toString());
        if (newQuantity <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be greater than zero");
        }

        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        Product product = item.getProduct();
        int oldQuantity = item.getQuantity();
        int difference = newQuantity - oldQuantity;

        if (difference > 0) {
            if (product.getStockQuantity() < difference) {
                return ResponseEntity.badRequest().body("Not enough stock available to increase quantity");
            }
            product.setStockQuantity(product.getStockQuantity() - difference);
        } else if (difference < 0) {
            product.setStockQuantity(product.getStockQuantity() + Math.abs(difference));
        }

        productRepository.save(product);
        item.setQuantity(newQuantity);

        return ResponseEntity.ok(cartItemRepository.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long id) {
        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        Product product = item.getProduct();
        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        productRepository.save(product);

        cartItemRepository.deleteById(id);
        return ResponseEntity.ok("Cart item deleted and stock restored");
    }

    @DeleteMapping("/checkout/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        if (items.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is already empty");
        }

        cartItemRepository.deleteAll(items);
        return ResponseEntity.ok("Checkout successful. Cart cleared.");
    }

    @PostMapping("/checkout/save/{userId}")
    public ResponseEntity<?> saveCheckout(@PathVariable Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        if (items.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is already empty");
        }

        double total = items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(total);

        List<OrderItem> orderItems = items.stream().map(cartItem -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductName(cartItem.getProduct().getName());
            oi.setQuantity(cartItem.getQuantity());
            oi.setPrice(cartItem.getProduct().getPrice());
            return oi;
        }).toList();

        order.setItems(orderItems);
        orderRepository.save(order);

        cartItemRepository.deleteAll(items);

        return ResponseEntity.ok("Checkout successful. Cart cleared and order saved.");
    }
}
