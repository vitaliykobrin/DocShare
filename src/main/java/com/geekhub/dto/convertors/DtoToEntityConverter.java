package com.geekhub.dto.convertors;

import com.geekhub.dto.UserDto;
import com.geekhub.entities.User;

public class DtoToEntityConverter {
    
    public static User merge(UserDto userDto, User user) {
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setLogin(userDto.getLogin());
        user.setEmail(userDto.getEmail());
        user.setCountry(userDto.getCountry());
        user.setState(userDto.getState());
        user.setCity(userDto.getCity());
        return user;
    }
}