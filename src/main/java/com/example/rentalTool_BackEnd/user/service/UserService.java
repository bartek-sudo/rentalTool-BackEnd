package com.example.rentalTool_BackEnd.user.service;

import com.example.rentalTool_BackEnd.user.exception.UserAlreadyExistException;
import com.example.rentalTool_BackEnd.user.exception.UserNotFoundException;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import com.example.rentalTool_BackEnd.user.web.requests.ChangePasswordRequest;
import com.example.rentalTool_BackEnd.user.web.requests.UserRegisterRequest;
import org.springframework.security.core.Authentication;

public interface UserService {

    User getUserFromAuthentication(Authentication authentication) throws UserNotFoundException;

    User getUserByEmail(String email) throws UserNotFoundException;

    User getUserById(long id) throws UserNotFoundException;

//    List<User> getUsersByFirstName(String firstName);

    User registerUser(UserRegisterRequest userRegisterRequest, UserType userType) throws UserAlreadyExistException;

    User updateUser(User user) throws UserNotFoundException;

    void changeUserPassword(long userId, ChangePasswordRequest changePasswordRequest) throws UserNotFoundException;
}
