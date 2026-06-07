package com.musicPlayer.app.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicPlayer.app.auth.dto.AuthDtos;
import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.exception.DuplicateResourceException;
import com.musicPlayer.app.security.jwt.JwtTokenProvider;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String RESET_TOKEN_PREFIX = "reset_token:";

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .country(request.getCountry())
                .role(User.Role.USER)
                .status(User.AccountStatus.ACTIVE)
                .build();

        if (request.getDateOfBirth() != null) {
            try {
                user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date format. Use yyyy-MM-dd");
            }
        }

        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        storeRefreshToken(user.getId(), refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        storeRefreshToken(user.getId(), refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthDtos.AuthResponse refreshToken(AuthDtos.RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String email = jwtTokenProvider.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!jwtTokenProvider.isTokenValid(token, user)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String storedToken = (String) redisTemplate.opsForValue()
                .get(REFRESH_TOKEN_PREFIX + user.getId());

        if (!token.equals(storedToken)) {
            throw new BadRequestException("Refresh token has been revoked");
        }

        String newAccessToken = jwtTokenProvider.generateToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
        storeRefreshToken(user.getId(), newRefreshToken);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    public void logout(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    @Transactional
    public void changePassword(Long userId, AuthDtos.ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public String initiatePasswordReset(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with that email"));

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + resetToken, email, 15, TimeUnit.MINUTES);
        return resetToken;
    }

    @Transactional
    public void resetPassword(AuthDtos.ResetPasswordRequest request) {
        String email = (String) redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + request.getToken());
        if (email == null) throw new BadRequestException("Invalid or expired reset token");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTemplate.delete(RESET_TOKEN_PREFIX + request.getToken());
    }

    private void storeRefreshToken(Long userId, String token) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId, token, 7, TimeUnit.DAYS
        );
    }

    private AuthDtos.AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthDtos.AuthResponse.UserInfo userInfo = new AuthDtos.AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(user.getName());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole().name());
        userInfo.setProfileImage(user.getProfileImage());
        userInfo.setPremium(user.isPremium());

        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtTokenProvider.getExpirationTime() / 1000);
        response.setUser(userInfo);
        return response;
    }
}