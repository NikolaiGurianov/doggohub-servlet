package ru.doggohub.service.dog;

import lombok.RequiredArgsConstructor;
import ru.doggohub.mapper.DogMapper;
import ru.doggohub.dto.dog.DogRequestDto;
import ru.doggohub.dto.dog.DogResponseDto;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.model.Dog;
import ru.doggohub.model.User;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;
import ru.doggohub.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


@RequiredArgsConstructor
public class DogServiceImpl implements DogService {

    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final Connection connection;


    public DogServiceImpl() {
        try {
            connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
        this.userRepository = new UserRepository(connection);
        this.dogRepository = new DogRepository(connection);
    }

    @Override
    public DogResponseDto add(DogRequestDto dogRequestDto) {
        long userId = dogRequestDto.getOwnerId();

        User user = validAndGetUser(userId);

        Dog dog = dogRepository.save(DogMapper.fromDto(dogRequestDto, userId));
        return DogMapper.toDto(dog, user);
    }

    @Override
    public DogResponseDto getById(long dogId) {
        Dog dog = validAndGetDog(dogId);

        User user = Optional.ofNullable(userRepository.findOwnerByDogId(dogId)).orElseThrow(()
                -> new NotFoundException("Владелец собаки с ID={} не найден", dogId));

        return DogMapper.toDto(dog, user);
    }

    @Override
    public List<DogResponseDto> getByOwnerId(long ownerId) {
        User user = validAndGetUser(ownerId);

        List<Dog> dogList = dogRepository.findAllByOwnerId(ownerId);

        return dogList
                .stream()
                .sorted(Comparator.comparing(Dog::getRegistrationTime))
                .map((Dog dog) -> DogMapper.toDto(dog, user))
                .collect(toList());
    }

    @Override
    public DogResponseDto update(DogRequestDto dto, long dogId) {
        Dog dog = validAndGetDog(dogId);

        if (dto.getName() != null) {
            dog.setName(dto.getName());
        }
        if (dto.getWeight() != null) {
            dog.setWeight(dto.getWeight());
        }
        User user = Optional.ofNullable(userRepository.findOwnerByDogId(dogId)).orElseThrow(()
                -> new NotFoundException("Владелец собаки с ID={} не найден", dogId));

        return DogMapper.toDto(dogRepository.update(dog), user);
    }

    @Override
    public void deleteById(long dogId) {
        validAndGetDog(dogId);
        dogRepository.removeById(dogId);
    }

    private User validAndGetUser(long userId) {
        if (userId <= 0) {
            throw new ValidationException("ID пользователя не может быть отрицательным");
        }
        return Optional.ofNullable(userRepository.findById(userId)).orElseThrow(()
                -> new NotFoundException("Пользователь с ID={} не найден", userId));
    }

    private Dog validAndGetDog(long dogId) {
        if (dogId <= 0) {
            throw new ValidationException("ID пользователя не может быть отрицательным");
        }
        return Optional.ofNullable(dogRepository.findById(dogId))
                .orElseThrow(() -> new NotFoundException("Собака с ID={} не найдена", dogId));
    }
}