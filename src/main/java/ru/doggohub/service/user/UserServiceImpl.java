package ru.doggohub.service.user;

import lombok.RequiredArgsConstructor;
import ru.doggohub.mapper.UserMapper;
import ru.doggohub.dto.user.UserRequestDto;
import ru.doggohub.dto.user.UserResponseDto;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.model.Dog;
import ru.doggohub.model.User;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;
import ru.doggohub.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final Connection connection;


    public UserServiceImpl() {

        try {
            connection = DatabaseUtil.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
        this.userRepository = new UserRepository(connection);
        this.dogRepository = new DogRepository(connection);
    }

    @Override
    public UserResponseDto addUser(UserRequestDto dto) {
        validEmail(dto.getEmail());
        User user = UserMapper.fromDto(dto);
        user = userRepository.save(user);
        user.setDogIds(getListDogIds(user.getId()));

        return UserMapper.toDto(user);
    }

    @Override
    public UserResponseDto getById(long userId) {
        User user = validAndGetUser(userId);
        user.setDogIds(getListDogIds(userId));

        return UserMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUser(UserRequestDto dto, long userId) {
        User user = validAndGetUser(userId);

        if (dto != null) {
            if (!dto.getName().isEmpty()) {
                user.setName(dto.getName());
            }
            if (!dto.getEmail().isEmpty()) {
                validEmail(dto.getEmail());
                user.setEmail(dto.getEmail());
            }
            user = userRepository.update(user);
        }
        user.setDogIds(getListDogIds(userId));

        return UserMapper.toDto(user);
    }

    @Override
    public void deleteById(Long userId) {
        validAndGetUser(userId);
        userRepository.deleteById(userId);
    }

    public List<UserResponseDto> getAll() {
        List<User> list = userRepository.findAll();
        List<UserResponseDto> list1 = new ArrayList<>();
        list.forEach(user -> {
            user.setDogIds(getListDogIds(user.getId()));
            list1.add(UserMapper.toDto(user));
        });
        return list1;
    }

    private void validEmail(String email) {
        for (User user1 : userRepository.findAll()) {
            if (email.equals(user1.getEmail())) {
                throw new ValidationException("Эл почта пользователя должна быть уникальной");
            }
        }
    }

    private List<Long> getListDogIds(long id) {
        List<Dog> dogs = dogRepository.findAllByOwnerId(id);
        if (dogs != null) {
            return dogs.stream().map(Dog::getId).sorted(Comparator.naturalOrder()).toList();
        } else {
            return new ArrayList<>();
        }
    }

    private User validAndGetUser(long userId) {
        if (userId == 0) throw new ValidationException("ID не может быть отрицательным числом");

        return Optional.ofNullable(userRepository.findById(userId)).orElseThrow(()
                -> new NotFoundException("Пользователь с ID={} не найден", userId));
    }
}
