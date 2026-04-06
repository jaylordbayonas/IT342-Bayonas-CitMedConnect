package edu.cit.bayonas.citmedconnect.repository;

import edu.cit.bayonas.citmedconnect.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    List<NotificationEntity> findBySchoolId(String schoolId);
    List<NotificationEntity> findByNotificationType(String notificationType);
    List<NotificationEntity> findBySchoolIdAndNotificationType(String schoolId, String notificationType);
}