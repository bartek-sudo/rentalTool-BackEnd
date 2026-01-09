package com.example.rentalTool_BackEnd.user.repo;

import com.example.rentalTool_BackEnd.user.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenJpaRepo extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}



