package ru.doggohub.dto.health;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HealthStoryResponseDto {
    private Long id;
    private Long dogId;
    private String text;
    private LocalDate visit;
}
