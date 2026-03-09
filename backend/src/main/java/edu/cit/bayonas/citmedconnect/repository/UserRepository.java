package edu.cit.bayonas.citmedconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.bayonas.citmedconnect.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    boolean existsByEmail(String email);
    UserEntity findByEmail(String email);
    UserEntity findBySchoolId(String schoolId);
    List<UserEntity> findByRole(String role);
}
