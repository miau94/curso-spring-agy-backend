package com.andres.course.agy.springboot.app.services;

import com.andres.course.agy.springboot.app.dto.UserDto;
import com.andres.course.agy.springboot.app.dto.UserRequestDto;
import com.andres.course.agy.springboot.app.exceptions.EmailAlreadyExistsException;
import com.andres.course.agy.springboot.app.exceptions.UserNotFoundException;
import com.andres.course.agy.springboot.app.mappers.UserMapper;
import com.andres.course.agy.springboot.app.models.Role;
import com.andres.course.agy.springboot.app.models.User;
import com.andres.course.agy.springboot.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testFindAll() {
        User user1 = new User("admin@example.com", "encodedPass", Role.ADMIN);
        User user2 = new User("user@example.com", "encodedPass", Role.USER);
        List<User> users = Arrays.asList(user1, user2);

        UserDto dto1 = new UserDto(1L, "admin@example.com", Role.ADMIN);
        UserDto dto2 = new UserDto(2L, "user@example.com", Role.USER);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.entityToDto(user1)).thenReturn(dto1);
        when(userMapper.entityToDto(user2)).thenReturn(dto2);

        List<UserDto> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals(Role.ADMIN, result.get(0).role());
        assertEquals(Role.USER, result.get(1).role());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testCreateSuccess() {
        UserRequestDto requestDto = new UserRequestDto("new@example.com", "secret123", Role.USER);
        User entity = new User("new@example.com", "secret123", Role.USER);
        User savedEntity = new User("new@example.com", "hashedSecret", Role.USER);
        savedEntity.setId(1L);
        UserDto outputDto = new UserDto(1L, "new@example.com", Role.USER);

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userMapper.dtoToEntity(requestDto)).thenReturn(entity);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedSecret");
        when(userRepository.save(entity)).thenReturn(savedEntity);
        when(userMapper.entityToDto(savedEntity)).thenReturn(outputDto);

        UserDto result = userService.create(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("new@example.com", result.email());
        assertEquals(Role.USER, result.role());
        assertEquals("hashedSecret", entity.getPassword());
        verify(passwordEncoder, times(1)).encode("secret123");
        verify(userRepository, times(1)).save(entity);
    }

    @Test
    void testCreateEmailAlreadyExists() {
        UserRequestDto requestDto = new UserRequestDto("existing@example.com", "secret123", Role.USER);
        User existing = new User("existing@example.com", "hashed", Role.USER);

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(requestDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(99L));
    }
}
