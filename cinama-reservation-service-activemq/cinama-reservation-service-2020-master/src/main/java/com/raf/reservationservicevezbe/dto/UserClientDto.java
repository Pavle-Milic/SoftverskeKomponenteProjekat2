package com.raf.reservationservicevezbe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserClientDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean blocked;
    private Double attendancePercentage;
}