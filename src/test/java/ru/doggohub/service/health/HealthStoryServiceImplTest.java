package ru.doggohub.service.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.doggohub.dto.health.HealthStoryRequestDto;
import ru.doggohub.dto.health.HealthStoryResponseDto;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.HealthStoryMapper;
import ru.doggohub.model.Dog;
import ru.doggohub.model.HealthStory;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.HealthStoryRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class HealthStoryServiceImplTest {
    @Mock
    private HealthStoryRepository healthStoryRepository;

    @Mock
    private DogRepository dogRepository;

    private HealthStoryServiceImpl healthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        healthService = new HealthStoryServiceImpl(healthStoryRepository, dogRepository);
    }

    @Test
    void addHealthStoryTest_Success() {
        HealthStoryRequestDto requestDto = HealthStoryRequestDto.builder().dogId(1L).text("Cough with blood").build();
        Dog dog = Dog.builder().id(1L).build();
        HealthStory healthStory = HealthStoryMapper.fromDto(requestDto, dog);
        HealthStoryResponseDto expectedStory = HealthStoryMapper.toDto(healthStory);

        when(dogRepository.findById(1L)).thenReturn(dog);
        when(healthStoryRepository.save(any(HealthStory.class))).thenReturn(healthStory); // Mocking saving of story and assigning an ID

        HealthStoryResponseDto actualStory = healthService.add(requestDto);

        assertNotNull(actualStory);
        assertEquals(expectedStory, actualStory);
    }

    @Test
    void addHealthStoryTest_InvalidText_And_InvalidDog_ThrowException() {
        HealthStoryRequestDto requestDto = HealthStoryRequestDto.builder().dogId(1L).text("").build();

        assertThrows(ValidationException.class, () -> healthService.add(requestDto));


        HealthStoryRequestDto negativeRequestDto = HealthStoryRequestDto.builder().dogId(-1L).text("Cough with blood").build();

        assertThrows(ValidationException.class, () -> healthService.add(negativeRequestDto));


        HealthStoryRequestDto requestDto1 = HealthStoryRequestDto.builder().dogId(1L).text("Cough with blood").build();

        when(dogRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> healthService.add(requestDto1));
    }

    @Test
    void getHealthStoriesByDogIdTest_Success() {
        Dog dog = Dog.builder().id(1L).build();

        List<HealthStory> dtos = new ArrayList<>();
        dtos.add(HealthStory.builder().dogId(1L).text("Cough with blood1").build());
        dtos.add(HealthStory.builder().dogId(1L).text("Cough with blood2").build());
        dtos.add(HealthStory.builder().dogId(1L).text("Cough with blood3").build());

        List<HealthStoryResponseDto> expectedStories = dtos.stream().map(HealthStoryMapper::toDto).toList();

        when(dogRepository.findById(1L)).thenReturn(dog);
        when(healthStoryRepository.findByDogId(1L)).thenReturn(dtos);

        List<HealthStoryResponseDto> actualResponseDtos = healthService.getByDogId(1L);

        assertNotNull(actualResponseDtos);
        assertEquals(expectedStories, actualResponseDtos);
    }

    @Test
    void getHealthStoriesByDogIdTest_ThrowNotFoundException() {
        when(dogRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> healthService.getByDogId(1L));
    }

    @Test
    void getHealthStoriesByIdTest_Successful() {
        HealthStory healthStory = new HealthStory();
        healthStory.setId(1L);

        HealthStoryResponseDto expectedDto = HealthStoryMapper.toDto(healthStory);

        when(healthStoryRepository.findById(1L)).thenReturn(healthStory);

        HealthStoryResponseDto responseDto = healthService.getById(1L);

        assertNotNull(responseDto);
        assertEquals(expectedDto, responseDto);
    }

    @Test
    void getHealthStoriesByIdTest_ThrowNotFoundException_And_ValidationException() {
        when(healthStoryRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> healthService.getById(1L));
        assertThrows(ValidationException.class, () -> healthService.getById(-1L));

    }

    @Test
    void deleteHealthStoryByIdTest_Success() {
        long healthId = 1L;
        HealthStoryRequestDto requestDto = HealthStoryRequestDto.builder().dogId(healthId).text("Cough with blood").build();
        Dog dog = Dog.builder().id(1L).build();
        HealthStory healthStory = HealthStoryMapper.fromDto(requestDto, dog);

        when(healthStoryRepository.save(any(HealthStory.class))).thenReturn(healthStory);
        when(healthStoryRepository.findById(healthId)).thenReturn(healthStory);

        healthService.deleteById(healthId);

        verify(healthStoryRepository, times(1)).removeById(healthId);
    }

    @Test
    void deleteHealthStoryByIdTest_ThrowNotFoundException() {
        when(healthStoryRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> healthService.deleteById(1L));
    }
}