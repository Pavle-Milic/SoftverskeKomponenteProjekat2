package com.raf.reservationservicevezbe.service;

import com.raf.reservationservicevezbe.domain.Game;
import com.raf.reservationservicevezbe.dto.GameDto;
import com.raf.reservationservicevezbe.mapper.GameMapper;
import com.raf.reservationservicevezbe.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;

    public GameService(GameRepository gameRepository, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
    }

    public GameDto createGame(GameDto gameDto) {
        Game game = gameMapper.dtoToEntity(gameDto);
        gameRepository.save(game);
        return gameMapper.entityToDto(game);
    }

    public GameDto updateGame(Long id, GameDto gameDto) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        game.setName(gameDto.getName());
        game.setDescription(gameDto.getDescription());
        game.setGenre(gameDto.getGenre());
        // ... ostala polja

        gameRepository.save(game);
        return gameMapper.entityToDto(game);
    }

    public List<GameDto> getAllGames() {
        return gameRepository.findAll().stream()
                .map(gameMapper::entityToDto)
                .collect(Collectors.toList());
    }
}