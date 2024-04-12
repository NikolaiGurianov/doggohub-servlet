package ru.doggohub.service.user;

import lombok.RequiredArgsConstructor;
import ru.doggohub.dto.user.UserRequestDto;
import ru.doggohub.dto.user.UserResponseDto;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.UserMapper;
import ru.doggohub.model.Dog;
import ru.doggohub.model.User;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DogRepository dogRepository;

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
            if (dto.getName() != null) {
                user.setName(dto.getName());
            }
            if (dto.getEmail() != null) {
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

    @Override
    public List<UserResponseDto> getAll() {
        List<User> list = userRepository.findAll();
        List<UserResponseDto> list1 = new ArrayList<>();
        for (User user : list) {
            user.setDogIds(getListDogIds(user.getId()));
            list1.add(UserMapper.toDto(user));
        }
        return list1;
    }

    protected void validEmail(String email) {
        if (email == null || email.isEmpty())
            throw new ValidationException("Эл почта пользователя должна быть заполнена");
        for (User user1 : userRepository.findAll()) {
            if (email.equals(user1.getEmail())) {
                throw new ValidationException("Эл почта пользователя должна быть уникальной");
            }
        }
    }

    protected List<Long> getListDogIds(long id) {
        List<Dog> dogs = dogRepository.findAllByOwnerId(id);
        if (!dogs.isEmpty()) {
            return dogs.stream().map(Dog::getId).sorted(Comparator.naturalOrder()).toList();
        } else {
            return new ArrayList<>();
        }
    }

    protected User validAndGetUser(long userId) {
        if (userId <= 0) throw new ValidationException("ID не может быть отрицательным числом");

        return Optional.ofNullable(userRepository.findById(userId)).orElseThrow(()
                -> new NotFoundException("Пользователь с ID={} не найден", userId));
    }
}
