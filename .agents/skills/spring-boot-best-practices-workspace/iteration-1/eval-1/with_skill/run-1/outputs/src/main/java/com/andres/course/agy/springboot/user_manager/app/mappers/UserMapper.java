package com.andres.course.agy.springboot.user_manager.app.mappers;

import com.andres.course.agy.springboot.user_manager.app.dto.UserDto;
import com.andres.course.agy.springboot.user_manager.app.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto entityToDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }

    public User dtoToEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.id());
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        return user;
    }
}
