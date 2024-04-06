package ru.doggohub.mapper;

import lombok.experimental.UtilityClass;
import ru.doggohub.dto.health.HealthStoryRequestDto;
import ru.doggohub.dto.health.HealthStoryResponseDto;
import ru.doggohub.model.Dog;
import ru.doggohub.model.HealthStory;

import java.time.LocalDate;

@UtilityClass
public class HealthStoryMapper {

    public static HealthStory fromDto(HealthStoryRequestDto dto, Dog dog) {
        return HealthStory.builder()
                .text(dto.getText())
                .dogId(dog.getId())
                .visit(LocalDate.now())
                .build();
    }

    public static HealthStoryResponseDto toDto(HealthStory healthStory) {
        return HealthStoryResponseDto.builder()
                .id(healthStory.getId())
                .dogId(healthStory.getDogId())
                .text(healthStory.getText())
                .visit(healthStory.getVisit())
                .build();
    }
}
