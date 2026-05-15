package edu.cit.bayonas.citmedconnect.features.appointments.mapper;

import edu.cit.bayonas.citmedconnect.features.appointments.dto.TimeSlotDTO;
import edu.cit.bayonas.citmedconnect.features.appointments.entity.TimeSlot;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {

    public TimeSlotDTO toDTO(TimeSlot entity) {
        if (entity == null) {
            return null;
        }

        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setTimeSlotId(entity.getTimeSlotId());
        dto.setSlotDate(entity.getSlotDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setAvailable(entity.isAvailable());
        dto.setMaxBookings(entity.getMaxBookings());
        dto.setCurrentBookings(entity.getCurrentBookings());
        dto.setStaffId(entity.getStaffId());
        dto.setWithinBusinessHours(entity.isWithinBusinessHours());

        return dto;
    }

    public TimeSlot toEntity(TimeSlotDTO dto) {
        if (dto == null) {
            return null;
        }

        TimeSlot entity = new TimeSlot();
        entity.setTimeSlotId(dto.getTimeSlotId());
        entity.setSlotDate(dto.getSlotDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setAvailable(dto.isAvailable());
        entity.setMaxBookings(dto.getMaxBookings());
        entity.setCurrentBookings(dto.getCurrentBookings());
        entity.setStaffId(dto.getStaffId());
        entity.setWithinBusinessHours(dto.isWithinBusinessHours());

        return entity;
    }
}