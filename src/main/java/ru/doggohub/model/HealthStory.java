package ru.doggohub.model;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class HealthStory {
    private Long id;
    private Long dogId;
    private String text;
    private LocalDate visit;

}
