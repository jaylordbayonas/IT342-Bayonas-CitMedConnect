package edu.cit.bayonas.citmedconnect.repository;

import edu.cit.bayonas.citmedconnect.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByUser(UserEntity user);
    
    List<AppointmentEntity> findByUser_SchoolId(String schoolId);
    
    List<AppointmentEntity> findByStatus(String status);
    
    List<AppointmentEntity> findByTimeSlot(TimeSlot timeSlot);

    List<AppointmentEntity> findByTimeSlot_TimeSlotId(Long timeSlotId);
    
    List<AppointmentEntity> findByUserAndStatus(UserEntity user, String status);

    boolean existsByUserAndTimeSlot(UserEntity user, TimeSlot timeSlot);

    long countByTimeSlot(TimeSlot timeSlot);
}
