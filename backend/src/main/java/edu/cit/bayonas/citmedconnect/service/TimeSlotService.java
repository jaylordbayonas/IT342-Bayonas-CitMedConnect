package edu.cit.bayonas.citmedconnect.service;

import edu.cit.bayonas.citmedconnect.entity.TimeSlot;
import edu.cit.bayonas.citmedconnect.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class TimeSlotService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public TimeSlot createSlot(TimeSlot slot) {
        return timeSlotRepository.save(slot);
    }

    public List<TimeSlot> getAllSlots() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot getSlotById(Long id) {
        return timeSlotRepository.findById(id).orElse(null);
    }

    public TimeSlot updateSlot(Long id, TimeSlot slot) {
        TimeSlot existing = timeSlotRepository.findById(id).orElse(null);
        
        if (existing != null) {
            existing.setSlotDate(slot.getSlotDate());
            existing.setStartTime(slot.getStartTime());
            existing.setEndTime(slot.getEndTime());
            existing.setAvailable(slot.isAvailable());
            existing.setMaxBookings(slot.getMaxBookings());
            existing.setCurrentBookings(slot.getCurrentBookings());
            existing.setStaffId(slot.getStaffId());
            existing.setWithinBusinessHours(slot.isWithinBusinessHours());
            return timeSlotRepository.save(existing);
        }
        
        return null;
    }

    public boolean deleteSlot(Long id) {
        if (timeSlotRepository.existsById(id)) {
            timeSlotRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<TimeSlot> findAvailableSlots(LocalDate date) {
        return timeSlotRepository.findBySlotDateAndIsAvailable(date, true);
    }
    
    public List<TimeSlot> getStaffSlots(String staffId) {
        return timeSlotRepository.findByStaffId(staffId);
    }
}