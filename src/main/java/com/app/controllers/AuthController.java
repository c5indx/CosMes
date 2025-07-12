package com.app.controllers;

import com.app.models.LoginRequest;
import com.app.models.User;
import com.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public User registerUser(@RequestBody User user) {
        // WARNING: Insecure! In real apps, hash the password first
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            return "Login successful"; // Replace with token generation later
        } else {
            return "Invalid credentials";
        }
    }
}
