package com.example.bankapi;

import com.example.bankapi.repository.UserRepository;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransactionRepository;
import com.example.bankapi.idempotency.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IdempotencyGuaranteeTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired IdempotencyRecordRepository idempotencyRecordRepository;

    private String token;
    private Long accountId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up before each test
        idempotencyRecordRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();


        // Register and get token
        String registerBody = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        String response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        // Extract token from response
        token = objectMapper.readTree(response).get("token").asText();

        // Open an account
        String accountResponse = mockMvc.perform(post("/api/v2/accounts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        accountId = objectMapper.readTree(accountResponse).get("id").asLong();

    }
    
    @Test void sameKeySameBodyReplaysResponse() throws Exception {
        String depositBody = """
                {
                    "amount": 100.00
                }
                """;

        // First deposit
        mockMvc.perform(post("/api/v2/accounts/" + accountId + "/deposits")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));

        // Second deposit with same key and body
        mockMvc.perform(post("/api/v2/accounts/" + accountId + "/deposits")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));

        // Verify only ONE transaction exists
        org.junit.jupiter.api.Assertions.assertEquals(1, transactionRepository.count());
    }

    @Test
    void sameKeyDifferentBodyConflicts() throws Exception {
        // First deposit
        mockMvc.perform(post("/api/v2/accounts/" + accountId + "/deposits")
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "k2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 100.00}"))
                .andExpect(status().isOk());

        // Same key, different amount -> 409
        mockMvc.perform(post("/api/v2/accounts/" + accountId + "/deposits")
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", "k2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 200.00}"))
                .andExpect(status().isConflict());

        // Balance should be 100, not 300
        org.junit.jupiter.api.Assertions.assertEquals(1, transactionRepository.count());
    }

    @Test
    void v1HasDeprecationHeadersV2DoesNot() throws Exception {
        // v1 should have deprecation headers
        mockMvc.perform(get("/api/v1/accounts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().exists("Sunset"));

        // v2 should NOT have deprecation headers
        mockMvc.perform(get("/api/v2/accounts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Deprecation"));

    }
}
