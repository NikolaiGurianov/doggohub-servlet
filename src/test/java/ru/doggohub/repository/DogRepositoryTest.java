package ru.doggohub.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.doggohub.model.Dog;
import ru.doggohub.model.enums.Breed;
import ru.doggohub.model.enums.Color;
import ru.doggohub.model.enums.Gender;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class DogRepositoryTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;
    private final long dogId = 1L;
    private final long ownerId = 1L;
    private final LocalDateTime regTime = LocalDateTime.of(2023, 5, 20, 10, 30);

    private Dog dog;

    private DogRepository dogRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dogRepository = new DogRepository(connection);


        dog = Dog.builder()
                .id(dogId)
                .name("Vegas")
                .birthDay(LocalDate.of(2019, 5, 15))
                .breed(Breed.LABRODOR)
                .color(Color.WHITE)
                .gender(Gender.MALE)
                .weight(30)
                .registrationTime(regTime)
                .ownerId(ownerId)
                .build();
    }

    @Test
    public void findById_ReturnDog() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(dog.getId());
        when(resultSet.getString("name")).thenReturn(dog.getName());
        when(resultSet.getDate("birth_day")).thenReturn(java.sql.Date.valueOf(dog.getBirthDay()));
        when(resultSet.getString("breed")).thenReturn(dog.getBreed().toString());
        when(resultSet.getString("color")).thenReturn(dog.getColor().toString());
        when(resultSet.getString("gender")).thenReturn(dog.getGender().toString());
        when(resultSet.getInt("weight")).thenReturn(dog.getWeight());
        when(resultSet.getTimestamp("reg_time")).thenReturn(java.sql.Timestamp.valueOf(dog.getRegistrationTime()));
        when(resultSet.getLong("owner_id")).thenReturn(dog.getOwnerId());

        Dog actualDog = dogRepository.findById(dogId);

        assertEquals(dog.toString(), actualDog.toString());
    }

    @Test
    public void findById_ThrowRuntimeException() throws SQLException {
        long id = 3L;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> dogRepository.findById(id));
    }

    @Test
    public void findAllByOwnerId_ReturnListDogs() throws SQLException {
        Dog dog2 = new Dog(2L, "Dog2", LocalDate.of(2019, 6, 1), Breed.BULLTERIER, Color.BLACK, Gender.FEMALE, 46, LocalDateTime.now(), ownerId);
        List<Dog> expectedDogs = List.of(dog, dog2);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("Vegas", "Dog2");
        when(resultSet.getDate("birth_day")).thenReturn(Date.valueOf(LocalDate.of(2019, 5, 15)), Date.valueOf(LocalDate.of(2019, 6, 1)));
        when(resultSet.getString("breed")).thenReturn("LABRODOR", "BULLTERIER");
        when(resultSet.getString("color")).thenReturn("WHITE", "BLACK");
        when(resultSet.getString("gender")).thenReturn("MALE", "FEMALE");
        when(resultSet.getInt("weight")).thenReturn(30, 46);
        when(resultSet.getTimestamp("reg_time")).thenReturn(Timestamp.valueOf(dog.getRegistrationTime()), Timestamp.valueOf(dog2.getRegistrationTime()));
        when(resultSet.getLong("owner_id")).thenReturn(ownerId);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        List<Dog> actualDogs = dogRepository.findAllByOwnerId(ownerId);

        assertEquals(expectedDogs.size(), actualDogs.size());
        assertIterableEquals(expectedDogs, actualDogs);
    }

    @Test
    public void save_ReturnDog() throws SQLException {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);

        Dog actualDog = dogRepository.save(dog);

        assertEquals(actualDog, dog);
    }

    @Test
    public void save_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        assertThrows(RuntimeException.class, () -> {
            dogRepository.save(dog);
        });
    }

    @Test
    public void saveDogOwner_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        dogRepository.saveDogOwner(dogId, ownerId);

        verify(preparedStatement, times(1)).executeUpdate();

        verify(preparedStatement, times(1)).setLong(1, dogId);
        verify(preparedStatement, times(1)).setLong(2, ownerId);
    }

    @Test
    public void saveDogOwner_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> {
            dogRepository.saveDogOwner(dogId, ownerId);
        });
    }

    @Test
    public void removeById_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        dogRepository.removeById(dogId);

        verify(preparedStatement, times(1)).executeUpdate();
        verify(preparedStatement, times(1)).setLong(1, dogId);
    }

    @Test
    public void removeByDogId_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> {
            dogRepository.removeById(dogId);
        });
    }

    @Test
    public void update_ReturnUpdatedDog() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Dog updatedDog = dogRepository.update(dog);

        assertEquals(dog, updatedDog);
    }

    @Test
    public void update_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> {
            dogRepository.update(dog);
        });
    }
}