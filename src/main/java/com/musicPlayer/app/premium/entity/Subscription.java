package com.musicPlayer.app.premium.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.musicPlayer.app.common.util.BaseEntity;
import com.musicPlayer.app.user.entity.User;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "amount_paid")
    private Double amountPaid;

    private String currency;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "auto_renew")
    @Builder.Default
    private boolean autoRenew = true;

    public enum PlanType {
        MONTHLY, QUARTERLY, ANNUAL, STUDENT, FAMILY
    }

    public enum SubscriptionStatus {
        ACTIVE, EXPIRED, CANCELLED, PENDING
    }
}