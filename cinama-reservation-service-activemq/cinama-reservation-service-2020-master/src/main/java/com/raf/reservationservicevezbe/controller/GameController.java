package com.raf.reservationservicevezbe.controller;

import com.raf.reservationservicevezbe.dto.GameDto;
import com.raf.reservationservicevezbe.secutiry.CheckSecurity;
import com.raf.reservationservicevezbe.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // Samo ADMIN može da doda novu igru
    @PostMapping("/create")
    @CheckSecurity(roles = {"ROLE_ADMIN"})
    public ResponseEntity<GameDto> createGame(@RequestHeader("Authorization") String authorization,
                                              @RequestBody GameDto gameDto) {
        return new ResponseEntity<>(gameService.createGame(gameDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @CheckSecurity(roles = {"ROLE_ADMIN"})
    public ResponseEntity<GameDto> updateGame(@RequestHeader("Authorization") String authorization,
                                              @PathVariable Long id,
                                              @RequestBody GameDto gameDto) {
        return new ResponseEntity<>(gameService.updateGame(id, gameDto), HttpStatus.OK);
    }

    @GetMapping
    @RequestMapping("/all")
    public ResponseEntity<List<GameDto>> getAllGames() {
        return new ResponseEntity<>(gameService.getAllGames(), HttpStatus.OK);
    }
}