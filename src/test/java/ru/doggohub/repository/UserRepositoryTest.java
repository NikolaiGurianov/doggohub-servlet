package ru.doggohub.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.doggohub.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserRepositoryTest {
    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;
    private final long userId = 1L;
    private final long dogId = 1L;
    private User user;
    private UserRepository userRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userRepository = new UserRepository(connection);
        user = new User(userId, "Borya", "mail@mail.ru", null);
    }

    @Test
    public void findAll_ReturnListWithUsers() throws SQLException {
        List<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(user);
        expectedUsers.add(new User(2L, "Alice", "alice@example.com", null));

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("Borya", "Alice");
        when(resultSet.getString("email")).thenReturn("mail@mail.ru", "alice@example.com");

        List<User> actualUsers = userRepository.findAll();

        assertIterableEquals(expectedUsers, actualUsers);
    }

    @Test
    public void findAll_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> userRepository.findAll());
    }

    @Test
    public void findById_ReturnUser() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(userId);
        when(resultSet.getString("name")).thenReturn(user.getName());
        when(resultSet.getString("email")).thenReturn(user.getEmail());

        User result = userRepository.findById(userId);

        assertEquals(user, result);
    }

    @Test
    public void findById_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> userRepository.findById(userId));
    }

    @Test
    public void save_ReturnUser() throws SQLException {
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(userId);

        User savedUser = userRepository.save(user);

        assertEquals(user, savedUser);
    }

    @Test
    public void save_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> userRepository.save(user));
    }

    @Test
    public void update_ReturnUpdatedUser() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(userId);

        User updatedUser = userRepository.update(user);

        assertEquals(user, updatedUser);
    }

    @Test
    public void update_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> userRepository.update(user));
    }

    @Test
    public void findOwnerByDogId_WhenOwnerExists_ShouldReturnOwner() throws SQLException {
        user.setDogIds(List.of(1L));

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(userId);
        when(resultSet.getString("name")).thenReturn(user.getName());
        when(resultSet.getString("email")).thenReturn(user.getEmail());

        User owner = userRepository.findOwnerByDogId(user.getDogIds().get(0));
        owner.setDogIds(List.of(1L));

        assertEquals(user, owner);
    }

    @Test
    public void findOwnerByDogId_WhenNoOwnerExists_ShouldReturnNull() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        User owner = userRepository.findOwnerByDogId(dogId);

        assertNull(owner);
    }

    @Test
    public void findOwnerByDogId_WhenSQLExceptionThrown_ShouldThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> userRepository.findOwnerByDogId(dogId));
    }
}