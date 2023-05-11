package com.example.s8HiringChallenge.producer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import com.google.gson.Gson;

public class Transaction {
    private String uniqueID;
    private String amount;
    private String accountIBAN;
    private String valueDate;
    private String description;

    public Transaction(String uniqueID, String amount, String accountIBAN, String valueDate, String description) {
        this.uniqueID = uniqueID;
        this.amount = amount;
        this.accountIBAN = accountIBAN;
        this.valueDate = valueDate;
        this.description = description;
    }

    public String getUniqueId() {
        return this.uniqueID;
    }

    @JsonProperty("amount")
    public String getAmount() {
        return this.amount;
    }

    @JsonProperty("accountIBAN")
    public String getAccountIBAN() {
        return this.accountIBAN;
    }

    @JsonProperty("valueDate")
    public String getValueDate() {
        return this.valueDate;
    }

    @JsonProperty("description")
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueID, amount, accountIBAN, valueDate, description);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("uniqueID:'").append(this.uniqueID).append('\'');
        sb.append(", amount:'").append(this.amount).append('\'');
        sb.append(", accountIBAN:'").append(this.accountIBAN).append('\'');
        sb.append(", valueData:'").append(this.valueDate).append('\'');
        sb.append(", description:'").append(this.description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static Transaction fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Transaction.class);
    }
}
