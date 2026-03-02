package com.example.rentalTool_BackEnd.user.service.impl;

import com.example.rentalTool_BackEnd.user.exception.UserNotFoundException;
import com.example.rentalTool_BackEnd.user.model.EmailVerificationToken;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.repo.EmailVerificationTokenJpaRepo;
import com.example.rentalTool_BackEnd.user.repo.UserRepo;
import com.example.rentalTool_BackEnd.user.service.EmailService;
import com.example.rentalTool_BackEnd.user.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class EmailVerificationServiceImpl implements EmailVerificationService {
    
    private final EmailVerificationTokenJpaRepo tokenRepo;
    private final UserRepo userRepo;
    private final EmailService emailService;
    
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    
    @Override
    @Transactional
    public EmailVerificationToken generateVerificationToken(User user) {
        // Usuń poprzedni token jeśli istnieje
        tokenRepo.findByUserId(user.getId()).ifPresent(tokenRepo::delete);
        
        // Generuj nowy token
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(TOKEN_EXPIRATION_HOURS * 3600);
        
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user.getId(), expiresAt);
        verificationToken = tokenRepo.save(verificationToken);
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), token, user.getFirstName());
            log.info("Verification email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}. Error: {}", user.getEmail(), e.getMessage());
        }
        
        return verificationToken;
    }
    
    @Override
    public Optional<EmailVerificationToken> getToken(String token) {
        return tokenRepo.findByToken(token);
    }
    
    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
        
        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Token has already been used");
        }
        
        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }
        
        User user = userRepo.findUserById(verificationToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setVerified(true);
        user.setVerifiedAt(Instant.now());
        userRepo.updateUser(user);
        
        verificationToken.setUsed(true);
        tokenRepo.save(verificationToken);
    }
    
    @Override
    @Transactional
    public void deleteToken(Long userId) {
        tokenRepo.deleteByUserId(userId);
    }
    
    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (user.isVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }
        
        generateVerificationToken(user);
    }
}

