package com.raf.cinemauserservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.raf.cinemauserservice.dto.TokenRequestDto;
import com.raf.cinemauserservice.dto.TokenResponseDto;
import com.raf.cinemauserservice.dto.UserAllDto;
import com.raf.cinemauserservice.dto.UserCreateDto;

public interface UserService {

    Page<UserAllDto> findAll(Pageable pageable);

    TokenResponseDto login(TokenRequestDto tokenRequestDto);

    TokenResponseDto register(UserCreateDto userCreateDto);

    TokenResponseDto updateInfo(Long id, UserCreateDto userCreateDto);

    void blockUser(Long id);

    boolean canCreateSession(Long userId);

    void incrementOrganizedSessions(Long id);

    void incrementRegisteredSession(Long userId);

    void updateSessionAttendance(Long userId, boolean present);
}