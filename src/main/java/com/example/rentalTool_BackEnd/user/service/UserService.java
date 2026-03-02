package com.example.rentalTool_BackEnd.user.service;

import com.example.rentalTool_BackEnd.user.exception.UserAlreadyExistException;
import com.example.rentalTool_BackEnd.user.exception.UserNotFoundException;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import com.example.rentalTool_BackEnd.user.web.requests.ChangePasswordRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserRegisterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    User getUserByEmail(String email) throws UserNotFoundException;

    Optional<User> findOptionalByEmail(String email);

    User getUserById(long id) throws UserNotFoundException;

    User registerUser(UserRegisterRequest userRegisterRequest, UserType userType) throws UserAlreadyExistException;

    User updateUser(User user) throws UserNotFoundException;

    void changeUserPassword(long userId, ChangePasswordRequest changePasswordRequest) throws UserNotFoundException;

    Page<User> getAllUsers(Pageable pageable, String search);

    User blockUser(Long id);
    User unblockUser(Long id);
    User changeUserRole(Long id, String role);

}
