package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionPlayer {
    public Long id;
    public Long userId;
    public String username;
    @Override
    public String toString() {
        return "User ID: " + userId;
    }
}
