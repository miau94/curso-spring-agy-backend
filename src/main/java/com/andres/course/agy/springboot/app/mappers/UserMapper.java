package com.andres.course.agy.springboot.app.mappers;

import com.andres.course.agy.springboot.app.dto.UserDto;
import com.andres.course.agy.springboot.app.dto.UserRequestDto;
import com.andres.course.agy.springboot.app.models.Role;
import com.andres.course.agy.springboot.app.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto entityToDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole());
    }

    public User dtoToEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setRole(dto.role() != null ? dto.role() : Role.USER);
        return user;
    }
}
