package ru.doggohub.service.health;

import ru.doggohub.dto.health.HealthStoryRequestDto;
import ru.doggohub.dto.health.HealthStoryResponseDto;

import java.util.List;

public interface HealthStoryService {
    HealthStoryResponseDto add(HealthStoryRequestDto dto);

    void deleteById(long storyId);

    List<HealthStoryResponseDto> getByDogId(long dogId);

    HealthStoryResponseDto getById(long storyId);
}
