package ru.doggohub.servlet;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.doggohub.dto.user.UserRequestDto;
import ru.doggohub.dto.user.UserResponseDto;
import ru.doggohub.exception.NotFoundException;
import ru.doggohub.exception.ValidationException;
import ru.doggohub.mapper.UserMapper;
import ru.doggohub.model.User;
import ru.doggohub.repository.DogRepository;
import ru.doggohub.repository.UserRepository;
import ru.doggohub.service.user.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserServletTest {
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    UserRepository userRepository;
    @Mock
    DogRepository dogRepository;
    @Mock
    UserService userService;
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter writer = new PrintWriter(stringWriter);
    private final Gson gson = new Gson();
    private final long userId = 1L;
    private final MockHttpServletRequest inputStream = new MockHttpServletRequest();

    UserServlet userServlet;
    UserRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userServlet = new UserServlet(userRepository, dogRepository);
        requestDto = UserRequestDto.builder().name("Borya").email("y2y@s.ru").build();

    }

    @Test
    void doGetTest_AllUsers_Success() throws IOException {
        when(response.getWriter()).thenReturn(writer);

        when(request.getParameter("id")).thenReturn(null);

        List<User> users = new ArrayList<>();
        users.add(User.builder().id(1L).name("Borya").email("email").build());
        users.add(User.builder().id(2L).name("Toma").email("email1").build());

        when(userRepository.findAll()).thenReturn(users);

        List<UserResponseDto> dtoList = users.stream().map(UserMapper::toDto).collect(Collectors.toList());

        when(userService.getAll()).thenReturn(dtoList);

        userServlet.doGet(request, response);

        String expectedJsonResponse = gson.toJson(dtoList);
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doGetTest_ById_Success() throws IOException {
        User user = User.builder().id(userId).name("Borya").email("email").build();
        UserResponseDto dto = UserMapper.toDto(user);

        when(userRepository.findById(userId)).thenReturn(user);
        when(userService.getById(userId)).thenReturn(dto);

        when(response.getWriter()).thenReturn(writer);
        when(request.getParameter("id")).thenReturn(String.valueOf(userId));

        userServlet.doGet(request, response);

        String expectedJsonResponse = gson.toJson(dto);
        String actualJsonResponse = stringWriter.toString().trim();

        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doGetTest_ById_InvalidUserIdFormat() throws IOException {
        when(request.getParameter("id")).thenReturn("invalid");

        when(response.getWriter()).thenReturn(writer);

        userServlet.doGet(request, response);

        String exceptedException = "Неверный формат ID";
        String actual = stringWriter.toString().trim();

        assertEquals(exceptedException, actual);
    }

    @Test
    void doPostTest_Success() throws IOException {
        User user = UserMapper.fromDto(requestDto);
        user.setId(userId);
        UserResponseDto responseDto = UserMapper.toDto(user);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userService.addUser(requestDto)).thenReturn(responseDto);

        String req = gson.toJson(requestDto);
        inputStream.setContent(req.getBytes());
        when(request.getInputStream()).thenReturn(inputStream.getInputStream());

        when(response.getWriter()).thenReturn(writer);

        userServlet.doPost(request, response);

        String expectedJsonResponse = gson.toJson(responseDto);
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPostTest_InvalidRequest() throws IOException {
        UserRequestDto requestDto1 = UserRequestDto.builder().name("Borya").build();

        String invalidRequestBody = gson.toJson(requestDto1);

        when(userService.addUser(requestDto1)).thenThrow(ValidationException.class);

        inputStream.setContent(invalidRequestBody.getBytes());
        when(request.getInputStream()).thenReturn(inputStream.getInputStream());

        when(response.getWriter()).thenReturn(writer);

        userServlet.doPost(request, response);

        String exceptedException = "При обработке запроса произошла ошибка: Эл почта пользователя должна быть заполнена";
        assertEquals(exceptedException, stringWriter.toString().trim());
    }

    @Test
    void doPostTest_ThrowException() throws IOException {
        when(userService.addUser(requestDto)).thenThrow(new RuntimeException("Internal server error"));

        String requestBody = gson.toJson(requestDto);
        inputStream.setContent(requestBody.getBytes());
        when(request.getInputStream()).thenReturn(inputStream.getInputStream());

        when(response.getWriter()).thenReturn(writer);

        userServlet.doPost(request, response);

        assertTrue(stringWriter.toString().contains("При обработке запроса произошла ошибка"));
    }

    @Test
    void doPatchTest_Successful() throws Exception {
        User user = UserMapper.fromDto(requestDto);
        user.setId(userId);

        UserRequestDto updateRequestDto = UserRequestDto.builder().name("Update name").build();
        User updateUser = UserMapper.fromDto(updateRequestDto);
        updateUser.setId(userId);
        UserResponseDto expectedUser = UserMapper.toDto(updateUser);

        when(userRepository.findById(userId)).thenReturn(user);
        when(userRepository.update(any(User.class))).thenReturn(updateUser);
        when(userService.updateUser(any(UserRequestDto.class), any(Long.class))).thenReturn(expectedUser);

        String req = gson.toJson(updateRequestDto);
        inputStream.setContent(req.getBytes());

        when(request.getParameter("id")).thenReturn(String.valueOf(userId));
        when(request.getInputStream()).thenReturn(inputStream.getInputStream());
        when(response.getWriter()).thenReturn(writer);

        userServlet.doPatch(request, response);

        String expectedJsonResponse = gson.toJson(expectedUser);
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPatchTest_MissingId() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        userServlet.doPatch(request, response);

        String expectedJsonResponse = "Параметр id не передан, проверьте запрос";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }

    @Test
    void doPatchTest_NotFoundUser_ThrowException() throws Exception {
        when(request.getParameter("id")).thenReturn(String.valueOf(userId));
        when(userService.updateUser(any(UserRequestDto.class), any(Long.class))).thenThrow(new NotFoundException("Пользователь с ID={} не найден"));

        String req = gson.toJson(requestDto);
        inputStream.setContent(req.getBytes());

        when(request.getInputStream()).thenReturn(inputStream.getInputStream());

        when(response.getWriter()).thenReturn(writer);

        userServlet.doPatch(request, response);

        String expectedJsonResponse = "При обработке запроса произошла ошибка: Пользователь с ID={} не найден";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
    }


    @Test
    void doDeleteTest_Success() throws Exception {
        User user = User.builder().id(userId).build();

        when(request.getParameter("id")).thenReturn(String.valueOf(userId));
        when(userRepository.findById(userId)).thenReturn(user);
        doNothing().when(userService).deleteById(userId);
        when(response.getWriter()).thenReturn(writer);

        userServlet.doDelete(request, response);

        String expectedJsonResponse = "Пользователь с ID={}, успешно удален из базы данных";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);

    }

    @Test
    void doDeleteTest_NoIdParameter() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        userServlet.doDelete(request, response);

        String expectedJsonResponse = "Не указан ID пользователя";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void doDeleteTest_InvalidFormatId() throws Exception {
        when(request.getParameter("id")).thenReturn("abc");
        when(response.getWriter()).thenReturn(writer);

        userServlet.doDelete(request, response);

        String expectedJsonResponse = "Неверный формат ID";
        String actualJsonResponse = stringWriter.toString().trim();
        assertEquals(expectedJsonResponse, actualJsonResponse);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}