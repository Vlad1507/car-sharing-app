package com.app.carsharing.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.carsharing.dto.rental.AddRentalRequestDto;
import com.app.carsharing.dto.rental.RentalDto;
import com.app.carsharing.model.Rental;
import com.app.carsharing.util.RentalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.Matchers;
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
        "classpath:database/payments/delete_payments.sql",
        "classpath:database/rentals/delete_rentals.sql",
        "classpath:database/roles/delete_roles.sql",
        "classpath:database/users/delete_user.sql",
        "classpath:database/cars/delete_cars.sql",
        "classpath:database/cars/insert_4_cars.sql",
        "classpath:database/roles/insert_roles.sql",
        "classpath:database/users/insert_users.sql",
        "classpath:database/users/insert_users-roles.sql",
        "classpath:database/rentals/insert_6_rentals.sql",
        "classpath:database/payments/insert_4_payments.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {
        "classpath:database/payments/delete_payments.sql",
        "classpath:database/rentals/delete_rentals.sql",
        "classpath:database/roles/delete_roles.sql",
        "classpath:database/users/delete_user.sql",
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RentalControllerTest {
    private static MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Autowired
    public RentalControllerTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Sql(scripts = "classpath:database/rentals/delete_rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/rentals/delete_rentals.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 201 Created status and rental dto")
    void addRental_validRental_statusCreated() throws Exception {
        AddRentalRequestDto requestDto = RentalUtil.getRentalRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        RentalDto expected = RentalUtil.getRentalDto();

        MvcResult result = mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected,actual, "id"));
    }

    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 400 Bad Request status due to invalid data inserted")
    void addRental_invalidRentalDate_statusBadRequest() throws Exception {
        AddRentalRequestDto requestDto = new AddRentalRequestDto(
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 20),
                1L
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        String expected = "must be a date in the present or in the future";

        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(containsString("Bad Request")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {
            "classpath:database/payments/delete_payments.sql",
            "classpath:database/rentals/delete_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/delete_payments.sql",
            "classpath:database/rentals/delete_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 404 Not Found status due to invalid data inserted")
    void addRental_invalidCarId_statusNotFound() throws Exception {
        AddRentalRequestDto requestDto = new AddRentalRequestDto(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                99L
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        String expected = "Car with car id: " + requestDto.carId() + " not found";

        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/rentals/delete_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 200 Ok status and page of dto with elements fit constraints")
    void getRentalsByUserIdAndActivityStatus_validUserIdActiveRentals_statusOk() throws Exception {
        long userId = 1L;
        boolean isActive = true;

        mockMvc.perform(get("/rentals" + "?userId=" + userId + "&isActive=" + isActive))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].rentalStatus")
                        .value("PENDING"))
                .andExpect(jsonPath("$.content[1].rentalStatus")
                        .value("CONFIRMED"))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/rentals/delete_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 200 Ok status and page of dto with elements fit constraints")
    void getRentalsByUserIdAndActivityStatus_validUserIdInactiveRentals_statusOk()
            throws Exception {
        long userId = 1L;
        boolean isActive = false;

        mockMvc.perform(get("/rentals" + "?userId=" + userId + "&isActive=" + isActive))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].rentalStatus")
                        .value("COMPLETED"))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andReturn();
    }

    @Sql(scripts = {
            "classpath:database/payments/delete_payments.sql",
            "classpath:database/rentals/delete_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 200 Ok status and empty page")
    void getRentalsByUserIdAndActivityStatus_noRentals_statusOk() throws Exception {
        long userId = 1L;
        boolean isActive = true;

        mockMvc.perform(get("/rentals" + "?userId=" + userId + "&isActive=" + isActive))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(0))
                .andExpect(jsonPath("$.content").value(Matchers.empty()))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andReturn();
    }

    @WithMockUser(username = "user", roles = {"MANAGER", "ADMIN"})
    @Test
    @DisplayName("Should return 404 Not Found status due to invalid user id")
    void getRentalsByUserIdAndActivityStatus_invalidUserId_statusNotFound() throws Exception {
        long userId = 99L;
        boolean isActive = true;
        String expected = "User by " + userId + " not found";

        mockMvc.perform(get("/rentals" + "?userId=" + userId + "&isActive=" + isActive))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/rentals/delete_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = {"CUSTOMER", "MANAGER"})
    @Test
    @DisplayName("Should return 200 Ok status and rental dto by id")
    void getRentalById_validRentalId_statusOk() throws Exception {
        long rentalId = 1L;
        RentalDto expected = RentalUtil.getRentalDto();

        MvcResult result = mockMvc.perform(get("/rentals/" + rentalId))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "user", roles = {"CUSTOMER", "MANAGER"})
    @Test
    @DisplayName("Should return 404 Not Found status due to invalid rental id")
    void getRentalById_invalidRentalId_statusNotFound() throws Exception {
        long rentalId = 99L;
        String expected = "Rental by rental id: " + rentalId + " not found";

        mockMvc.perform(get("/rentals/" + rentalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/delete_payments.sql",
            "classpath:database/rentals/delete_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 200 Ok status for in-time completed rental dto")
    void endRental_inTimeRental_statusOk() throws Exception {
        RentalDto expected = RentalUtil.getInTimeRentalDto();

        MvcResult result = mockMvc.perform(post("/rentals/" + expected.id() + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalStatus").value(expected.rentalStatus()))
                .andExpect(jsonPath("$.sessionUrl").doesNotExist())
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id", "userId"));
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/delete_payments.sql",
            "classpath:database/rentals/delete_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 200 Ok status for late return rental with session url")
    void endRental_lateReturnRental_statusOk() throws Exception {
        RentalDto expected = RentalUtil.getLateReturnRentalDto();

        MvcResult result = mockMvc.perform(post("/rentals/" + expected.id() + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalStatus").value(expected.rentalStatus()))
                .andExpect(jsonPath("$.sessionUrl").isString())
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "id", "sessionUrl"));
    }

    @WithMockUser(username = "user", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 404 Not Found status due to invalid rental id")
    void endRental_invalidRentalId_statusNotFound() throws Exception {
        long rentalId = 99L;
        String expected = "Rental by rental id: " + rentalId + " not found";

        mockMvc.perform(post("/rentals/" + rentalId + "/return"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = "classpath:database/rentals/insert_6_rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/rentals/delete_rentals.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 409 Conflict status due to already ended rental")
    void endRental_alreadyEndedRental_statusBadRequest() throws Exception {
        long rentalId = 3L;
        String expected = "Rental by id: " + rentalId + " already returned";

        mockMvc.perform(post("/rentals/" + rentalId + "/return"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value(containsString("Rental Failed")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/rentals/delete_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 204 Not Content status "
            + "and delete the pending rental of valid user")
    void cancelPendingRental_validCustomer_statusNoContent() throws Exception {
        long rentalId = 1L;

        mockMvc.perform(delete("/rentals/" + rentalId))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/rentals/delete_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "vo_admin@gmail.com", roles = "ADMIN")
    @Test
    @DisplayName("Should return 204 Not Content status "
            + "and delete the pending rental due to using admin rights")
    void cancelPendingRental_adminAccess_statusNoContent() throws Exception {
        long rentalId = 1L;

        mockMvc.perform(delete("/rentals/" + rentalId))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/rentals/delete_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 409 Conflict status due to inappropriate rental status")
    void cancelPendingRental_invalidRentalStatus_statusConflict() throws Exception {
        long rentalId = 2L;
        Rental.RentalStatus status = Rental.RentalStatus.CONFIRMED;
        String expected = "Rental by id: " + rentalId
                + " cannot be cancelled as it is: " + status;

        mockMvc.perform(delete("/rentals/" + rentalId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value(containsString("Rental Failed")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }

    @Sql(scripts = {"classpath:database/rentals/insert_6_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/rentals/delete_rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "sam1spam@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 403 Forbidden status due to not owning of rental")
    void cancelPendingRental_nonOwnerCustomer_statusForbidden() throws Exception {
        long rentalId = 1L;

        mockMvc.perform(delete("/rentals/" + rentalId))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 404 Not Found status due to invalid rental id")
    void cancelPendingRental_rentalNotFound_statusNotFound() throws Exception {
        long rentalId = 99L;
        String expected = "Rental by rental id: " + rentalId + " not found";

        mockMvc.perform(delete("/rentals/" + rentalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(containsString("Not Found")))
                .andExpect(jsonPath("$.detail").value(containsString(expected)))
                .andReturn();
    }
}
