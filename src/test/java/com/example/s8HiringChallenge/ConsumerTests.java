package com.example.s8HiringChallenge;

import com.example.s8HiringChallenge.consumer.Consumer;
import com.example.s8HiringChallenge.producer.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class ConsumerTests {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // isSameCustomer method takes account IBAN and customer ID as parameters.
    // Returns true if a customer ID is same as account IBAN
    @Test
    void testIsSameCustomer() {
        String accountIban = "CH93-1234-0000-0000-0000-0";
        String customerId = "P-12340000";

        boolean result = Consumer.isSameCustomer(accountIban, customerId);

        assertTrue(result);
    }

    // Returns false if a customer ID is not same as account IBAN
    @Test
    void testIsNotSameCustomer() {
        String accountIban = "CH93-1234-0000-0000-0000-0";
        String customerId = "P-12350000";

        boolean result = Consumer.isSameCustomer(accountIban, customerId);

        assertFalse(result);
    }

    // isCorrectDate method takes a transaction date and month-year as parameters
    // Returns true if the date of transaction is in the same month as input month
    @Test
    void testIsCorrectDate() {
        String transactionDate = "23-01-2023";
        String inputDate = "01-2023";

        boolean result = Consumer.isCorrectDate(transactionDate, inputDate);

        assertTrue(result);
    }

    // Returns false if the date of transaction is in the same month as input month
    @Test
    void testIsNotCorrectDate() {
        String transactionDate = "23-01-2023";
        String inputDate = "03-2023";

        boolean result = Consumer.isCorrectDate(transactionDate, inputDate);

        assertFalse(result);
    }

    @Test
    void testGetValidTransactions() {
        String kafkaTopic = "transactions";
        String groupId = "P-10000000_06-05-2023-03-29";
        String userId = "P-10000000";
        String calendarMonth = "03-2023";

        List<Transaction> transactions = Consumer.getValidTransactions(kafkaTopic, groupId, userId, calendarMonth);

        assertTrue(transactions.size() <= 10); // Assuming pageSize is 10
        for (Transaction transaction : transactions) {
            assertTrue(Consumer.isSameCustomer(transaction.getAccountIBAN(), userId));
            assertTrue(Consumer.isCorrectDate(transaction.getValueDate(), calendarMonth));
        }
    }

}
