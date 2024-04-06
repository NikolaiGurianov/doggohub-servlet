package ru.doggohub.service.dog;

import ru.doggohub.dto.dog.DogRequestDto;
import ru.doggohub.dto.dog.DogResponseDto;

import java.util.List;

public interface DogService {
    DogResponseDto getById(long id);

    List<DogResponseDto> getByOwnerId(long id);

    DogResponseDto add(DogRequestDto dogRequestDto);

    DogResponseDto update(DogRequestDto dogRequestDto, long dogId);

    void deleteById(long id);
}
