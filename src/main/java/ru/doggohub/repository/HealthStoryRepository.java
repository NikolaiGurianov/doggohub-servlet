package ru.doggohub.repository;

import lombok.AllArgsConstructor;
import ru.doggohub.model.HealthStory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class HealthStoryRepository {
    private final Connection connection;

    public List<HealthStory> findByDogId(long dogId) {
        List<HealthStory> storyList = new ArrayList<>();

        String query = "SELECT * FROM schema_name.dog_details " +
                "WHERE dog_id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, dogId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                storyList.add(HealthStory.builder()
                        .id(resultSet.getLong("id"))
                        .dogId(resultSet.getLong("dog_id"))
                        .text(resultSet.getString("health_history"))
                        .visit(resultSet.getDate("visit_time").toLocalDate())
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return storyList;
    }

    public HealthStory save(HealthStory healthStory) {
        String query = "INSERT INTO schema_name.dog_details (dog_id, health_history, visit_time)" +
                " VALUES (?,?,?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, healthStory.getDogId());
            preparedStatement.setString(2, healthStory.getText());
            preparedStatement.setDate(3, Date.valueOf(healthStory.getVisit()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Вставка записи не выполнена, ни одна строка не была изменена.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long storyId = generatedKeys.getLong(1);

                    return findById(storyId);

                } else {
                    throw new SQLException("Ошибка при добавлении новой собаки. ID не сгенерировано");
                }
            } catch (
                    SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при добавлении новой истории болезни", e);
        }
    }

    public HealthStory findById(long storyId) {
        HealthStory healthStory = null;

        String query = "SELECT * FROM schema_name.dog_details " +
                "WHERE id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, storyId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                healthStory = HealthStory.builder()
                        .id(resultSet.getLong("id"))
                        .dogId(resultSet.getLong("dog_id"))
                        .text(resultSet.getString("health_history"))
                        .visit(resultSet.getDate("visit_time").toLocalDate())
                        .build();
            }
        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }
        return healthStory;
    }

    public void removeById(long id) {
        String query = "DELETE FROM schema_name.dog_details WHERE id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}