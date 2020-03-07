package com.example.spring.boot.kafka.kafkatest;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

    // As you know Spring Boot dependency injection, this is the starting part that you configure here.
    // When you run the application, @Bean will be read by Spring and run this method, get the object out from this method,
    // and put it into the Spring context.

    // ProducerFactory<String, SimpleModel>: String is the id's variable type, SimpleModel
    @Bean
    ProducerFactory<String, SimpleModel> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // The IP address of the Kafka server that you have set up.
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");

        // Needed for serializing the key and value of the JSON objects that are passing through Kafka
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    // We need another bean for KafkaTemplate(Like WebSocketTemplate).
    // Need to setup the topic (in this project is static but it can be made into dynamic) and send it to Kafka
    // This application becomes the PRODUCER (Observable)
    // The application will become a CONSUMER (Subscriber) when a WebSocket user connected to WebSocket and listens to the topic.
    @Bean
    public KafkaTemplate<String, SimpleModel> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory()); // The KafkaTemplate object loads with the Producer object in it.
    }

    // Remember when you run this application, please run the Zookeeper with Kafka too****
}
