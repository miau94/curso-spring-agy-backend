package com.example.usermanager.service;

import com.example.usermanager.dto.UserCreateRequest;
import com.example.usermanager.dto.UserDto;
import com.example.usermanager.mapper.UserMapper;
import com.example.usermanager.model.User;
import com.example.usermanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    public UserDto createUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public Optional<UserDto> updateUser(Long id, UserCreateRequest request) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(request.username());
            existingUser.setEmail(request.email());
            existingUser.setPassword(request.password());
            User savedUser = userRepository.save(existingUser);
            return userMapper.toDto(savedUser);
        });
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
