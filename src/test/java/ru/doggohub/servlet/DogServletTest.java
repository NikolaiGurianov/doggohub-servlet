package ru.doggohub.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.doggohub.dto.dog.DogRequestDto;
import ru.doggohub.dto.dog.DogResponseDto;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.DogMapper;
import ru.doggohub.model.Dog;
import ru.doggohub.model.User;
import ru.doggohub.model.enums.Breed;
import ru.doggohub.model.enums.Color;
import ru.doggohub.model.enums.Gender;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;
import ru.doggohub.service.dog.DogServiceImpl;
import ru.doggohub.util.LocalDateTypeAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DogServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private DogServiceImpl dogService;
    @Mock
    private DogRepository dogRepository;
    @Mock
    private UserRepository userRepository;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .create();
    private final long dogId = 1L;
    private final long ownerId = 1L;
    private DogServlet dogServlet;
    private DogRequestDto dogRequestDto;
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter writer = new PrintWriter(stringWriter);
    private final MockHttpServletRequest inputStream = new MockHttpServletRequest();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dogServlet = new DogServlet(userRepository, dogRepository);

        dogRequestDto = DogRequestDto.builder()
                .name("Vegas")
                .ownerId(ownerId)
                .color(Color.WHITE)
                .breed(Breed.LABRODOR)
                .gender(Gender.MALE)
                .birthDay(LocalDate.of(2022, 7, 1))
                .weight(30)
                .build();
    }

    @Test
    void doGetTest_WithValidDogId() throws Exception {
        User owner = new User();
        owner.setId(ownerId);

        Dog dog = DogMapper.fromDto(dogRequestDto, ownerId);
        dog.setId(dogId);

        DogResponseDto expectedDog = DogMapper.toDto(dog, owner);

        when(userRepository.findOwnerByDogId(dogId)).thenReturn(owner);
        when(dogRepository.findById(dogId)).thenReturn(dog);
        when(dogService.getById(dogId)).thenReturn(expectedDog);

        when(response.getWriter()).thenReturn(writer);
        when(request.getParameter("id")).thenReturn(String.valueOf(dogId));

        dogServlet.doGet(request, response);

        String expectedJsonResponse = gson.toJson(expectedDog);
        String actualJsonResponse = stringWriter.toString().trim();

        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doGetTest_WithValidUserId() throws Exception {
        DogRequestDto dogRequestDto1 = DogRequestDto.builder()
                .name("Legas")
                .ownerId(ownerId)
                .color(Color.BROWN)
                .breed(Breed.BULLTERIER)
                .gender(Gender.MALE)
                .birthDay(LocalDate.of(2021, 7, 1))
                .weight(40)
                .build();

        User owner = new User();
        owner.setId(ownerId);
        owner.setDogIds(List.of(1L, 2L));

        List<Dog> dogs = new ArrayList<>();
        Dog dog1 = DogMapper.fromDto(dogRequestDto, ownerId);
        dog1.setId(dogId);

        Dog dog2 = DogMapper.fromDto(dogRequestDto1, ownerId);
        dog2.setId(2L);
        dogs.add(dog1);
        dogs.add(dog2);

        List<DogResponseDto> expectedDogs = new ArrayList<>();
        expectedDogs.add(DogMapper.toDto(dog1, owner));
        expectedDogs.add(DogMapper.toDto(dog2, owner));

        when(userRepository.findById(ownerId)).thenReturn(owner);
        when(dogRepository.findAllByOwnerId(ownerId)).thenReturn(dogs);
        when(dogService.getByOwnerId(ownerId)).thenReturn(expectedDogs);

        when(response.getWriter()).thenReturn(writer);
        when(request.getParameter("user_id")).thenReturn(String.valueOf(ownerId));

        dogServlet.doGet(request, response);

        String expectedJsonResponse = gson.toJson(expectedDogs);
        String actualJsonResponse = stringWriter.toString().trim();

        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doGetTest_WithBadRequest() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        when(request.getParameter("user_id")).thenReturn(null);

        when(response.getWriter()).thenReturn(writer);

        dogServlet.doGet(request, response);

        String expectedJsonResponse = "Не указан ID собаки или ID владельца";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }


    @Test
    void doGetTest_WithInvalidFormat() throws Exception {
        when(request.getParameter("id")).thenReturn("abc");

        when(response.getWriter()).thenReturn(writer);

        dogServlet.doGet(request, response);

        String expectedJsonResponse = "Неверный формат ID";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPostTest_WithValidRequestBody() throws Exception {
        User user = new User();
        user.setId(ownerId);

        Dog dog = DogMapper.fromDto(dogRequestDto, ownerId);
        dog.setId(dogId);
        DogResponseDto expectedDog = DogMapper.toDto(dog, user);

        when(userRepository.findById(ownerId)).thenReturn(user);
        when(dogRepository.save(any(Dog.class))).thenReturn(dog);
        when(userRepository.findOwnerByDogId(dogId)).thenReturn(user);
        when(dogService.add(any(DogRequestDto.class))).thenReturn(expectedDog);

        String req = gson.toJson(dogRequestDto);
        inputStream.setContent(req.getBytes());

        when(request.getInputStream()).thenReturn(inputStream.getInputStream());
        when(response.getWriter()).thenReturn(writer);

        dogServlet.doPost(request, response);

        String expectedJsonResponse = gson.toJson(expectedDog);
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPostTest_InvalidRequestBody() throws IOException {
        DogRequestDto dogRequestDto = DogRequestDto.builder()
                .name("Vegas")
                .build();

        String invalidRequestBody = gson.toJson(dogRequestDto);

        when(dogService.add(dogRequestDto)).thenThrow(ValidationException.class);

        inputStream.setContent(invalidRequestBody.getBytes());

        when(request.getInputStream()).thenReturn(inputStream.getInputStream());
        when(response.getWriter()).thenReturn(writer);

        dogServlet.doPost(request, response);

        assertEquals("При обработке запроса произошла ошибка: Все поля для записи должны быть заполнены", stringWriter.toString().trim());
    }

    @Test
    void doPostTest_NotFoundOwner() throws Exception {
        when(dogService.add(any(DogRequestDto.class))).thenThrow(new NotFoundException("Пользователь с ID={} не найден"));

        String req = gson.toJson(dogRequestDto);
        inputStream.setContent(req.getBytes());

        when(request.getInputStream()).thenReturn(inputStream.getInputStream());

        when(response.getWriter()).thenReturn(writer);

        dogServlet.doPost(request, response);

        String expectedJsonResponse = "При обработке запроса произошла ошибка: Пользователь с ID={} не найден";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPatchTest_WithValidRequestBody() throws Exception {
        User user = new User();
        user.setId(ownerId);

        DogRequestDto updatedDogRequestDto = DogRequestDto.builder()
                .name("Updated Dog")
                .ownerId(ownerId)
                .color(Color.WHITE)
                .breed(Breed.LABRODOR)
                .gender(Gender.MALE)
                .birthDay(LocalDate.of(2022, 7, 1))
                .weight(35)
                .build();

        Dog dog = DogMapper.fromDto(dogRequestDto, ownerId);
        dog.setId(dogId);
        Dog updatedDog = DogMapper.fromDto(updatedDogRequestDto, ownerId);
        updatedDog.setId(dogId);

        DogResponseDto expectedDog = DogMapper.toDto(updatedDog, user);

        when(userRepository.findOwnerByDogId(dogId)).thenReturn(user);
        when(dogRepository.findById(dogId)).thenReturn(dog);
        when(dogRepository.update(any(Dog.class))).thenReturn(updatedDog);
        when(dogService.update(any(DogRequestDto.class), any(Long.class))).thenReturn(expectedDog);

        String req = gson.toJson(updatedDogRequestDto);
        inputStream.setContent(req.getBytes());

        when(request.getParameter("id")).thenReturn(String.valueOf(dogId));
        when(request.getInputStream()).thenReturn(inputStream.getInputStream());
        when(response.getWriter()).thenReturn(writer);

        dogServlet.doPatch(request, response);

        String expectedJsonResponse = gson.toJson(expectedDog);
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPatchTest_BadRequest() throws Exception {
        when(request.getParameter("id")).thenReturn(null);

        when(response.getWriter()).thenReturn(writer);

        dogServlet.doPatch(request, response);

        String expectedJsonResponse = "Параметр id не передан, проверьте запрос";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPatchTest_NotFoundOwner() throws Exception {
        Dog dog = new Dog();
        when(request.getParameter("id")).thenReturn(String.valueOf(dogId));

        when(dogRepository.findById(dogId)).thenReturn(dog);
        when(dogService.update(any(DogRequestDto.class), any(Long.class))).thenThrow(new NotFoundException("Пользователь с ID={} не найден"));

        String req = gson.toJson(dogRequestDto);
        inputStream.setContent(req.getBytes());

        when(request.getInputStream()).thenReturn(inputStream.getInputStream());

        when(response.getWriter()).thenReturn(writer);

        dogServlet.doPatch(request, response);

        String expectedJsonResponse = "При обработке запроса произошла ошибка: Владелец собаки с ID={} не найден";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doDeleteTest_Success() throws Exception {
        Dog dog = Dog.builder().id(dogId).build();

        when(request.getParameter("id")).thenReturn(String.valueOf(dogId));
        when(dogRepository.findById(dogId)).thenReturn(dog);
        doNothing().when(dogService).deleteById(dogId);
        when(response.getWriter()).thenReturn(writer);

        dogServlet.doDelete(request, response);

        String expectedJsonResponse = "Собака с ID={}, успешно удалены из базы данных";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);

    }

    @Test
    void doDeleteTest_NoIdParameter() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        dogServlet.doDelete(request, response);

        String expectedJsonResponse = "Не указан ID собаки";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doDeleteTest_InvalidFormatId() throws Exception {
        when(request.getParameter("id")).thenReturn("abc");
        when(response.getWriter()).thenReturn(writer);

        dogServlet.doDelete(request, response);

        String expectedJsonResponse = "Неверный формат ID";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}
