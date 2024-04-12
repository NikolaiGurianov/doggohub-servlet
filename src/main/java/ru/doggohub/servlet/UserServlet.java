package ru.doggohub.servlet;

import com.google.gson.Gson;
import ru.doggohub.dto.user.UserRequestDto;
import ru.doggohub.dto.user.UserResponseDto;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;
import ru.doggohub.service.user.UserService;
import ru.doggohub.service.user.UserServiceImpl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(urlPatterns = {"/user"})
public class UserServlet extends HttpServlet {
    private final UserService userService;

    public UserServlet() {
        super();
        this.userService = new UserServiceImpl(new UserRepository(), new DogRepository());
    }

    public UserServlet(UserRepository userRepository, DogRepository dogRepository) {
        super();
        this.userService = new UserServiceImpl(userRepository, dogRepository);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();

        try {
            String userIdParam = req.getParameter("id");
            if (userIdParam == null) {
                List<UserResponseDto> dtoList = userService.getAll();
                String jsonResponse = new Gson().toJson(dtoList);
                writer.println(jsonResponse);

            } else {
                long userId = Long.parseLong(userIdParam);
                UserResponseDto userResponseDto = userService.getById(userId);
                String jsonResponse = new Gson().toJson(userResponseDto);
                writer.println(jsonResponse);
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.println("Неверный формат ID");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            UserRequestDto userRequestDto = new Gson().fromJson(reader, UserRequestDto.class);
            UserResponseDto userResponseDto = userService.addUser(userRequestDto);
            String userResponseJson = new Gson().toJson(userResponseDto);
            resp.getWriter().println(userResponseJson);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();
        String userIdParam = req.getParameter("id");

        if (userIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.println("Параметр id не передан, проверьте запрос");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            long userId = Long.parseLong(userIdParam);
            UserRequestDto userRequestDto = new Gson().fromJson(reader, UserRequestDto.class);
            UserResponseDto userResponseDto = userService.updateUser(userRequestDto, userId);
            String userResponseJson = new Gson().toJson(userResponseDto);
            resp.getWriter().println(userResponseJson);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("При обработке запроса произошла ошибка: " + e.getMessage());
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();

        try {
            String userIdParam = req.getParameter("id");

            if (userIdParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.println("Не указан ID пользователя");
                return;
            }
            long userId = Long.parseLong(userIdParam);
            userService.deleteById(userId);

            writer.println("Пользователь с ID={}, успешно удален из базы данных");
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.println("Неверный формат ID");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
}
