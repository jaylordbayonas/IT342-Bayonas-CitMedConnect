package edu.cit.bayonas.citmedconnect.features.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.bayonas.citmedconnect.features.auth.dto.AuthResponse;
import edu.cit.bayonas.citmedconnect.features.auth.dto.AuthResponse.UserData;
import edu.cit.bayonas.citmedconnect.features.auth.dto.LoginRequest;
import edu.cit.bayonas.citmedconnect.features.auth.dto.RegisterRequest;
import edu.cit.bayonas.citmedconnect.features.auth.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.features.auth.service.UserService;

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
            // Resolve first/last name — prefer explicit fields, fall back to splitting "name"
            String firstName = request.getFirstName();
            String lastName  = request.getLastName();
            if ((firstName == null || firstName.isBlank()) && request.getName() != null) {
                String[] parts = request.getName().trim().split(" ", 2);
                firstName = parts[0];
                lastName  = parts.length > 1 ? parts[1] : "";
            }

            if (firstName == null || firstName.trim().isEmpty()) {
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

            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "Email already registered"));
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setFirstName(firstName.trim());
            userDTO.setLastName(lastName != null ? lastName.trim() : "");
            userDTO.setEmail(request.getEmail().trim());
            userDTO.setPassword(request.getPassword());

            // Use provided schoolId or generate one
            String schoolId = (request.getSchoolId() != null && !request.getSchoolId().isBlank())
                ? request.getSchoolId().trim()
                : UUID.randomUUID().toString();
            userDTO.setSchoolId(schoolId);

            userDTO.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
            userDTO.setRole(request.getRole() != null && !request.getRole().isBlank()
                ? request.getRole().trim().toUpperCase() : "STUDENT");
            userDTO.setGender(request.getGender() != null && !request.getGender().isBlank()
                ? request.getGender().trim() : "OTHER");
            userDTO.setAge(request.getAge() > 0 ? request.getAge() : 0);

            UserDTO createdUser = userService.createUser(userDTO);

            UserData userData = new UserData(
                createdUser.getSchoolId(),
                createdUser.getFirstName(),
                createdUser.getLastName(),
                createdUser.getEmail(),
                createdUser.getRole()
            );

            String token = generateSimpleToken();

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
                authenticatedUser.getFirstName(),
                authenticatedUser.getLastName(),
                authenticatedUser.getEmail(),
                authenticatedUser.getRole()
            );

            String token = generateSimpleToken();

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

    // User Management Endpoints
    
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{schoolId}")
    public ResponseEntity<?> getUserById(@PathVariable String schoolId) {
        try {
            UserDTO user = userService.getUserById(schoolId);
            if (user == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found with school_id: " + schoolId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            UserDTO user = userService.getUserByEmail(email);
            if (user == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found with email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            List<UserDTO> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/users/{schoolId}")
    public ResponseEntity<?> updateUser(@PathVariable String schoolId, @RequestBody UserDTO userDetails) {
        try {
            UserDTO updatedUser = userService.updateUserBySchoolId(schoolId, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/users/{schoolId}")
    public ResponseEntity<?> deleteUser(@PathVariable String schoolId) {
        try {
            boolean deleted = userService.deleteUserBySchoolId(schoolId);
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/users/{schoolId}/password")
    public ResponseEntity<?> changePassword(@PathVariable String schoolId, @RequestBody Map<String, String> passwordData) {
        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Current password and new password are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            UserDTO updatedUser = userService.changePassword(schoolId, currentPassword, newPassword);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/users/{schoolId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String schoolId, @RequestParam String role) {
        try {
            UserDTO updatedUser = userService.updateUserRole(schoolId, role);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        long count = userService.getUserCount();
        Map<String, Long> response = new HashMap<>();
        response.put("totalUsers", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/count/role/{role}")
    public ResponseEntity<Map<String, Object>> getUserCountByRole(@PathVariable String role) {
        long count = userService.getUserCountByRole(role);
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        List<UserDTO> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/exists/{schoolId}")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(@PathVariable String schoolId) {
        boolean exists = userService.userExists(schoolId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/email-exists")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate a simple token (replace with JWT in production)
     */
    private String generateSimpleToken() {
        // Simple token generation - replace with JWT in production
        return UUID.randomUUID().toString();
    }
}
