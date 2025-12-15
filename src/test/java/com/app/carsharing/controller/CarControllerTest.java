package com.app.carsharing.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.carsharing.dto.car.CarDto;
import com.app.carsharing.dto.car.CarListDto;
import com.app.carsharing.dto.car.CreateCarRequestDto;
import com.app.carsharing.dto.car.UpdateCarRequestDto;
import com.app.carsharing.util.CarUtil;
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

@Sql(scripts = {"classpath:database/cars/delete_cars.sql",
        "classpath:database/cars/insert_4_cars.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:database/cars/delete_cars.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarControllerTest {
    private static MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Autowired
    public CarControllerTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username = "manager", roles = "MANAGER")
    @Test
    @DisplayName("Should return 201 Created status and return dto of added car")
    void addCar_validCarPositiveQuantity_statusCreated() throws Exception {
        CreateCarRequestDto carRequestDto = CarUtil.getCreateCarDto();
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);
        CarDto expected = CarUtil.getCarDto();

        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "manager", roles = "MANAGER")
    @Test
    @DisplayName("Should return 400 Bad Request status due to invalid quantity of cars")
    void addCar_invalidCarQuantity_statusBadRequest() throws Exception {
        CreateCarRequestDto carRequestDto = CarUtil.getCreateCarInvalidQuantityDto();
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);
        String expected = "quantity: must be greater than 0";

        mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/cars/delete_cars.sql",
            "classpath:database/cars/insert_4_cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/cars/delete_cars.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"CUSTOMER", "MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 200 Ok status and all cars from database")
    void getAllCars_receiveCars_statusOk() throws Exception {
        CarListDto expected = CarUtil.getCarListDto();

        mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("size", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(expected.id()))
                .andExpect(jsonPath("$.content[0].model").value(expected.model()))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/cars/delete_cars.sql",
            "classpath:database/cars/insert_4_cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/cars/delete_cars.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"CUSTOMER", "MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 200 Ok status and car by it id")
    void getCarInfo_validCarId_statusOk() throws Exception {
        CarDto expected = CarUtil.getCarDto();
        long id = 1L;

        MvcResult result = mockMvc.perform(get("/cars/" + id))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals(0, expected.dailyFee().compareTo(actual.dailyFee()));
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id", "dailyFee"));
    }

    @WithMockUser(username = "user", roles = {"CUSTOMER", "MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 404 Not Found due to invalid car id")
    void getCarInfo_invalidCarId_statusNotFound() throws Exception {
        long id = 99L;
        String expected = "Car with carId: " + id + " is not found";

        mockMvc.perform(get("/cars/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(expected))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/cars/delete_cars.sql",
            "classpath:database/cars/insert_4_cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/cars/delete_cars.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return status 200 Ok and update existed car")
    void updateCarById_validCarId_statusOk() throws Exception {
        UpdateCarRequestDto carRequestDto = CarUtil.getUpdateCarRequestDto();
        long carId = 1;
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);
        CarDto expected = CarUtil.getUpdatedCarDto();

        MvcResult result = mockMvc.perform(patch("/cars/" + carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult verifyResult = mockMvc.perform(get("/cars/" + expected.id()))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CarDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        CarDto verifyActual = objectMapper.readValue(
                verifyResult.getResponse().getContentAsString(),
                CarDto.class);
        assertEquals(0, expected.dailyFee().compareTo(actual.dailyFee()));
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
        assertTrue(EqualsBuilder.reflectionEquals(expected, verifyActual, "id", "dailyFee"));
    }

    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return status 404 Not Found due to invalid car id")
    void updateCarById_invalidCarId_statusNotFound() throws Exception {
        UpdateCarRequestDto carRequestDto = CarUtil.getUpdateCarRequestDto();
        long id = 99L;
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);
        String expected = "Car with carId: " + id + " is not found";

        mockMvc.perform(patch("/cars/" + id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(expected))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/cars/delete_cars.sql",
            "classpath:database/cars/insert_4_cars.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/cars/delete_cars.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return status 204 No Content and delete car by id")
    void deleteCar_validCarId_statusNoContent() throws Exception {
        long id = 1L;

        mockMvc.perform(delete("/cars/" + id))
                .andExpect(status().isNoContent())
                .andReturn();
        mockMvc.perform(get("/cars/" + id))
                .andExpect(status().isNotFound()).andReturn();
    }

    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return status 404 Not Found due to invalid car id")
    void deleteCar_invalidCarId_statusNotFound() throws Exception {
        long id = 99L;
        String expected = "Car with carId: " + id + " is not found";

        mockMvc.perform(delete("/cars/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(expected))
                .andReturn();
    }
}
