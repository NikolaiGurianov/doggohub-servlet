package ru.doggohub.dto.health;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class HealthStoryRequestDto {
    private Long dogId;
    private String text;
}
