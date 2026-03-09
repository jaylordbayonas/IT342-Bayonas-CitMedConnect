package edu.cit.bayonas.citmedconnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.cit.bayonas.citmedconnect.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.mapper.UserMapper;
import edu.cit.bayonas.citmedconnect.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }

        UserEntity entity = mapper.toEntity(userDTO);

        if (userDTO.getPassword() != null) {
            entity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        UserEntity savedEntity = userRepository.save(entity);
        return mapper.toDTO(savedEntity);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(String id) {
        UserEntity entity = userRepository.findById(id).orElse(null);
        return mapper.toDTO(entity);
    }

    public UserDTO updateUser(String id, UserDTO userDTO) {
        UserEntity existingEntity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        mapper.updateEntityFromDTO(userDTO, existingEntity);
        UserEntity updatedEntity = userRepository.save(existingEntity);

        return mapper.toDTO(updatedEntity);
    }

    public UserDTO updateUserBySchoolId(String schoolId, UserDTO userDTO) {
        UserEntity existingEntity = Optional.ofNullable(userRepository.findBySchoolId(schoolId))
                .orElseThrow(() -> new RuntimeException("User not found with schoolId: " + schoolId));

        mapper.updateEntityFromDTO(userDTO, existingEntity);
        UserEntity updatedEntity = userRepository.save(existingEntity);

        return mapper.toDTO(updatedEntity);
    }

    public ResponseEntity<Map<String, Boolean>> deleteUser(String id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            Map<String, Boolean> response = new HashMap<>();
            response.put("deleted", Boolean.TRUE);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    public UserDTO getUserByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email);
        return mapper.toDTO(entity);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserDTO findByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email);
        return mapper.toDTO(entity);
    }

    public UserDTO authenticateUser(String email, String password) {
        UserEntity entity = userRepository.findByEmail(email);
        if (entity == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        if (!passwordEncoder.matches(password, entity.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return mapper.toDTO(entity);
    }
}
