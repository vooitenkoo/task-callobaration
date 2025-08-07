package com.example.task_collaboration.domain.repository;

import com.example.task_collaboration.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Modifying
    @Query(value = "UPDATE users SET profile_id = :profileId WHERE id = :userId", nativeQuery = true)
    void updateProfileId(@Param("userId") UUID userId, @Param("profileId") UUID profileId);


    Optional<User> findByEmail(String email);
}