package com.app.carsharing.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.dto.user.UserLoginRequestDto;
import com.app.carsharing.dto.user.UserLoginResponseDto;
import com.app.carsharing.dto.user.UserRegistrationRequestDto;
import com.app.carsharing.security.JwtUtil;
import com.app.carsharing.util.UserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    private static MockMvc mockMvc;

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthenticationControllerTest(ObjectMapper objectMapper, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
    }

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Sql(scripts = {"classpath:database/users/delete_user.sql",
            "classpath:database/roles/insert_roles.sql" },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/roles/delete_roles.sql",
            "classpath:database/users/delete_user.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @DisplayName("Should return 201 Created register user with provided valid email")
    void register_userWithValidEmail_statusCreated() throws Exception {
        UserDto expected = UserUtil.getUserDto();
        UserRegistrationRequestDto requestDto = UserUtil.getUserRegistrationRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/auth/registration")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        UserDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request due to invalid user email")
    void register_userWithInvalidEmail_statusBadRequest() throws Exception {
        UserRegistrationRequestDto requestDto = UserUtil.getRequestDtoWithInvalidEmail();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/auth/registration")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value(equalToIgnoringCase("bad request")))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail")
                        .value(containsString("email: must be a well-formed email address")))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/users/insert_users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/users/delete_user.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @DisplayName("Should return 409 Conflict because user already exist with that email")
    void register_userAlreadyExists_statusConflict() throws Exception {
        UserRegistrationRequestDto requestDto = UserUtil.getUserRegistrationRequestDto();
        String expected = "User with email: " + requestDto.email() + " is already exist";
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/auth/registration")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(equalToIgnoringCase("Registration Failed")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Test
    @DisplayName("Should return 400 Bad Request because password requirements are not met")
    void register_invalidPassword_statusBadRequest() throws Exception {
        UserRegistrationRequestDto requestDto = UserUtil.getRequestDtoInvalidPassword();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/auth/registration")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(equalToIgnoringCase("Bad Request")))
                .andExpect(jsonPath("$.detail")
                        .value(containsString("password: Invalid Password")))
                .andReturn();
    }

    @Sql(scripts = {
            "classpath:database/roles/delete_roles.sql",
            "classpath:database/users/delete_user.sql",
            "classpath:database/roles/insert_roles.sql",
            "classpath:database/users/insert_users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/users/delete_user.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @DisplayName("Should return status 200 Ok and user login token")
    void login_existedUser_statusOk() throws Exception {
        UserLoginRequestDto requestDto = UserUtil.getLoginRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserLoginResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserLoginResponseDto.class);
        assertNotNull(actual);
        assertNotNull(actual.token());
        assertFalse(actual.token().isEmpty());
        assertTrue(jwtUtil.isTokenValid(actual.token()));

    }

    @Test
    @DisplayName("Should return 401 Unauthorized for login with no registered user email")
    void login_nonExistentUser_statusUnauthorized() throws Exception {
        UserLoginRequestDto requestDto = UserUtil.getNoExistLoginRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/auth/login")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(equalToIgnoringCase("Unauthorized")))
                .andExpect(jsonPath("$.detail").value(containsString("Authentication Failed")))
                .andReturn();
    }
}
