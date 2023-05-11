package com.example.s8HiringChallenge.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.s8HiringChallenge.producer.Transaction;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.web.client.RestTemplate;


@RestController
public class ConsumerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerManager.class);
    private static String exchangeRateApiKey = "169173f1f7f54f8caa69349514d97c9e";

    public static void main(String [] args) {
        SpringApplication.run(ConsumerManager.class, args);
    }

    @GetMapping("/transactions")
    public static ResponseEntity<List<String>> getTransactions(@RequestParam("sessionId") String groupId, @RequestParam("month") String calendarMonth) {

        String kafkaTopic = "transactions";
        String userId = getUserId(groupId);
        LOGGER.info("INFO: getTransactions: API call received to get a list of transactions");

        List<Transaction> transactions = Consumer.getValidTransactions(kafkaTopic, groupId, userId, calendarMonth);
        List<String> transactionStrings = new ArrayList<>();
        if (transactions.size() == 0) {
            LOGGER.info("INFO: getTransactions: No records found for the given user.");
            transactionStrings.add("No more records found for user: " + userId);
            return new ResponseEntity<>(transactionStrings, HttpStatus.OK);
        }

        // segregatedTransactions[0] = all credit transactions for this page
        // segregatedTransactions[1] = all debit transactions for this page
        List<List<Transaction>> segregatedTransactions = segregateTransactions(transactions);
        Double totalCredit = getCredit(segregatedTransactions.get(0));
        Double totalDebit = getDebit(segregatedTransactions.get(1));
        for (Transaction transaction : transactions) {
            transactionStrings.add(transaction.toString());
        }

        transactionStrings.add("Total credit: " + totalCredit + "Total debit: " + totalDebit);

        return new ResponseEntity<>(transactionStrings, HttpStatus.OK);
    }


    public static List<List<Transaction>> segregateTransactions (List<Transaction> initTransaction) {

        LOGGER.info("INFO: segregateTransactions: Segregating the list of 10 transactions based on credit and debit.");

        List<Transaction> creditTransactions = new ArrayList<>();
        List<Transaction> debitTransactions = new ArrayList<>();
        List<List<Transaction>> segregatedTransactions = new ArrayList<>();
        for (Transaction t : initTransaction) {
            String amount = t.getAmount();
            if (amount.contains("-")){
                debitTransactions.add(t);
            }
            else {
                creditTransactions.add(t);
            }
        }
        segregatedTransactions.add(creditTransactions);
        segregatedTransactions.add(debitTransactions);
        return  segregatedTransactions;
    }

    private static String getUserId(String sessionId) {

        String [] parts = sessionId.split("_");
        return parts[0];
    }


    public static Double getCredit(List<Transaction> transactions) {

        LOGGER.info("INFO: getCredit: calculating total credit value for this page.");

        JsonNode rates = getExchangeRate();
        double creditValue = 0.0;
        for (Transaction t : transactions) {
            String amount = t.getAmount();
            String currency = amount.substring(0, 3);
            double value = Double.parseDouble(amount.substring(3));
            creditValue += value / rates.get(currency).asDouble();
        }
        return creditValue;
    }

    public static Double getDebit(List<Transaction> transactions) {

        LOGGER.info("INFO: getDebit: calculating total debit value for this page.");

        JsonNode rates = getExchangeRate();
        double debitValue = 0.0;
        for (Transaction t : transactions) {
            String amount = t.getAmount();
            String currency = amount.substring(0, 3);
            double value = Double.parseDouble(amount.substring(3, amount.length() - 1));
            debitValue += value / rates.get(currency).asDouble();
        }
        return debitValue;
    }

    public static JsonNode getExchangeRate() {

        String url = "https://api.currencyfreaks.com/v2.0/rates/latest?apikey=" + exchangeRateApiKey + "&symbols=CHF,GBP,EUR,USD";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        LOGGER.info("INFO: getExchangeRate: Getting the current exchange rate from a third party API.");

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );
        JsonNode rates = null;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LOGGER.info("INFO: getExchangeRate: Extracting the exchange rate from the received response object from the API.");
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            rates = jsonNode.get("rates");

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return rates;
    }

}
