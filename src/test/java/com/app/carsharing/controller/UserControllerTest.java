package com.app.carsharing.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.carsharing.dto.user.UpdateRoleRequestDto;
import com.app.carsharing.dto.user.UpdateUserRequestDto;
import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.util.UserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Sql(scripts = {
        "classpath:database/roles/delete_roles.sql",
        "classpath:database/users/delete_user.sql",
        "classpath:database/roles/insert_roles.sql",
        "classpath:database/users/insert_users.sql",
        "classpath:database/users/insert_users-roles.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/roles/delete_roles.sql",
        "classpath:database/users/delete_user.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    private static MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Autowired
    public UserControllerTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Should return 200 Ok status update user role and return user dto")
    void updateUserRole_forValidUser_statusOk() throws Exception {
        UpdateRoleRequestDto updateRoleRequestDto = new UpdateRoleRequestDto("ROLE_MANAGER");
        long userId = 1L;
        UserDto expected = UserUtil.getManagerUserDto();
        String jsonRequest = objectMapper.writeValueAsString(updateRoleRequestDto);

        MvcResult result = mockMvc.perform(patch("/users/" + userId + "/role")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Should return 404 Not Found for a user with non-existent id")
    void updateUserRole_noExistentUser_statusNotFound() throws Exception {
        UpdateRoleRequestDto updateRoleRequestDto = new UpdateRoleRequestDto("ROLE_MANAGER");
        long userId = 99L;
        String expected = "User with id: " + userId + " not found";
        String jsonRequest = objectMapper.writeValueAsString(updateRoleRequestDto);

        mockMvc.perform(patch("/users/" + userId + "/role")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @WithMockUser(username = "alice@gmail.com", roles = {"CONSUMER", "MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 200 Ok status with information about the user")
    void getUserInfo_validUser_statusOk() throws Exception {
        UserDto expected = UserUtil.getUserDto();

        MvcResult result = mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andReturn();

        UserDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "testUser", roles = {"CONSUMER", "MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 404 Not Found for the user "
            + "with non-existent email in the database")
    void getUserInfo__statusNotFound_statusNotFound() throws Exception {
        String expected = "User with email: testUser not found";

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {
            "classpath:database/roles/delete_roles.sql",
            "classpath:database/users/delete_user.sql",
            "classpath:database/roles/insert_roles.sql",
            "classpath:database/users/insert_users.sql",
            "classpath:database/users/insert_users-roles.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = {"CONSUMER", "MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 200 Ok status and update user data first and last name")
    void updateProfileInfo_validUser_statusOk() throws Exception {
        UpdateUserRequestDto requestDto = new UpdateUserRequestDto("Alex", "Stone");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        UserDto expected = UserUtil.getUpdatedUserDto();

        MvcResult result = mockMvc.perform(patch("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "testUser", roles = {"CONSUMER", "MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 404 Not Found due to invalid user email")
    void updateProfileInfo_invalidUser_statusNotFound() throws Exception {
        UpdateUserRequestDto requestDto = new UpdateUserRequestDto("UserName", "LastName");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        String expected = "User with email: testUser not found";

        mockMvc.perform(patch("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }
}
