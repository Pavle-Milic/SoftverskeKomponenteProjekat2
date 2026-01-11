package com.raf.cinemauserservice.service.impl;

import com.raf.cinemauserservice.domain.Rank;
import com.raf.cinemauserservice.domain.User;
import com.raf.cinemauserservice.dto.*;
import com.raf.cinemauserservice.exception.BlockedOrNotActivatedException;
import com.raf.cinemauserservice.exception.NotFoundException;
import com.raf.cinemauserservice.mapper.UserMapper;
import com.raf.cinemauserservice.repository.RoleRepository;
import com.raf.cinemauserservice.repository.UserRepository;
import com.raf.cinemauserservice.secutiry.service.TokenService;
import com.raf.cinemauserservice.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserMapper userMapper,
                           TokenService tokenService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserAllDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::userToUserAllDto);
    }

    @Override
    public TokenResponseDto register(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        // Moraš dodati metodu existsByUsername u UserRepository interfejs!
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = userMapper.userCreateDtoToUser(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setRole(roleRepository.findRoleByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER not found")));

        user.setActivated(false);
        user.setBlocked(false);

        userRepository.save(user);

        // TODO: Notification service message
        return new TokenResponseDto(null);
    }

    @Override
    public TokenResponseDto login(TokenRequestDto dto) {
        User user = userRepository.findUserByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException("User with email " + dto.getEmail() + " not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (user.isBlocked()) {
            throw new BlockedOrNotActivatedException("User is blocked.");
        }

        // if (!user.isActivated()) throw ...

        Claims claims = Jwts.claims();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("rank", user.getRank());
        claims.put("role", user.getRole().getName());

        return new TokenResponseDto(tokenService.generate(claims));
    }

    @Override
    public TokenResponseDto updateInfo(Long id, UserCreateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        if (!user.getUsername().equals(dto.getUsername()) && !userRepository.existsByUsername(dto.getUsername())) {
            user.setUsername(dto.getUsername());
        }
        user.setBirthDate(dto.getBirthDate());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return null;
    }

    @Override
    public void blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    // --- SESSION SERVICE LOGIKA ---

    @Override
    public boolean canCreateSession(Long userId) {
        return userRepository.findById(userId)
                .map(User::canCreateSession)
                .orElse(false);
    }

    @Override
    public void incrementRegisteredSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.incrementRegistered();
    }

    @Override
    public void updateSessionAttendance(Long userId, boolean present) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Pazi: ovo mora da se poklapa sa imenom metode u User.java (concludeSession ili concludeAttendance)
        user.concludeSession(present);
    }

    @Override
    public void incrementOrganizedSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.incrementOrganized();
    }
}