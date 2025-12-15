package com.app.carsharing.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.carsharing.dto.payment.CreatePaymentRequestSessionDto;
import com.app.carsharing.dto.payment.PaymentDto;
import com.app.carsharing.model.Payment;
import com.app.carsharing.model.User;
import com.app.carsharing.util.PaymentUtil;
import com.app.carsharing.util.UserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
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
        "classpath:database/users/delete_user.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {
    private static MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Autowired
    public PaymentControllerTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 200 OK status and page of payments dto")
    void getPayments_customerAccessOwner_statusOk() throws Exception {
        User user = UserUtil.getExistedUser();

        mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andReturn();
    }

    @WithMockUser(username = "vo_admin@gmail.com", roles = "ADMIN")
    @Test
    @DisplayName("Should return 200 OK status and page of payments dto of user by id"
            + " for user with admin rights")
    void getPayments_adminAccess_statusOk() throws Exception {
        User user = UserUtil.getExistedUser();

        mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andReturn();
    }

    @WithMockUser(username = "sam1spam@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 403 Forbidden status due to lack of rights")
    void getPayments_customerNotOwner_statusForbidden() throws Exception {
        User user = UserUtil.getExistedUser();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String expectedMessage = "User with email " + authentication.getName()
                + " has no rights to access those payments";

        mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(user.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Payment Access Denied"))
                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andReturn();
    }

    @WithAnonymousUser
    @Test
    @DisplayName("Should return 401 Unauthorized due to user is unauthenticated")
    void getPayments_invalidUserId_statusUnauthorized() throws Exception {
        long userId = 1L;
        String expectedMessage = "Authentication Failed "
                + "Full authentication is required to access this resource";

        mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andReturn();
    }

    @Sql(scripts = "classpath:database/payments/delete_payments.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/payments/delete_payments.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 201 Created status "
            + "when a valid user creates a session with valid data")
    void createPaymentSession_validCustomer_statusOk() throws Exception {
        CreatePaymentRequestSessionDto requestSessionDto = new CreatePaymentRequestSessionDto(
                "payment",
                1L
        );
        String jsonRequest = objectMapper.writeValueAsString(requestSessionDto);

        MvcResult result = mockMvc.perform(post("/payments")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        PaymentDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaymentDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertNotNull(actual.sessionUrl());
        assertNotNull(actual.sessionId());
        PaymentDto expected = PaymentUtil.getPendingPaymentDto();
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual,
                "id", "sessionUrl", "sessionId", "amountToPay"));
        assertEquals(0, expected.amountToPay().compareTo(actual.amountToPay()));
    }

    @WithMockUser(username = "sam1spam@gmail.com", roles = "MANAGER")
    @Test
    @DisplayName("Should return 403 Forbidden status "
            + "when non-Consumer role tries to create a payment session")
    void createPaymentSession_nonConsumerRole_statusForbidden() throws Exception {
        String expectedMessage = "Access is denied. No permission for this action";
        CreatePaymentRequestSessionDto requestSessionDto =
                new CreatePaymentRequestSessionDto("payment", 1L);
        String jsonRequest = objectMapper.writeValueAsString(requestSessionDto);

        mockMvc.perform(post("/payments")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andReturn();
    }

    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 400 Bad Request when a request body contains invalid data")
    void createPaymentSession_invalidData_statusBadRequest() throws Exception {
        CreatePaymentRequestSessionDto requestSessionDto = new CreatePaymentRequestSessionDto(
                "DEBT",
                1L
        );
        String expectedMessage = "Unsupported or invalid payment type: "
                + requestSessionDto.paymentType();
        String jsonRequest = objectMapper.writeValueAsString(requestSessionDto);

        mockMvc.perform(post("/payments")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Payment Type Error"))
                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andReturn();
    }

    @Sql(scripts = "classpath:database/payments/insert_4_payments.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/payments/delete_payments.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 307 Temporary Redirect status and payment dto with status paid")
    void getSuccessfulPayments_validSessionId_statusTemporaryRedirect() throws Exception {
        PaymentDto expected = PaymentUtil.getSuccessfulRegularPaymentDto();
        String paramSessionId = expected.sessionId();

        MvcResult result = mockMvc.perform(get("/payments/successful")
                        .param("session_id", paramSessionId))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        PaymentDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), PaymentDto.class);
        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "amountToPay"));
        assertEquals(0, expected.amountToPay().compareTo(actual.amountToPay()));
    }

    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 404 Not Found status due to invalid session id")
    void getSuccessfulPayments_invalidSessionId_statusNotFound() throws Exception {
        String invalidSessionId = UUID.randomUUID().toString();
        String expectedMessage = "No payment found for sessionId: " + invalidSessionId;

        mockMvc.perform(get("/payments/successful")
                        .param("session_id", invalidSessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Payment Not Found"))
                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andReturn();
    }

    @Sql(scripts = "classpath:database/payments/insert_4_payments.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/payments/delete_payments.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 409 Conflict status due to inappropriate payment status")
    void getSuccessfulPayments_alreadyPaid_statusConflict() throws Exception {
        String sessionId = "cs_test_session67890";
        String expectedMessage = "Payment status: "
                + Payment.Status.PAID + " cannot continue";

        mockMvc.perform(get("/payments/successful")
                        .param("session_id", sessionId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment Failed"))
                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andReturn();
    }

    @Sql(scripts = "classpath:database/payments/insert_4_payments.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/payments/delete_payments.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 307 Temporary Redirect status "
            + "and payment dto with status canceled")
    void getCanceledPayments_validSessionId_statusTemporaryRedirect() throws Exception {
        PaymentDto expected = PaymentUtil.getCancelledPaymentDto();
        String paramSessionId = expected.sessionId();

        MvcResult result = mockMvc.perform(get("/payments/canceled")
                        .param("session_id", paramSessionId))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        PaymentDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), PaymentDto.class);
        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "amountToPay"));
        assertEquals(0, expected.amountToPay().compareTo(actual.amountToPay()));
    }

    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 404 Not Found status due to invalid session id")
    void getCanceledPayments_invalidSessionId_statusNotFound() throws Exception {
        String invalidSessionId = UUID.randomUUID().toString();
        String expectedMessage = "No payment found for sessionId: " + invalidSessionId;

        mockMvc.perform(get("/payments/canceled")
                        .param("session_id", invalidSessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Payment Not Found"))
                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andReturn();
    }

    @Sql(scripts = "classpath:database/payments/insert_4_payments.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/payments/delete_payments.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "alice@gmail.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Should return 409 Conflict status due to inappropriate payment status")
    void getCanceledPayments_alreadyProcessed_statusConflict() throws Exception {
        String sessionId = "cs_test_session67890";
        String expectedMessage = "Payment status: "
                + Payment.Status.PAID + " cannot cancel payment";

        mockMvc.perform(get("/payments/canceled")
                        .param("session_id", sessionId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment Failed"))
                .andExpect(jsonPath("$.detail").value(expectedMessage))
                .andReturn();
    }
}
