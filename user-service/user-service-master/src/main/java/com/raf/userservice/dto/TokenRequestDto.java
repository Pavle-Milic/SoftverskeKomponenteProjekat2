package com.raf.cinemauserservice.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;

@Getter
@Setter
public class TokenRequestDto {

    @Email
    private String email;
    private String password;

    public TokenRequestDto() {
    }

    public TokenRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}