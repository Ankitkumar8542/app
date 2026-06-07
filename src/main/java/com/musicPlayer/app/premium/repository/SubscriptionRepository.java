package com.musicPlayer.app.premium.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.premium.entity.Subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByUserIdAndStatusOrderByEndDateDesc(
            Long userId, Subscription.SubscriptionStatus status);

    List<Subscription> findByEndDateBeforeAndStatus(
            LocalDateTime dateTime, Subscription.SubscriptionStatus status);

    Page<Subscription> findByUserId(Long userId, Pageable pageable);
}