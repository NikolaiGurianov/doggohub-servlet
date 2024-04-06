package ru.doggohub.mapper;

import lombok.experimental.UtilityClass;
import ru.doggohub.dto.user.UserRequestDto;
import ru.doggohub.dto.user.UserResponseDto;
import ru.doggohub.model.User;

@UtilityClass
public class UserMapper {
    public User fromDto(UserRequestDto dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .dogs(user.getDogIds())
                .build();
    }
}
