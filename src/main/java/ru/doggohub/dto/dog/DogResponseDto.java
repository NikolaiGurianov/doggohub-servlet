package ru.doggohub.dto.dog;

import lombok.*;
import ru.doggohub.dto.user.UserResponseDto;
import ru.doggohub.model.enums.Breed;
import ru.doggohub.model.enums.Color;
import ru.doggohub.model.enums.Gender;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class DogResponseDto {
    private Long id;
    private String name;
    private LocalDate birthDay;
    private Breed breed;
    private Color color;
    private Gender gender;
    private Integer weight;
    private UserResponseDto owner;
}
