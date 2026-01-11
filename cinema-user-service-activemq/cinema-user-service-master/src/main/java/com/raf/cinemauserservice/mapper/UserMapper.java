package com.raf.cinemauserservice.mapper;

import com.raf.cinemauserservice.domain.User;
import com.raf.cinemauserservice.dto.UserAllDto;
import com.raf.cinemauserservice.dto.UserCreateDto;
import com.raf.cinemauserservice.dto.UserDto;
import com.raf.cinemauserservice.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private RoleRepository roleRepository;

    public UserMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public UserDto userToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setBirthDate(user.getBirthDate());
        dto.setRank(user.getRank().toString());
        dto.setAttendancePercentage(user.getAttendancePercentage());
        return dto;
    }

    public UserAllDto userToUserAllDto(User user) {
        UserAllDto dto = new UserAllDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setBirthDate(user.getBirthDate());
        dto.setRank(user.getRank().toString());
        dto.setAttendancePercentage(user.getAttendancePercentage());
        dto.setActive(user.isActivated());
        dto.setBlocked(user.isBlocked());
        return dto;
    }

    public User userCreateDtoToUser(UserCreateDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setBirthDate(dto.getBirthDate());
        user.setRole(roleRepository.findRoleByName("ROLE_USER").orElseThrow());
        return user;
    }
}