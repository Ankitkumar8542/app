package com.musicPlayer.app.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.user.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:query% OR u.email LIKE %:query%")
    Page<User> searchUsers(String query, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'PREMIUM'")
    Long countPremiumUsers();
}