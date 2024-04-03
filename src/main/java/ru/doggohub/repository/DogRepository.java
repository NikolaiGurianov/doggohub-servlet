package ru.doggohub.repository;

import lombok.AllArgsConstructor;
import ru.doggohub.model.Dog;
import ru.doggohub.model.enums.Breed;
import ru.doggohub.model.enums.Color;
import ru.doggohub.model.enums.Gender;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class DogRepository {
    private final Connection connection;

    public Dog findById(long id) {
        Dog dog = null;
        String query = "SELECT * FROM schema_name.dogs d " +
                "JOIN schema_name.dog_owners dow ON d.id = dow.dog_id " +
                "WHERE d.id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    dog = Dog.builder()
                            .id(resultSet.getLong("id"))
                            .name(resultSet.getString("name"))
                            .birthDay(resultSet.getDate("birth_day").toLocalDate())
                            .breed(Breed.valueOf(resultSet.getString("breed")))
                            .color(Color.valueOf(resultSet.getString("color")))
                            .gender(Gender.valueOf(resultSet.getString("gender")))
                            .weight(resultSet.getInt("weight"))
                            .registrationTime(resultSet.getTimestamp("reg_time").toLocalDateTime())
                            .ownerId(resultSet.getLong("owner_id"))
                            .build();
                }
            } catch (
                    SQLException e) {
                throw new RuntimeException("Ошибка базы данных при поиске информации о собаке с ID={}: " + id, e);
            }
            return dog;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения базы данных", e);
        }
    }

    public List<Dog> findAllByOwnerId(long ownerId) {
        List<Dog> dogList = new ArrayList<>();
        String query = "SELECT * FROM schema_name.dogs d " +
                "JOIN schema_name.dog_owners dow ON d.id = dow.dog_id " +
                "WHERE dow.owner_id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, ownerId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                dogList.add(Dog.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .birthDay(resultSet.getDate("birth_day").toLocalDate())
                        .breed(Breed.valueOf(resultSet.getString("breed")))
                        .color(Color.valueOf(resultSet.getString("color")))
                        .gender(Gender.valueOf(resultSet.getString("gender")))
                        .weight(resultSet.getInt("weight"))
                        .registrationTime(resultSet.getTimestamp("reg_time").toLocalDateTime())
                        .ownerId(resultSet.getLong("owner_id"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при поиске информации по владельцу с ID={}: " + ownerId, e);
        }

        return dogList;
    }

    public Dog save(Dog dog) {
        String insertDogQuery = "INSERT INTO schema_name.dogs (name, birth_day, breed, color, gender, weight, reg_time)" +
                " VALUES (?,?,?,?,?,?,?)";

        try {
            PreparedStatement insertStatement = connection.prepareStatement(insertDogQuery, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setString(1, dog.getName());
            insertStatement.setDate(2, Date.valueOf(dog.getBirthDay()));
            insertStatement.setString(3, String.valueOf(dog.getBreed()));
            insertStatement.setString(4, String.valueOf(dog.getColor()));
            insertStatement.setString(5, String.valueOf(dog.getGender()));
            insertStatement.setInt(6, dog.getWeight());
            insertStatement.setTimestamp(7, Timestamp.valueOf(dog.getRegistrationTime()));

            int affectedRows = insertStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Вставка записи не выполнена, ни одна строка не была изменена.");
            }
            try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long dogId = generatedKeys.getLong(1);

                    saveDogOwner(dogId, dog.getOwnerId());

                    return findById(dogId);

                } else {
                    throw new SQLException("Ошибка при добавлении новой собаки. ID не сгенерировано");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при добавлении новой собаки", e);
        }
    }

    public void saveDogOwner(long dogId, long ownerId) {
        String insertDogOwnerQuery = "INSERT INTO schema_name.dog_owners (dog_id, owner_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(insertDogOwnerQuery);
            statement.setLong(1, dogId);
            statement.setLong(2, ownerId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при заполнении таблицы владельцев собак" + e);
        }
    }

    public void removeById(long id) {
        String query = "DELETE FROM schema_name.dogs WHERE id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при удалении объекта", e);
        }
    }

    public Dog update(Dog dog) {
        String query = "UPDATE schema_name.dogs SET name = ?, weight =? WHERE id =?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, dog.getName());
            preparedStatement.setInt(2, dog.getWeight());
            preparedStatement.setLong(3, dog.getId());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Вставка записи не выполнена, ни одна строка не была изменена.");
            }
            return findById(dog.getId());


        } catch (SQLException e) {
            throw new RuntimeException("Ошибка базы данных при обновлении информации: " + dog, e);
        }
    }
}