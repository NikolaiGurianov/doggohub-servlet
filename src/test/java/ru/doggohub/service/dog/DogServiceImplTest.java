package ru.doggohub.service.dog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.doggohub.dto.dog.DogRequestDto;
import ru.doggohub.dto.dog.DogResponseDto;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.DogMapper;
import ru.doggohub.model.Dog;
import ru.doggohub.model.User;
import ru.doggohub.model.enums.Breed;
import ru.doggohub.model.enums.Color;
import ru.doggohub.model.enums.Gender;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class DogServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private DogRepository dogRepository;

    private DogServiceImpl dogService;
    private final long dogId = 1L;

    private final long ownerId = 1L;
    private DogRequestDto dogRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dogService = new DogServiceImpl(userRepository, dogRepository);

        dogRequestDto = DogRequestDto.builder()
                .name("Vegas")
                .ownerId(ownerId)
                .color(Color.WHITE)
                .breed(Breed.LABRODOR)
                .gender(Gender.MALE)
                .birthDay(LocalDate.of(2022, 7, 1))
                .weight(30)
                .build();
    }

    @Test
    void addUserTest_Success() {
        User owner = new User();
        owner.setId(ownerId);

        when(userRepository.findById(ownerId)).thenReturn(owner);

        Dog dog = DogMapper.fromDto(dogRequestDto, ownerId);
        dog.setId(1L);
        dog.setOwnerId(ownerId);
        DogResponseDto expectedDog = DogMapper.toDto(dog, owner);

        when(dogRepository.save(any(Dog.class))).thenReturn(dog);
        when(userRepository.findOwnerByDogId(dogId)).thenReturn(owner);

        DogResponseDto dogResponseDto = dogService.add(dogRequestDto);

        assertNotNull(dogResponseDto);
        assertEquals(expectedDog, dogResponseDto);
    }

    @Test
    void addDogTest_WithoutOwner_ThrowNotFoundException() {
        when(userRepository.findById(ownerId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> dogService.add(dogRequestDto));
    }

    @Test
    void getDogsByOwnerIdTest_Successful() {
        User owner = new User();
        owner.setId(ownerId);
        owner.setDogIds(List.of(1L, 2L));

        when(userRepository.findById(ownerId)).thenReturn(owner);

        Dog dog = DogMapper.fromDto(dogRequestDto, ownerId);
        dog.setId(1L);

        Dog dog1 = DogMapper.fromDto(DogRequestDto.builder()
                .name("Legas")
                .ownerId(ownerId)
                .color(Color.BROWN)
                .breed(Breed.BULLTERIER)
                .gender(Gender.FEMALE)
                .birthDay(LocalDate.of(2022, 7, 1))
                .weight(30)
                .build(), ownerId);
        dog1.setId(2L);

        List<Dog> dogs = new ArrayList<>();
        dogs.add(dog);
        dogs.add(dog1);

        List<DogResponseDto> expectedDogResponseDto = dogs
                .stream()
                .sorted(Comparator.comparing(Dog::getRegistrationTime))
                .map((Dog dogx) -> DogMapper.toDto(dogx, owner))
                .collect(toList());

        when(dogRepository.findAllByOwnerId(ownerId)).thenReturn(dogs);

        List<DogResponseDto> dogResponseDtos = dogService.getByOwnerId(ownerId);

        assertNotNull(dogResponseDtos);
        assertEquals(expectedDogResponseDto, dogResponseDtos);
    }

    @Test
    void getByOwnerIdTest_WithoutOwner_ThrowNotFoundException() {
        when(userRepository.findById(ownerId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> dogService.getByOwnerId(dogId));
    }

    @Test
    void getDogByIdTest_Success() {
        User owner = new User();
        owner.setId(ownerId);

        Dog dog = DogMapper.fromDto(dogRequestDto, ownerId);
        dog.setId(dogId);

        DogResponseDto expectedDog = DogMapper.toDto(dog, owner);

        when(dogRepository.findById(dogId)).thenReturn(dog);
        when(userRepository.findOwnerByDogId(dogId)).thenReturn(owner);

        DogResponseDto dogResponseDto = dogService.getById(dogId);

        assertNotNull(dogResponseDto);
        assertEquals(expectedDog, dogResponseDto);
    }

    @Test
    void getDogByIdTest_ExpectExceptions_WithInvalidId() {
        long negativeDogId = -1L;

        when(dogRepository.findById(dogId)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> dogService.getById(dogId));

        when(dogRepository.findById(negativeDogId)).thenReturn(null);
        assertThrows(ValidationException.class, () -> dogService.getById(negativeDogId));
    }

    @Test
    void updateDogTest_Successful() {
        String newName = "NewName";

        User owner = new User();
        owner.setId(ownerId);

        Dog dog = DogMapper.fromDto(dogRequestDto, owner.getId());
        dog.setId(dogId);

        when(dogRepository.findById(dogId)).thenReturn(dog);
        when(userRepository.findOwnerByDogId(dogId)).thenReturn(owner);
        when(dogRepository.update(any(Dog.class))).thenReturn(dog);

        dogRequestDto.setName(newName);
        dogRequestDto.setWeight(10);
        DogResponseDto dogResponseDto = dogService.update(dogRequestDto, dogId);

        assertNotNull(dogResponseDto);
        assertEquals(newName, dogResponseDto.getName());
        assertEquals(10, dogResponseDto.getWeight());
    }

    @Test
    void updateDogTest_WithoutOwner_ThrowNotFoundException() {
        when(userRepository.findById(ownerId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> dogService.update(dogRequestDto, dogId));
    }
}