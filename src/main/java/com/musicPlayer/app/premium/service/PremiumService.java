package com.musicPlayer.app.premium.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.premium.dto.PremiumDtos;
import com.musicPlayer.app.premium.entity.Subscription;
import com.musicPlayer.app.premium.repository.SubscriptionRepository;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PremiumService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    private static final Map<String, Integer> PLAN_DURATIONS = Map.of(
            "MONTHLY", 1,
            "QUARTERLY", 3,
            "ANNUAL", 12,
            "STUDENT", 1,
            "FAMILY", 1
    );

    private static final Map<String, Double> PLAN_PRICES = Map.of(
            "MONTHLY", 9.99,
            "QUARTERLY", 26.99,
            "ANNUAL", 99.99,
            "STUDENT", 4.99,
            "FAMILY", 14.99
    );

    @Transactional
    public PremiumDtos.SubscriptionResponse subscribe(PremiumDtos.SubscribeRequest request) {
        User user = getCurrentUser();
        String planType = request.getPlanType().toUpperCase();

        if (!PLAN_DURATIONS.containsKey(planType)) {
            throw new BadRequestException("Invalid plan type: " + request.getPlanType());
        }

        int durationMonths = PLAN_DURATIONS.get(planType);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusMonths(durationMonths);

        Subscription sub = Subscription.builder()
                .user(user)
                .planType(Subscription.PlanType.valueOf(planType))
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .amountPaid(PLAN_PRICES.getOrDefault(planType, 0.0))
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .paymentReference(request.getPaymentReference())
                .autoRenew(true)
                .build();

        subscriptionRepository.save(sub);

        user.setRole(User.Role.PREMIUM);
        user.setPremiumExpiresAt(endDate);
        userRepository.save(user);

        return toResponse(sub);
    }

    @Transactional
    public void cancelSubscription() {
        User user = getCurrentUser();
        subscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(
                user.getId(), Subscription.SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    sub.setStatus(Subscription.SubscriptionStatus.CANCELLED);
                    sub.setAutoRenew(false);
                    subscriptionRepository.save(sub);
                });
    }

    @Transactional(readOnly = true)
    public PageResponse<PremiumDtos.SubscriptionResponse> getMySubscriptions(int page, int size) {
        User user = getCurrentUser();
        return PageResponse.of(subscriptionRepository.findByUserId(user.getId(), PageRequest.of(page, size))
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<PremiumDtos.PlanInfo> getAvailablePlans() {
        return Arrays.asList(
                buildPlan("MONTHLY", "Monthly", 9.99, 1, List.of("Ad-free music", "Unlimited skips", "Download 10,000 songs", "HD quality")),
                buildPlan("QUARTERLY", "Quarterly (Save 10%)", 26.99, 3, List.of("All Monthly features", "10% discount")),
                buildPlan("ANNUAL", "Annual (Save 17%)", 99.99, 12, List.of("All Monthly features", "17% discount", "Priority support")),
                buildPlan("STUDENT", "Student", 4.99, 1, List.of("All Monthly features", "50% student discount")),
                buildPlan("FAMILY", "Family (6 accounts)", 14.99, 1, List.of("All Monthly features", "Up to 6 accounts"))
        );
    }

    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    @Transactional
    public void processExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findByEndDateBeforeAndStatus(
                LocalDateTime.now(), Subscription.SubscriptionStatus.ACTIVE);

        for (Subscription sub : expired) {
            sub.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);

            User user = sub.getUser();
            user.setRole(User.Role.USER);
            user.setPremiumExpiresAt(null);
            userRepository.save(user);
        }
    }

    private PremiumDtos.PlanInfo buildPlan(String type, String name, double price, int months,
                                            List<String> features) {
        PremiumDtos.PlanInfo p = new PremiumDtos.PlanInfo();
        p.setPlanType(type);
        p.setDisplayName(name);
        p.setPrice(price);
        p.setCurrency("USD");
        p.setDurationMonths(months);
        p.setFeatures(features);
        return p;
    }

    private PremiumDtos.SubscriptionResponse toResponse(Subscription sub) {
        PremiumDtos.SubscriptionResponse r = new PremiumDtos.SubscriptionResponse();
        r.setId(sub.getId());
        r.setPlanType(sub.getPlanType().name());
        r.setStatus(sub.getStatus().name());
        r.setStartDate(sub.getStartDate().toString());
        r.setEndDate(sub.getEndDate().toString());
        r.setAmountPaid(sub.getAmountPaid());
        r.setCurrency(sub.getCurrency());
        r.setAutoRenew(sub.isAutoRenew());
        r.setCreatedAt(sub.getCreatedAt() != null ? sub.getCreatedAt().toString() : null);
        return r;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}