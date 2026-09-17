package com.raf.cinemauserservice.controller;

import com.raf.cinemauserservice.dto.*;
import com.raf.cinemauserservice.secutiry.CheckSecurity;
import com.raf.cinemauserservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- STANDARDNE METODE ---

    @GetMapping("/all")
    @CheckSecurity(roles = {"ROLE_ADMIN", "ROLE_USER"})
    public ResponseEntity<Page<UserAllDto>> getAllUsers(@RequestHeader("Authorization") String authorization,
                                                        Pageable pageable) {
        return new ResponseEntity<>(userService.findAll(pageable), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponseDto> register(@RequestBody @Valid UserCreateDto userCreateDto) {
        return new ResponseEntity<>(userService.register(userCreateDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid TokenRequestDto tokenRequestDto) {
        return new ResponseEntity<>(userService.login(tokenRequestDto), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @CheckSecurity(roles = {"ROLE_ADMIN", "ROLE_USER"})
    public ResponseEntity<TokenResponseDto> updateUser(@RequestHeader("Authorization") String authorization,
                                                       @PathVariable("id") Long id,
                                                       @RequestBody @Valid UserCreateDto userCreateDto) {
        return new ResponseEntity<>(userService.updateInfo(id, userCreateDto), HttpStatus.OK);
    }

    @PostMapping("/{id}/block")
    @CheckSecurity(roles = {"ROLE_ADMIN"})
    public ResponseEntity<Void> blockUser(@RequestHeader("Authorization") String authorization,
                                          @PathVariable("id") Long id) {
        userService.blockUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // --- INTERNI POZIVI (ZA SESSION SERVIS) ---

    @GetMapping("/{id}/eligibility")
    public ResponseEntity<Boolean> checkSessionEligibility(@PathVariable("id") Long id) {
        return new ResponseEntity<>(userService.canCreateSession(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/increment-registered")
    public ResponseEntity<Void> incrementRegistered(@PathVariable("id") Long id) {
        userService.incrementRegisteredSession(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/attendance")
    public ResponseEntity<Void> updateAttendance(@PathVariable("id") Long id,
                                                 @RequestParam("present") boolean present) {
        userService.updateSessionAttendance(id, present);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/organizer-stats")
    public ResponseEntity<Void> incrementOrganizerStats(@PathVariable("id") Long id) {
        userService.incrementOrganizedSessions(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}