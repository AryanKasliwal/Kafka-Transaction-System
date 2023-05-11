package com.example.s8HiringChallenge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.s8HiringChallenge.consumer.ConsumerManager;
import com.example.s8HiringChallenge.producer.ProducerManager;
import com.example.s8HiringChallenge.producer.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumerManagerTests {
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    public List<Transaction> createDummyTransactions() {
        List<Transaction> creditTransactions = new ArrayList<>();
        Transaction t = null;
        for (int i = 0; i < 10; i++) {
            t = new Transaction(
                    ProducerManager.randomUniqueId(),
                    ProducerManager.randomAmount(),
                    "CH93-1200-0000-0000-0000-0",
                    ProducerManager.randomDate(i),
                    ProducerManager.randomDescription()
            );
            if (!t.getAmount().contains("-")) {
                creditTransactions.add(t);
            }
        }
        return creditTransactions;
    }


    @Test
    public void testGetTransactions() {
        String sessionId = "P-12000000_09-05-2023-13-44";
        String calendarMonth = "03-2023";

        ResponseEntity<List<String>> response = ConsumerManager.getTransactions(sessionId, calendarMonth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<String> transactionStrings = response.getBody();
        assertTrue(transactionStrings.size() <= 11); // Assuming only one transaction returned
    }


    @Test
    public void testSegregateTransactions() {
        List<Transaction> transactions = createDummyTransactions();
        List<List<Transaction>> realSegregatedTransactions = ConsumerManager.segregateTransactions(transactions);

        List<Transaction> creditTransactions = new ArrayList<>();
        List<Transaction> debitTransactions = new ArrayList<>();
        List<List<Transaction>> testSegregatedTransactions = new ArrayList<>();
        for (Transaction t : transactions) {
            String amount = t.getAmount();
            if (amount.contains("-")){
                debitTransactions.add(t);
            }
            else {
                creditTransactions.add(t);
            }
        }
        testSegregatedTransactions.add(creditTransactions);
        testSegregatedTransactions.add(debitTransactions);
        assertEquals(realSegregatedTransactions.size(), testSegregatedTransactions.size());
        for (int i = 0; i < realSegregatedTransactions.size(); i++) {
            assertEquals(realSegregatedTransactions.get(i).size(), testSegregatedTransactions.get(i).size());
            for (int j = 0; j < realSegregatedTransactions.get(i).size(); j++) {
                assertEquals(realSegregatedTransactions.get(i).get(j).toString(), testSegregatedTransactions.get(i).get(j).toString());
            }
        }
    }

    @Test
    public void testGetCredit() {
        // Mock the necessary parameters for the test
        List<Transaction> creditTransactions = createDummyTransactions();
        Double realCreditValue = ConsumerManager.getCredit(creditTransactions);

        JsonNode rates = ConsumerManager.getExchangeRate();
        double testCreditValue = 0.0;
        for (Transaction transaction : creditTransactions) {
            String amount = transaction.getAmount();
            String currency = amount.substring(0, 3);
            double value = Double.parseDouble(amount.substring(3));
            testCreditValue += value / rates.get(currency).asDouble();
        }
        assertEquals(testCreditValue, realCreditValue);
    }

    @Test
    public void testGetDebit() {
        List<Transaction> debitTransactions = createDummyTransactions();

        // Call the method under test
        Double realDebitValue = ConsumerManager.getDebit(debitTransactions);

        JsonNode rates = ConsumerManager.getExchangeRate();
        double testCreditValue = 0.0;
        for (Transaction transaction : debitTransactions) {
            String amount = transaction.getAmount();
            String currency = amount.substring(0, 3);
            double value = Double.parseDouble(amount.substring(3, amount.length() - 1));
            testCreditValue += value / rates.get(currency).asDouble();
        }
        assertEquals(testCreditValue, realDebitValue);
    }

    // API contract test
    @Test
    public void shouldReturnExpectedExchangeRates() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        TestRestTemplate restTemplate = new TestRestTemplate();


        String url = "https://api.currencyfreaks.com/v2.0/rates/latest?apikey=169173f1f7f54f8caa69349514d97c9e&symbols=CHF,GBP,EUR,USD";

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );


        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(response.getHeaders().getContentType(), MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode rates = jsonNode.get("rates");

        assertNotNull(rates);
        assertEquals(rates.size(), 4);

        Iterator<Map.Entry<String, JsonNode>> fields = rates.fields();
        assertEquals(fields.next().getKey(), "CHF");
        assertEquals(fields.next().getKey(), "EUR");
        assertEquals(fields.next().getKey(), "GBP");
        assertEquals(fields.next().getKey(), "USD");
    }

}
