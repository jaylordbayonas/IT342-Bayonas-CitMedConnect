package edu.cit.bayonas.citmedconnect.mapper;

import org.springframework.stereotype.Component;

import edu.cit.bayonas.citmedconnect.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;

@Component
public class UserMapper {

    public UserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setSchoolId(entity.getSchoolId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setRole(entity.getRole());
        dto.setGender(entity.getGender());
        dto.setAge(entity.getAge());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }

    public UserEntity toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setSchoolId(dto.getSchoolId());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setRole(dto.getRole());
        entity.setGender(dto.getGender());
        entity.setAge(dto.getAge());

        return entity;
    }

    public void updateEntityFromDTO(UserDTO dto, UserEntity entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getFirstName() != null) {
            entity.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            entity.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
        // Role update intentionally excluded — only modifiable through admin endpoints
        if (dto.getGender() != null) {
            entity.setGender(dto.getGender());
        }
        if (dto.getAge() > 0) {
            entity.setAge(dto.getAge());
        }
    }
}
