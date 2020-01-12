package com.github.hamzanasirr.kafka.tutorial1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

public class KafkaCsvProducer {
    private static String KafkaBrokerEndpoint = null;
    private static String KafkaTopic = null;
    private static String CsvFile = null;

    private Producer<String, String> ProducerProperties(){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaBrokerEndpoint);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaCsvProducer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<String, String>(properties);
    }

    public static void main(String[] args) {
        if (args != null){
            KafkaBrokerEndpoint = args[0];
            KafkaTopic = args[1];
            CsvFile = args[2];
        }

        KafkaCsvProducer kafkaProducer = new KafkaCsvProducer();
        kafkaProducer.PublishMessages();
    }

    private void PublishMessages(){
        final Producer<String, String> CsvProducer = ProducerProperties();
        try{
            Stream<String> FileStream = Files.lines(Paths.get(CsvFile));
            FileStream.forEach(line -> {
                final ProducerRecord<String, String> CsvRecord = new ProducerRecord<String, String>(
                        KafkaTopic, UUID.randomUUID().toString(), line
                );

                CsvProducer.send(CsvRecord, (metadata, exception) -> {
                    if(metadata != null){
                        System.out.println("CsvData: -> "+ CsvRecord.key()+" | "+ CsvRecord.value());
                    }
                    else{
                        System.out.println("Error Sending Csv Record -> "+ CsvRecord.value());
                    }
                });
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
