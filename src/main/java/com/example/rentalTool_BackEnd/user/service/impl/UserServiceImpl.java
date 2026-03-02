package com.example.rentalTool_BackEnd.user.service.impl;

import com.example.rentalTool_BackEnd.user.exception.IllegalAccountAccessException;
import com.example.rentalTool_BackEnd.user.exception.UserAlreadyExistException;
import com.example.rentalTool_BackEnd.user.exception.UserNotFoundException;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import com.example.rentalTool_BackEnd.user.repo.UserRepo;
import com.example.rentalTool_BackEnd.user.service.EmailVerificationService;
import com.example.rentalTool_BackEnd.user.service.UserService;
import com.example.rentalTool_BackEnd.user.service.mapper.UserExternalMapper;
import com.example.rentalTool_BackEnd.user.spi.UserExternalDto;
import com.example.rentalTool_BackEnd.user.spi.UserExternalService;
import com.example.rentalTool_BackEnd.user.web.requests.ChangePasswordRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService, UserExternalService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final UserExternalMapper userExternalMapper;

    @Override
    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepo.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found by email"));
    }

    @Override
    public Optional<User> findOptionalByEmail(String email) {
        return userRepo.findUserByEmail(email);
    }

    @Override
    public User getUserById(long id) throws UserNotFoundException {
        return userRepo.findUserById(id).orElseThrow(() -> new UserNotFoundException("User not found by id"));
    }

    @Override
    @Transactional
    public User registerUser(UserRegisterRequest userRegisterRequest, UserType userType) throws UserAlreadyExistException {
        userRepo.findUserByEmail(userRegisterRequest.email()).ifPresent(u -> {
            throw new UserAlreadyExistException("User already exists");
        });
        User user = userRepo.createUser(new User(
                passwordEncoder.encode(userRegisterRequest.password()), 
                userRegisterRequest.email(), 
                userRegisterRequest.lastName(), 
                userRegisterRequest.firstName(), 
                userRegisterRequest.phoneNumber(),
                userType));
        
        // Wyślij email weryfikacyjny
        emailVerificationService.generateVerificationToken(user);
        
        return user;
    }

    @Override
    public User updateUser(User user) throws UserNotFoundException {
        userRepo.findUserById(user.getId()).orElseThrow(() -> new UserNotFoundException("User not found by id"));
        return userRepo.updateUser(user);
    }

    @Override
    @Transactional
    public void changeUserPassword(long userId, ChangePasswordRequest changePasswordRequest) {
        final User user = getUserById(userId);

        if (!passwordEncoder.matches(changePasswordRequest.oldPassword(), user.getPassword())) {
            throw new IllegalAccountAccessException("Old password is incorrect");
        }

        user.changePassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
        user.setUpdatedAt(Instant.now());
        updateUser(user);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepo.findUsersBySearch(search.trim(), pageable);
        }
        return userRepo.findAll(pageable);
    }

    @Override
    public User blockUser(Long id) {
        User user = getUserById(id);
        user.setBlocked(true);
        user.setBlockedAt(Instant.now());
        return updateUser(user);
    }

    @Override
    public User unblockUser(Long id) {
        User user = getUserById(id);
        user.setBlocked(false);
        user.setUpdatedAt(Instant.now());
        return updateUser(user);
    }

    @Override
    public User changeUserRole(Long id, String role) {
        User user = getUserById(id);
        try {
            UserType newRole = UserType.valueOf(role.toUpperCase());
            user.setUserType(newRole);
            user.setUpdatedAt(Instant.now());
            return updateUser(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Override
    public UserExternalDto getUserDtoById(long userId) {
        User user = userRepo.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found by id"));
        return userExternalMapper.toDto(user);
    }

}
