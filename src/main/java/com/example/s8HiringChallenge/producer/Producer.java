package com.example.s8HiringChallenge.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {
    private static KafkaProducer <String, String> producer;
    private static Producer producerInstance = null;


    public static Producer getProducerInstance() {
        if (producerInstance == null) {
            producerInstance = new Producer();
            producerInstance.createKafkaProducer();
        }
        return producerInstance;
    }


    private void createKafkaProducer() {
        Properties properties = new Properties();
        String bootstrapServers = "127.0.0.1:9092";
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(properties);
    }


    public void kafkaSendData(String kafkaTopic, Transaction t) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json;
        try {
            json = ow.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ProducerRecord <String, String> producerRecord = new ProducerRecord<>(kafkaTopic, t.getUniqueId(), json);
        producer.send(producerRecord);
    }

}



