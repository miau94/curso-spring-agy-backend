package com.andres.course.agy.springboot.app.services;

import com.andres.course.agy.springboot.app.dto.UserDto;
import com.andres.course.agy.springboot.app.dto.UserRequestDto;

import java.util.List;

public interface UserService {

    List<UserDto> findAll();

    UserDto findById(Long id);

    UserDto findByEmail(String email);

    UserDto create(UserRequestDto userRequestDto);

    UserDto update(Long id, UserRequestDto userRequestDto);

    void delete(Long id);
}
