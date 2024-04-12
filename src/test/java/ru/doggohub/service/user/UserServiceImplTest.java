package ru.doggohub.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DogRepository dogRepository;

    private UserServiceImpl userService;
    private final long userId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, dogRepository);
    }

    @Test
    void addUserTest_Success() {
        UserRequestDto requestDto = new UserRequestDto("User1", "popo@yan.ru");
        User user = UserMapper.fromDto(requestDto);
        user.setId(userId);
        user.setDogIds(new ArrayList<>());
        UserResponseDto expectedUser = UserMapper.toDto(user);

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto actualUser = userService.addUser(requestDto);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
    }

    @Test
    void addUserTest_InvalidEmail_ThrowValidationException() {
        UserRequestDto requestDto = new UserRequestDto("User1", "");

        assertThrows(ValidationException.class, () -> userService.addUser(requestDto));
    }

    @Test
    void getUserByIdTest_Success() {
        List<Dog> dogs = List.of(
                Dog.builder().id(1L).name("Dog1").build(),
                Dog.builder().id(2L).name("Dog2").build()
        );

        User user = User.builder().id(1L).name("User1").email("popo@yan.ru").build();

        user.setDogIds(List.of(1L, 2L));

        when(userRepository.findById(userId)).thenReturn(user);
        when(dogRepository.findAllByOwnerId(userId)).thenReturn(dogs);

        UserResponseDto actualUserDto = userService.getById(userId);

        assertNotNull(actualUserDto);
        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(user.getName(), actualUserDto.getName());
        assertEquals(user.getDogIds(), actualUserDto.getDogs());
    }

    @Test
    void getUserByIdTest_Success_WithEmptyDogs() {
        List<Dog> emptyDogsList = List.of();

        User user = User.builder().id(userId).name("User1").email("popo@yan.ru").build();

        when(userRepository.findById(userId)).thenReturn(user);
        when(dogRepository.findAllByOwnerId(userId)).thenReturn(emptyDogsList);

        UserResponseDto actualUserDto = userService.getById(userId);

        assertNotNull(actualUserDto);
        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(user.getName(), actualUserDto.getName());
        assertEquals(user.getDogIds(), actualUserDto.getDogs());
    }

    @Test
    void getUserByIdTest_ExpectException() {
        long userId = 100L;
        long negativeUserId = -1L;

        when(userRepository.findById(userId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getById(userId));

        assertThrows(ValidationException.class, () -> userService.getById(negativeUserId));
    }

    @Test
    void updateUserTest_Success_UpdateName() {
        String newName = "NewName";
        String newEmail = "newemail@example.com";

        UserRequestDto dto = new UserRequestDto(newName, newEmail);
        User user = User.builder().id(userId).name("OldName").email("popo@yan.ru").build();

        when(userRepository.findById(userId)).thenReturn(user);
        when(userRepository.update(any(User.class))).thenReturn(user);

        UserResponseDto actualUserDto = userService.updateUser(dto, userId);
        User actualUser = userRepository.findById(userId);

        assertNotNull(actualUserDto);
        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(newName, actualUserDto.getName());
        assertEquals(newEmail, actualUser.getEmail());
    }

    @Test
    void updateUserTest_Success_NoChanges() {
        UserRequestDto dto = new UserRequestDto(null, null);

        User user = User.builder().id(userId).name("OldName").email("oldemail@example.com").build();

        when(userRepository.findById(userId)).thenReturn(user);
        when(userRepository.update(any(User.class))).thenReturn(user);

        UserResponseDto actualUserDto = userService.updateUser(dto, userId);
        User actualUser = userRepository.findById(userId);

        assertNotNull(actualUserDto);
        assertEquals(user.getId(), actualUserDto.getId());
        assertEquals(user.getName(), actualUserDto.getName());
        assertEquals(user.getEmail(), actualUser.getEmail());
    }

    @Test
    void deleteUserByIdTest_Success() {
        when(userRepository.findById(userId)).thenReturn(new User());

        userService.deleteById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void getAllUsersTest_Success() {
        List<User> users = List.of(
                User.builder().id(1L).name("User1").email("popo1@yan.ru").build(),
                User.builder().id(2L).name("User2").email("popo2@yan.ru").build()
        );

        List<UserResponseDto> expectedUserDtos = users.stream()
                .map(UserMapper::toDto)
                .toList();

        when(userRepository.findAll()).thenReturn(users);
        when(userService.getListDogIds(anyLong())).thenReturn(List.of());

        List<UserResponseDto> actualUserDtos = userService.getAll();

        assertNotNull(actualUserDtos);
        assertEquals(expectedUserDtos.size(), actualUserDtos.size());
        assertIterableEquals(expectedUserDtos, actualUserDtos);
    }

    @Test
    void getAllUsersTest_Success_EmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDto> actualUserDtos = userService.getAll();

        assertNotNull(actualUserDtos);
        assertTrue(actualUserDtos.isEmpty());
    }
}