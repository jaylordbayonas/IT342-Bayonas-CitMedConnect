package edu.cit.bayonas.citmedconnect.controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import edu.cit.bayonas.citmedconnect.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        UserDTO user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/school-id/{schoolId}")
    public ResponseEntity<?> updateUserBySchoolId(@PathVariable String schoolId, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUserBySchoolId(schoolId, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/email/user/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity loginRequest) {
        try {
            UserDTO authenticatedUser = userService.authenticateUser(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            );
            return ResponseEntity.ok(authenticatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/staff/login")
    public ResponseEntity<?> staffLogin(@RequestBody UserEntity loginRequest) {
        try {
            UserDTO authenticatedUser = userService.authenticateUser(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            );

            if (!"staff".equalsIgnoreCase(authenticatedUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                            "error", "Access denied. Staff credentials required.",
                            "actualRole", authenticatedUser.getRole()
                        ));
            }

            String adminName = "ADMIN-" + authenticatedUser.getFirstName().toUpperCase()
                             + "-" + authenticatedUser.getLastName().toUpperCase();

            return ResponseEntity.ok(Map.of(
                "user", authenticatedUser,
                "adminName", adminName,
                "permissions", Map.of(
                    "canCreateSlots", true,
                    "canCreateRecords", true,
                    "canSendNotifications", true,
                    "canManageUsers", true
                )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<?> getUserProfile(@PathVariable String email) {
        try {
            UserDTO user = userService.getUserByEmail(email);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail(),
                    "age", user.getAge(),
                    "phone", user.getPhone(),
                    "gender", user.getGender(),
                    "schoolId", user.getSchoolId(),
                    "role", user.getRole(),
                    "createdAt", user.getCreatedAt()
                ));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile/{email}")
    public ResponseEntity<?> updateUserProfile(@PathVariable String email, @RequestBody Map<String, Object> updates) {
        try {
            UserDTO existingUser = userService.getUserByEmail(email);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }

            if (updates.containsKey("role")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Unauthorized: Role modification not allowed"));
            }

            UserDTO updateDTO = new UserDTO();

            if (updates.containsKey("age")) {
                updateDTO.setAge((Integer) updates.get("age"));
            }
            if (updates.containsKey("phone")) {
                updateDTO.setPhone((String) updates.get("phone"));
            }
            if (updates.containsKey("gender")) {
                updateDTO.setGender((String) updates.get("gender"));
            }

            UserDTO updatedUser = userService.updateUserBySchoolId(existingUser.getSchoolId(), updateDTO);

            return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "user", Map.of(
                    "firstName", updatedUser.getFirstName(),
                    "lastName", updatedUser.getLastName(),
                    "email", updatedUser.getEmail(),
                    "age", updatedUser.getAge(),
                    "phone", updatedUser.getPhone(),
                    "gender", updatedUser.getGender(),
                    "schoolId", updatedUser.getSchoolId(),
                    "role", updatedUser.getRole()
                )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/verify-role/{email}")
    public ResponseEntity<?> verifyUserRole(@PathVariable String email) {
        try {
            UserDTO user = userService.getUserByEmail(email);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "schoolId", user.getSchoolId()
                ));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
