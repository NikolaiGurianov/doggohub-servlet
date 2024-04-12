package ru.doggohub.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.doggohub.dto.health.HealthStoryRequestDto;
import ru.doggohub.dto.health.HealthStoryResponseDto;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.HealthStoryMapper;
import ru.doggohub.model.Dog;
import ru.doggohub.model.HealthStory;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.HealthStoryRepository;
import ru.doggohub.service.health.HealthStoryServiceImpl;
import ru.doggohub.util.LocalDateTypeAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class HealthServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HealthStoryServiceImpl healthStoryService;
    @Mock
    private HealthStoryRepository healthStoryRepository;
    @Mock
    private DogRepository dogRepository;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter writer = new PrintWriter(stringWriter);
    private final MockHttpServletRequest inputStream = new MockHttpServletRequest();

    private HealthServlet healthServlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        healthServlet = new HealthServlet(healthStoryRepository, dogRepository);
    }

    @Test
    void doGetTest_StoryIdParam_Success() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(response.getWriter()).thenReturn(writer);

        HealthStory healthStory = new HealthStory();
        HealthStoryResponseDto expectedStory = new HealthStoryResponseDto();

        when(healthStoryRepository.findById(anyLong())).thenReturn(healthStory);
        when(healthStoryService.getById(anyLong())).thenReturn(expectedStory);

        healthServlet.doGet(request, response);

        String expectedJsonResponse = gson.toJson(expectedStory);
        String actualJsonResponse = stringWriter.toString().trim();

        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doGetTest_DogIdParam_Success() throws Exception {

        when(request.getParameter("dog_id")).thenReturn("1");
        when(response.getWriter()).thenReturn(writer);

        Dog dog = new Dog();
        List<HealthStory> stories = List.of(new HealthStory());

        List<HealthStoryResponseDto> expectedResponse = List.of(new HealthStoryResponseDto());

        when(dogRepository.findById(anyLong())).thenReturn(dog);
        when(healthStoryRepository.findByDogId(anyLong())).thenReturn(stories);
        when(healthStoryService.getByDogId(anyLong())).thenReturn(expectedResponse);

        healthServlet.doGet(request, response);

        String expectedJsonResponse = gson.toJson(expectedResponse);
        String actualJsonResponse = stringWriter.toString().trim();

        assertEquals(expectedJsonResponse, actualJsonResponse);

    }

    @Test
    void doGetTest_NoParams() throws Exception {
        when(response.getWriter()).thenReturn(writer);

        healthServlet.doGet(request, response);

        String expectedJsonResponse = "Не указан ID истории болезни или ID питомца";
        String actualJsonResponse = stringWriter.toString().trim();

        assertEquals(expectedJsonResponse, actualJsonResponse);

    }

    @Test
    void doGetTest_InvalidIdFormat() throws Exception {
        when(request.getParameter("id")).thenReturn("abc");
        when(response.getWriter()).thenReturn(writer);

        healthServlet.doGet(request, response);


        String expectedJsonResponse = "Неверный формат ID";
        String actualJsonResponse = stringWriter.toString().trim();

        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPostTest_Success() throws Exception {
        Dog dog = Dog.builder().id(1L).name("Vegas").build();
        HealthStoryRequestDto requestDto = HealthStoryRequestDto.builder().text("Cough with blood").dogId(1L).build();
        HealthStory healthStory = HealthStoryMapper.fromDto(requestDto, dog);
        healthStory.setId(1L);
        HealthStoryResponseDto expected = HealthStoryMapper.toDto(healthStory);

        when(dogRepository.findById(anyLong())).thenReturn(dog);
        when(healthStoryRepository.save(any(HealthStory.class))).thenReturn(healthStory);
        when(healthStoryService.add(requestDto)).thenReturn(expected);

        String req = gson.toJson(requestDto);
        inputStream.setContent(req.getBytes());

        when(request.getInputStream()).thenReturn(inputStream.getInputStream());
        when(response.getWriter()).thenReturn(writer);

        healthServlet.doPost(request, response);

        String expectedJsonResponse = gson.toJson(expected);
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPostTest_ThrowException() throws Exception {
        HealthStoryRequestDto requestDto = HealthStoryRequestDto.builder().dogId(1L).build();

        String invalidRequestBody = gson.toJson(requestDto);

        when(healthStoryService.add(requestDto)).thenThrow(ValidationException.class);

        inputStream.setContent(invalidRequestBody.getBytes());

        when(request.getInputStream()).thenReturn(inputStream.getInputStream());
        when(response.getWriter()).thenReturn(writer);

        healthServlet.doPost(request, response);

        String expectedJsonResponse = "При обработке запроса произошла ошибка: Получен пустой текст истории болезни";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doDeleteTest_Success() throws Exception {
        HealthStory healthStory = new HealthStory();
        when(request.getParameter("id")).thenReturn("1");
        when(healthStoryRepository.findById(anyLong())).thenReturn(healthStory);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        healthServlet.doDelete(request, response);

        String expectedJsonResponse = "История болезни с ID={}, успешно удалены из базы данных";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void doDeleteTest_InvalidFormatId() throws Exception {
        when(request.getParameter("id")).thenReturn("invalid_id");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        healthServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String expectedErrorMessage = "Неверный формат ID";
        assertEquals(expectedErrorMessage, stringWriter.toString().trim());
    }

    @Test
    void doDeleteTest_EmptyIdParam() throws Exception {
        when(request.getParameter("id")).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        healthServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String expectedErrorMessage = "Не указан ID истории болезни";
        assertEquals(expectedErrorMessage, stringWriter.toString().trim());
    }
}