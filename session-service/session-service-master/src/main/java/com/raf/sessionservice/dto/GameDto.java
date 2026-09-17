package com.raf.reservationservicevezbe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameDto {
    private Long id;            // ID je null prilikom kreiranja, ali se vraća u odgovoru
    private String name;
    private String description;
    private String genre;
}