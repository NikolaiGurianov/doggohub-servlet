package ru.doggohub.dto.dog;

import lombok.*;
import ru.doggohub.model.enums.Breed;
import ru.doggohub.model.enums.Color;
import ru.doggohub.model.enums.Gender;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DogRequestDto {
    private String name;
    private LocalDate birthDay;
    private Breed breed;
    private Color color;
    private Gender gender;
    private Integer weight;
    private Long ownerId;
}
