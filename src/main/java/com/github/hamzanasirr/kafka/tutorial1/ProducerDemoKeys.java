package com.github.hamzanasirr.kafka.tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoKeys {
    private static final String BOOTSTRAP_SERVERS = Constants.BOOTSTRAP_SERVER_IP;
    public static final Logger logger = LoggerFactory.getLogger(ProducerDemoKeys.class);

    public static void main(String[] args) {
        // Create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        String topic = "third_topic";
        // Produce record to a topic 10 times.
        for (int i = 0; i < 10; i++) {
            String key = "New Key " + i * 10;
            String value = "Java Producer produced data. " + i;
            // Create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            // Send data - Asynchronous
            // producer.send(record);
            // Executes every time a record is successfully sent or an exception is thrown.
            producer.send(record, (recordMetadata, e) -> {
                if (e == null) {
                    // Successfully sent
                    logger.info("Received new metadata: " + "\n" +
                            "Topic: " + recordMetadata.topic() + "\n" +
                            "Partition: " + recordMetadata.partition() + "\n" +
                            "Offset: " + recordMetadata.offset() + "\n" +
                            "Timestamp: " + recordMetadata.timestamp());
                } else {
                    // Deal with the error
                    logger.error("Error while producing: " + e);
                }
            });
        }

        // Because of it being asynchronous, the consumer didn't receive the data,
        // hence, we need to flush and close the producer.
        producer.flush();
        producer.close();
    }
}
