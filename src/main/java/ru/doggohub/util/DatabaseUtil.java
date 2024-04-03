package ru.doggohub.util;

import lombok.experimental.UtilityClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@UtilityClass
public class DatabaseUtil {
    private static final String PROPERTIES_FILE = "src/main/resources/database.properties";

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке файла свойств", e);
        }

        String url = properties.getProperty("url");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");

        return DriverManager.getConnection(url, user, password);
    }
}
