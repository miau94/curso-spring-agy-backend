package com.andres.course.agy.springboot.user_manager.app.services;

import com.andres.course.agy.springboot.user_manager.app.dto.UserDto;
import com.andres.course.agy.springboot.user_manager.app.mappers.UserMapper;
import com.andres.course.agy.springboot.user_manager.app.models.User;
import com.andres.course.agy.springboot.user_manager.app.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::entityToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::entityToDto)
                .orElse(null);
    }

    public UserDto createUser(UserDto userDto) {
        User user = userMapper.dtoToEntity(userDto);
        // Set a default valid password since DTO is password-less and password is required.
        user.setPassword("default_password");
        User savedUser = userRepository.save(user);
        return userMapper.entityToDto(savedUser);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(userDto.username());
            existingUser.setEmail(userDto.email());
            User savedUser = userRepository.save(existingUser);
            return userMapper.entityToDto(savedUser);
        }).orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
