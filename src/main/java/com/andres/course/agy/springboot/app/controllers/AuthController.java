package com.andres.course.agy.springboot.app.controllers;

import com.andres.course.agy.springboot.app.dto.AuthRequestDto;
import com.andres.course.agy.springboot.app.dto.UserDto;
import com.andres.course.agy.springboot.app.dto.UserRequestDto;
import com.andres.course.agy.springboot.app.models.Role;
import com.andres.course.agy.springboot.app.security.JwtService;
import com.andres.course.agy.springboot.app.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            UserDetailsService userDetailsService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody AuthRequestDto authRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDto.email(),
                        authRequestDto.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(jwtToken);

        UserDto userDto = userService.findByEmail(authRequestDto.email());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody AuthRequestDto authRequestDto) {
        UserRequestDto newUserRequest = new UserRequestDto(
                authRequestDto.email(),
                authRequestDto.password(),
                Role.USER);
        UserDto createdUser = userService.create(newUserRequest);

        UserDetails userDetails = userDetailsService.loadUserByUsername(createdUser.email());
        String jwtToken = jwtService.generateToken(userDetails);
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(jwtToken);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(createdUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie cleanCookie = jwtService.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
}
