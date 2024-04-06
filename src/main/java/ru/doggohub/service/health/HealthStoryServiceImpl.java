package ru.doggohub.service.health;

import lombok.RequiredArgsConstructor;
import ru.doggohub.mapper.HealthStoryMapper;
import ru.doggohub.dto.health.HealthStoryRequestDto;
import ru.doggohub.dto.health.HealthStoryResponseDto;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.model.Dog;
import ru.doggohub.model.HealthStory;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.HealthStoryRepository;
import ru.doggohub.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class HealthStoryServiceImpl implements HealthStoryService {

    private final HealthStoryRepository healthStoryRepository;
    private final DogRepository dogRepository;
    private final Connection connection;

    public HealthStoryServiceImpl() {
        try {
            connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }

        this.healthStoryRepository = new HealthStoryRepository(connection);
        this.dogRepository = new DogRepository(connection);
    }

    @Override
    public HealthStoryResponseDto add(HealthStoryRequestDto story) {
        if (story.getText().isEmpty()) throw new ValidationException("Получен пустой текст истории болезни");
        validId(story.getDogId());

        Dog dog = Optional.ofNullable(dogRepository.findById(story.getDogId()))
                .orElseThrow(() -> new NotFoundException("Собака с ID={} не найдена", story.getDogId()));

        HealthStory healthStory = healthStoryRepository.save(HealthStoryMapper.fromDto(story, dog));

        return HealthStoryMapper.toDto(healthStory);
    }

    @Override
    public void deleteById(long storyId) {
        HealthStory story = Optional.ofNullable(healthStoryRepository.findById(storyId))
                .orElseThrow(() -> new NotFoundException("История болезни с ID={} не найдена", storyId));

        healthStoryRepository.removeById(story.getId());
    }

    @Override
    public List<HealthStoryResponseDto> getByDogId(long dogId) {
        validId(dogId);
        Optional.ofNullable(dogRepository.findById(dogId))
                .orElseThrow(() -> new NotFoundException("Собака с ID={} не найдена", dogId));

        List<HealthStory> healthStoryList = healthStoryRepository.findByDogId(dogId);

        return healthStoryList.stream()
                .map(HealthStoryMapper::toDto)
                .collect(toList());
    }

    @Override
    public HealthStoryResponseDto getById(long storyId) {
        validId(storyId);
        HealthStory story = Optional.ofNullable(healthStoryRepository.findById(storyId))
                .orElseThrow(() -> new NotFoundException("История болезни с ID={} не найдена", storyId));

        return HealthStoryMapper.toDto(story);
    }

    private void validId(long id) {
        if (id <= 0) throw new ValidationException("ID не может быть отрицательным числом");
    }
}
