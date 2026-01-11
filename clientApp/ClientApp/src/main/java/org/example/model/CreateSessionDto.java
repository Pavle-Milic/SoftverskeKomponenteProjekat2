package org.example.model;

import java.time.LocalDateTime;

public class CreateSessionDto {
    public Long gameId;
    public String title;
    public String description;
    public Integer maxPlayers;
    public LocalDateTime time;
    public String type; // "OPEN" ili "CLOSED"
}