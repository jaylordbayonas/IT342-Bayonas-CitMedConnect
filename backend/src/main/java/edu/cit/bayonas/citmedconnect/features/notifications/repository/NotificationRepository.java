package edu.cit.bayonas.citmedconnect.features.notifications.repository;

import edu.cit.bayonas.citmedconnect.features.notifications.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    
    List<NotificationEntity> findBySchoolId(String schoolId);
    
    List<NotificationEntity> findByNotificationType(String notificationType);
    
    List<NotificationEntity> findBySchoolIdAndNotificationType(String schoolId, String notificationType);
    
    List<NotificationEntity> findBySchoolIdAndIsRead(String schoolId, Boolean isRead);
    
    List<NotificationEntity> findByIsGlobal(Boolean isGlobal);
    
    List<NotificationEntity> findBySchoolIdOrderByCreatedAtDesc(String schoolId);
    
    List<NotificationEntity> findByNotificationTypeOrderByCreatedAtDesc(String notificationType);
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.createdAt >= :startDate AND n.createdAt <= :endDate ORDER BY n.createdAt DESC")
    List<NotificationEntity> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.schoolId = :schoolId AND n.isRead = false")
    long countUnreadBySchoolId(@Param("schoolId") String schoolId);
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.schoolId = :schoolId OR n.isGlobal = true ORDER BY n.createdAt DESC")
    List<NotificationEntity> findAllNotificationsForUser(@Param("schoolId") String schoolId);
}