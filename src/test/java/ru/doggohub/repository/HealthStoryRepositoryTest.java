package ru.doggohub.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.doggohub.model.HealthStory;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HealthStoryRepositoryTest {
    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private final long dogId = 1L;
    private final long healthId = 1L;

    private HealthStory healthStory;
    private HealthStoryRepository healthStoryRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        healthStoryRepository = new HealthStoryRepository(connection);
        healthStory = new HealthStory(healthId, dogId, "Test health story", LocalDate.now());

    }

    @Test
    public void findByDogId_ReturnListWithHistory() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong("id")).thenReturn(healthStory.getId());
        when(resultSet.getLong("dog_id")).thenReturn(healthStory.getDogId());
        when(resultSet.getString("health_history")).thenReturn(healthStory.getText());
        when(resultSet.getDate("visit_time")).thenReturn(java.sql.Date.valueOf(healthStory.getVisit()));

        List<HealthStory> foundStories = healthStoryRepository.findByDogId(dogId);

        assertEquals(1, foundStories.size());
        assertEquals(healthStory, foundStories.get(0));
    }

    @Test
    public void findByDogId_ThrowRuntimeException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);

        assertThrows(RuntimeException.class, () -> healthStoryRepository.findByDogId(dogId));
    }

    @Test
    public void save_ReturnHistory() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);

        HealthStory savedStory = healthStoryRepository.save(healthStory);

        assertNotNull(savedStory.getId());
    }

    @Test
    public void save_ThrowRuntimeException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);

        assertThrows(RuntimeException.class, () -> healthStoryRepository.save(healthStory));
    }

    @Test
    public void findById_ReturnHistory() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(healthStory.getId());
        when(resultSet.getLong("dog_id")).thenReturn(healthStory.getDogId());
        when(resultSet.getString("health_history")).thenReturn(healthStory.getText());
        when(resultSet.getDate("visit_time")).thenReturn(Date.valueOf(healthStory.getVisit()));
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        HealthStory foundStory = healthStoryRepository.findById(1L);

        assertEquals(healthStory, foundStory);
    }

    @Test
    public void findById_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> healthStoryRepository.findById(healthId));
    }

    @Test
    public void removeById_Successful() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        healthStoryRepository.removeById(healthId);

        verify(preparedStatement).setLong(1, healthId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void removeById_ThrowRuntimeException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        assertThrows(RuntimeException.class, () -> healthStoryRepository.removeById(healthId));
    }
}