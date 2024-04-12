package ru.doggohub.service.health;

import lombok.RequiredArgsConstructor;
import ru.doggohub.dto.health.HealthStoryRequestDto;
import ru.doggohub.dto.health.HealthStoryResponseDto;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.HealthStoryMapper;
import ru.doggohub.model.Dog;
import ru.doggohub.model.HealthStory;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.HealthStoryRepository;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class HealthStoryServiceImpl implements HealthStoryService {

    private final HealthStoryRepository healthStoryRepository;
    private final DogRepository dogRepository;

    @Override
    public HealthStoryResponseDto add(HealthStoryRequestDto story) {
        if (story.getText() == null || story.getText().isEmpty())
            throw new ValidationException("Получен пустой текст истории болезни");

        Dog dog = validAndGetDog(story.getDogId());

        HealthStory healthStory = healthStoryRepository.save(HealthStoryMapper.fromDto(story, dog));

        return HealthStoryMapper.toDto(healthStory);
    }

    @Override
    public void deleteById(long storyId) {
        Optional.ofNullable(healthStoryRepository.findById(storyId))
                .orElseThrow(() -> new NotFoundException("История болезни с ID={} не найдена", storyId));

        healthStoryRepository.removeById(storyId);
    }

    @Override
    public List<HealthStoryResponseDto> getByDogId(long dogId) {
        validAndGetDog(dogId);

        List<HealthStory> healthStoryList = healthStoryRepository.findByDogId(dogId);

        return healthStoryList.stream()
                .map(HealthStoryMapper::toDto)
                .collect(toList());
    }

    @Override
    public HealthStoryResponseDto getById(long storyId) {
        if (storyId > 0) {
            HealthStory story = Optional.ofNullable(healthStoryRepository.findById(storyId))
                    .orElseThrow(() -> new NotFoundException("История болезни с ID={} не найдена", storyId));

            return HealthStoryMapper.toDto(story);
        }
        throw new ValidationException("ID не может быть отрицательным числом");
    }

    private Dog validAndGetDog(long id) {
        if (id <= 0) throw new ValidationException("ID не может быть отрицательным числом");
        return Optional.ofNullable(dogRepository.findById(id))
                .orElseThrow(() -> new NotFoundException("Собака с ID={} не найдена", id));

    }
}
