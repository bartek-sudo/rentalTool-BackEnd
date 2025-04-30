package com.example.rentalTool_BackEnd.user.service.impl;

import com.example.rentalTool_BackEnd.user.exception.IllegalAccountAccessException;
import com.example.rentalTool_BackEnd.user.exception.UserAlreadyExistException;
import com.example.rentalTool_BackEnd.user.exception.UserNotFoundException;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import com.example.rentalTool_BackEnd.user.repo.UserRepo;
import com.example.rentalTool_BackEnd.user.service.UserService;
import com.example.rentalTool_BackEnd.user.web.requests.ChangePasswordRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserFromAuthentication(Authentication authentication) throws UserNotFoundException {
        return userRepo.findUserByEmail(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found by email"));
    }

    @Override
    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepo.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found by email"));
    }

    @Override
    public User getUserById(long id) throws UserNotFoundException {
        return userRepo.findUserById(id).orElseThrow(() -> new UserNotFoundException("User not found by id"));
    }

//    @Override
//    public List<User> getUsersByFirstName(String firstName) {
//        return userRepo.findUsersByFirstName(firstName);
//    }

    @Override
    @Transactional
    public User registerUser(UserRegisterRequest userRegisterRequest, UserType userType) throws UserAlreadyExistException {
        userRepo.findUserByEmail(userRegisterRequest.email()).ifPresent(u -> {
            throw new UserAlreadyExistException("User already exists");
        });
        return userRepo.createUser(new User(passwordEncoder.encode(userRegisterRequest.password()), userRegisterRequest.email(), userRegisterRequest.lastName(), userRegisterRequest.firstName(), userType));
    }

    @Override
    public User updateUser(User user) throws UserNotFoundException {
        userRepo.findUserByEmail(user.getEmail()).orElseThrow(() -> new UserNotFoundException("User not found by email"));
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
        updateUser(user);
    }
}
