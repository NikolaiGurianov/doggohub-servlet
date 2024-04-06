package ru.doggohub.model;

import lombok.*;
import ru.doggohub.model.enums.Breed;
import ru.doggohub.model.enums.Color;
import ru.doggohub.model.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Dog {
    private Long id;
    private String name;
    private LocalDate birthDay;
    private Breed breed;
    private Color color;
    private Gender gender;
    private Integer weight;
    private LocalDateTime registrationTime;
    private Long ownerId;

}
