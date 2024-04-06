package ru.doggohub.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.doggohub.util.LocalDateTypeAdapter;
import ru.doggohub.dto.dog.DogRequestDto;
import ru.doggohub.dto.dog.DogResponseDto;
import ru.doggohub.service.dog.DogService;
import ru.doggohub.service.dog.DogServiceImpl;

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
import java.time.LocalDate;
import java.util.List;

@WebServlet(urlPatterns = {"/dog"})
public class DogServlet extends HttpServlet {
    private final DogService dogService = new DogServiceImpl();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();

    public DogServlet() {
        super();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();

        try {
            String dogIdParam = req.getParameter("id");
            String userIdParam = req.getParameter("user_id");

            if (dogIdParam != null) {
                long dogId = Long.parseLong(dogIdParam);
                DogResponseDto dogResponseDto = dogService.getById(dogId);
                String jsonResponse = gson.toJson(dogResponseDto);
                writer.println(jsonResponse);

            } else if (userIdParam != null) {
                long userId = Long.parseLong(userIdParam);
                List<DogResponseDto> dogResponseDto = dogService.getByOwnerId(userId);
                String jsonResponse = gson.toJson(dogResponseDto);
                writer.println(jsonResponse);

            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.println("Не указан ID собаки или ID владельца");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.println("Неверный формат ID");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            DogRequestDto dogRequestDto = gson.fromJson(reader, DogRequestDto.class);
            DogResponseDto dogResponseDto = dogService.add(dogRequestDto);
            String jsonResponse = gson.toJson(dogResponseDto);

            writer.println(jsonResponse);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            String dogIdParam = req.getParameter("id");

            if (dogIdParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.println("Не указан ID собаки");
                return;
            }

            long dogId = Long.parseLong(dogIdParam);
            DogRequestDto dogRequestDto = gson.fromJson(reader, DogRequestDto.class);
            DogResponseDto dogResponseDto = dogService.update(dogRequestDto, dogId);
            String jsonResponse = gson.toJson(dogResponseDto);

            writer.println(jsonResponse);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.println("Неверный формат ID");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();

        try {
            String dogIdString = req.getParameter("id");

            if (dogIdString == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.println("Не указан ID собаки");
                return;
            }

            long dogId = Long.parseLong(dogIdString);
            dogService.deleteById(dogId);

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.println("Неверный формат ID");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
