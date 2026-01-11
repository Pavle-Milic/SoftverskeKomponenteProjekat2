package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    public Long id;
    public String username;
    public String email;
    public String rank;
    public Boolean blocked;
}
