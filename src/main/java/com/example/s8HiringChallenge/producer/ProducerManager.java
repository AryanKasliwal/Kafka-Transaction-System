package com.example.s8HiringChallenge.producer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProducerManager {
    private static final List<String> currencies = Arrays.asList("GBP", "EUR", "CHF");
    private static Random r = new Random();
    private static LocalDate localDate = LocalDate.now();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static List <String> action = Arrays.asList("-", "");
    private static String chosenCurrency = "";
    private static final Producer producer = Producer.getProducerInstance();
    private static final String kafkaTopic = "transactions";


    public static void main(String [] args) {

        List <String> accountIbans = getAccountList();
        Transaction testT = null;
        int count = 0;

        for (String account: accountIbans) {
            for (int i = 365; i >= 0; i--) {
                for (int j = 0; j < 2; j++) {
                    testT = new Transaction(
                            randomUniqueId(),
                            randomAmount(),
                            account,
                            randomDate(i),
                            randomDescription()
                    );
                    producer.kafkaSendData(kafkaTopic, testT);
                    count ++;
                    System.out.println("Transactions created: " + count);
                }
            }
        }

    }


    public static String randomUniqueId() {

        return UUID.randomUUID().toString();
    }

    //Generates random amounts in three different currencies [GPB, EUR, CHF] and values in range[-200 to 200]
    public static String randomAmount() {

        int currencyH = 3;
        int currencyL = 0;
        int currencyResult = r.nextInt(currencyH - currencyL) + currencyL;
        chosenCurrency = currencies.get(currencyResult);
        int amountH = 201;
        int amountL = 0;
        int amountResult = r.nextInt(amountH - amountL) + amountL;
        int actionH = 2;
        int actionL = 0;
        int actionResult = r.nextInt(actionH - actionL) + actionL;

        StringBuilder s = new StringBuilder();
        s.append(currencies.get(currencyResult));
        s.append(Integer.toString(amountResult));
        s.append(action.get(actionResult));
        return s.toString();
    }

    // Generates random account numbers in the range(CH93-0000-0000-0000-0000-0 to CH93-9999-9000-0000-0000-0)
    public static List<String> getAccountList() {

        List <String> accountNumbers = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            StringBuilder sFinal = new StringBuilder();
            StringBuilder sTemp = new StringBuilder();
            sFinal.append("CH93-");
            String temp = Integer.toString(i);
            sTemp.append(temp);
            while (sTemp.length() < 8) {
                sTemp.append("0");
            }
            sFinal.append(sTemp.toString().substring(0, 4));
            sFinal.append("-");
            sFinal.append(sTemp.toString().substring(4));
            sFinal.append("-0000-0000-0");
            accountNumbers.add(sFinal.toString());
        }
        return accountNumbers;
    }


    public static String randomDate(int daysToSubtract) {

        if (daysToSubtract == 365) {
            daysToSubtract = 0;
        }
        LocalDate date = localDate.minusDays(daysToSubtract);
        String formattedDate = date.format(formatter);
        return formattedDate;
    }


    public static String randomDescription() {

        StringBuilder s = new StringBuilder("Online payment ");
        s.append(chosenCurrency);
        return s.toString();
    }
}
