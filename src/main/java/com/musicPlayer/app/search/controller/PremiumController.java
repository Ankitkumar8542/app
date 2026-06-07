package com.musicPlayer.app.search.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.premium.dto.PremiumDtos;
import com.musicPlayer.app.premium.service.PremiumService;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/premium")
@RequiredArgsConstructor
@Tag(name = "Premium", description = "Subscription plans and management")
public class PremiumController {

    private final PremiumService premiumService;

    @GetMapping("/plans")
    @Operation(summary = "Get all available subscription plans")
    public ResponseEntity<ApiResponse<List<PremiumDtos.PlanInfo>>> getAvailablePlans() {
        return ResponseEntity.ok(ApiResponse.success(premiumService.getAvailablePlans()));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to a premium plan")
    public ResponseEntity<ApiResponse<PremiumDtos.SubscriptionResponse>> subscribe(
            @Valid @RequestBody PremiumDtos.SubscribeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subscription activated",
                premiumService.subscribe(request)));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel current subscription")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription() {
        premiumService.cancelSubscription();
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled", null));
    }

    @GetMapping("/my-subscriptions")
    @Operation(summary = "Get current user's subscription history")
    public ResponseEntity<ApiResponse<PageResponse<PremiumDtos.SubscriptionResponse>>> getMySubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(premiumService.getMySubscriptions(page, size)));
    }
}