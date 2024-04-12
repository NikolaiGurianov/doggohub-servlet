package ru.doggohub.service.dog;

import lombok.RequiredArgsConstructor;
import ru.doggohub.dto.dog.DogRequestDto;
import ru.doggohub.dto.dog.DogResponseDto;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.DogMapper;
import ru.doggohub.model.Dog;
import ru.doggohub.model.User;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


@RequiredArgsConstructor
public class DogServiceImpl implements DogService {

    private final UserRepository userRepository;
    private final DogRepository dogRepository;

    @Override
    public DogResponseDto add(DogRequestDto dogRequestDto) {
        validNewDog(dogRequestDto);

        long userId = dogRequestDto.getOwnerId();
        validAndGetUser(userId);

        Dog dog = dogRepository.save(DogMapper.fromDto(dogRequestDto, userId));
        dogRepository.saveDogOwner(dog.getId(), userId);
        User user = getUserByDogId(dog.getId());

        return DogMapper.toDto(dog, user);
    }

    @Override
    public DogResponseDto getById(long dogId) {
        Dog dog = validAndGetDog(dogId);

        return DogMapper.toDto(dog, getUserByDogId(dogId));
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

        return DogMapper.toDto(dogRepository.update(dog), getUserByDogId(dogId));
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

        User user = Optional.ofNullable(userRepository.findById(userId))
                .orElseThrow(() -> new NotFoundException("Пользователь с ID={} не найден", userId));

        List<Long> dogIdList = dogRepository.findAllByOwnerId(userId)
                .stream()
                .map(Dog::getId)
                .toList();

        user.setDogIds(dogIdList);
        return user;
    }

    private User getUserByDogId(long dogId) {
        User user = Optional.ofNullable(userRepository.findOwnerByDogId(dogId)).orElseThrow(()
                -> new NotFoundException("Владелец собаки с ID={} не найден", dogId));
        List<Dog> dogList = dogRepository.findAllByOwnerId(user.getId());
        List<Long> dogIdList = dogList.stream().map(Dog::getId).toList();
        user.setDogIds(dogIdList);
        return user;
    }

    private Dog validAndGetDog(long dogId) {
        if (dogId < 0) {
            throw new ValidationException("ID собаки не может быть отрицательным");
        }
        return Optional.ofNullable(dogRepository.findById(dogId))
                .orElseThrow(() -> new NotFoundException(String.format("Собака с ID=%d не найдена", dogId)));
    }

    private void validNewDog(DogRequestDto dto) {
        if (dto == null || dto.getName() == null || dto.getBirthDay() == null || dto.getBreed() == null ||
                dto.getColor() == null || dto.getGender() == null || dto.getWeight() == null) {
            throw new ValidationException("Все поля для записи должны быть заполнены");
        }
    }
}