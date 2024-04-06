package ru.doggohub.mapper;

import lombok.experimental.UtilityClass;
import ru.doggohub.dto.dog.DogRequestDto;
import ru.doggohub.dto.dog.DogResponseDto;
import ru.doggohub.model.Dog;
import ru.doggohub.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class DogMapper {

    public Dog fromDto(DogRequestDto dto, long ownerId) {
        return Dog.builder()
                .name(dto.getName())
                .birthDay(dto.getBirthDay())
                .breed(dto.getBreed())
                .color(dto.getColor())
                .gender(dto.getGender())
                .weight(dto.getWeight())
                .ownerId(ownerId)
                .registrationTime(LocalDateTime.now())
                .build();
    }


    public DogResponseDto toDto(Dog dog, User user) {
        return DogResponseDto.builder()
                .id(dog.getId())
                .name(dog.getName())
                .birthDay(dog.getBirthDay())
                .breed(dog.getBreed())
                .color(dog.getColor())
                .gender(dog.getGender())
                .weight(dog.getWeight())
                .owner(UserMapper.toDto(user))
                .build();
    }
}
