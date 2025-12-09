package com.example.rentalTool_BackEnd.user.web.mapper;

import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.web.model.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserDto toDto(User user){

        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isVerified(),
                user.isBlocked(),
                TimeUtil.getTimeInStandardFormat(user.getCreatedAt()),
                TimeUtil.getTimeInStandardFormat(user.getUpdatedAt()),
                TimeUtil.getTimeInStandardFormat(user.getBlockedAt()),
                TimeUtil.getTimeInStandardFormat(user.getVerifiedAt()),
                user.getUserType().name()
        );
    }
}
