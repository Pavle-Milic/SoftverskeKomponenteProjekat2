package com.raf.cinemauserservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserAllDto {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    private boolean active;
    private boolean blocked;

    private Double attendancePercentage;
    private String rank;
}