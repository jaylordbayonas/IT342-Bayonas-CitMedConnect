package edu.cit.bayonas.citmedconnect.repository;

import edu.cit.bayonas.citmedconnect.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    // Find available time slots for a specific date
    List<TimeSlot> findBySlotDateAndIsAvailable(LocalDate date, boolean isAvailable);
    
    // Find time slots for a specific staff member
    List<TimeSlot> findByStaffId(String staffId);
}