package com.deadstock.marketplace.controller.api;

import com.deadstock.marketplace.dto.LoginRequest;
import com.deadstock.marketplace.dto.RegisterRequest;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for user registration and authentication")
public class AuthApiController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository;

    public AuthApiController(UserService userService, 
                             AuthenticationManager authenticationManager,
                             org.springframework.security.web.context.SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new business user (Seller or Buyer)")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User registered = userService.registerUser(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", registered.getId());
            response.put("email", registered.getEmail());
            response.put("role", registered.getRole().name());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and create session")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginReq, 
                                   jakarta.servlet.http.HttpServletRequest request, 
                                   jakarta.servlet.http.HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword())
            );
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            Map<String, Object> resMap = new HashMap<>();
            resMap.put("message", "Authentication successful");
            resMap.put("email", loginReq.getEmail());
            resMap.put("authorities", authentication.getAuthorities());
            return ResponseEntity.ok(resMap);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid credentials provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
