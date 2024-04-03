package ru.doggohub.service.user;

import ru.doggohub.dto.user.UserRequestDto;
import ru.doggohub.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto addUser(UserRequestDto dto);

    UserResponseDto getById(long id);

    UserResponseDto updateUser(UserRequestDto dto, long userId);

    List<UserResponseDto> getAll();

    void deleteById(Long userId);
}
