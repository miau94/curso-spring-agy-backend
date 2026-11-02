package com.andres.course.agy.springboot.app.services;

import com.andres.course.agy.springboot.app.dto.UserDto;
import com.andres.course.agy.springboot.app.dto.UserRequestDto;
import com.andres.course.agy.springboot.app.exceptions.EmailAlreadyExistsException;
import com.andres.course.agy.springboot.app.exceptions.UserNotFoundException;
import com.andres.course.agy.springboot.app.mappers.UserMapper;
import com.andres.course.agy.springboot.app.models.Role;
import com.andres.course.agy.springboot.app.models.User;
import com.andres.course.agy.springboot.app.repositories.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.entityToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.entityToDto(user);
    }

    // User de spring boot Security
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = "ROLE_" + user.getRole().name();
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(roleName)));
    }

    @Override
    @Transactional
    public UserDto create(UserRequestDto userRequestDto) {
        if (userRepository.findByEmail(userRequestDto.email()).isPresent()) {
            throw new EmailAlreadyExistsException(userRequestDto.email());
        }
        User user = userMapper.dtoToEntity(userRequestDto);
        user.setId(null);
        user.setPassword(passwordEncoder.encode(userRequestDto.password()));
        User savedUser = userRepository.save(user);
        return userMapper.entityToDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existingUser.getEmail().equalsIgnoreCase(userRequestDto.email()) &&
                userRepository.findByEmail(userRequestDto.email()).isPresent()) {
            throw new EmailAlreadyExistsException(userRequestDto.email());
        }

        existingUser.setEmail(userRequestDto.email());
        existingUser.setPassword(passwordEncoder.encode(userRequestDto.password()));
        if (userRequestDto.role() != null) {
            existingUser.setRole(userRequestDto.role());
        } else if (existingUser.getRole() == null) {
            existingUser.setRole(Role.USER);
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.entityToDto(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
