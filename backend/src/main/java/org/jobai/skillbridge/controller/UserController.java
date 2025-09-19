package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.dto.AuthResponse;
import org.jobai.skillbridge.dto.UserDTO;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.UserService;
import org.jobai.skillbridge.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, HttpServletResponse response) {
        try {
            if (user.getUsername() == null || user.getUsername().isBlank()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body("Password is required");
            }
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().body("Username is already taken!");
            }
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("Email is already in use!");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userService.saveUser(user);

            // Generate JWT token for the newly registered user
            final String jwt = jwtUtil.generateToken(savedUser);

            // Set JWT as HTTP-only cookie for better security
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new AuthResponse(jwt, UserDTO.fromEntity(savedUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            final String jwt = jwtUtil.generateToken((User) userDetails);
            User userData = (User) userDetails;

            // Set JWT as HTTP-only cookie for better security
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new AuthResponse(jwt, UserDTO.fromEntity(userData)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateCurrentUserProfile(@RequestBody User userDetails, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Update only non-null fields
        if (userDetails.getUsername() != null) {
            currentUser.setUsername(userDetails.getUsername());
        }
        if (userDetails.getEmail() != null) {
            currentUser.setEmail(userDetails.getEmail());
        }
        if (userDetails.getFirstName() != null) {
            currentUser.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            currentUser.setLastName(userDetails.getLastName());
        }
        if (userDetails.getBio() != null) {
            currentUser.setBio(userDetails.getBio());
        }

        // Update employer-specific fields if user is an employer
        if ("EMPLOYER".equals(currentUser.getRole().name())) {
            if (userDetails.getCompanyName() != null) {
                currentUser.setCompanyName(userDetails.getCompanyName());
            }
            if (userDetails.getCompanyDescription() != null) {
                currentUser.setCompanyDescription(userDetails.getCompanyDescription());
            }
            if (userDetails.getCompanyWebsite() != null) {
                currentUser.setCompanyWebsite(userDetails.getCompanyWebsite());
            }
            if (userDetails.getCompanyLocation() != null) {
                currentUser.setCompanyLocation(userDetails.getCompanyLocation());
            }
            if (userDetails.getContactPhone() != null) {
                currentUser.setContactPhone(userDetails.getContactPhone());
            }
        }

        User updatedUser = userService.saveUser(currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile/employer")
    public ResponseEntity<?> updateEmployerProfile(@RequestBody User userDetails, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        User currentUser = (User) authentication.getPrincipal();

        // Verify user is an employer
        if (!"EMPLOYER".equals(currentUser.getRole().name())) {
            return ResponseEntity.status(403).body("Access denied: Only employers can update employer profile");
        }

        // Update employer-specific fields
        if (userDetails.getCompanyName() != null) {
            currentUser.setCompanyName(userDetails.getCompanyName());
        }
        if (userDetails.getCompanyDescription() != null) {
            currentUser.setCompanyDescription(userDetails.getCompanyDescription());
        }
        if (userDetails.getCompanyWebsite() != null) {
            currentUser.setCompanyWebsite(userDetails.getCompanyWebsite());
        }
        if (userDetails.getCompanyLocation() != null) {
            currentUser.setCompanyLocation(userDetails.getCompanyLocation());
        }
        if (userDetails.getContactPhone() != null) {
            currentUser.setContactPhone(userDetails.getContactPhone());
        }

        User updatedUser = userService.saveUser(currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Check if username or email already exists
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        user.setRole(userDetails.getRole());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setBio(userDetails.getBio());
        user.setActive(userDetails.isActive());

        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Logged out successfully");
    }
}