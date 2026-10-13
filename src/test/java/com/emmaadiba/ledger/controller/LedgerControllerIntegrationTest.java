package com.emmaadiba.ledger.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full stack: HTTP request -> controller -> service -> JPA -> H2 database.
 * Each test uses its own account id, so tests can share one database without interfering.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LedgerControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private void postTxn(String json, int expectedStatus) throws Exception {
        mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void createsTransactionsAndReportsBalance() throws Exception {
        postTxn("""
                {"accountId":"ACC1","amount":"250.00","type":"CREDIT","description":"paycheck"}""", 201);
        postTxn("""
                {"accountId":"ACC1","amount":"75.25","type":"DEBIT","description":"groceries"}""", 201);

        mvc.perform(get("/api/accounts/ACC1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(174.75));

        mvc.perform(get("/api/transactions").param("accountId", "ACC1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].runningBalance").value(250.00))
                .andExpect(jsonPath("$[1].runningBalance").value(174.75));
    }

    @Test
    void overdraftReturns422() throws Exception {
        postTxn("""
                {"accountId":"ACC2","amount":"10.00","type":"DEBIT"}""", 422);

        mvc.perform(get("/api/accounts/ACC2/balance"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void invalidRequestsReturn400() throws Exception {
        postTxn("""
                {"accountId":"","amount":"-5","type":"CREDIT"}""", 400);
        postTxn("""
                {"accountId":"ACC3","amount":"5.00","type":"REFUND"}""", 400);
        postTxn("""
                {"accountId":"ACC3","amount":"5.001","type":"CREDIT"}""", 400);
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
