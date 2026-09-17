package com.raf.reservationservicevezbe.dto;

import com.raf.reservationservicevezbe.domain.SessionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateSessionDto {

    private Long gameId;
    private Long hostId;
    private String title;
    private String description;

    private Integer maxPlayers;

    private LocalDateTime time;

    private SessionType type;
}