package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameDto {
    public Long id;
    public String name;
    public String description;
    public String genre;

    @Override
    public String toString() {
        return name; // Ovo se prikazuje u ComboBox-u
    }
}