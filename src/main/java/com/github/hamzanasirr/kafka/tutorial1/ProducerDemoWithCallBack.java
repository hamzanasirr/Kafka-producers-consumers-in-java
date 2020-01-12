package com.github.hamzanasirr.kafka.tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallBack {
    private static final String BOOTSTRAP_SERVERS = "192.168.100.47:9092"; // Replace it with your own ip:port
    public static final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallBack.class);

    public static void main(String[] args) {
        // Create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        // Produce record to a topic 10 times.
        for (int i = 0; i < 10; i++) {
            // Create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "Callback Java Consumer practice No. " + Integer.toString(i));
            // Send data - Asynchronous
            // producer.send(record);
            producer.send(record, new Callback() {
                // Executes every time a record is successfully sent or an exception is thrown.
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
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
                }
            });
        }

        // Because of the producer.send() method being asynchronous, the consumer didn't receive the data,
        // hence, we need to flush and close the producer.
        producer.flush();
        producer.close();
    }
}
