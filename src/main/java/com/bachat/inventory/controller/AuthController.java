package com.bachat.inventory.controller;

import com.bachat.inventory.domain.AppUser;
import com.bachat.inventory.dto.AuthRequest;
import com.bachat.inventory.dto.AuthResponse;
import com.bachat.inventory.exception.BadRequestException;
import com.bachat.inventory.exception.ConflictException;
import com.bachat.inventory.repository.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Authentication", description = "Login, register, and session management")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager,
                          AppUserRepository userRepo,
                          PasswordEncoder encoder) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest req) {
        if (userRepo.existsByUsername(req.getUsername().trim().toLowerCase())) {
            throw new ConflictException("Username already exists: " + req.getUsername());
        }

        AppUser user = new AppUser();
        user.setUsername(req.getUsername().trim().toLowerCase());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setDisplayName(req.getDisplayName() != null ? req.getDisplayName().trim() : req.getUsername());
        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(user.getUsername(), user.getDisplayName(),
                        "User registered successfully", null));
    }

    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req,
                                               HttpServletRequest httpReq) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getUsername().trim().toLowerCase(),
                            req.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = httpReq.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            AppUser user = userRepo.findByUsername(req.getUsername().trim().toLowerCase())
                    .orElseThrow();
            LocalDateTime previousLogin = user.getLastLogin();
            user.setLastLogin(LocalDateTime.now());
            userRepo.save(user);

            return ResponseEntity.ok(new AuthResponse(
                    user.getUsername(), user.getDisplayName(),
                    "Login successful", previousLogin));

        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid username or password");
        }
    }

    @Operation(summary = "Get current logged-in user info")
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(
                user.getUsername(), user.getDisplayName(),
                "Authenticated", user.getLastLogin()));
    }
}
