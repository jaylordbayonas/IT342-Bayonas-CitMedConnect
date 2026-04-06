package edu.cit.bayonas.citmedconnect.repository;

import edu.cit.bayonas.citmedconnect.entity.MedicalRecordEntity;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecordEntity, Long> {
    
    List<MedicalRecordEntity> findByUser(UserEntity user);
    
    List<MedicalRecordEntity> findByUser_SchoolId(String schoolId);
    
    List<MedicalRecordEntity> findByAppointment(AppointmentEntity appointment);
    
    MedicalRecordEntity findByAppointment_AppointmentId(Long appointmentId);
    
    List<MedicalRecordEntity> findByUserAndRecordDateBetween(UserEntity user, LocalDateTime startDate, LocalDateTime endDate);
    
    List<MedicalRecordEntity> findByRecordDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<MedicalRecordEntity> findByCreatedBy(String createdBy);
    
    List<MedicalRecordEntity> findByUserOrderByRecordDateDesc(UserEntity user);
    
    List<MedicalRecordEntity> findByUser_SchoolIdOrderByRecordDateDesc(String schoolId);
    
    long countByUser(UserEntity user);
}

