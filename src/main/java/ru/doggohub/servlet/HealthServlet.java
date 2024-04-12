package ru.doggohub.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.doggohub.dto.health.HealthStoryRequestDto;
import ru.doggohub.dto.health.HealthStoryResponseDto;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.HealthStoryRepository;
import ru.doggohub.service.health.HealthStoryService;
import ru.doggohub.service.health.HealthStoryServiceImpl;
import ru.doggohub.util.LocalDateTypeAdapter;

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

@WebServlet(urlPatterns = {"/health"})
public class HealthServlet extends HttpServlet {

    private final HealthStoryService healthStoryService;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();

    public HealthServlet(HealthStoryRepository healthStoryRepository, DogRepository dogRepository) {
        super();
        this.healthStoryService = new HealthStoryServiceImpl(healthStoryRepository, dogRepository);
    }

    public HealthServlet() {
        super();
        this.healthStoryService = new HealthStoryServiceImpl(new HealthStoryRepository(), new DogRepository());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();

        try {
            String idStoryParam = req.getParameter("id");
            String dogIdParam = req.getParameter("dog_id");

            if (idStoryParam != null) {
                long storyId = Long.parseLong(idStoryParam);
                HealthStoryResponseDto responseDto = healthStoryService.getById(storyId);
                String jsonResponse = gson.toJson(responseDto);
                writer.println(jsonResponse);

            } else if (dogIdParam != null) {
                long dogId = Long.parseLong(dogIdParam);
                List<HealthStoryResponseDto> responseDtoList = healthStoryService.getByDogId(dogId);
                String jsonResponse = gson.toJson(responseDtoList);
                writer.println(jsonResponse);

            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.println("Не указан ID истории болезни или ID питомца");
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            HealthStoryRequestDto healthStoryRequestDto = gson.fromJson(reader, HealthStoryRequestDto.class);
            HealthStoryResponseDto healthStoryResponseDto = healthStoryService.add(healthStoryRequestDto);
            String jsonResponse = gson.toJson(healthStoryResponseDto);

            writer.println(jsonResponse);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("При обработке запроса произошла ошибка: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter writer = resp.getWriter();

        try {
            String idStoryParam = req.getParameter("id");

            if (idStoryParam.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.println("Не указан ID истории болезни");
                return;
            }

            long storyId = Long.parseLong(idStoryParam);
            healthStoryService.deleteById(storyId);

            writer.println("История болезни с ID={}, успешно удалены из базы данных");
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
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}