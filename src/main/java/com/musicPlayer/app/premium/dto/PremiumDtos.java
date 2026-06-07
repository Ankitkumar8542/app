package com.musicPlayer.app.premium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class PremiumDtos {

   @Data
    public static class SubscriptionResponse {
        private Long id;
        private String planType;
        private String status;
        private String startDate;
        private String endDate;
        private Double amountPaid;
        private String currency;
        private boolean autoRenew;
        private String createdAt;
       
    }

    @Data
    public static class PlanInfo {
        private String planType;
        private String displayName;
        private Double price;
        private String currency;
        private int durationMonths;
        private List<String> features;
    }

    @Data
    public static class SubscribeRequest {
        @NotNull(message = "Plan type is required")
        private String planType;

        @NotBlank(message = "Payment reference is required")
        private String paymentReference;

        private String currency;
    }
}