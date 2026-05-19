package com.pos.auth.service;

import com.pos.auth.dto.LoginRequest;
import com.pos.auth.dto.TokenResponse;
import com.pos.auth.entity.RefreshToken;
import com.pos.auth.entity.User;
import com.pos.auth.jwt.JwtProvider;
import com.pos.auth.repository.RefreshTokenRepository;
import com.pos.auth.repository.UserRepository;
import com.pos.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = (User) auth.getPrincipal();
        return buildTokenResponse(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED));
        refreshTokenRepository.delete(stored);
        return buildTokenResponse(user);
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user ->
                refreshTokenRepository.deleteByUserId(user.getId()));
    }

    private TokenResponse buildTokenResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String newRefresh = jwtProvider.generateRefreshToken(user.getUsername());

        RefreshToken entity = new RefreshToken();
        entity.setUserId(user.getId());
        entity.setToken(newRefresh);
        entity.setExpiresAt(LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshExpirationMs() / 1000));
        refreshTokenRepository.save(entity);

        return new TokenResponse(accessToken, newRefresh, jwtProvider.getRefreshExpirationMs() / 1000);
    }
}
