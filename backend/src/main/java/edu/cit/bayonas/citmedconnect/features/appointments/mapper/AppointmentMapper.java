package edu.cit.bayonas.citmedconnect.features.appointments.mapper;

import edu.cit.bayonas.citmedconnect.features.appointments.dto.AppointmentResponseDTO;
import edu.cit.bayonas.citmedconnect.features.appointments.entity.AppointmentEntity;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDTO toResponseDTO(AppointmentEntity entity) {
        if (entity == null) {
            return null;
        }

        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setAppointmentId(entity.getAppointmentId());
        dto.setStatus(entity.getStatus());
        dto.setReason(entity.getReason());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getUser() != null) {
            dto.setStudentId(entity.getUser().getSchoolId());
            dto.setStudentName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName());
        }

        if (entity.getTimeSlot() != null) {
            dto.setTimeSlotId(entity.getTimeSlot().getTimeSlotId());
        }

        return dto;
    }
}