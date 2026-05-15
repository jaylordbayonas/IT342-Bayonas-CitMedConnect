package edu.cit.bayonas.citmedconnect.features.notifications.mapper;

import edu.cit.bayonas.citmedconnect.features.notifications.dto.NotificationDTO;
import edu.cit.bayonas.citmedconnect.features.notifications.entity.NotificationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {
    
    public NotificationDTO toDTO(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(entity.getNotificationId());
        dto.setSchoolId(entity.getSchoolId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setNotificationType(entity.getNotificationType());
        dto.setIsGlobal(entity.getIsGlobal());
        dto.setIsRead(entity.getIsRead());
        dto.setCreatedAt(entity.getCreatedAt());
        
        return dto;
    }
    
    public NotificationEntity toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }
        
        NotificationEntity entity = new NotificationEntity();
        entity.setNotificationId(dto.getNotificationId());
        entity.setSchoolId(dto.getSchoolId());
        entity.setTitle(dto.getTitle());
        entity.setMessage(dto.getMessage());
        entity.setNotificationType(dto.getNotificationType());
        entity.setIsGlobal(dto.getIsGlobal());
        entity.setIsRead(dto.getIsRead());
        entity.setCreatedAt(dto.getCreatedAt());
        
        return entity;
    }
    
    public List<NotificationDTO> toDTOList(List<NotificationEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<NotificationEntity> toEntityList(List<NotificationDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    public void updateEntityFromDTO(NotificationDTO dto, NotificationEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getSchoolId() != null) {
            entity.setSchoolId(dto.getSchoolId());
        }
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getMessage() != null) {
            entity.setMessage(dto.getMessage());
        }
        if (dto.getNotificationType() != null) {
            entity.setNotificationType(dto.getNotificationType());
        }
        if (dto.getIsGlobal() != null) {
            entity.setIsGlobal(dto.getIsGlobal());
        }
        if (dto.getIsRead() != null) {
            entity.setIsRead(dto.getIsRead());
        }
    }
}