package ru.doggohub.repository;

import lombok.AllArgsConstructor;
import ru.doggohub.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class UserRepository {
    private final Connection connection;

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM schema_name.owners";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.add(User.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .email(resultSet.getString("email"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных");
        }
        return users;
    }


    public User findById(long id) {
        User user = null;
        String query = "SELECT * FROM schema_name.owners o WHERE o.id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = User.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .email(resultSet.getString("email"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Пользователь не найден", e);
        }
        return user;
    }

    public User save(User user) {
        String insertQuery = "INSERT INTO schema_name.owners (name, email) VALUES (?,?)";

        try {
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setString(1, user.getName());
            insertStatement.setString(2, user.getEmail());

            int affectedRows = insertStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Вставка записи не выполнена, ни одна строка не была добавлена.");
            }

            try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {

                    long userId = generatedKeys.getLong(1);

                    return findById(userId);
                } else {
                    throw new SQLException("Сгенерированный ключ не был получен.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при добавлении нового владельца", e);
        }
    }

    public User update(User user) {
        String query = "UPDATE schema_name.owners SET name = ?, email = ? WHERE id = ?";

        try {

            PreparedStatement updateStatement = connection.prepareStatement(query);
            updateStatement.setString(1, user.getName());
            updateStatement.setString(2, user.getEmail());
            updateStatement.setLong(3, user.getId());

            int affectedRows = updateStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Вставка записи не выполнена, ни одна строка не была изменена.");
            }
            return findById(user.getId());

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при обновлении пользователя", e);
        }
    }

    public User findOwnerByDogId(long dogId) {
        User user = null;
        String query = "SELECT * FROM schema_name.owners o " +
                "JOIN schema_name.dog_owners dow ON o.id = dow.owner_id " +
                "WHERE dow.dog_id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, dogId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = User.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .email(resultSet.getString("email"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Пользователь не найден", e);
        }
        return user;
    }

    public void deleteById(Long userId) {

        String query = "DELETE FROM schema_name.owners WHERE id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, userId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при удалении объекта", e);
        }
    }
}