package com.example.rentalTool_BackEnd.user.service.mapper;

import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.spi.UserExternalDto;
import org.springframework.stereotype.Component;

@Component
public class UserExternalMapper {
    public UserExternalDto toDto(User user) {
        return new UserExternalDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber()
        );
    }
}
