package edu.cit.bayonas.citmedconnect.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.bayonas.citmedconnect.dto.AuthResponse;
import edu.cit.bayonas.citmedconnect.dto.AuthResponse.UserData;
import edu.cit.bayonas.citmedconnect.dto.LoginRequest;
import edu.cit.bayonas.citmedconnect.dto.RegisterRequest;
import edu.cit.bayonas.citmedconnect.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            // Validate input
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Name is required"));
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Password must be at least 6 characters"));
            }

            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "Email already registered"));
            }

            // Create new user
            UserDTO userDTO = new UserDTO();
            
            // Split name into first and last name
            String[] nameParts = request.getName().trim().split(" ", 2);
            userDTO.setFirstName(nameParts[0]);
            userDTO.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            
            userDTO.setEmail(request.getEmail());
            userDTO.setPassword(request.getPassword());
            userDTO.setSchoolId(UUID.randomUUID().toString()); // Generate unique ID
            
            UserDTO createdUser = userService.createUser(userDTO);

            // Prepare response
            UserData userData = new UserData(
                createdUser.getSchoolId(),
                createdUser.getFirstName() + " " + createdUser.getLastName(),
                createdUser.getEmail()
            );
            
            String token = generateSimpleToken(createdUser.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(true, "Registration successful", token, userData));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Password is required"));
            }

            // Authenticate user
            UserDTO authenticatedUser = userService.authenticateUser(request.getEmail(), request.getPassword());

            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password"));
            }

            // Prepare response
            UserData userData = new UserData(
                authenticatedUser.getSchoolId(),
                authenticatedUser.getFirstName() + " " + authenticatedUser.getLastName(),
                authenticatedUser.getEmail()
            );
            
            String token = generateSimpleToken(authenticatedUser.getEmail());

            return ResponseEntity.ok()
                .body(new AuthResponse(true, "Login successful", token, userData));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(false, "Login failed: " + e.getMessage()));
        }
    }

    /**
     * Generate a simple token (replace with JWT in production)
     */
    private String generateSimpleToken(String email) {
        // Simple token generation - replace with JWT in production
        return UUID.randomUUID().toString();
    }
}
