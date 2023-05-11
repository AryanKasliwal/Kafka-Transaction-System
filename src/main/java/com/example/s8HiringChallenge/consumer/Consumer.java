package com.example.s8HiringChallenge.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.example.s8HiringChallenge.producer.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
public class Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerManager.class);
    private Consumer() {

    }

    private static Properties getProperties(String groupId) {
        String bootstrapServer = "127.0.0.1:9092";
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
        return properties;
    }


    public static List<Transaction> getValidTransactions(String kafkaTopic, String groupId, String userId, String calendarMonth) {
        List<Transaction> buffer = new ArrayList<>();

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(getProperties(groupId));
        TopicPartition p = new TopicPartition(kafkaTopic, 0);
        long endOffset = consumer.endOffsets(Collections.singletonList(p)).get(p);
        consumer.assign(Collections.singletonList(p));

        int pageSize = 10;
        boolean endReached = false;

        LOGGER.info("INFO: getValidTransactions: Getting valid transactions for the given user and month.");

        try {
            while (!endReached) {
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(consumer.assignment());
                boolean allPartitionsRead = true;
                for (TopicPartition partition : consumer.assignment()) {
                    if (consumer.position(partition) < endOffsets.get(partition)) {
                        allPartitionsRead = false;
                        break;
                    }
                }
                if (allPartitionsRead) {
                    endReached = true;
                    break;
                }
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    Transaction t = Transaction.fromJson(record.value());
                    if (record.offset() == endOffset - 1){
                        LOGGER.info("INFO: getValidTransactions: Reached enf of offset for kafka topic with given groupId.");
                        return buffer;
                    }
                    if (isSameCustomer(t.getAccountIBAN(), userId) && isCorrectDate(t.getValueDate(), calendarMonth)) {
                        buffer.add(t);
//                        LOGGER.info("INFO: getValidTransactions: Found a valid transaction for given customer in the given month.");
                        if (buffer.size() >= pageSize) {
                            endReached = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Caught an error: " + e);
        } finally {
            consumer.close();
        }
        return buffer;
    }

    public static boolean isSameCustomer(String transactionId, String customerId) {
        StringBuilder transactionString = new StringBuilder();
        transactionString.append(transactionId.substring(5, 9));
        transactionString.append(transactionId.substring(10, 14));
        customerId = customerId.substring(2);
        return transactionString.toString().equals(customerId);
    }

    public static boolean isCorrectDate(String transactionDate, String inputDate) {
        return transactionDate.substring(3).equals(inputDate);
    }
}
