package com.raf.reservationservicevezbe.mapper;

import com.raf.reservationservicevezbe.domain.Game;
import com.raf.reservationservicevezbe.dto.GameDto;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    // Konverzija iz Entiteta u DTO (za slanje na frontend)
    public GameDto entityToDto(Game game) {
        if (game == null) {
            return null;
        }
        GameDto dto = new GameDto();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setDescription(game.getDescription());
        dto.setGenre(game.getGenre());
        return dto;
    }

    // Konverzija iz DTO u Entitet (za upis u bazu)
    public Game dtoToEntity(GameDto dto) {
        if (dto == null) {
            return null;
        }
        Game game = new Game();
        // ID ne setujemo ovde jer ga baza generiše, osim ako je update u pitanju
        // Ali za update obično vadimo objekat iz baze pa mu menjamo polja
        game.setId(dto.getId());
        game.setName(dto.getName());
        game.setDescription(dto.getDescription());
        game.setGenre(dto.getGenre());
        return game;
    }
}